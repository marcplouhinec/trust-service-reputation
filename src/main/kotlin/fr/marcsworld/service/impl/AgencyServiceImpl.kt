package fr.marcsworld.service.impl

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.dto.AgencyNode
import fr.marcsworld.model.dto.DocumentNode
import fr.marcsworld.model.dto.DocumentStatistics
import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.AgencyName
import fr.marcsworld.model.entity.Document
import fr.marcsworld.repository.AgencyNameRepository
import fr.marcsworld.repository.AgencyRepository
import fr.marcsworld.repository.DocumentCheckingResultRepository
import fr.marcsworld.repository.DocumentRepository
import fr.marcsworld.service.AgencyService
import fr.marcsworld.utils.AgencyComparator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Default implementation of [AgencyService].
 *
 * @author Marc Plouhinec
 */
@Service
class AgencyServiceImpl(
        val agencyRepository: AgencyRepository,
        val agencyNameRepository: AgencyNameRepository,
        val documentRepository: DocumentRepository,
        val documentCheckingResultRepository: DocumentCheckingResultRepository
) : AgencyService {

    companion object {
        val LOGGER = LoggerFactory.getLogger(AgencyServiceImpl::class.java)!!
    }

    @Transactional(readOnly = true)
    override fun findRootAgency(): Agency {
        return agencyRepository.findRootAgency() ?: throw IllegalStateException("Unable to find the root agency.")
    }

    @Transactional(readOnly = true)
    override fun findAllStillReferencedAgenciesByParentAgencyId(parentAgencyId: Long): List<Agency> {
        return agencyRepository.findAllStillReferencedAgenciesByParentAgencyId(parentAgencyId)
    }

    @Transactional(readOnly = true)
    override fun findAgencyTree(): AgencyNode {
        // Find all the agencies
        val agencies = agencyRepository.findAll()

        // Find all documents with statistical information
        val documents = documentRepository.findAll()
        val documentStatistics = documentCheckingResultRepository.findAllDocumentStatistics()

        // Build the tree
        val agenciesByParent = agencies.groupBy(Agency::parentAgency)
        val documentsByAgency = documents.groupBy(Document::providedByAgency)
        val documentStatisticsByUrl = documentStatistics.associateBy { it.url }
        val rootAgencies = agenciesByParent[null] ?: throw IllegalStateException("Unable to find the root agency.")
        val rootAgency = rootAgencies[0]

        fun buildAgencyNode(currentAgency: Agency): AgencyNode {
            // Choose the main agency name by firstly looking for the english one. If not found find the first one.
            var mainAgencyName = currentAgency.names.findLast { it.languageCode.equals("en", ignoreCase = true) }
            if (mainAgencyName !is AgencyName) {
                mainAgencyName = currentAgency.names.firstOrNull()
            }
            if (mainAgencyName !is AgencyName) {
                mainAgencyName = AgencyName(agency = currentAgency, languageCode = "en", name = "NO NAME")
            }

            // Set the agency status
            val active = when {
                currentAgency.parentAgency !is Agency -> true
                currentAgency.isStillReferencedByDocument == true -> true
                else -> false
            }

            // Build the document nodes
            val agencyDocuments = documentsByAgency[currentAgency] ?: listOf()
            val documentNodes = agencyDocuments.map { document ->
                val statistics = documentStatisticsByUrl[document.url]
                if (statistics is DocumentStatistics) {
                    DocumentNode(
                            document,
                            statistics.availabilityPercentage,
                            statistics.validityPercentage,
                            statistics.currentSize,
                            if (statistics.lastDownloadDurationInMillis == 0L) 0F else statistics.currentSize / (statistics.lastDownloadDurationInMillis / 1000F))
                } else {
                    DocumentNode(document, 0F, 0F, 0, 0F)
                }
            }

            // Build the children agency nodes
            val childrenAgencies = agenciesByParent[currentAgency]
            val childrenAgencyNodes = (childrenAgencies as? List)?.map(::buildAgencyNode) ?: listOf()

            // Compute a rating
            var rating: Double?
            if (currentAgency.type == AgencyType.TRUST_SERVICE) {
                rating = 0.0

                // Active agency => one point
                if (active) {
                    rating += 1
                }

                // At least one available and valid document => one point
                val availableAndValidDocumentNodes = documentNodes.filter { it.availabilityPercentage > 99 && it.validityPercentage > 99 }
                if (availableAndValidDocumentNodes.isNotEmpty()) {
                    rating += 1
                }

                // All documents are available and valid => one point
                if (documentNodes.isNotEmpty() && documentNodes.size == availableAndValidDocumentNodes.size) {
                    rating += 1
                }

                // At least one CERTIFICATE_REVOCATION_LIST => one point
                val crlAvailableAndValidDocumentNodes = availableAndValidDocumentNodes.filter { it.document.type == DocumentType.CERTIFICATE_REVOCATION_LIST }
                if (crlAvailableAndValidDocumentNodes.isNotEmpty()) {
                    rating += 1
                }

                // At least one CERTIFICATE_REVOCATION_LIST coming from a TS_STATUS_LIST_XML (easier to parse than a TSP_SERVICE_DEFINITION) => one point
                if (crlAvailableAndValidDocumentNodes.any { it.document.referencedByDocumentType == DocumentType.TS_STATUS_LIST_XML }) {
                    rating += 1
                }
            } else {
                rating = childrenAgencyNodes
                        .map { it.rating ?: -1.0 }
                        .filter { it > 0 }
                        .average()
                if (rating.isNaN()) {
                    rating = null
                }
            }

            // Build the agency node
            return AgencyNode(currentAgency, mainAgencyName, active, rating, documentNodes, childrenAgencyNodes)
        }
        return buildAgencyNode(rootAgency)
    }

    @Transactional
    override fun updateTrustServiceListOperatorAgency(tsloAgency: Agency) {
        LOGGER.debug("Update the TrustServiceListOperatorAgency for the territory: {}.", tsloAgency.territoryCode)

        // Find the existing agency in the database
        val territoryCode = tsloAgency.territoryCode ?: throw IllegalArgumentException("Missing territoryCode.")
        val existingTsloAgency = agencyRepository.findTrustServiceListOperatorByTerritoryCode(territoryCode) ?: throw IllegalStateException(
                "Unable to find an existing TrustServiceListOperatorAgency with the territoryCode = $territoryCode. It must be created by a parent agency.")

        // Update the referencedByDocumentUrl is necessary (for info, there is no other column to update in the AGENCY table)
        if (tsloAgency.referencedByDocumentUrl is String && existingTsloAgency.referencedByDocumentUrl != tsloAgency.referencedByDocumentUrl) {
            existingTsloAgency.referencedByDocumentUrl = tsloAgency.referencedByDocumentUrl
            agencyRepository.save(existingTsloAgency)
        }

        // Update the names if necessary
        updateAgencyNames(tsloAgency.names, existingTsloAgency)

        // Update the providingDocuments
        updateProvidingDocuments(tsloAgency.providingDocuments, existingTsloAgency, markNotProvidedAnymoreDocuments = false)

        // Update the children agencies that are also TRUST_SERVICE_LIST_OPERATORS
        val childrenTsloAgencies = tsloAgency.childrenAgencies.filter { it.type == AgencyType.TRUST_SERVICE_LIST_OPERATOR }
        if (childrenTsloAgencies.isNotEmpty()) {
            updateChildrenAgencies(childrenTsloAgencies, existingTsloAgency, false)
        }

        // Update the children agencies that are TRUST_SERVICE_PROVIDERS (including their children that are TRUST_SERVICES)
        val childrenTspAgencies = tsloAgency.childrenAgencies.filter { it.type == AgencyType.TRUST_SERVICE_PROVIDER }
        if (childrenTspAgencies.isNotEmpty()) {
            // Check the equality by comparing their x509 certificates and names
            updateChildrenAgencies(childrenTspAgencies, existingTsloAgency, true)
        }
    }

    /**
     * Update the given children [Agency]s.
     * Note that the grand children are also updated.
     *
     * @param childrenAgencies Up-to-date list of children [Agency]s.
     * @param existingParentAgency [Agency] entity from the Database that own the given children [Agency]s.
     * @param deleteMissingNames If true, for each child [Agency] that already exists in the database, existing but missing [Agency.names]s are deleted.
     */
    private fun updateChildrenAgencies(childrenAgencies: List<Agency>, existingParentAgency: Agency, deleteMissingNames: Boolean = true) {

        val existingChildrenAgencies = agencyRepository.findAllByParentAgencyId(existingParentAgency.id ?: throw IllegalArgumentException("Missing agency ID."))

        // Merge duplicated children agencies
        val distinctChildrenAgencies = childrenAgencies.fold(initial = mutableListOf<Agency>(), operation = { acc, agency ->
            val duplicatedAgency = acc.findLast { AgencyComparator.compare(it, agency) == 0 }
            if (duplicatedAgency is Agency) {
                // Merge the duplicated agencies names
                duplicatedAgency.names = (duplicatedAgency.names + agency.names).distinctBy { "[${it.languageCode}]=${it.name}" }
                duplicatedAgency.providingDocuments = (duplicatedAgency.providingDocuments + agency.providingDocuments).distinctBy { "[${it.languageCode}]=${it.url}" }
            } else {
                acc.add(agency)
            }
            acc
        })

        // Find the new children agencies
        val newChildrenAgencies = distinctChildrenAgencies.filter { agency ->
            existingChildrenAgencies.none { AgencyComparator.compare(agency, it) == 0 }
        }

        // Find the children agencies to update
        val modifiedChildrenAgencies = existingChildrenAgencies.filter { agency ->
            distinctChildrenAgencies.any { AgencyComparator.compare(agency, it) == 0 && it.referencedByDocumentUrl != agency.referencedByDocumentUrl }
        }

        // Find the children agencies that are not referenced anymore
        val notReferencedAnymoreChildrenAgencies = existingChildrenAgencies.filter { agency ->
            (agency.isStillReferencedByDocument ?: false) && distinctChildrenAgencies.none { AgencyComparator.compare(agency, it) == 0 }
        }

        // Find the children agencies that are referenced again
        val referencedAgainChildrenAgencies = existingChildrenAgencies.filter { agency ->
            !(agency.isStillReferencedByDocument ?: false) && distinctChildrenAgencies.any { AgencyComparator.compare(agency, it) == 0 }
        }

        // Update the agency in the database
        for (agency in newChildrenAgencies) {
            agency.parentAgency = existingParentAgency
            agencyRepository.save(agency)
        }
        for (agency in modifiedChildrenAgencies) {
            val upToDateAgency = distinctChildrenAgencies.findLast { AgencyComparator.compare(agency, it) == 0 }
            if (upToDateAgency is Agency) {
                agency.referencedByDocumentUrl = upToDateAgency.referencedByDocumentUrl
                agencyRepository.save(agency)
            }
        }
        for (agency in notReferencedAnymoreChildrenAgencies) {
            agency.isStillReferencedByDocument = false
            agencyRepository.save(agency)
        }
        for (agency in referencedAgainChildrenAgencies) {
            agency.isStillReferencedByDocument = true
            agencyRepository.save(agency)
        }

        // Update the names and documents
        val persistedTsloAgencies = distinctChildrenAgencies
                .map { agency ->
                    val existingAgency = existingChildrenAgencies.findLast { AgencyComparator.compare(agency, it) == 0 }
                    existingAgency ?: agency
                }
                .filter { it.id != null }
        for (agency in persistedTsloAgencies) {
            val upToDateAgency = distinctChildrenAgencies.findLast { AgencyComparator.compare(agency, it) == 0 }

            if (upToDateAgency is Agency) {
                // Create but don't delete missing names
                updateAgencyNames(upToDateAgency.names, agency, deleteMissingNames)

                // Update provided documents
                updateProvidingDocuments(upToDateAgency.providingDocuments, agency)

                // Update the children agencies
                updateChildrenAgencies(upToDateAgency.childrenAgencies, agency, deleteMissingNames)
            }
        }
    }

    /**
     * Update the given [AgencyName]s that belong to the given [Agency].
     *
     * @param agencyNames Up-to-date list of [AgencyName]s that belong to the given [Agency].
     * @param existingTsloAgency [Agency] entity from the Database that own the given [AgencyName]s.
     * @param deleteMissingNames If true, existing [AgencyName]s that are not part of the given list of [agencyNames] are deleted.
     */
    private fun updateAgencyNames(agencyNames: List<AgencyName>, existingTsloAgency: Agency, deleteMissingNames: Boolean = true) {
        val existingAgencyNames = agencyNameRepository.findAllByAgencyId(existingTsloAgency.id ?: throw IllegalArgumentException("Missing agency ID."))

        // Find the AgencyNames to create
        val newAgencyNames = agencyNames.filter { agencyName -> existingAgencyNames.none { it.languageCode.equals(agencyName.languageCode, ignoreCase = true) && it.name == agencyName.name } }

        // Find the AgencyNames to delete
        val missingAgencyNames = if (!deleteMissingNames) {
            listOf()
        } else {
            existingAgencyNames.filter { agencyName -> agencyNames.none { it.languageCode.equals(agencyName.languageCode, ignoreCase = true) && it.name == agencyName.name } }
        }

        // Update the database
        for (agencyName in newAgencyNames) {
            agencyName.agency = existingTsloAgency
            agencyNameRepository.save(agencyName)
        }
        for (agencyName in missingAgencyNames) {
            agencyNameRepository.delete(agencyName)
        }
    }

    /**
     * Update the given [Document]s that are provided by the given [Agency].
     *
     * @param documents Up-to-date list of [Document]s that are provided by the given [Agency].
     * @param existingAgency [Agency] entity from the Database that provides the given [Document]s.
     * @param markNotProvidedAnymoreDocuments If true, update the [Document.isStillProvidedByAgency] property of missing documents.
     */
    private fun updateProvidingDocuments(documents: List<Document>, existingAgency: Agency, markNotProvidedAnymoreDocuments: Boolean = true) {
        val existingDocuments = documentRepository.findAllByProvidedByAgencyId(existingAgency.id ?: throw IllegalArgumentException("Missing agency ID."))

        // Find the documents to create
        val newDocuments = documents.filter { document -> existingDocuments.none { it.url == document.url } }

        // Find the documents to update
        val modifiedDocuments = existingDocuments.filter { document -> documents.any { it.url == document.url && !it.languageCode.equals(document.languageCode, ignoreCase = true) } }

        // Find the documents that are not provided anymore
        val notProvidedAnymoreDocuments = if (!markNotProvidedAnymoreDocuments) {
            listOf()
        } else {
            existingDocuments.filter { document -> document.isStillProvidedByAgency && documents.none { it.url == document.url } }
        }

        // Find the documents that are provided again
        val providedAgainDocuments = existingDocuments.filter { document -> !document.isStillProvidedByAgency && documents.any { it.url == document.url } }

        // Update the database
        for (document in newDocuments) {
            document.providedByAgency = existingAgency
            documentRepository.save(document)
        }
        for (document in modifiedDocuments) {
            val upToDateDocument = documents.findLast { it.url == document.url }
            if (upToDateDocument is Document) {
                document.languageCode = upToDateDocument.languageCode
                documentRepository.save(document)
            }
        }
        for (document in notProvidedAnymoreDocuments) {
            document.isStillProvidedByAgency = false
            documentRepository.save(document)
        }
        for (document in providedAgainDocuments) {
            document.isStillProvidedByAgency = true
            documentRepository.save(document)
        }
    }

}
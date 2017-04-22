package fr.marcsworld.service.impl

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.model.Agency
import fr.marcsworld.model.AgencyName
import fr.marcsworld.model.Document
import fr.marcsworld.repository.AgencyNameRepository
import fr.marcsworld.repository.AgencyRepository
import fr.marcsworld.repository.DocumentRepository
import fr.marcsworld.service.AgencyService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Default implementation of [AgencyService].
 *
 * @author Marc Plouhinec
 */
@Service
class AgencyServiceImpl(
        val agencyRepository: AgencyRepository,
        val agencyNameRepository: AgencyNameRepository,
        val documentRepository: DocumentRepository
) : AgencyService {

    companion object {
        val LOGGER = LoggerFactory.getLogger(AgencyServiceImpl::class.java)!!
    }

    @Transactional
    override fun updateTrustServiceListOperatorAgency(tsloAgency: Agency) {
        LOGGER.info("Update the TrustServiceListOperatorAgency for the territory: {}.", tsloAgency.territoryCode)

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
        val childrenTsloAgencies = tsloAgency.childAgencies.filter { it.type == AgencyType.TRUST_SERVICE_LIST_OPERATOR }
        if (childrenTsloAgencies.isNotEmpty()) {
            updateChildrenAgencies(childrenTsloAgencies, existingTsloAgency, false, { agency1, agency2 -> agency1.territoryCode == agency2.territoryCode })
        }

        // Update the children agencies that are TRUST_SERVICE_PROVIDERS (including their children that are TRUST_SERVICES)
        val childrenTspAgencies = tsloAgency.childAgencies.filter { it.type == AgencyType.TRUST_SERVICE_PROVIDER }
        if (childrenTspAgencies.isNotEmpty()) {
            // Unfortunately, the only way to check the equality of two agencies (that are not TRUST_SERVICE_LIST_OPERATOR) is by comparing their names
            updateChildrenAgencies(childrenTspAgencies, existingTsloAgency, true, { agency1, agency2 ->
                // Find a common language code
                val commonLanguageCodes = agency1.names
                        .filter { agencyName -> agency2.names.any { it.languageCode.equals(agencyName.languageCode, ignoreCase = true) } }
                        .map { it.languageCode }

                if (commonLanguageCodes.isNotEmpty()) {
                    // Compare the names for the first common language code
                    val commonLanguageCode = commonLanguageCodes[0]
                    val agencyName1 = agency1.names.findLast { it.languageCode.equals(commonLanguageCode, ignoreCase = true) }
                    val agencyName2 = agency2.names.findLast { it.languageCode.equals(commonLanguageCode, ignoreCase = true) }

                    agencyName1 is AgencyName && agencyName2 is AgencyName && agencyName1.name == agencyName2.name
                } else {
                    false
                }
            })
        }
    }

    /**
     * Update the given children [Agency]s.
     * Note that the grand children are also updated.
     *
     * @param childrenAgencies Up-to-date list of children [Agency]s.
     * @param existingParentAgency [Agency] entity from the Database that own the given children [Agency]s.
     * @param deleteMissingNames If true, for each child [Agency] that already exists in the database, existing but missing [Agency.names]s are deleted.
     * @param areAgenciesEquals Lambda that returns true when the given agencies are equals and false if not.
     */
    private fun updateChildrenAgencies(
            childrenAgencies: List<Agency>,
            existingParentAgency: Agency,
            deleteMissingNames: Boolean = true,
            areAgenciesEquals: (agency1: Agency, agency2: Agency) -> Boolean) {

        val existingChildrenAgencies = agencyRepository.findAllByParentAgencyId(existingParentAgency.id ?: throw IllegalArgumentException("Missing agency ID."))

        // Find the new children agencies
        val newChildrenAgencies = childrenAgencies.filter { agency -> existingChildrenAgencies.none { areAgenciesEquals(agency, it) } }

        // Find the children agencies to update
        val modifiedChildrenAgencies = existingChildrenAgencies.filter { agency ->
            existingChildrenAgencies.any { areAgenciesEquals(agency, it) && it.referencedByDocumentUrl != agency.referencedByDocumentUrl }
        }

        // Find the children agencies that are not referenced anymore
        val notReferencedAnymoreChildrenAgencies = existingChildrenAgencies.filter { agency ->
            (agency.isStillReferencedByDocument ?: false) && childrenAgencies.none { areAgenciesEquals(agency, it) }
        }

        // Find the children agencies that are referenced again
        val referencedAgainChildrenAgencies = existingChildrenAgencies.filter { agency ->
            !(agency.isStillReferencedByDocument ?: false) && childrenAgencies.any { areAgenciesEquals(agency, it) }
        }

        // Update the agency in the database
        for (agency in newChildrenAgencies) {
            agency.parentAgency = existingParentAgency
            agencyRepository.save(agency)
        }
        for (agency in modifiedChildrenAgencies) {
            val upToDateAgency = childrenAgencies.findLast { areAgenciesEquals(agency, it) }
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
        val persistedTsloAgencies = childrenAgencies
                .map { agency ->
                    val existingAgency = existingChildrenAgencies.findLast { areAgenciesEquals(agency, it) }
                    existingAgency ?: agency
                }
                .filter { it.id != null }
        for (agency in persistedTsloAgencies) {
            val upToDateAgency = childrenAgencies.findLast { areAgenciesEquals(agency, it) }

            if (upToDateAgency is Agency) {
                // Create but don't delete missing names
                updateAgencyNames(upToDateAgency.names, agency, deleteMissingNames)

                // Update provided documents
                updateProvidingDocuments(upToDateAgency.providingDocuments, agency)

                // Update the children agencies
                updateChildrenAgencies(upToDateAgency.childAgencies, agency, deleteMissingNames, areAgenciesEquals)
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
     * @param existingTsloAgency [Agency] entity from the Database that provides the given [Document]s.
     */
    private fun updateProvidingDocuments(documents: List<Document>, existingTsloAgency: Agency, markNotProvidedAnymoreDocuments: Boolean = true) {
        val existingDocuments = documentRepository.findAllByProvidedByAgencyId(existingTsloAgency.id ?: throw IllegalArgumentException("Missing agency ID."))

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
            document.providedByAgency = existingTsloAgency
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
package fr.marcsworld.scheduled

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Agency
import fr.marcsworld.service.AgencyService
import fr.marcsworld.service.DocumentParsingService
import fr.marcsworld.service.DocumentService
import org.slf4j.LoggerFactory
import org.springframework.core.io.UrlResource
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Tasks for parsing documents and updating the database.
 *
 * @author Marc Plouhinec
 */
@Component
open class DocumentParsingTasks(
        val agencyService: AgencyService,
        val documentService: DocumentService,
        val documentParsingService: DocumentParsingService,
        val documentParsingTaskExecutor: TaskExecutor
) {
    companion object {
        val LOGGER = LoggerFactory.getLogger(DocumentParsingTasks::class.java)!!
    }

    /**
     * Load the root agency documents, parse them and update the database and schedule tasks for parsing children agency documents.
     */
    @Scheduled(fixedRate = 100 * 60 * 1000, initialDelay = 100 * 60 * 1000)
    fun parseRootAgencyDocuments() {
        LOGGER.info("Parse the root agency documents.")

        // Parse the root agency documents and update the database
        val rootAgency = agencyService.findRootAgency()
        val providedDocuments = documentService.findAllStillProvidedDocumentsByAgencyIdAndByType(rootAgency.id!!, DocumentType.TS_STATUS_LIST_XML)
        for (document in providedDocuments) {
            val tsloAgency = documentParsingService.parseTsStatusList(UrlResource(document.url))
            agencyService.updateTrustServiceListOperatorAgency(tsloAgency)
        }

        // Find the children agencies and schedule tasks for each of them
        val rootAgencyId = rootAgency.id ?: throw IllegalStateException("Missing rootAgency.id")
        val childrenAgencies = agencyService.findAllStillReferencedAgenciesByParentAgencyId(rootAgencyId)
        for (childAgency in childrenAgencies) {
            documentParsingTaskExecutor.execute(ChildTrustServiceListOperatorAgencyDocumentParser(childAgency))
        }
    }

    /**
     * Parse document of type [DocumentType.TS_STATUS_LIST_XML] provided by the given child agency.
     *
     * @param agency Agency of type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_LIST_OPERATOR].
     */
    inner class ChildTrustServiceListOperatorAgencyDocumentParser(
            val agency: Agency
    ) : Runnable {

        override fun run() {
            LOGGER.debug("Parse the documents of the TrustServiceListOperator agency {}.", agency.territoryCode)

            // Parse the root agency documents and update the database
            val providedDocuments = documentService.findAllStillProvidedDocumentsByAgencyIdAndByType(agency.id!!, DocumentType.TS_STATUS_LIST_XML)
            for (document in providedDocuments) {
                val tsloAgency = documentParsingService.parseTsStatusList(UrlResource(document.url))
                agencyService.updateTrustServiceListOperatorAgency(tsloAgency)
            }

            // Find children TRUST_SERVICE agencies and schedule tasks for each of them
            val childrenTspAgencies = agencyService.findAllStillReferencedAgenciesByParentAgencyId(agency.id!!)
            val childrenTsAgencies = childrenTspAgencies.flatMap { agencyService.findAllStillReferencedAgenciesByParentAgencyId(it.id!!) }
            for (childAgency in childrenTsAgencies) {
                documentParsingTaskExecutor.execute(ChildTrustServiceAgencyDocumentParser(childAgency))
            }
        }

    }

    /**
     * Parse document of type [DocumentType.TSP_SERVICE_DEFINITION] provided by the given child agency.
     *
     * @param agency Agency of type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE].
     */
    inner class ChildTrustServiceAgencyDocumentParser(
            val agency: Agency
    ) : Runnable {

        override fun run() {
            LOGGER.debug("Parse the documents of the TrustService agency {}.", agency.names.joinToString { "[${it.languageCode}]${it.name}" })

            val providedDocuments = documentService.findAllStillProvidedDocumentsByAgencyIdAndByType(agency.id!!, DocumentType.TSP_SERVICE_DEFINITION)
            val certificateRevocationListDocuments = providedDocuments
                    .flatMap { documentParsingService.parseTspServiceDefinition(UrlResource(it.url), agency) }
                    .distinctBy { it.url }
                    .toList()
            agencyService.updateTrustServiceAgencyDocuments(agency, certificateRevocationListDocuments)
        }

    }
}
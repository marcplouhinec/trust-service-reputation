package fr.marcsworld.service

import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.Document
import org.springframework.core.io.Resource

/**
 * Service for parsing documents.
 *
 * @author Marc Plouhinec
 */
interface DocumentParsingService {

    /**
     * Parse the content of a document of the type [fr.marcsworld.enums.DocumentType.TS_STATUS_LIST_XML].
     *
     * @param resource Document resource.
     * @return Top-level agency with its children located in the document.
     */
    fun parseTsStatusList(resource: Resource): Agency

    /**
     * Parse the content of a document of the type [fr.marcsworld.enums.DocumentType.TSP_SERVICE_DEFINITION].
     *
     * @param resource Document resource.
     * @param providerAgency Trust Service [Agency] that provides this resource.
     * @return List of document of type [fr.marcsworld.enums.DocumentType.CERTIFICATE_REVOCATION_LIST].
     */
    fun parseTspServiceDefinition(resource: Resource, providerAgency: Agency): List<Document>

}
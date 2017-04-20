package fr.marcsworld.enums

/**
 * Type of document the application need to download.
 *
 * @author Marc Plouhinec
 */
enum class DocumentType {
    /**
     * XML document that gives information about Trust Services that deliver certificates.
     */
    TS_STATUS_LIST_XML,

    /**
     * PDF document that describes a particular Trust Service.
     */
    TSP_SERVICE_DEFINITION_PDF,

    /**
     * Document containing revoked signing certificates.
     */
    CERTIFICATE_REVOCATION_LIST
}
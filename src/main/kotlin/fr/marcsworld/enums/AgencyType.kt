package fr.marcsworld.enums

/**
 * Type of agencies dealing with Trust Services.
 *
 * @author Marc Plouhinec
 */
enum class AgencyType {
    /**
     * Agency representing the European Commission or a Member State.
     */
    TRUST_SERVICE_LIST_OPERATOR,

    /**
     * Agency of a Member State that provides Trust Services.
     */
    TRUST_SERVICE_PROVIDER,

    /**
     * Agency that provides signing certificates.
     */
    TRUST_SERVICE
}

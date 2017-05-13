package fr.marcsworld.utils

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.model.entity.Agency
import fr.marcsworld.model.entity.AgencyName

/**
 * Unfortunately there is no unique ID or equivalent found for comparing [Agency]s.
 * This class combines the properties [Agency.type], [Agency.territoryCode], [Agency.names] and [Agency.x509Certificate].
 * Note 1: a null value is always considered lower.
 * Note 2: [Agency.names] are compared by selecting only one name of a common language.
 *
 * @author Marc Plouhinec
 */
object AgencyComparator : Comparator<Agency> {

    override fun compare(agency1: Agency?, agency2: Agency?): Int {
        // Handle the cases when any agency is null
        if (agency1 !is Agency || agency2 !is Agency) {
            return when {
                agency1 !is Agency && agency2 !is Agency -> 0
                agency1 !is Agency && agency2 is Agency -> -1
                agency1 is Agency && agency2 !is Agency -> 1
                else -> 0 // Case that never happen
            }
        }

        // Compare types
        val typeComparisonResult = when {
            agency1.type is AgencyType && agency2.type is AgencyType -> agency1.type.compareTo(agency2.type)
            agency1.type !is AgencyType && agency2.type is AgencyType -> -1
            agency1.type is AgencyType && agency2.type !is AgencyType -> 1
            else -> 0
        }
        if (typeComparisonResult != 0) {
            return typeComparisonResult
        }

        // Compare territory codes
        val territoryCodeComparisonResult = when {
            agency1.territoryCode is String && agency2.territoryCode is String -> {
                agency1.territoryCode!!.compareTo(agency2.territoryCode!!)
            }
            agency1.territoryCode !is String && agency2.territoryCode is String -> -1
            agency1.territoryCode is String && agency2.territoryCode !is String -> 1
            else -> 0
        }
        if (territoryCodeComparisonResult != 0) {
            return territoryCodeComparisonResult
        }

        // Two agencies with the same territory code are equal, no matter what are the other attributes
        if (agency1.territoryCode is String && agency2.territoryCode is String && agency1.territoryCode == agency2.territoryCode) {
            return 0
        }

        // Compare names
        val commonLanguageCodes = agency1.names // Find a common language code
                .filter { agencyName ->
                    agency2.names.any {
                        it.languageCode.equals(agencyName.languageCode, ignoreCase = true)
                    }
                }
                .map { it.languageCode }

        val agencyNameComparisonResult = if (commonLanguageCodes.isNotEmpty()) {
            // Compare the names for the first common language code
            val commonLanguageCode = commonLanguageCodes[0]
            val agencyName1 = agency1.names.findLast { it.languageCode.equals(commonLanguageCode, ignoreCase = true) }
            val agencyName2 = agency2.names.findLast { it.languageCode.equals(commonLanguageCode, ignoreCase = true) }

            when {
                agencyName1 is AgencyName && agencyName2 is AgencyName ->  agencyName1.name.compareTo(agencyName2.name)
                agencyName1 !is AgencyName && agencyName2 is AgencyName -> -1
                agencyName1 is AgencyName && agencyName2 !is AgencyName -> 1
                else -> 0
            }
        } else {
            0 // No agency name to compare
        }
        if (agencyNameComparisonResult != 0) {
            return agencyNameComparisonResult
        }

        // Compare x509 certificates
        return when {
            agency1.x509Certificate is String && agency2.x509Certificate is String -> {
                agency1.x509Certificate!!.compareTo(agency2.x509Certificate!!)
            }
            agency1.x509Certificate !is String && agency2.x509Certificate is String -> -1
            agency1.x509Certificate is String && agency2.x509Certificate !is String -> 1
            else -> 0
        }
    }
}
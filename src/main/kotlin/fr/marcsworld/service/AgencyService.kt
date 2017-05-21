package fr.marcsworld.service

import fr.marcsworld.model.dto.AgencyNode
import fr.marcsworld.model.entity.Agency

/**
 * Service for handling [Agency]s.
 *
 * @author Marc Plouhinec
 */
interface AgencyService {

    /**
     * Find the root [Agency].
     *
     * @return Root [Agency].
     */
    fun findRootAgency(): Agency

    /**
     * Find all the [Agency]s with their parameter [Agency.isStillReferencedByDocument] as true by their [Agency.parentAgency] ID.
     *
     * @param parentAgencyId ID of the parent [Agency].
     * @return Found [Agency]s.
     */
    fun findAllStillReferencedAgenciesByParentAgencyId(parentAgencyId: Long): List<Agency>

    /**
     * Find [Agency]s with related information and build a tree.
     *
     * @param includeRating If true, fill the [AgencyNode.rating] parameter.
     * @return Tree root [AgencyNode].
     */
    fun findAgencyTree(includeRating: Boolean): AgencyNode

    /**
     * Update in the database the given [Agency], then create or update the children agencies and documents.
     *
     * This method is not generic and expects two types of agencies:
     * * The European Commission, that provides other [Agency]s with the type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_LIST_OPERATOR].
     * * A Member State, that provides other [Agency]s with the type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_LIST_OPERATOR].
     *
     * With the second type, children agencies are expected to provide [Agency]s with the types
     * [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_PROVIDER] and [fr.marcsworld.enums.AgencyType.TRUST_SERVICE].
     *
     * @param tsloAgency [Agency] with the type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_LIST_OPERATOR]
     */
    fun updateTrustServiceListOperatorAgency(tsloAgency: Agency)

}
package fr.marcsworld.repository

import fr.marcsworld.enums.AgencyType
import fr.marcsworld.model.Agency
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * Repository for agencies.
 *
 * @author Marc Plouhinec
 */
interface AgencyRepository : CrudRepository<Agency, Long> {

    /**
     * Find an [Agency] of the type [fr.marcsworld.enums.AgencyType.TRUST_SERVICE_LIST_OPERATOR] by its [Agency.territoryCode].
     *
     * @param territoryCode Code of the territory that the agency is responsible for (e.g. "FR").
     * @return Found [Agency] or null.
     */
    @Query("SELECT agency FROM Agency agency WHERE agency.territoryCode = ?1 AND agency.type = 'TRUST_SERVICE_LIST_OPERATOR'")
    fun findTrustServiceListOperatorByTerritoryCode(territoryCode: String): Agency?

    /**
     * Find all the [Agency]s by their [Agency.parentAgency] ID.
     *
     * @param parentAgencyId ID of the parent [Agency].
     * @return Found [Agency]s.
     */
    fun findAllByParentAgencyId(parentAgencyId: Long): List<Agency>

}

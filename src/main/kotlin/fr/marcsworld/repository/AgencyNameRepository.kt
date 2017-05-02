package fr.marcsworld.repository

import fr.marcsworld.model.entity.AgencyName
import org.springframework.data.repository.CrudRepository

/**
 * Repository for agency names.
 *
 * @author Marc Plouhinec
 */
interface AgencyNameRepository : CrudRepository<AgencyName, Long> {

    /**
     * Find all the [AgencyName]s for the given [AgencyName.agency] ID.
     *
     * @param agencyId [fr.marcsworld.model.entity.Agency.id] that owns the [AgencyName]s.
     * @return Found [AgencyName]s.
     */
    fun findAllByAgencyId(agencyId: Long): List<AgencyName>

}

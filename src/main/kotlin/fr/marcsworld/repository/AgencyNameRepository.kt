package fr.marcsworld.repository

import fr.marcsworld.model.AgencyName
import org.springframework.data.repository.CrudRepository

/**
 * Repository for agency names.
 *
 * @author Marc Plouhinec
 */
interface AgencyNameRepository : CrudRepository<AgencyName, Long>

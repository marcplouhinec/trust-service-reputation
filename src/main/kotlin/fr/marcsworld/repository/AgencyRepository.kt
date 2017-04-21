package fr.marcsworld.repository

import fr.marcsworld.model.Agency
import org.springframework.data.repository.CrudRepository

/**
 * Repository for agencies.
 *
 * @author Marc Plouhinec
 */
interface AgencyRepository : CrudRepository<Agency, Long>

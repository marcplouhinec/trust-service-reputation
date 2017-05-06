package fr.marcsworld.repository

import fr.marcsworld.model.entity.DocumentCheckingResult
import org.springframework.data.repository.CrudRepository

/**
 * Repository for [DocumentCheckingResult]s.
 *
 * @author Marc Plouhinec
 */
interface DocumentCheckingResultRepository : CrudRepository<DocumentCheckingResult, Long>
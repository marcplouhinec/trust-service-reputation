package fr.marcsworld.repository

import fr.marcsworld.model.dto.DocumentStatistics
import fr.marcsworld.model.entity.DocumentCheckingResult
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * Repository for [DocumentCheckingResult]s.
 *
 * @author Marc Plouhinec
 */
interface DocumentCheckingResultRepository : CrudRepository<DocumentCheckingResult, Long> {

    /**
     * Compute all the [DocumentStatistics] for each document URL.
     *
     * @return One [DocumentStatistics] per URL.
     */
    @Query(name = "DocumentCheckingResult.findAllDocumentStatistics")
    fun findAllDocumentStatistics(): List<DocumentStatistics>

}
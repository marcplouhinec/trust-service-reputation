package fr.marcsworld.repository

import fr.marcsworld.model.Document
import org.springframework.data.repository.CrudRepository

/**
 * Repository for documents.
 *
 * @author Marc Plouhinec
 */
interface DocumentRepository : CrudRepository<Document, String> {

    /**
     * Find all the [Document]s that have been provided by the given [fr.marcsworld.model.Agency].
     *
     * @param agencyId ID of the [fr.marcsworld.model.Agency] that provides the [Document]s.
     * @return Found [Document]s.
     */
    fun findAllByProvidedByAgencyId(agencyId: Long): List<Document>

}
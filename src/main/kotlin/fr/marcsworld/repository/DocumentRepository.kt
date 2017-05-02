package fr.marcsworld.repository

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Document
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * Repository for documents.
 *
 * @author Marc Plouhinec
 */
interface DocumentRepository : CrudRepository<Document, String> {

    /**
     * Find all the [Document]s that have been provided by the given [fr.marcsworld.model.entity.Agency].
     *
     * @param agencyId ID of the [fr.marcsworld.model.entity.Agency] that provides the [Document]s.
     * @return Found [Document]s.
     */
    fun findAllByProvidedByAgencyId(agencyId: Long): List<Document>

    /**
     * Find all the [Document]s of the given [documentType] that have been provided by the given [agencyId].
     * Note that the results only contains documents with their property [Document.isStillProvidedByAgency] equals to true.
     *
     * @param agencyId ID of the [fr.marcsworld.model.entity.Agency] that provided the [Document]s.
     * @return Found [Document]s.
     */
    @Query("SELECT document FROM Document document WHERE document.isStillProvidedByAgency = true AND document.providedByAgency.id = ?1 AND document.type = ?2")
    fun findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId: Long, documentType: DocumentType): List<Document>

}
package fr.marcsworld.service

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.dto.DocumentUrlAndType
import fr.marcsworld.model.entity.Document
import fr.marcsworld.model.entity.DocumentCheckingResult

/**
 * Service for handling [Document]s.
 *
 * @author Marc Plouhinec
 */
interface DocumentService {

    /**
     * Save the given [DocumentCheckingResult].
     */
    fun saveDocumentCheckingResult(documentCheckingResult: DocumentCheckingResult)

    /**
     * Find all uniques pairs of [Document.url] and [Document.type] for [Document]s that have
     * their property [Document.isStillProvidedByAgency] equal to true.
     *
     * @return Found distinct [DocumentUrlAndType]s.
     */
    fun findAllStillProvidedDocumentUrlAndTypes(): List<DocumentUrlAndType>

    /**
     * Find all the [Document]s of the given [documentType] that have been provided by the given [agencyId].
     * Note that the results only contains documents with their property [Document.isStillProvidedByAgency] equals to true.
     *
     * @param agencyId ID of the [fr.marcsworld.model.entity.Agency] that provided the [Document]s.
     * @return Found [Document]s.
     */
    fun findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId: Long, documentType: DocumentType): List<Document>

}
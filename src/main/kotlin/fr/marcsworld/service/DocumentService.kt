package fr.marcsworld.service

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Document

/**
 * Service for handling [Document]s.
 *
 * @author Marc Plouhinec
 */
interface DocumentService {

    /**
     * Find all the [Document]s of the given [documentType] that have been provided by the given [agencyId].
     * Note that the results only contains documents with their property [Document.isStillProvidedByAgency] equals to true.
     *
     * @param agencyId ID of the [fr.marcsworld.model.entity.Agency] that provided the [Document]s.
     * @return Found [Document]s.
     */
    fun findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId: Long, documentType: DocumentType): List<Document>

}
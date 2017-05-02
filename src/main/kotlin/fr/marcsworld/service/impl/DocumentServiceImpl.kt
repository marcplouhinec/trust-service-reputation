package fr.marcsworld.service.impl

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.entity.Document
import fr.marcsworld.repository.DocumentRepository
import fr.marcsworld.service.DocumentService
import org.springframework.stereotype.Service

/**
 * Default implementation of [DocumentService].
 *
 * @author Marc Plouhinec
 */
@Service
class DocumentServiceImpl(
        val documentRepository: DocumentRepository
) : DocumentService {

    override fun findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId: Long, documentType: DocumentType): List<Document> {
        return documentRepository.findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId, documentType)
    }

}
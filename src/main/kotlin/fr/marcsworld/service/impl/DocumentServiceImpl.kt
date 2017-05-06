package fr.marcsworld.service.impl

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.dto.DocumentUrlAndType
import fr.marcsworld.model.entity.Document
import fr.marcsworld.model.entity.DocumentCheckingResult
import fr.marcsworld.repository.DocumentCheckingResultRepository
import fr.marcsworld.repository.DocumentRepository
import fr.marcsworld.service.DocumentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Default implementation of [DocumentService].
 *
 * @author Marc Plouhinec
 */
@Service
class DocumentServiceImpl(
        val documentRepository: DocumentRepository,
        val documentCheckingResultRepository: DocumentCheckingResultRepository
) : DocumentService {

    @Transactional
    override fun saveDocumentCheckingResult(documentCheckingResult: DocumentCheckingResult) {
        documentCheckingResultRepository.save(documentCheckingResult)
    }

    @Transactional(readOnly = true)
    override fun findAllStillProvidedDocumentUrlAndTypes(): List<DocumentUrlAndType> {
        return documentRepository.findAllStillProvidedDocumentUrlAndTypes()
    }

    @Transactional(readOnly = true)
    override fun findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId: Long, documentType: DocumentType): List<Document> {
        return documentRepository.findAllStillProvidedDocumentsByAgencyIdAndByType(agencyId, documentType)
    }

}
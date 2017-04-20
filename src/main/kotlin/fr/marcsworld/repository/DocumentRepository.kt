package fr.marcsworld.repository

import fr.marcsworld.model.Document
import org.springframework.data.repository.CrudRepository

/**
 * Repository for documents.
 *
 * @author Marc Plouhinec
 */
interface DocumentRepository : CrudRepository<Document, String>
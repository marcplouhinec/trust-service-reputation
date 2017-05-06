package fr.marcsworld.model.dto

import fr.marcsworld.enums.DocumentType

/**
 * URL and type of a document.
 *
 * @author Marc Plouhinec
 */
class DocumentUrlAndType(
        val url: String,
        val type: DocumentType
)
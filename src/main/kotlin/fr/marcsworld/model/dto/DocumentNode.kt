package fr.marcsworld.model.dto

import fr.marcsworld.model.entity.Document

/**
 * Tree node representing a document with statistics.
 *
 * @author Marc Plouhinec
 */
class DocumentNode(

        val document: Document,

        /**
         * Percentage of times when the document could be downloaded.
         */
        val availabilityPercentage: Float,

        /**
         * Percentage of times when document is valid (e.g. valid XML document).
         */
        val validityPercentage: Float,

        /**
         * Current document size in bytes.
         */
        val currentSize: Int,

        /**
         * Average download speed in byte per second.
         */
        val averageDownloadSpeed: Float
)
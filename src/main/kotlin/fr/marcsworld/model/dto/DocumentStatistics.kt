package fr.marcsworld.model.dto

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Contain statistics of a document.
 *
 * @author Marc Plouhinec
 */
@Entity
class DocumentStatistics(
        /**
         * URL of the document.
         */
        @Id
        @Column(name = "DOCUMENT_URL")
        val url: String,

        /**
         * Percentage of times when the document could be downloaded.
         */
        @Column(name = "AVG_AVAILABILITY")
        val availabilityPercentage: Float,

        /**
         * Percentage of times when document is valid (e.g. valid XML document).
         */
        @Column(name = "AVG_VALIDITY")
        val validityPercentage: Float,

        /**
         * Current document size in bytes.
         */
        @Column(name = "SIZE_IN_BYTES")
        val currentSize: Int,

        /**
         * Last download duration in milliseconds.
         */
        @Column(name = "DOWNLOAD_DURATION_IN_MILLIS")
        val lastDownloadDurationInMillis: Long
)
package fr.marcsworld.model.entity

import java.util.*
import javax.persistence.*

/**
 * Entity containing the result of the checking of a document.
 *
 * @author Marc Plouhinec
 */
@Entity
@Table(name = "DOCUMENT_CHECKING_RESULT")
class DocumentCheckingResult(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "DOCUMENT_CHECKING_RESULT_ID", nullable = false)
        var id: Long? = null,

        @Column(name = "DOCUMENT_URL", nullable = false, length = 2000)
        var url: String,

        /**
         * Checking date in UTC.
         */
        @Column(name = "CHECKING_DATE", nullable = false)
        val date: Date,

        @Column(name = "IS_AVAILABLE", nullable = false)
        val isAvailable: Boolean,

        @Column(name = "IS_VALID", nullable = false)
        val isValid: Boolean,

        @Column(name = "SIZE_IN_BYTES", nullable = false)
        val sizeInBytes: Int,

        @Column(name = "DOWNLOAD_DURATION_IN_MILLIS", nullable = false)
        val downloadDurationInMillis: Long
)
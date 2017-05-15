package fr.marcsworld.model.entity

import fr.marcsworld.model.dto.DocumentStatistics
import java.util.*
import javax.persistence.*

/**
 * Entity containing the result of the checking of a document.
 *
 * @author Marc Plouhinec
 */
@Entity
@Table(name = "DOCUMENT_CHECKING_RESULT")
@NamedNativeQueries(
        NamedNativeQuery(
                name = "DocumentCheckingResult.findAllDocumentStatistics",
                query = "SELECT " +
                        "    mostRecentResult.DOCUMENT_URL, " +
                        "    stats.AVG_AVAILABILITY, " +
                        "    stats.AVG_VALIDITY, " +
                        "    mostRecentResult.SIZE_IN_BYTES, " +
                        "    mostRecentResult.DOWNLOAD_DURATION_IN_MILLIS " +
                        "FROM document_checking_result mostRecentResult " +
                        "INNER JOIN ( " +
                        "    SELECT " +
                        "        result.DOCUMENT_URL AS documentUrl, " +
                        "        MAX(result.CHECKING_DATE) AS maxCheckingDate, " +
                        "        AVG(result.IS_AVAILABLE * 100) AS AVG_AVAILABILITY, " +
                        "        AVG(result.IS_VALID * 100) AS AVG_VALIDITY " +
                        "    FROM document_checking_result result " +
                        "    GROUP BY result.DOCUMENT_URL " +
                        ") stats ON mostRecentResult.DOCUMENT_URL = stats.documentUrl AND mostRecentResult.CHECKING_DATE = stats.maxCheckingDate",
                resultClass = DocumentStatistics::class)
)
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
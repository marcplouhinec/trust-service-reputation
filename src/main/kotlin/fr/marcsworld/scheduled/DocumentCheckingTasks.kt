package fr.marcsworld.scheduled

import fr.marcsworld.enums.DocumentType
import fr.marcsworld.model.dto.DocumentUrlAndType
import fr.marcsworld.model.entity.DocumentCheckingResult
import fr.marcsworld.service.DocumentService
import fr.marcsworld.utils.ResourceDownloader
import org.slf4j.LoggerFactory
import org.springframework.core.io.UrlResource
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.helpers.XMLReaderFactory
import java.util.*

/**
 * Tasks for checking documents and updating the database.
 *
 * @author Marc Plouhinec
 */
@Component
open class DocumentCheckingTasks(
        val documentService: DocumentService,
        val documentCheckingTaskExecutor: TaskExecutor
) {

    companion object {
        val LOGGER = LoggerFactory.getLogger(DocumentCheckingTasks::class.java)!!
    }

    /**
     * Find all documents that need to be checked and create one task for each of them.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 10 * 1000)
    fun scheduleDocumentCheckingTasks() {
        LOGGER.info("Check documents.")
        val documentUrlAndTypes = documentService.findAllStillProvidedDocumentUrlAndTypes()
        for (documentUrlAndType in documentUrlAndTypes) {
            documentCheckingTaskExecutor.execute(DocumentCheckingTask(documentUrlAndType))
        }
    }

    /**
     * Download and check the given document.
     */
    inner class DocumentCheckingTask(
            val documentUrlAndType: DocumentUrlAndType
    ) : Runnable {

        override fun run() {
            LOGGER.debug("Check the document: {}", documentUrlAndType.url)
            val currentDate = Date()

            // Try to download the document
            val timeBeforeDownloading = System.currentTimeMillis()
            val documentData: ByteArray? = try {
                ResourceDownloader.downloadResource(UrlResource(documentUrlAndType.url))
            } catch (e: Exception) {
                LOGGER.warn("Unable to download the document ${documentUrlAndType.url}", e)
                null
            }
            val timeAfterDownloading = System.currentTimeMillis()

            // Validate the document if applicable
            val isDocumentValid =
                    if (documentData is ByteArray && documentUrlAndType.type == DocumentType.TS_STATUS_LIST_XML) {
                        // Check that the XML document is well-formed
                        try {
                            documentData.inputStream().use {
                                val xmlReader = XMLReaderFactory.createXMLReader()
                                xmlReader.parse(InputSource(it))
                            }
                            true
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        true
                    }

            // Store the result in the database
            val result = DocumentCheckingResult(
                    url = documentUrlAndType.url,
                    date = currentDate,
                    isAvailable = documentData is ByteArray,
                    isValid = isDocumentValid,
                    sizeInBytes = (documentData as? ByteArray)?.size ?: 0,
                    downloadDurationInMillis =
                    if (documentData is ByteArray) timeAfterDownloading - timeBeforeDownloading else 0)
            documentService.saveDocumentCheckingResult(result)
        }

    }
}
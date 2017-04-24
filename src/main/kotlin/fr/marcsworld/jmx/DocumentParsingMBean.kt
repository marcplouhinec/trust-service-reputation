package fr.marcsworld.jmx

import org.springframework.core.task.TaskExecutor
import org.springframework.jmx.export.annotation.ManagedAttribute
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component

/**
 * Expose some properties via JMX related to document parsing tasks.
 *
 * @author Marc Plouhinec
 */
@Component
@ManagedResource
open class DocumentParsingMBean(
        val documentParsingTaskExecutor: TaskExecutor
) {

    /**
     * @return Current size of the queue of the [ThreadPoolTaskExecutor] responsible for executing parsing document tasks.
     */
    @ManagedAttribute
    fun getDocumentParsingTaskExecutorQueueSize(): Int {
        return (documentParsingTaskExecutor as? ThreadPoolTaskExecutor)?.threadPoolExecutor?.queue?.size ?: -1
    }
}
package fr.marcsworld.jmx

import org.springframework.core.task.TaskExecutor
import org.springframework.jmx.export.annotation.ManagedAttribute
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component

/**
 * Expose some properties via JMX related to task executors.
 *
 * @author Marc Plouhinec
 */
@Component
@ManagedResource
open class TaskExecutorStatusMBean(
        val documentParsingTaskExecutor: TaskExecutor,
        val documentCheckingTaskExecutor: TaskExecutor
) {

    /**
     * @return Current size of the queue of the [ThreadPoolTaskExecutor] responsible for executing parsing document tasks.
     */
    @ManagedAttribute
    fun getDocumentParsingTaskExecutorQueueSize(): Int {
        return (documentParsingTaskExecutor as? ThreadPoolTaskExecutor)?.threadPoolExecutor?.queue?.size ?: -1
    }

    /**
     * @return Current size of the queue of the [ThreadPoolTaskExecutor] responsible for executing checking document tasks.
     */
    @ManagedAttribute
    fun getDocumentCheckingTaskExecutorQueueSize(): Int {
        return (documentCheckingTaskExecutor as? ThreadPoolTaskExecutor)?.threadPoolExecutor?.queue?.size ?: -1
    }
}
package fr.marcsworld

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@SpringBootApplication
@EnableScheduling
class Application : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar?) {
        taskRegistrar?.setScheduler(taskScheduler())
    }

    /**
     * [TaskScheduler] used for starting tasks automatically like downloading and parsing documents.
     */
    @Bean(destroyMethod = "shutdown")
    fun taskScheduler(): TaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 2
        return taskScheduler
    }

    /**
     * [TaskExecutor] used for the document parsing scheduled tasks to create sub-tasks.
     */
    @Bean(destroyMethod = "shutdown")
    fun documentParsingTaskExecutor(): TaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 10
        taskExecutor.maxPoolSize = 20
        return taskExecutor
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

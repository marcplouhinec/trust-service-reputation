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
import java.util.TimeZone
import javax.annotation.PostConstruct



@SpringBootApplication
@EnableScheduling
class Application : SchedulingConfigurer {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }

    /**
     * Simple-but-not-perfect solution to always work in UTC, especially with the database.
     * For more info, see https://moelholm.com/2016/11/09/spring-boot-controlling-timezones-with-hibernate/
     */
    @PostConstruct
    fun setTimeZoneToUTC() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

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
        taskExecutor.corePoolSize = 30
        taskExecutor.setAllowCoreThreadTimeOut(true)
        taskExecutor.threadNamePrefix = "document-parsing-"
        return taskExecutor
    }

    /**
     * [TaskExecutor] used for the document checking scheduled tasks to create sub-tasks.
     */
    @Bean(destroyMethod = "shutdown")
    fun documentCheckingTaskExecutor(): TaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 30
        taskExecutor.setAllowCoreThreadTimeOut(true)
        taskExecutor.threadNamePrefix = "document-checking-"
        return taskExecutor
    }

}

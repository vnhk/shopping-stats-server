package com.bervan.shstat.queue;

import com.bervan.logging.JsonLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "shopping");

    @Bean("productTaskExecutor")
    public Executor productTaskExecutor() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int core = Math.max(1, availableProcessors * 2);

        log.info("Available processors in application: {}", availableProcessors);
        log.info("Product Task Executor pool size : {}", core);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core + 1);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ProductTask-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        executor.getThreadPoolExecutor().allowCoreThreadTimeOut(true);

        return executor;
    }
}

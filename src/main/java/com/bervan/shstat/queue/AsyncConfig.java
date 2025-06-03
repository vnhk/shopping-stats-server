package com.bervan.shstat.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig {

    @Bean("productTaskExecutor")
    public Executor productTaskExecutor() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int core = Math.max(1, availableProcessors - 1);

        log.info("Available processors in application: {}", availableProcessors);

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

package com.main_group_ekn47.eventlib.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

//@Configuration
public class ReactorConfig {

 //   @Bean("rabbitReactiveExecutor")
    public TaskExecutor rabbitReactiveExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rabbit-reactive-");
        executor.initialize();
        return executor;
    }
}
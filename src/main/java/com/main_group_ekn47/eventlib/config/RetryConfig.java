package com.main_group_ekn47.eventlib.config;

import com.main_group_ekn47.eventlib.core.RetryPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "messaging.retry")
    public RetryPolicy retryPolicy() {
        return new RetryPolicy();
    }
}
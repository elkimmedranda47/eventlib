package com.main_group_ekn47.eventlib.config;

import com.main_group_ekn47.eventlib.monitoring.MetricsCollector;
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class MetricsConfig {
    
    @Bean
    public MetricsCollector metricsCollector(MeterRegistry meterRegistry) {
        return new MetricsCollector(meterRegistry);
    }
    
    @Bean
    public LoggingHandler loggingHandler() {
        return new LoggingHandler();
    }
}
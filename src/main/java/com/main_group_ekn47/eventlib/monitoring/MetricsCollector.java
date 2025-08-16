package com.main_group_ekn47.eventlib.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final AtomicInteger outboxPendingEvents = new AtomicInteger(0);

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeCoreMetrics();
    }

    private void initializeCoreMetrics() {
        // Gauge para eventos pendientes en Outbox
        Gauge.builder("eventlib.outbox.pending", outboxPendingEvents, AtomicInteger::get)
             .description("Número de eventos pendientes en Outbox")
             .register(meterRegistry);
    }

    public void recordPublishedEvent(String eventType, boolean success) {
        String status = success ? "success" : "error";
        String key = "event.publish." + eventType + "." + status;
        
        counters.computeIfAbsent(key, k -> 
            Counter.builder("eventlib.events.published")
                .tag("event_type", eventType)
                .tag("status", status)
                .register(meterRegistry)
        ).increment();
    }

    public void recordConsumedEvent(String eventType, boolean success) {
        String status = success ? "success" : "error";
        String key = "event.consume." + eventType + "." + status;
        
        counters.computeIfAbsent(key, k -> 
            Counter.builder("eventlib.events.consumed")
                .tag("event_type", eventType)
                .tag("status", status)
                .register(meterRegistry)
        ).increment();
    }

    public void recordOutboxPendingCount(int count) {
        outboxPendingEvents.set(count);
    }

    public void recordProcessingTime(String operation, long durationMs) {
        timers.computeIfAbsent(operation, op -> 
            Timer.builder("eventlib.processing.time")
                .tag("operation", op)
                .register(meterRegistry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }
}
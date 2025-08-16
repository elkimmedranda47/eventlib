package com.main_group_ekn47.eventlib.monitoring;

import com.main_group_ekn47.eventlib.producer.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OutboxMetricsScheduler {

    private final OutboxRepository outboxRepository;
    private final MetricsCollector metricsCollector;

    public OutboxMetricsScheduler(OutboxRepository outboxRepository, 
                                  MetricsCollector metricsCollector) {
        this.outboxRepository = outboxRepository;
        this.metricsCollector = metricsCollector;
    }
/*
    @Scheduled(fixedRate = 10000)
    public void updateOutboxMetrics() {
        outboxRepository.countByPublishedAtIsNull()
            .doOnNext(metricsCollector::recordOutboxPendingCount)
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }
    */
    /*
    *****************************************************************
     */
    @Scheduled(fixedRate = 10000)
    public void updateOutboxMetrics() {
        outboxRepository.countByPublishedAtIsNull()
                .doOnNext(count -> metricsCollector.recordOutboxPendingCount(count.intValue()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
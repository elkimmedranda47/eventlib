package com.main_group_ekn47.eventlib.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "messaging.idempotency.store", havingValue = "memory", matchIfMissing = true)
public class MemoryIdempotencyStore implements IdempotencyStore {

    private final Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> processingIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public Mono<Boolean> isProcessed(String eventId) {
        // Verificar si ya fue procesado completamente
        if (processedIds.contains(eventId)) {
            return Mono.just(true);
        }
        
        // Manejo de eventos en proceso (evita procesamiento paralelo)
        synchronized (this) {
            if (processingIds.contains(eventId)) {
                return Mono.just(true); // Ya está siendo procesado
            }
            processingIds.add(eventId);
        }
        
        return Mono.just(false);
    }

    @Override
    public Mono<Void> markProcessed(String eventId) {
        return Mono.fromRunnable(() -> {
            processedIds.add(eventId);
            processingIds.remove(eventId);
        });
    }

    @Override
    public Mono<Void> cleanProcessedEvents(Duration retentionPeriod) {
        // Implementación simple: no necesita limpieza en memoria
        return Mono.empty();
    }
}
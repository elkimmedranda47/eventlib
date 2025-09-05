/*package com.main_group_ekn47.eventlib.consumer;

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
        System.out.println("🔍 Checking if processed: " + eventId);
        System.out.println("Processed IDs: " + processedIds);


        if (processedIds.contains(eventId)) {
            System.out.println("Processed IDs: *1 ");
            return Mono.just(true);
        }
        
        // Manejo de eventos en proceso (evita procesamiento paralelo)
        synchronized (this) {
            System.out.println("Processed IDs: *2 ");
            if (processingIds.contains(eventId)) {
                System.out.println("Processed IDs: *2 ");
                return Mono.just(true); // Ya está siendo procesado
            }
            System.out.println("Processed IDs: *3 ");
            processingIds.add(eventId);
        }
        System.out.println("Processed IDs: *4 ");
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
}*/

package com.main_group_ekn47.eventlib.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//@Component
//@ConditionalOnProperty(name = "messaging.idempotency.store", havingValue = "memory", matchIfMissing = true)
public class MemoryIdempotencyStore implements IdempotencyStore {

    private final Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public Mono<Boolean> isProcessed(String eventId) {
        boolean processed = processedIds.contains(eventId);
        System.out.println("🔍 MemoryIdempotencyStore check: " + eventId + " = " + processed);
        return Mono.just(processed);
    }

    @Override
    public Mono<Void> markProcessed(String eventId) {
        return Mono.fromRunnable(() -> {
            processedIds.add(eventId);
            System.out.println("✅ MemoryIdempotencyStore marcado: " + eventId);
        });
    }

    @Override
    public Mono<Void> cleanProcessedEvents(Duration retentionPeriod) {
        // Limpieza opcional para memoria
        return Mono.fromRunnable(() -> {
            System.out.println("🧹 Limpiando eventos antiguos en memoria");
            // processedIds.clear(); // O lógica más sofisticada
        });
    }
}
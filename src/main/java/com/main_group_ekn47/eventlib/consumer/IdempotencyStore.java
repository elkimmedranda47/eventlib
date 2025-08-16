package com.main_group_ekn47.eventlib.consumer;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface IdempotencyStore {
    Mono<Boolean> isProcessed(String eventId);
    Mono<Void> markProcessed(String eventId);
    // Solo añade este nuevo método
    Mono<Void> cleanProcessedEvents(Duration retention);
}
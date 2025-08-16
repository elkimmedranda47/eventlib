package com.main_group_ekn47.eventlib.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "messaging.idempotency.store", havingValue = "redis")
@ConditionalOnBean(ReactiveRedisTemplate.class)
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "eventlib:idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisIdempotencyStore(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Boolean> isProcessed(String eventId) {
        String key = KEY_PREFIX + eventId;
        
        return redisTemplate.opsForValue().get(key)
            .map("PROCESSED"::equals)
            .defaultIfEmpty(false)
            .onErrorReturn(false);
    }

    @Override
    public Mono<Void> markProcessed(String eventId) {
        String key = KEY_PREFIX + eventId;
        return redisTemplate.opsForValue().set(key, "PROCESSED", DEFAULT_TTL)
            .then()
            .onErrorResume(e -> Mono.empty());
    }

    @Override
    public Mono<Void> cleanProcessedEvents(Duration retentionPeriod) {
        // Redis maneja TTL automáticamente
        return Mono.empty();
    }
}
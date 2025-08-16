package com.main_group_ekn47.eventlib.producer.aop;

import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.core.PublishEvent;
import com.main_group_ekn47.eventlib.producer.OutboxEvent;
import com.main_group_ekn47.eventlib.producer.OutboxRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Aspect
@Component
public class PublishEventAspect {

    private final OutboxRepository outboxRepository;
    private final MessageSerializer serializer;

    public PublishEventAspect(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        this.serializer = new MessageSerializer();
    }

    @Around("@annotation(publishEvent)")
    @Transactional
    public Object handlePublishEvent(ProceedingJoinPoint joinPoint, PublishEvent publishEvent) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (result instanceof Mono) {
            return ((Mono<?>) result).flatMap(event -> {
                if (event instanceof IntegrationEvent) {
                    return saveOutboxEvent(publishEvent, (IntegrationEvent) event);
                }
                return Mono.just(event);
            });
        } else if (result instanceof IntegrationEvent) {
            return saveOutboxEvent(publishEvent, (IntegrationEvent) result);
        }
        
        return result;
    }

    private Mono<IntegrationEvent> saveOutboxEvent(PublishEvent annotation, IntegrationEvent event) {
        String payload = serializer.serialize(event);
        
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .id(UUID.randomUUID())
            .eventName(annotation.eventName())
            .topic(annotation.topic())
            .payload(payload)
            .createdAt(Instant.now())
            .build();
        
        return outboxRepository.save(outboxEvent)
            .thenReturn(event)
            .onErrorMap(e -> new RuntimeException("Error saving to Outbox", e));
    }
}

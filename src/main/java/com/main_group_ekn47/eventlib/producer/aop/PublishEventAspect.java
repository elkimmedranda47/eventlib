/*
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
        System.out.println(">>> Aspecto interceptando método...1");
    }

    @Around("@annotation(com.main_group_ekn47.eventlib.core.PublishEvent)")
    @Transactional
    public Object handlePublishEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(">>> Aspecto interceptando método...2");

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        PublishEvent publishEvent = methodSignature.getMethod().getAnnotation(PublishEvent.class);
        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return ((Mono<?>) result).flatMap(event -> {
                if (event instanceof IntegrationEvent) {
                    // El Mono del servicio se completa, luego encadenamos la operación de guardado.
                    return saveOutboxEvent(publishEvent, (IntegrationEvent) event)
                            .then(Mono.just(event)); // Asegura que el Mono original se retorne
                }
                return Mono.just(event);
            });
        } else if (result instanceof IntegrationEvent) {
            // Para casos síncronos, encadena la operación de guardado y bloquea
            // para asegurar que se complete antes de retornar.
            // Nota: Esto no es ideal en un entorno reactivo pero asegura la persistencia.
            return saveOutboxEvent(publishEvent, (IntegrationEvent) result).block();
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
    */
    /*
        https://gemini.google.com/app/7d38653a6427bb1b?hl=es
        2. Usar Mono.then() para encadenar
        El cambio clave es saveOutboxEvent(...).then(Mono.just(event)). Esto garantiza que:

        El Mono del servicio (joinPoint.proceed()) se ejecuta.

        Cuando ese Mono se completa, se invoca saveOutboxEvent.

        La operación de save se ejecuta y, cuando se completa, el Mono original (que contiene el evento) es propagado. Esto mantiene la transparencia del flujo.
     */
//}
package com.main_group_ekn47.eventlib.producer.aop;

import com.main_group_ekn47.eventlib.broker.rabbitmq.RabbitMQPublisher;
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
    private final RabbitMQPublisher publisher;


    public PublishEventAspect(OutboxRepository outboxRepository, RabbitMQPublisher publisher,MessageSerializer serializer) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
        this.serializer = serializer; // ⬅️ ¡Asignación correcta!

    }

    @Around("@annotation(com.main_group_ekn47.eventlib.core.PublishEvent)")
    @Transactional
    public Object handlePublishEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        PublishEvent publishEvent = methodSignature.getMethod().getAnnotation(PublishEvent.class);
        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return ((Mono<?>) result).flatMap(event -> {

                if (event instanceof IntegrationEvent) {
                    return saveOutboxEvent(publishEvent, (IntegrationEvent) event).then(Mono.just(event));
                }

                return Mono.just(event);
            });
        } else if (result instanceof IntegrationEvent) {
            return saveOutboxEvent(publishEvent, (IntegrationEvent) result).block();
        }

        return result;
    }

    private Mono<OutboxEvent> saveOutboxEvent(PublishEvent annotation, IntegrationEvent event) {
        String payload = serializer.serialize(event);
        String className = event.getClass().getName(); // <-- ¡Obtén el nombre de la clase!
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventName(annotation.eventName())
                .topic(annotation.topic())
                .payload(payload)
                .createdAt(Instant.now())
                .className(className) // <-- ¡Añádelo aquí!
                .build();
        return outboxRepository.save(outboxEvent).onErrorMap(e -> new RuntimeException("Error saving to Outbox", e));
    }


}
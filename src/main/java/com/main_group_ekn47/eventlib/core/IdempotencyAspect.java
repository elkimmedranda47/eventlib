package com.main_group_ekn47.eventlib.core;

import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;
    private final MessageSerializer serializer;

    public IdempotencyAspect(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = new MessageSerializer();
    }

    @Around("@annotation(rabbitListener)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, RabbitListener rabbitListener) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return joinPoint.proceed();

        // Manejo para IntegrationEvent
        if (args[0] instanceof IntegrationEvent event) {
            return processEvent(event, joinPoint);
        }
        
        // Manejo para mensajes String (JSON)
        if (args[0] instanceof String jsonPayload) {
            try {
                IntegrationEvent event = serializer.deserialize(jsonPayload, IntegrationEvent.class);
                return processEvent(event, joinPoint);
            } catch (Exception e) {
                // Fallback si no se puede deserializar
                return joinPoint.proceed();
            }
        }
        
        return joinPoint.proceed();
    }

    private Object processEvent(IntegrationEvent event, ProceedingJoinPoint joinPoint) {
        String eventId = event.getMetadata().getEventId();
        
        return idempotencyStore.isProcessed(eventId)  // Consulta a Redis (KEY: "eventlib:idempotency:<eventId>")
            .flatMap(processed -> {
                if (processed) return Mono.empty();
                
                try {
                    Object result = joinPoint.proceed();
                    if (result instanceof Mono) {
                        return ((Mono<?>) result)
                            .flatMap(res -> idempotencyStore.markProcessed(eventId).thenReturn(res));//idempotencyStore.markProcessed(eventId) // Guarda en Redis con TTL (24h por defecto)

                    }
                    return idempotencyStore.markProcessed(eventId).thenReturn(result);
                } catch (Throwable e) {
                    return Mono.error(e);
                }
            });
    }
}

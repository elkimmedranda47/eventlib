package com.main_group_ekn47.eventlib.core;
/*
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
        System.out.println("||0|||++++++++++******++++++++archivo IdempotencyAspect *******");

    }

    @Around("@annotation(rabbitListener)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, RabbitListener rabbitListener) throws Throwable {
        // Obtiene los argumentos del método interceptado.
        System.out.println("||2|||++++++++++******++++++++archivo IdempotencyAspect *******");

        Object[] args = joinPoint.getArgs();
        if (args.length == 0)
            // Si no hay argumentos, simplemente procede con la ejecución del método original.
            return joinPoint.proceed();
        System.out.println("||3|||++++++++++******++++++++archivo IdempotencyAspect *******");

        // --- Paso 1: Intercepta el mensaje y determina su tipo ---

        // Maneja el caso en que el primer argumento es un IntegrationEvent.
        // Manejo para IntegrationEvent
        if (args[0] instanceof IntegrationEvent event) {
            // Procesa el evento de forma reactiva y no bloqueante.
            return processEvent(event, joinPoint);
        }

        // Maneja el caso en que el primer argumento es un String (payload JSON).
        // Manejo para mensajes String (JSON)
        if (args[0] instanceof String jsonPayload) {
            try {
                System.out.println("||1|||++++++++++******++++++++archivo IdempotencyAspect *******");
                // Deserializa el JSON en un objeto IntegrationEvent.
                IntegrationEvent event = serializer.deserialize(jsonPayload, IntegrationEvent.class);
                return processEvent(event, joinPoint);
            } catch (Exception e) {
                // Si la deserialización falla, se asume que no es un evento
                // válido y se permite la ejecución original para evitar bloquear el sistema.
                // Fallback si no se puede deserializar
                return joinPoint.proceed();
            }
        }
        // Si el argumento no es de los tipos esperados, procede sin la lógica de idempotencia.
        System.out.println("||4|||++++++++++******++++++++archivo IdempotencyAspect *******");

        return joinPoint.proceed();
    }




    private Object processEvent(IntegrationEvent event, ProceedingJoinPoint joinPoint) {
        String eventId = event.getMetadata().getEventId();
        System.out.println("🎯 EVENTO RECIBIDO EN ASPECTO: " + eventId);

        try {
            // Verificación síncrona
            Boolean processed = idempotencyStore.isProcessed(eventId).block();
            System.out.println("🔍 Evento " + eventId + " procesado?: " + processed);

            if (Boolean.TRUE.equals(processed)) {
                System.out.println("⚠️  Mensaje descartado por idempotencia");
                return null;
            }

            System.out.println("✅  Evento NO procesado, continuando...");

            // Ejecutar el consumidor
            Object result = joinPoint.proceed();

            // Marcar como procesado
            idempotencyStore.markProcessed(eventId).block();
            System.out.println("✅ Mensaje procesado exitosamente");

            return result;

        } catch (Throwable e) {
            throw new RuntimeException("Error en idempotencia", e);
        }
    }




}
*/
/*
import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;
    private final MessageSerializer serializer;

    public IdempotencyAspect(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = new MessageSerializer();
        System.out.println("✅ IdempotencyAspect inicializado (modo sincrónico)");
    }

    @Around("@annotation(rabbitListener)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, RabbitListener rabbitListener) throws Throwable {
        System.out.println("🎯 IdempotencyAspect interceptando mensaje");

        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return joinPoint.proceed();
        }

        // 1. Extraer el mensaje
        Object message = args[0];
        IntegrationEvent event = null;

        if (message instanceof IntegrationEvent) {
            event = (IntegrationEvent) message;
            System.out.println("📦 Evento IntegrationEvent detectado directamente");
        }
        else if (message instanceof String jsonPayload) {
            System.out.println("📄 Mensaje JSON detectado, deserializando...");
            try {
                event = serializer.deserialize(jsonPayload, IntegrationEvent.class);
                System.out.println("✅ Deserialización exitosa");
            } catch (Exception e) {
                System.out.println("❌ Error deserializando JSON: " + e.getMessage());
                return joinPoint.proceed(); // Fallback
            }
        }
        else {
            System.out.println("🔍 Tipo de mensaje no manejado: " + message.getClass());
            return joinPoint.proceed();
        }

        // 2. Verificar idempotencia (BLOQUEANTE - porque estamos en AOP)
        String eventId = event.getMetadata().getEventId();
        System.out.println("🎯 Verificando idempotencia para: " + eventId);

        // 🔥 BLOQUEANTE pero necesario en AOP
        boolean processed = idempotencyStore.isProcessed(eventId).block();
        System.out.println("🔍 Resultado verificación: " + processed);

        if (processed) {
            System.out.println("⚠️ Mensaje descartado (ya procesado): " + eventId);
            return null; // Descarta el mensaje
        }

        // 3. Ejecutar el método original
        System.out.println("🚀 Ejecutando método original...");
        Object result = joinPoint.proceed();
        System.out.println("✅ Método original ejecutado exitosamente");

        // 4. Marcar como procesado
        System.out.println("✅ Marcando evento como procesado...");
        idempotencyStore.markProcessed(eventId).block();
        System.out.println("🎉 Evento completado y marcado: " + eventId);

        return result;
    }
}*/

/*
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
        if (args.length == 0) {
            return joinPoint.proceed();
        }

        Object message = args[0];
        Mono<IntegrationEvent> eventMono;

        if (message instanceof IntegrationEvent event) {
            eventMono = Mono.just(event);
        } else if (message instanceof String jsonPayload) {
            try {
                IntegrationEvent event = serializer.deserialize(jsonPayload, IntegrationEvent.class);
                eventMono = Mono.just(event);
            } catch (Exception e) {
                return joinPoint.proceed();
            }
        } else {
            return joinPoint.proceed();
        }

        // El flujo reactivo no bloqueante comienza aquí
        return eventMono
                .flatMap(event -> {
                    String eventId = event.getMetadata().getEventId();
                    return idempotencyStore.isProcessed(eventId)
                            .flatMap(processed -> {
                                if (Boolean.TRUE.equals(processed)) {
                                    System.out.println("⚠️ Mensaje descartado (ya procesado): " + eventId);
                                    return Mono.empty();
                                }
                                // No fue procesado, se continúa con el flujo normal
                                try {
                                    Object result = joinPoint.proceed();
                                    if (result instanceof Mono) {
                                        return ((Mono<?>) result)
                                                .flatMap(res -> idempotencyStore.markProcessed(eventId).thenReturn(res));
                                    }
                                    return idempotencyStore.markProcessed(eventId).thenReturn(result);
                                } catch (Throwable e) {
                                    return Mono.error(e);
                                }
                            });
                });
    }
}*/

import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;

    public IdempotencyAspect(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @Around("@annotation(rabbitListener)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, RabbitListener rabbitListener) {
        System.out.println("🎯🔍 ASPECTO - Iniciando procesamiento...");

        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            System.out.println("🎯🔍 ASPECTO - Sin argumentos, procediendo...");
            return executeBlocking(joinPoint);
        }

        Object message = args[0];
        System.out.println("🎯🔍 ASPECTO - Tipo de mensaje: " + message.getClass().getName());

        if (!(message instanceof IntegrationEvent)) {
            System.out.println("🎯🔍 ASPECTO - No es IntegrationEvent, procediendo...");
            return executeBlocking(joinPoint);
        }

        IntegrationEvent event = (IntegrationEvent) message;
        String eventId = event.getMetadata().getEventId();
        System.out.println("🎯🔍 ASPECTO - Procesando evento: " + eventId);

        return idempotencyStore.isProcessed(eventId)
                .doOnNext(processed -> System.out.println("🎯🔍 ASPECTO - isProcessed: " + processed))
                .flatMap(processed -> {
                    if (processed) {
                        System.out.println("🎯🔍 ASPECTO - Evento ya procesado, descartando: " + eventId);
                        return Mono.empty();
                    }
                    System.out.println("🎯🔍 ASPECTO - Evento NUEVO, ejecutando: " + eventId);
                    return executeAndMark(joinPoint, eventId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .block();
    }
    private Mono<Object> executeBlocking(ProceedingJoinPoint joinPoint) {
        return Mono.fromCallable(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {  // ✅ Capturar Throwable explícitamente
                        throw new RuntimeException("Error ejecutando método original", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
    private Mono<Object> executeAndMark(ProceedingJoinPoint joinPoint, String eventId) {
        return Mono.fromCallable(() -> {
                    try {
                        System.out.println("🚀🔍 ASPECTO - Ejecutando método original...");
                        Object result = joinPoint.proceed();
                        System.out.println("✅🔍 ASPECTO - Método original ejecutado exitosamente");
                        return result;
                    } catch (Throwable e) {  // ✅ Capturar Throwable
                        System.out.println("❌🔍 ASPECTO - Error ejecutando método: " + e.getMessage());
                        throw new RuntimeException("Error ejecutando método original", e);
                    }
                })
                .flatMap(result ->
                        idempotencyStore.markProcessed(eventId)
                                .doOnSuccess(v -> System.out.println("✅🔍 ASPECTO - Evento marcado como procesado: " + eventId))
                                .thenReturn(result)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }
}

    // ... resto del código igual

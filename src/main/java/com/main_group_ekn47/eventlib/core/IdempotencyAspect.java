package com.main_group_ekn47.eventlib.core;

import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * El IdempotencyAspect utiliza la Programación Orientada a Aspectos (AOP)
 * para garantizar que los mensajes recibidos a través de RabbitMQ
 * (escuchados con @RabbitListener) sean procesados solo una vez.
 * Esto previene problemas de duplicación de datos en caso de reintentos
 * de mensajes por fallos de la red o del servicio.
 */
@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;
    private final MessageSerializer serializer;

    /**
     * Inyección de dependencias para el almacén de idempotencia (normalmente Redis)
     * y el serializador.
     */
    public IdempotencyAspect(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = new MessageSerializer();
        System.out.println("||0|||++++++++++******++++++++archivo IdempotencyAspect *******");

    }
    /**
     * Este es el "punto de corte" (pointcut) del aspecto.
     * La anotación @Around indica que este método "envolverá" la ejecución
     * de cualquier método que tenga la anotación @RabbitListener.
     *
     * @param joinPoint El objeto que representa la ejecución del método original.
     * @param rabbitListener La instancia de la anotación @RabbitListener del método.
     * @return El resultado (Mono) de la ejecución del flujo reactivo.
     * @throws Throwable si el método original o la lógica del aspecto fallan.
     */
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

    /**
     * Contiene la lógica principal de idempotencia de manera reactiva y no bloqueante.
     * Este método construye la cadena de operaciones que se resolverá en el futuro.
     *
     * @param event El evento de integración.
     * @param joinPoint El objeto de ejecución del método original.
     * @return Un Mono que representa el flujo completo de procesamiento.
     */
    private Object xxprocessEvent(IntegrationEvent event, ProceedingJoinPoint joinPoint) {

        String eventId = event.getMetadata().getEventId();
        /* System.out.println("||5|||++++++++++******++++++++archivo IdempotencyAspect *******");

        // --- Paso 2: Verifica la idempotencia (consulta asíncrona) ---
        // Se llama a idempotencyStore.isProcessed, que devuelve un Mono.
        // La ejecución no se bloquea aquí; la lógica continúa dentro del flatMap.
        return idempotencyStore.isProcessed(eventId)  // Consulta a Redis (KEY: "eventlib:idempotency:<eventId>")
            .flatMap(processed -> {
                System.out.println("🔍 Evento " + eventId + " procesado?: " + processed);

                // Si el evento ya fue procesado, se devuelve un Mono vacío y el flujo termina.
                if (processed) return Mono.empty();
                System.out.println("⚠️  Mensaje descartado por idempotencia");

                // Si el evento no ha sido procesado, se procede con la lógica de negocio.
                try {
                    // Ejecuta el método original del consumidor (@RabbitListener).
                    Object result = joinPoint.proceed();
                    if (result instanceof Mono) {
                        return ((Mono<?>) result)
                                // --- Paso 3: Marca como procesado (después del éxito) ---
                                // El flatMap espera a que el Mono de la lógica de negocio
                                // se complete y luego ejecuta la siguiente operación.
                                // Esto garantiza que solo se marca como procesado si todo va bien.
                                .flatMap(res -> idempotencyStore.markProcessed(eventId).thenReturn(res));//idempotencyStore.markProcessed(eventId) // Guarda en Redis con TTL (24h por defecto)

                    }
                    // Si el resultado no es un Mono (caso síncrono), se marca como procesado
                    // de forma bloqueante para garantizar la persistencia.
                    return idempotencyStore.markProcessed(eventId).thenReturn(result);
                } catch (Throwable e) {
                    // Si el método original lanza una excepción, se propaga el error.
                    return Mono.error(e);
                }
            });
            */

        return idempotencyStore.isProcessed(eventId)
                .flatMap(processed -> {
                    System.out.println("🔍 Evento " + eventId + " procesado?: " + processed);

                    if (processed) {
                        System.out.println("⚠️  Mensaje descartado por idempotencia");
                        return Mono.empty();
                    }

                    // ¡FALTA ESTA LÍNEA! ↓
                    System.out.println("✅  Evento NO procesado, continuando...");

                    try {
                        Object result = joinPoint.proceed();  // ← Esto debería ejecutar tu consumidor
                        if (result instanceof Mono) {
                            return ((Mono<?>) result)
                                    .flatMap(res -> idempotencyStore.markProcessed(eventId).thenReturn(res));
                        }
                        return idempotencyStore.markProcessed(eventId).thenReturn(result);
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                });



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

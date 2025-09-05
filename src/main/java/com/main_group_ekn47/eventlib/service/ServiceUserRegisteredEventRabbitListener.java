/*package com.main_group_ekn47.eventlib.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceUserRegisteredEventRabbitListener {

    //private final EmailSenderService emailSenderService;

    private  IdempotencyStore idempotencyStore;
    private  MessageSerializer serializer;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Puedes inyectarlo si ya lo tienes


    // Utiliza la inyección por constructor para todas las dependencias
    public ServiceUserRegisteredEventRabbitListener(IdempotencyStore idempotencyStore, MessageSerializer serializer) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = serializer;
    }


    //@RabbitListener(queues = "eventlib_queue")
    @RabbitListener(queues = "eventlib_queue")

    public void handleUserRegisteredEvent(JsonNode eventJson) {
        // La librería maneja la idempotencia por nosotros con el IdempotencyAspect
        //JsonNode payload = serializer.deserializeToJson(event.getPayload());
       // System.out.println("Payload recibido: " + event.getPayload()); // <- Agrega esto
        System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: ");

        try {

            UserRegisteredEvent event = objectMapper.treeToValue(eventJson, UserRegisteredEvent.class);

            System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: " + event.getUserEmail());


            // Lógica de negocio para enviar el correo
           // emailSenderService.sendWelcomeEmail(event.getUserEmail());
           // System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: "+event.getActivationToken());

            // Si todo fue bien, marcas el mensaje como procesado
           // idempotencyStore.storeMessageId(event.getEventId());

        } catch (Exception e) {
            // La librería RetryHandler se encargará de los reintentos
            // O si es un error fatal, irá a la DLQ
        }


    }

}

 */
/*
package com.main_group_ekn47.eventlib.service;
import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ServiceUserRegisteredEventRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceUserRegisteredEventRabbitListener.class);

    private final IdempotencyStore idempotencyStore;

    // Se inyecta la interfaz IdempotencyStore a través del constructor.
    public ServiceUserRegisteredEventRabbitListener(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @RabbitListener(queues = "eventlib_queue")
    public Mono<Void> handleUserRegisteredEvent(UserRegisteredEvent event) {
        String eventId = event.getMetadata().getEventId();

        // 1. Usa la tienda de idempotencia para verificar si el evento ya fue procesado.
        return idempotencyStore.isProcessed(eventId)
                .flatMap(isProcessed -> {
                    if (Boolean.TRUE.equals(isProcessed)) {
                        // Si el evento ya fue procesado, se registra y se termina el flujo.
                        log.info("Evento duplicado detectado, ignorando: {}", eventId);
                        return Mono.empty(); // Termina el flujo sin hacer nada más.
                    }

                    // Si es un evento nuevo, se marca como procesado inmediatamente.
                    return idempotencyStore.markProcessed(eventId)
                            .then(Mono.defer(() -> {
                                // 2. Se ejecuta la lógica de negocio solo si el evento es nuevo y fue marcado.
                                log.info("Procesando nuevo evento: {}", eventId);
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! UserId:    " + event.getUserId());
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Email:     " + event.getUserEmail());
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Token:     " + event.getActivationToken());
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Fecha:     " + event.getMetadata().getTimestamp());
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! EventType: " + event.getMetadata().getEventType());
                                log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! EventId:    " + eventId);

                                // Aquí iría el resto de tu lógica de negocio (por ejemplo, guardar en base de datos).
                                // Retorna un Mono<Void> para indicar que la operación está completa.
                                return Mono.empty();
                            }));
                });
    }
}
 */

package com.main_group_ekn47.eventlib.service;

import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ServiceUserRegisteredEventRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceUserRegisteredEventRabbitListener.class);

    private final IdempotencyStore idempotencyStore;

    // Se inyecta la interfaz IdempotencyStore a través del constructor.
    public ServiceUserRegisteredEventRabbitListener(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @RabbitListener(queues = "eventlib_queue")
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        String eventId = event.getMetadata().getEventId();

        try {
            // 1. Usa la tienda de idempotencia para verificar si el evento ya fue procesado.
            // Se usa .block() para convertir el Mono<Boolean> en un Boolean normal.
            Boolean isProcessed = idempotencyStore.isProcessed(eventId).block();

            if (Boolean.TRUE.equals(isProcessed)) {
                // Si el evento ya fue procesado, se registra y se termina el flujo.
                log.info("Evento duplicado detectado, ignorando: {}", eventId);
                return; // Se usa 'return' para salir del método.
            }

            // 2. Si es un evento nuevo, se marca como procesado inmediatamente.
            // Se usa .block() para esperar que la operación en Redis se complete.
            idempotencyStore.markProcessed(eventId).block();

            // 3. Se ejecuta la lógica de negocio solo si el evento es nuevo y fue marcado.
            log.info("Procesando nuevo evento: {}", eventId);
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! UserId:    " + event.getUserId());
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Email:     " + event.getUserEmail());
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Token:     " + event.getActivationToken());
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! Fecha:     " + event.getMetadata().getTimestamp());
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! EventType: " + event.getMetadata().getEventType());
            log.info("🎯🔥🔥🔥 ¡ENTRÓ AL CONSUMIDOR! EventId:    " + eventId);

            // Aquí iría el resto de tu lógica de negocio.

        } catch (Exception e) {
            log.error("Error al procesar el evento con ID {}: {}", eventId, e.getMessage());
            // Puedes decidir si relanzar la excepción o manejarla de otra forma.
        }
    }
}



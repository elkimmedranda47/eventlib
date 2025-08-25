package com.main_group_ekn47.eventlib.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.main_group_ekn47.eventlib.core.MessagePublisher;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final MessagePublisher messagePublisher;
    private final LoggingHandler loggingHandler;
    private final MessageSerializer serializer;

 public OutboxPublisher(OutboxRepository outboxRepository,
                        MessagePublisher messagePublisher,
                        LoggingHandler loggingHandler,
                        MessageSerializer serializer) {
     // Inyección de dependencias
     this.outboxRepository = outboxRepository; // Acceso a la DB
     this.messagePublisher = messagePublisher; // Envía a RabbitMQ
     this.loggingHandler = loggingHandler;     // Registra logs
     this.serializer = serializer; // Serializa/deserializa JSON
   //  this.pollingInterval = pollingInterval;   // Intervalo de polling (ej: 5000 ms)

 }

    @Scheduled(fixedDelayString = "${messaging.outbox.polling-interval}")
    public void publishPendingEvents() {
        loggingHandler.logInfo(">>> Ejecutando publishPendingEvents...");

        outboxRepository.findUnpublishedEvents()
                .delayElements(Duration.ofMillis(100)) // Control de tasa (100ms entre eventos)
                .flatMap(this::publishEvent)           // Publica cada evento
                .onErrorContinue((e, o) ->             // Manejo de errores
                        loggingHandler.logError("Error processing outbox event: " + o, e))
                .subscribe();                          // Inicia el flujo Reactor
    }



private Mono<Void> publishEvent(OutboxEvent event) {
    try {
        /*
        // 1. Deserializa el payload JSON
        JsonNode payload = serializer.deserializeToJson(event.getPayload());
        System.out.println("Payload recibido: " + event.getPayload()); // <- Agrega esto

        System.out.println("!!!**********topic:"+event.getTopic()+" !!!**********EventName"+event.getEventName()+" !!!**********payload"+payload);
        // 2. Publica a RabbitMQ
        messagePublisher.publish(event.getTopic(), event.getEventName(), payload);
        */
        // Paso 1: Obtener la clase a partir del nombre guardado
        Class<?> eventType = Class.forName(event.getClassName());

        System.out.println("*********|||||********Odjeto des de la tabla : "+event.getClassName());

        // Paso 2: Deserializar el payload JSON al objeto Java
        Object payloadObject = serializer.deserialize(event.getPayload(), eventType);

        // Paso 3: Publicar el objeto Java. El RabbitTemplate se encarga del resto.
        messagePublisher.publish(event.getTopic(), event.getEventName(), payloadObject);

        // Paso 4: Marcar el evento como publicado en la base de datos


        // 3. Marca como publicado en DB
        return outboxRepository.markAsPublished(event.getId())
                .doOnSuccess(v -> loggingHandler.logInfo(
                        "Event published successfully: " + event.getId()))
                .doOnError(e -> loggingHandler.logError(
                        "Error marking event as published: " + event.getId(), e));
    } catch (Exception e) {
        loggingHandler.logError("Error publishing event: " + event.getId(), e);
        return Mono.empty(); // Retorna un Mono vacío para continuar
    }
}
}


/*
package com.main_group_ekn47.eventlib.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.core.MessagePublisher;
import com.main_group_ekn47.eventlib.producer.OutboxEvent;
import com.main_group_ekn47.eventlib.producer.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Objects;

@Component
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final MessagePublisher messagePublisher;
    private final ObjectMapper objectMapper;

    @Autowired
    public OutboxPublisher(OutboxRepository outboxRepository, MessagePublisher messagePublisher, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.messagePublisher = messagePublisher;
        this.objectMapper = objectMapper;
    }

    // Este es el programador que se ejecutará periódicamente (ej: cada 1 segundo)
    @Scheduled(fixedRateString = "${eventlib.producer.publish-rate:1000}")
    public void publishPendingEvents() {
        System.out.println(">>> Buscando eventos en la Outbox...");

        outboxRepository.findAll()
                .flatMap(this::publishEvent)
                .flatMap(outboxEvent -> outboxRepository.delete(outboxEvent))
                .subscribe();
    }

    private Mono<OutboxEvent> publishEvent(OutboxEvent outboxEvent) {
        try {
            // Usa el nombre de la clase para deserializar correctamente
            Class<?> eventClass = Class.forName(outboxEvent.getClassName());
            // Deserializa el payload de la base de datos al objeto Java original
            //Object event = objectMapper.readValue(outboxEvent.getPayload(), Object.class); // Use Object.class or a superclass like IntegrationEvent.class
            Object event = objectMapper.readValue(outboxEvent.getPayload(), eventClass);

            // Publica el objeto Java. El RabbitTemplate se encargará de la serialización
            messagePublisher.publish(outboxEvent.getTopic(), outboxEvent.getEventName(), event);

            return Mono.just(outboxEvent); // Retorna el evento para que sea eliminado
        } catch (Exception e) {
            // Maneja el error, por ejemplo, moviendo el evento a una tabla de "errores"
            System.err.println("Error deserializing or publishing event from outbox: " + e.getMessage());
            return Mono.error(e);
        }
    }
}

*/
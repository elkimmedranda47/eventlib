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
   // private final long pollingInterval;

 /*   public OutboxPublisher(OutboxRepository outboxRepository,
                           MessagePublisher messagePublisher,
                           LoggingHandler loggingHandler,
                           long pollingInterval) {
        this.outboxRepository = outboxRepository;
        this.messagePublisher = messagePublisher;
        this.loggingHandler = loggingHandler;
        this.serializer = new MessageSerializer();
        this.pollingInterval = pollingInterval;
    }
*/
 public OutboxPublisher(OutboxRepository outboxRepository,
                        MessagePublisher messagePublisher,
                        LoggingHandler loggingHandler,
                        long pollingInterval) {
     // Inyección de dependencias
     this.outboxRepository = outboxRepository; // Acceso a la DB
     this.messagePublisher = messagePublisher; // Envía a RabbitMQ
     this.loggingHandler = loggingHandler;     // Registra logs
     this.serializer = new MessageSerializer(); // Serializa/deserializa JSON
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
        // 1. Deserializa el payload JSON
        JsonNode payload = serializer.deserializeToJson(event.getPayload());
        System.out.println("Payload recibido: " + event.getPayload()); // <- Agrega esto

        System.out.println("!!!**********topic:"+event.getTopic()+" !!!**********EventName"+event.getEventName()+" !!!**********payload"+payload);
        // 2. Publica a RabbitMQ
        messagePublisher.publish(event.getTopic(), event.getEventName(), payload);

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
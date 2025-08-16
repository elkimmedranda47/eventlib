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
    private final long pollingInterval;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           MessagePublisher messagePublisher,
                           LoggingHandler loggingHandler,
                           long pollingInterval) {
        this.outboxRepository = outboxRepository;
        this.messagePublisher = messagePublisher;
        this.loggingHandler = loggingHandler;
        this.serializer = new MessageSerializer();
        this.pollingInterval = pollingInterval;
    }

    //@Scheduled(fixedDelayString = "#{@pollingInterval}")
    @Scheduled(fixedDelayString = "${messaging.outbox.polling-interval}")

    public void publishPendingEvents() {
        outboxRepository.findUnpublishedEvents()
            .delayElements(Duration.ofMillis(100)) // Control de tasa
            .flatMap(this::publishEvent)
            .onErrorContinue((e, o) -> 
                loggingHandler.logError("Error processing outbox event: " + o, e))
            .subscribe();
    }

    private Mono<Void> publishEvent(OutboxEvent event) {
        try {
            JsonNode payload = serializer.deserializeToJson(event.getPayload());
            messagePublisher.publish(event.getTopic(), event.getEventName(), payload);
            
            return outboxRepository.markAsPublished(event.getId())
                .doOnSuccess(v -> loggingHandler.logInfo(
                    "Event published successfully: " + event.getId()))
                .doOnError(e -> loggingHandler.logError(
                    "Error marking event as published: " + event.getId(), e));
                    
        } catch (Exception e) {
            loggingHandler.logError("Error publishing event: " + event.getId(), e);
            return Mono.empty();
        }
    }
}
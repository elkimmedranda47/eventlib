package com.main_group_ekn47.eventlib.consumer;

import com.main_group_ekn47.eventlib.config.MessagingProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class IdempotencyCleanupScheduler {

    private final IdempotencyStore idempotencyStore;
    //private final MessagingProperties properties;
    private final ConsumerConfig.ConsumerProperties properties;


    public IdempotencyCleanupScheduler(IdempotencyStore idempotencyStore, 
                                       /*MessagingProperties properties*/
                                       ConsumerConfig.ConsumerProperties properties
    ) {
        this.idempotencyStore = idempotencyStore;
        this.properties = properties;
    }

   /* @Scheduled(cron = "0 0 3 * * ?") // Ejecutar diario a las 3AM
    public void cleanOldEvents() {
        Duration retention = Duration.ofDays(
            properties.getConsumer().getEventRetentionDays()
        );
        
        idempotencyStore.cleanProcessedEvents(retention)
            .subscribe();
    }
    */

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldEvents() {
        Duration retention = Duration.ofDays(properties.getEventRetentionDays());
        idempotencyStore.cleanProcessedEvents(retention).subscribe();
    }
}

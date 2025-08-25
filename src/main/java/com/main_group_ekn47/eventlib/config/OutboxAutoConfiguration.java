package com.main_group_ekn47.eventlib.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.broker.rabbitmq.RabbitMQPublisher;
import com.main_group_ekn47.eventlib.core.MessagePublisher;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import com.main_group_ekn47.eventlib.producer.OutboxPublisher;
import com.main_group_ekn47.eventlib.producer.OutboxRepository;
import com.main_group_ekn47.eventlib.producer.aop.PublishEventAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "messaging.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxAutoConfiguration {
    
   /* @Bean
    public PublishEventAspect publishEventAspect(OutboxRepository outboxRepository) {
        return new PublishEventAspect(outboxRepository);
    }

    */
  /* @Bean
   public PublishEventAspect publishEventAspect(OutboxRepository outboxRepository, RabbitMQPublisher publisher , MessageSerializer serializer) { // ⬅️ Inyectar RabbitMQPublisher
       return new PublishEventAspect(outboxRepository, publisher,serializer); // ⬅️ Pasar ambos argumentos al constructor
   }*/


    /*@Bean
    public OutboxPublisher outboxPublisher(OutboxRepository outboxRepository,
                                          MessagePublisher messagePublisher,
                                          LoggingHandler loggingHandler,
                                          MessagingProperties properties) {
        return new OutboxPublisher(
            outboxRepository,
            messagePublisher,
            loggingHandler,
            properties.getOutbox().getPollingInterval()
        );
    }
    */

    /*
    @Bean
    public OutboxPublisher outboxPublisher(OutboxRepository outboxRepository,
                                           MessagePublisher messagePublisher,
                                           ObjectMapper objectMapper) {
        return new OutboxPublisher(
                outboxRepository,
                messagePublisher,
                objectMapper
        );
    }
    */

    @Bean
    public PublishEventAspect publishEventAspect(OutboxRepository outboxRepository, RabbitMQPublisher publisher , MessageSerializer serializer) {
        return new PublishEventAspect(outboxRepository, publisher,serializer);
    }

    @Bean
    public OutboxPublisher outboxPublisher(OutboxRepository outboxRepository,
                                           MessagePublisher messagePublisher,
                                           LoggingHandler loggingHandler,
                                           MessagingProperties properties,
                                           MessageSerializer serializer) { // ⬅️ INYECTAR MessageSerializer
        return new OutboxPublisher(
                outboxRepository,
                messagePublisher,
                loggingHandler,
                serializer // ⬅️ PASARLO AL CONSTRUCTOR
        );
    }
}
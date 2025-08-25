package com.main_group_ekn47.eventlib.broker.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.main_group_ekn47.eventlib.config.MessagingProperties;
import com.main_group_ekn47.eventlib.core.MessagePublisher;
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPublisher implements MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final LoggingHandler loggingHandler;
    private final String eventExchange;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate,
                             LoggingHandler loggingHandler,
                             MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.loggingHandler = loggingHandler;
        this.eventExchange = properties.getRabbitmq().getExchange();

        System.out.println("********************************************************"+properties.getRabbitmq().getExchange());
        System.out.println("Configuración RabbitMQ:");
        System.out.println("- Exchange: " + this.eventExchange);
        System.out.println("- Template: " + (rabbitTemplate != null ? "OK" : "NULL"));
    }

    @Override
    public void publish(String topic, String eventName, Object payload) {
        try {
            String routingKey = topic + "." + eventName;
            rabbitTemplate.convertAndSend(eventExchange, routingKey, payload);
            System.out.println("******************************************************** routingKey:  -->"+routingKey);
            System.out.println("******************************************************** eventExchange:  -->"+eventExchange);
            System.out.println("******************************************************** payload:  -->"+payload);



            loggingHandler.logInfo("Event published to RabbitMQ. Exchange: " + eventExchange + ", RoutingKey: " + routingKey);
        } catch (Exception e) {
            loggingHandler.logError("Error publishing event to RabbitMQ", e);
            throw new RuntimeException("Error publishing event", e);
        }
    }
}

/*
package com.main_group_ekn47.eventlib.broker.rabbitmq;

import com.main_group_ekn47.eventlib.config.MessagingProperties;
import com.main_group_ekn47.eventlib.core.MessagePublisher;
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPublisher implements MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final LoggingHandler loggingHandler;
    private final String eventExchange;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate,
                             LoggingHandler loggingHandler,
                             MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.loggingHandler = loggingHandler;
        this.eventExchange = properties.getRabbitmq().getExchange();
    }

    @Override
    public void publish(String topic, String eventName, Object payload) {
        try {
            String routingKey = topic + "." + eventName;
            // Envía el objeto Java directamente. RabbitTemplate añade el encabezado
            rabbitTemplate.convertAndSend(eventExchange, routingKey, payload);
            loggingHandler.logInfo("Event published to RabbitMQ. Exchange: " + eventExchange + ", RoutingKey: " + routingKey);
        } catch (Exception e) {
            loggingHandler.logError("Error publishing event to RabbitMQ", e);
            throw new RuntimeException("Error publishing event", e);
        }
    }
}

 */
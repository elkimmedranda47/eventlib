package com.main_group_ekn47.eventlib.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component // Añade esta anotación
@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {
    private String broker;
    private Rabbitmq rabbitmq;
    private Kafka kafka;
    private Outbox outbox;
    private Retry retry;



    @Data
    public static class Rabbitmq {
        private String host;
        private int port;
        private String username;
        private String password;
        private String exchange;
        private String queue;
        private String dlq;
  	    private String routingKey; // Nueva propiedad
    }
    
    @Data
    public static class Kafka {
        private String bootstrapServers;
        private String topic;
        private String groupId;
    }
    
    @Data
    public static class Outbox {
        private boolean enabled = true;
        private long pollingInterval = 5000;
    }
    
    @Data
    public static class Retry {
        private int maxRetries = 3;
        private long initialDelayMillis = 1000;
        private long maxDelayMillis = 10000;
        private double backoffMultiplier = 2.0;
    }
}

/*package com.main_group_ekn47.eventlib.consumer;

import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

    @Bean
    @ConfigurationProperties(prefix = "messaging.consumer")
    public ConsumerProperties consumerProperties() {
        return new ConsumerProperties();
    }
    
    @Data
    public static class ConsumerProperties {
        private int eventRetentionDays = 7;
        private String idempotencyStore = "memory"; // memory|redis
    }
}
 */
package com.main_group_ekn47.eventlib.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

    @ConfigurationProperties(prefix = "messaging.consumer")
    public static class ConsumerProperties {
        private int eventRetentionDays = 7;
        private String idempotencyStore = "memory";

        public int getEventRetentionDays() {
            return eventRetentionDays;
        }

        public void setEventRetentionDays(int eventRetentionDays) {
            this.eventRetentionDays = eventRetentionDays;
        }

        public String getIdempotencyStore() {
            return idempotencyStore;
        }

        public void setIdempotencyStore(String idempotencyStore) {
            this.idempotencyStore = idempotencyStore;
        }
    }
}
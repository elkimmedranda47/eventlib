package com.main_group_ekn47.eventlib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {
    private String broker;
    private Rabbitmq rabbitmq = new Rabbitmq();
    private Kafka kafka = new Kafka();
    private Outbox outbox = new Outbox();
    private Retry retry = new Retry();

    // Getters y Setters
    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public Rabbitmq getRabbitmq() {
        return rabbitmq;
    }

    public void setRabbitmq(Rabbitmq rabbitmq) {
        this.rabbitmq = rabbitmq;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public void setOutbox(Outbox outbox) {
        this.outbox = outbox;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    // Clases internas
    public static class Rabbitmq {
        private String host;
        private int port;
        private String username;
        private String password;
        private String exchange;
        private String queue;
        private String dlq;
        private String routingKey;

        // Getters y Setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getDlq() {
            return dlq;
        }

        public void setDlq(String dlq) {
            this.dlq = dlq;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        // equals, hashCode y toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Rabbitmq rabbitmq = (Rabbitmq) o;
            return port == rabbitmq.port &&
                    Objects.equals(host, rabbitmq.host) &&
                    Objects.equals(username, rabbitmq.username) &&
                    Objects.equals(password, rabbitmq.password) &&
                    Objects.equals(exchange, rabbitmq.exchange) &&
                    Objects.equals(queue, rabbitmq.queue) &&
                    Objects.equals(dlq, rabbitmq.dlq) &&
                    Objects.equals(routingKey, rabbitmq.routingKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port, username, password, exchange, queue, dlq, routingKey);
        }

        @Override
        public String toString() {
            return "Rabbitmq{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", exchange='" + exchange + '\'' +
                    ", queue='" + queue + '\'' +
                    ", dlq='" + dlq + '\'' +
                    ", routingKey='" + routingKey + '\'' +
                    '}';
        }
    }

    public static class Kafka {
        private String bootstrapServers;
        private String topic;
        private String groupId;

        // Getters y Setters
        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        // equals, hashCode y toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Kafka kafka = (Kafka) o;
            return Objects.equals(bootstrapServers, kafka.bootstrapServers) &&
                    Objects.equals(topic, kafka.topic) &&
                    Objects.equals(groupId, kafka.groupId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bootstrapServers, topic, groupId);
        }

        @Override
        public String toString() {
            return "Kafka{" +
                    "bootstrapServers='" + bootstrapServers + '\'' +
                    ", topic='" + topic + '\'' +
                    ", groupId='" + groupId + '\'' +
                    '}';
        }
    }

    public static class Outbox {
        private boolean enabled = true;
        private long pollingInterval = 5000;

        // Getters y Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getPollingInterval() {
            return pollingInterval;
        }

        public void setPollingInterval(long pollingInterval) {
            this.pollingInterval = pollingInterval;
        }

        // equals, hashCode y toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Outbox outbox = (Outbox) o;
            return enabled == outbox.enabled &&
                    pollingInterval == outbox.pollingInterval;
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled, pollingInterval);
        }

        @Override
        public String toString() {
            return "Outbox{" +
                    "enabled=" + enabled +
                    ", pollingInterval=" + pollingInterval +
                    '}';
        }
    }

    public static class Retry {
        private int maxRetries = 3;
        private long initialDelayMillis = 1000;
        private long maxDelayMillis = 10000;
        private double backoffMultiplier = 2.0;

        // Getters y Setters
        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getInitialDelayMillis() {
            return initialDelayMillis;
        }

        public void setInitialDelayMillis(long initialDelayMillis) {
            this.initialDelayMillis = initialDelayMillis;
        }

        public long getMaxDelayMillis() {
            return maxDelayMillis;
        }

        public void setMaxDelayMillis(long maxDelayMillis) {
            this.maxDelayMillis = maxDelayMillis;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }

        // equals, hashCode y toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Retry retry = (Retry) o;
            return maxRetries == retry.maxRetries &&
                    initialDelayMillis == retry.initialDelayMillis &&
                    maxDelayMillis == retry.maxDelayMillis &&
                    Double.compare(retry.backoffMultiplier, backoffMultiplier) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxRetries, initialDelayMillis, maxDelayMillis, backoffMultiplier);
        }

        @Override
        public String toString() {
            return "Retry{" +
                    "maxRetries=" + maxRetries +
                    ", initialDelayMillis=" + initialDelayMillis +
                    ", maxDelayMillis=" + maxDelayMillis +
                    ", backoffMultiplier=" + backoffMultiplier +
                    '}';
        }
    }

    // equals, hashCode y toString para la clase principal
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessagingProperties that = (MessagingProperties) o;
        return Objects.equals(broker, that.broker) &&
                Objects.equals(rabbitmq, that.rabbitmq) &&
                Objects.equals(kafka, that.kafka) &&
                Objects.equals(outbox, that.outbox) &&
                Objects.equals(retry, that.retry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(broker, rabbitmq, kafka, outbox, retry);
    }

    @Override
    public String toString() {
        return "MessagingProperties{" +
                "broker='" + broker + '\'' +
                ", rabbitmq=" + rabbitmq +
                ", kafka=" + kafka +
                ", outbox=" + outbox +
                ", retry=" + retry +
                '}';
    }
}
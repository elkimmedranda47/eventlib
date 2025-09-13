package com.main_group_ekn47.eventlib.config;

import com.main_group_ekn47.eventlib.broker.rabbitmq.RabbitMQConfig;
//import com.main_group_ekn47.eventlib.broker.rabbitmq.RabbitMQReliabilityConfig;
import com.main_group_ekn47.eventlib.consumer.*;
import com.main_group_ekn47.eventlib.monitoring.MetricsCollector;
import com.main_group_ekn47.eventlib.monitoring.OutboxMetricsScheduler;
import com.main_group_ekn47.eventlib.producer.OutboxRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@EnableConfigurationProperties({
   // MessagingProperties.class,
    ConsumerConfig.ConsumerProperties.class
})
@Import({
    RabbitMQConfig.class,
    DLQConfig.class,
    OutboxAutoConfiguration.class,
    MetricsConfig.class,
    RetryConfig.class,
    //RabbitMQReliabilityConfig.class,
    ConsumerConfig.class // Importar explícitamente
})
public class EventLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore(
       // MessagingProperties properties,
       ConsumerConfig.ConsumerProperties consumerProperties, // Cambio clave aquí
        ReactiveRedisTemplate<String, String> redisTemplate) {

        /*
        if ("redis".equals(properties.getConsumer().getIdempotencyStore())) {
            return new RedisIdempotencyStore(redisTemplate);
        }
        */

        if ("redis".equals(consumerProperties.getIdempotencyStore())) {
            return new RedisIdempotencyStore(redisTemplate);
        }
        return new MemoryIdempotencyStore();
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
        ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, String> context =
            RedisSerializationContext.<String, String>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .value(StringRedisSerializer.UTF_8)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(StringRedisSerializer.UTF_8)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public OutboxMetricsScheduler outboxMetricsScheduler(
        OutboxRepository outboxRepository,
        MetricsCollector metricsCollector) {
        return new OutboxMetricsScheduler(outboxRepository, metricsCollector);
    }

    @Bean
    public IdempotencyCleanupScheduler idempotencyCleanupScheduler(
        IdempotencyStore idempotencyStore,
        //ConsumerConfig.ConsumerProperties properties
        ConsumerConfig.ConsumerProperties properties
    ) {
        return new IdempotencyCleanupScheduler(idempotencyStore, properties);
    }
}

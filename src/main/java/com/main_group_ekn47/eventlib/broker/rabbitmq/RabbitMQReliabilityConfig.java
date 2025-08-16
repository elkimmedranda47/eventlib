package com.main_group_ekn47.eventlib.broker.rabbitmq;

import com.main_group_ekn47.eventlib.config.DLQConfig;
import com.main_group_ekn47.eventlib.core.RetryPolicy;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQReliabilityConfig {


    private final RabbitTemplate rabbitTemplate;

    // Constructor que inyecta RabbitTemplate
    public RabbitMQReliabilityConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        SimpleRabbitListenerContainerFactoryConfigurer configurer,
       ConnectionFactory rabbitConnectionFactory, // Usa el nuevo nombre
      //  ConnectionFactory connectionFactory,
        RetryPolicy retryPolicy) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, rabbitConnectionFactory);
        factory.setAdviceChain(retryInterceptor(retryPolicy));
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RetryPolicy retryPolicy) {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configuración de reintentos exponencial
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryPolicy.getInitialDelayMillis());
        backOffPolicy.setMultiplier(retryPolicy.getBackoffMultiplier());
        backOffPolicy.setMaxInterval(retryPolicy.getMaxDelayMillis());
        
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(retryPolicy.getMaxRetries()));
        
        return RetryInterceptorBuilder.stateless()
            .retryOperations(retryTemplate)
            .recoverer(messageRecoverer())
            .build();
    }

    @Bean
    public MessageRecoverer messageRecoverer() {
        return new RepublishMessageRecoverer(rabbitTemplate, DLQConfig.DLX_NAME, "#");
    }
}
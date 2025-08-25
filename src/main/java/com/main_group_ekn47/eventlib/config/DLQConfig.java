package com.main_group_ekn47.eventlib.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "messaging.broker", havingValue = "rabbitmq")
public class DLQConfig {

    //Creacion del exchange de la cola de muerte Manual
    public static final String DLX_NAME = "eventlib.dlx";
    
    @Bean
    public Exchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DLX_NAME).durable(true).build();
    }
    /*

    //para agregar la cola de muerte des de el archivo de propiedades y crear el Exchage de la cola de muerte
    @Bean
    public Exchange deadLetterExchange(MessagingProperties properties) { return ExchangeBuilder.topicExchange(properties.getRabbitmq().getDlx()).durable(true).build();}

     */
    
    @Bean
    public Queue deadLetterQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.getRabbitmq().getDlq()).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, Exchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
            .to(deadLetterExchange)
            .with("#")
            .noargs();
    }
}
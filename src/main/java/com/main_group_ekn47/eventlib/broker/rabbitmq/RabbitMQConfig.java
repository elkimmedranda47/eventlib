package com.main_group_ekn47.eventlib.broker.rabbitmq;

import com.main_group_ekn47.eventlib.config.DLQConfig;
import com.main_group_ekn47.eventlib.config.MessagingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final MessagingProperties properties;

    public RabbitMQConfig(MessagingProperties properties) {
        this.properties = properties;
    }

  //@Bean
    @Bean(name = "rabbitConnectionFactory")
   // public ConnectionFactory connectionFactory() {
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(
            properties.getRabbitmq().getHost(),
            properties.getRabbitmq().getPort()
        );
        System.out.printf("***((((+))))***********((((*))))***        ¬||¬||  "+properties.getRabbitmq().getUsername());
        System.out.printf("   ( * )  ****()****  ( * )   *              "+properties.getRabbitmq().getPassword());
        System.out.printf("            *******                          "+properties.getRabbitmq().getPassword());

        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
       // RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Exchange eventExchange() {
        return ExchangeBuilder.topicExchange(properties.getRabbitmq().getExchange())
            .durable(true)
            .build();
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(properties.getRabbitmq().getQueue())
            .withArgument("x-dead-letter-exchange", DLQConfig.DLX_NAME)
            .build();
    }

/*

    @Bean
    public Binding eventBinding(Queue eventQueue, Exchange eventExchange) {
        return BindingBuilder.bind(eventQueue)
            .to(eventExchange)
            .with(properties.getRabbitmq().getQueue())
            .noargs();
    }
*/
@Bean
public Binding eventBinding(Queue eventQueue, Exchange eventExchange) {
    String routingKey = properties.getRabbitmq().getRoutingKey() != null ? 
                        properties.getRabbitmq().getRoutingKey() :
                        properties.getRabbitmq().getQueue();
    
    return BindingBuilder.bind(eventQueue)
        .to(eventExchange)
        .with(routingKey)
        .noargs();
}
    /**
     * Este bean es crucial para que Spring declare automáticamente
     * los exchanges, colas y bindings en el servidor de RabbitMQ.
     * Sin este bean, la aplicación solo los tiene definidos en su contexto.
     * @param connectionFactory La fábrica de conexión usada para interactuar con RabbitMQ.
     * @return Una instancia de RabbitAdmin que se encarga de la declaración.
     */
    /*
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    */
}
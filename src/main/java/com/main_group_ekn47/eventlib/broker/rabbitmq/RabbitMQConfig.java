package com.main_group_ekn47.eventlib.broker.rabbitmq;
/*


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
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Indica a Spring que esta clase contiene definiciones de beans.
public class RabbitMQConfig {

    private final MessagingProperties properties;

    // Spring inyecta automáticamente las propiedades de tu archivo de configuración
    // gracias a la anotación @ConfigurationProperties en MessagingProperties.
    public RabbitMQConfig(MessagingProperties properties) {
        this.properties = properties;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Bean(name = "rabbitConnectionFactory")
    public ConnectionFactory rabbitConnectionFactory() {
        // Usa las propiedades inyectadas para configurar la conexión.
        CachingConnectionFactory factory = new CachingConnectionFactory(
                properties.getRabbitmq().getHost(),
                properties.getRabbitmq().getPort()
        );
        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        return factory;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true); // Muy importante para que la declaración ocurra al arrancar la app.
        return admin;
    }


    @Bean
    public Exchange eventExchange() {
        return ExchangeBuilder.topicExchange(properties.getRabbitmq().getExchange())
                .durable(true)
                .build();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(properties.getRabbitmq().getQueue())
                // Conecta esta cola a un DLX. Si un mensaje falla, se enviará al DLX.
                .withArgument("x-dead-letter-exchange", DLQConfig.DLX_NAME)
                .build();
    }

    @Bean
    public Binding eventBinding() {
        // Lógica para determinar la clave de enrutamiento (routingKey).
        String routingKey = properties.getRabbitmq().getRoutingKey() != null ?
                properties.getRabbitmq().getRoutingKey() :
                properties.getRabbitmq().getQueue();

        return BindingBuilder.bind(eventQueue())
                .to(eventExchange())
                .with(routingKey)
                .noargs();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory,
                                         MessageConverter jsonMessageConverter)
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setMessageConverter(jsonMessageConverter);  // ← Usa el converter inyectado
        return rabbitTemplate;
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}*/

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
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQConfig {

    private final MessagingProperties properties;

    public RabbitMQConfig(MessagingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(properties.getRabbitmq().getQueue())
                .withArgument("x-dead-letter-exchange", DLQConfig.DLX_NAME)
                .build();
    }

    @Bean
    public Exchange eventExchange() {
        return ExchangeBuilder.topicExchange(properties.getRabbitmq().getExchange())
                .durable(true)
                .build();
    }

    @Bean
    public Binding eventBinding() {
        return BindingBuilder.bind(eventQueue())
                .to(eventExchange())
                .with(properties.getRabbitmq().getRoutingKey())
                .noargs(); // Esto completa la construcción y devuelve un objeto Binding. ✅
    }
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);}
}
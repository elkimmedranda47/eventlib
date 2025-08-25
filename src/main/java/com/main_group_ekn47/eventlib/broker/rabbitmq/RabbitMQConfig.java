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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
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
        System.out.printf("***((((+))))***********((((*))))***        ¬||¬||  "+properties.getRabbitmq().getUsername()+"\n");
        System.out.printf("   ( * )  ****()****  ( * )   *              "+properties.getRabbitmq().getPassword()+"\n");
        System.out.printf("            *******                          "+properties.getRabbitmq().getHost()+"\n");
        System.out.printf("            *******                          "+properties.getRabbitmq().getPort()+"\n");

        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        return factory;
    }

*/
    /**
     * Este bean es crucial para que Spring declare automáticamente
     * los exchanges, colas y bindings en el servidor de RabbitMQ.
     * Sin este bean, la aplicación solo los tiene definidos en su contexto.
     * @param connectionFactory La fábrica de conexión usada para interactuar con RabbitMQ.
     * @return Una instancia de RabbitAdmin que se encarga de la declaración.
     */

    /*
    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true); // Esto es crucial
        return admin;
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


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
       // RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }


    @Bean
    public Binding eventBinding() {
        String routingKey = properties.getRabbitmq().getRoutingKey() != null ?
                properties.getRabbitmq().getRoutingKey() :
                properties.getRabbitmq().getQueue();

        System.out.printf("            ***<0|0>***                          "+eventExchange()+"\n");
        System.out.printf("            ***0|0>****                          "+eventQueue()+"\n");
        System.out.printf("            ***0|0>****                          "+routingKey+"\n");
        //System.out.printf("            *******                          "+properties.getRabbitmq().getPort()+"\n");






        return BindingBuilder.bind(eventQueue())
                .to(eventExchange())
                .with(routingKey)
                .noargs();
    }

}

 */


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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
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
        System.out.printf("***((((+))))***********((((*))))***        ¬||¬||  "+properties.getRabbitmq().getUsername()+"\n");
        System.out.printf("   ( * )  ****()****  ( * )   *              "+properties.getRabbitmq().getPassword()+"\n");
        System.out.printf("            *******                          "+properties.getRabbitmq().getHost()+"\n");
        System.out.printf("            *******                          "+properties.getRabbitmq().getPort()+"\n");

        factory.setUsername(properties.getRabbitmq().getUsername());
        factory.setPassword(properties.getRabbitmq().getPassword());
        return factory;
    }

*/
    /**
     * Este bean es crucial para que Spring declare automáticamente
     * los exchanges, colas y bindings en el servidor de RabbitMQ.
     * Sin este bean, la aplicación solo los tiene definidos en su contexto.
     * @param connectionFactory La fábrica de conexión usada para interactuar con RabbitMQ.
     * @return Una instancia de RabbitAdmin que se encarga de la declaración.
     */

  /*
    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true); // Esto es crucial
        return admin;
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



    @Bean
    public Binding eventBinding() {
        String routingKey = properties.getRabbitmq().getRoutingKey() != null ?
                properties.getRabbitmq().getRoutingKey() :
                properties.getRabbitmq().getQueue();

        System.out.printf("            ***<0|0>***                          "+eventExchange()+"\n");
        System.out.printf("            ***0|0>****                          "+eventQueue()+"\n");
        System.out.printf("            ***0|0>****                          "+routingKey+"\n");
        //System.out.printf("            *******                          "+properties.getRabbitmq().getPort()+"\n");

        return BindingBuilder.bind(eventQueue())
                .to(eventExchange())
                .with(routingKey)
                .noargs();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
        // RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
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

@Configuration // Indica a Spring que esta clase contiene definiciones de beans.
public class RabbitMQConfig {

    private final MessagingProperties properties;

    // Spring inyecta automáticamente las propiedades de tu archivo de configuración
    // gracias a la anotación @ConfigurationProperties en MessagingProperties.
    public RabbitMQConfig(MessagingProperties properties) {
        this.properties = properties;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Bean para la fábrica de conexiones. Es el primer punto de contacto
     * con el servidor de RabbitMQ.
     * CachingConnectionFactory es una fábrica optimizada para reutilizar conexiones,
     * lo cual es eficiente en producción.
     * @return Una fábrica de conexiones a RabbitMQ.
     */
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

    /**
     * Bean para RabbitAdmin. Es fundamental para la autodeclaración.
     * Este objeto se encarga de crear el exchange, la cola y el binding
     * en el servidor de RabbitMQ si no existen.
     * @param connectionFactory La fábrica de conexión para que RabbitAdmin interactúe con el servidor.
     * @return Una instancia de RabbitAdmin.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true); // Muy importante para que la declaración ocurra al arrancar la app.
        return admin;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Bean para el Exchange de eventos.
     * Un exchange es el punto de entrada para los mensajes en RabbitMQ.
     * Este exchange es de tipo topic y es durable (persistente).
     * @return Un objeto Exchange.
     */
    @Bean
    public Exchange eventExchange() {
        return ExchangeBuilder.topicExchange(properties.getRabbitmq().getExchange())
                .durable(true)
                .build();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Bean para la cola de eventos.
     * Una cola es donde los mensajes esperan a ser consumidos.
     * Esta cola es durable y se configura con un argumento especial
     * para el "dead-letter-exchange", lo que permite que los mensajes fallidos
     * sean enviados a la DLQ.
     * @return Un objeto Queue.
     */
    //properties.getRabbitmq().getQueue() cola origen publicador si no se va a  DLQConfig.DLX_NAME
    /*
        Cola del Consumidor: Aquí es donde ocurre la magia. La cola del consumidor (eventlib_queue en este caso)
        tiene un argumento x-dead-letter-exchange que le dice: "Si recibes un
         mensaje que ha muerto (por nack, TTL o capacidad), envíalo a este exchange".
         En este caso, el destino es DLQConfig.DLX_NAME
     */
    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(properties.getRabbitmq().getQueue())
                // Conecta esta cola a un DLX. Si un mensaje falla, se enviará al DLX.
                .withArgument("x-dead-letter-exchange", DLQConfig.DLX_NAME)
                .build();
    }
    /*
    para crear  cola de muerte des de el archivo de propiedades
    @Bean
        public Queue eventQueue() {
        return QueueBuilder.durable(properties.getRabbitmq().getQueue())
                .withArgument("x-dead-letter-exchange", properties.getRabbitmq().getDlx()) // ⬅️ Usa el valor de las propiedades
                .withArgument("x-dead-letter-routing-key", "tu-routing-key-de-ejemplo") // ⬅️ Es una buena práctica usar una routing key específica para el dead-letter
                .build();
    }
    */


    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Bean para el Binding entre la cola y el exchange.
     * Un binding es lo que conecta un exchange con una cola,
     * usando una clave de enrutamiento para determinar qué mensajes
     * deben ser entregados a qué cola.
     * @return Un objeto Binding.
     */
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

    /**
     * Bean para el RabbitTemplate.
     * Es la clase principal para enviar mensajes a RabbitMQ.
     * También configura un convertidor de mensajes para serializar
     * y deserializar objetos Java a JSON automáticamente.
     * @param rabbitConnectionFactory La fábrica de conexiones.
     * @return Una instancia de RabbitTemplate.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory,
                                         MessageConverter jsonMessageConverter)
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
        //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setMessageConverter(jsonMessageConverter);  // ← Usa el converter inyectado
        return rabbitTemplate;
    }


}
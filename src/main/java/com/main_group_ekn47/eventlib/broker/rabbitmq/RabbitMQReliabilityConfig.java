/*package com.main_group_ekn47.eventlib.broker.rabbitmq;

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
*/
package com.main_group_ekn47.eventlib.broker.rabbitmq;

import com.main_group_ekn47.eventlib.config.DLQConfig;
import com.main_group_ekn47.eventlib.core.RetryPolicy;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
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

    // El constructor inyecta el RabbitTemplate, que es necesario para
    // republicar los mensajes fallidos a la cola de muerte.
    public RabbitMQReliabilityConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Define la fábrica de contenedores para los RabbitListeners.
     * Esto permite aplicar configuraciones globales a todos los consumidores
     * que usen la anotación @RabbitListener en la aplicación.
     * Aquí se le adjunta el interceptor de reintentos.
     *
     * @param configurer Ayuda a aplicar las configuraciones por defecto de Spring Boot.
     * @param rabbitConnectionFactory La fábrica de conexiones a RabbitMQ.
     * @param retryPolicy La política de reintentos definida en las propiedades de la aplicación.
     * @return Una fábrica de contenedores de escucha de RabbitMQ.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory rabbitConnectionFactory,
            RetryPolicy retryPolicy,
            MessageConverter jsonMessageConverter) { // ← Agrega este parámetro

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, rabbitConnectionFactory);
        factory.setMessageConverter(jsonMessageConverter);  // ← Esta línea es CLAV
        // Agrega el interceptor de reintentos a la cadena de ejecución.
        factory.setAdviceChain(retryInterceptor(retryPolicy));

        return factory;
    }

    /**
     * Define el interceptor de operaciones de reintento.
     * Este es el "cerebro" que controla cómo y cuándo se reintenta un mensaje.
     *
     * @param retryPolicy La política de reintentos cargada desde las propiedades.
     * @return Un interceptor que aplica la lógica de reintentos.
     */
    @Bean
    public RetryOperationsInterceptor retryInterceptor(RetryPolicy retryPolicy) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configura la política de "backoff" exponencial.
        // Esto significa que el tiempo de espera entre reintentos aumenta exponencialmente,
        // evitando sobrecargar el servicio si hay un fallo prolongado.
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryPolicy.getInitialDelayMillis());
        backOffPolicy.setMultiplier(retryPolicy.getBackoffMultiplier());
        backOffPolicy.setMaxInterval(retryPolicy.getMaxDelayMillis());

        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configura la política de reintentos.
        // Aquí se define el número máximo de reintentos antes de considerar
        // que el mensaje ha fallado definitivamente.
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(retryPolicy.getMaxRetries()));

        // Construye el interceptor, indicando qué hacer después de que
        // se agoten los reintentos (`messageRecoverer`).
        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .recoverer(messageRecoverer())
                .build();
    }

    /**
     * Define el mecanismo de recuperación de mensajes.
     * Esto especifica qué sucede con un mensaje después de que ha fallado todos los reintentos.
     *
     * @return Un recuperador de mensajes.
     */
    //publicador de las de los mensajes fallidos al  Dead Letter Exchange y despues a la cola de muerte
    @Bean
    public MessageRecoverer messageRecoverer() {
        // RepublishMessageRecoverer toma el mensaje fallido
        // en el Dead Letter Exchange (DLX) para que sea enrutado a la DLQ.
        // El `DLQConfig.DLX_NAME` es la constante que define el nombre del DLX,
        // y el "#" indica que se usa la misma clave de enrutamiento del mensaje original.
        return new RepublishMessageRecoverer(rabbitTemplate, DLQConfig.DLX_NAME, "#");
    }



}

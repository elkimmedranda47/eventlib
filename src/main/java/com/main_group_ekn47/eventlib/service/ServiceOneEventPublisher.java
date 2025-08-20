package com.main_group_ekn47.eventlib.service;

import com.main_group_ekn47.eventlib.core.PublishEvent;
//import com.main_group_ekn47.eventlib.config.EventLibApplicationRunner.TestEvent;
import com.main_group_ekn47.eventlib.service.eventObjectDto.TestEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ServiceOneEventPublisher {
    /*
    4. Uso de la Anotación @PublishEvent en un Servicio
    La parte más importante es cómo tu lógica de negocio utiliza la librería. En lugar de publicar un evento directamente en el broker,
    usarás la anotación @PublishEvent en un método de servicio.

    El aspecto (PublishEventAspect) de la librería interceptará este método. Cuando el método se ejecute, el evento se
    guardará en la tabla outbox_events dentro de la misma transacción de la base de datos de la lógica de negocio.
    Esto garantiza la consistencia.  

    Aquí tienes un ejemplo:
            @Service
            public class OrderService {

            @Autowired
            private OrderRepository orderRepository; // Tu repositorio de negocio

            // Este método es interceptado por la librería
            @Transactional
            @PublishEvent(topic = "order.events", eventName = "order_created")
            public Mono<OrderCreatedEvent> createOrder(Order order) {
            return orderRepository.save(order)
            .map(savedOrder -> new OrderCreatedEvent(savedOrder.getId()));
            }
            }
     */

   // @PublishEvent(topic = "test", eventName = "TestEvent")
    public Mono<TestEvent> publishTestEvent() {
        System.out.println("!!!!1**<0>**** ejecutando TestEvent() desde el servicio");
        return Mono.just(new TestEvent("Mi primer evento"));
    }

    /*
        Exchange: eventlib_exchange (tipo topic).
        Queue: eventlib_queue. Esta cola tiene una configuración para un Dead Letter Exchange (DLX),
        que es un patrón de resiliencia importante en RabbitMQ.
        Queue: eventlib_dlq (Dead Letter Queue).
        Binding: La cola eventlib_queue se enlaza al exchange eventlib_exchange con el routing key test.TestEvent.
        Binding: La cola eventlib_dlq se enlaza al exchange eventlib.dlx para manejar mensajes que no pueden ser procesados.
     */
}
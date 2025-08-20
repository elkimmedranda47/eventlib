package com.main_group_ekn47.eventlib.service;


import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.service.eventObjectDto.TestEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Service
public class ServiceTwoRabbitListener {
 /*
    5. Configuración del Consumidor (Listener)
    Para consumir los eventos, necesitas crear una clase de escucha en el microservicio destino.
    Tu librería ya ha configurado la conexión y la cola de RabbitMQ.  

    Simplemente usa la anotación @RabbitListener en el método del consumidor, especificando el nombre de la cola
    @Component
    public class InventoryConsumer {

        @RabbitListener(queues = "eventlib.queue")
        public void handleOrderCreated(OrderCreatedEvent event) {
            // Lógica de negocio para procesar el evento, por ejemplo, actualizar el inventario.
            System.out.println("Evento de orden creada recibido: " + event.getOrderId());
            }
        }https://chat.deepseek.com/a/chat/s/d5833a1f-e3c0-45d1-8044-b3c7be61129f

        Consumo del Evento:

        java
        @RabbitListener(queues = "${messaging.rabbitmq.queue}")
        public void receiveEvent(TestEvent event) {
        System.out.println("✉️ Received event: " + event.getMessage());
        }
        Escucha en la cola eventlib_queue

        Deserializa automáticamente el JSON a TestEvent


  */


    @Autowired
    private  IdempotencyStore idempotencyStore;

//    @RabbitListener(queues = "eventlib_queue")
    public void handleOrderCreated() {
        // Lógica de negocio para procesar el evento, por ejemplo, actualizar el inventario.
        //System.out.println("Evento de orden creada recibido: " + event.getOrderId());
        System.out.println("!!!!4**<0>**** ejecutando TestEvent() desde el servicio Evento recibido...");

    }



}

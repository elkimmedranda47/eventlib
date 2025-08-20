/*package com.main_group_ekn47.eventlib.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceUserRegisteredEventRabbitListener {

    //private final EmailSenderService emailSenderService;

    private  IdempotencyStore idempotencyStore;
    private  MessageSerializer serializer;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Puedes inyectarlo si ya lo tienes


    // Utiliza la inyección por constructor para todas las dependencias
    public ServiceUserRegisteredEventRabbitListener(IdempotencyStore idempotencyStore, MessageSerializer serializer) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = serializer;
    }


    //@RabbitListener(queues = "eventlib_queue")
    @RabbitListener(queues = "eventlib_queue")

    public void handleUserRegisteredEvent(JsonNode eventJson) {
        // La librería maneja la idempotencia por nosotros con el IdempotencyAspect
        //JsonNode payload = serializer.deserializeToJson(event.getPayload());
       // System.out.println("Payload recibido: " + event.getPayload()); // <- Agrega esto
        System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: ");

        try {

            UserRegisteredEvent event = objectMapper.treeToValue(eventJson, UserRegisteredEvent.class);

            System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: " + event.getUserEmail());


            // Lógica de negocio para enviar el correo
           // emailSenderService.sendWelcomeEmail(event.getUserEmail());
           // System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: "+event.getActivationToken());

            // Si todo fue bien, marcas el mensaje como procesado
           // idempotencyStore.storeMessageId(event.getEventId());

        } catch (Exception e) {
            // La librería RetryHandler se encargará de los reintentos
            // O si es un error fatal, irá a la DLQ
        }


    }

}

 */
package com.main_group_ekn47.eventlib.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.consumer.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceUserRegisteredEventRabbitListener {

    private final IdempotencyStore idempotencyStore;
    private final MessageSerializer serializer;
    // Inyecta el ObjectMapper para un mejor manejo
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceUserRegisteredEventRabbitListener(IdempotencyStore idempotencyStore, MessageSerializer serializer, ObjectMapper objectMapper) {
        this.idempotencyStore = idempotencyStore;
        this.serializer = serializer;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "eventlib_queue")
    // Cambia el tipo de argumento a byte[]
    public void handleUserRegisteredEvent(byte[] messageBody) {
        System.out.println("!!!!5**<0>**** Recibido mensaje como array de bytes.");
        try {
            //  Usa el ObjectMapper para leer los bytes y convertirlos a JsonNode
            JsonNode eventJson = objectMapper.readTree(messageBody);

            UserRegisteredEvent event = objectMapper.treeToValue(eventJson, UserRegisteredEvent.class);

            System.out.println("!!!!5**<0>**** Objeto Recibido desde el servicio Evento recibido...Email: " + event.getUserEmail());

        } catch (Exception e) {
            System.err.println("Error procesando el mensaje: " + e.getMessage());
            // La librería RetryHandler o la DLQ manejarán esto
        }
    }
}
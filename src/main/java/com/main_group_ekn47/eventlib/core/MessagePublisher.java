package com.main_group_ekn47.eventlib.core;

    /*+                             **
    *INTERFACE PARA PUBLICAR EVENTO*
    * *                            **/

//interface que se implenta en package com.main_group_ekn47.eventlib.broker.rabbitmq;RabbitMQPublisher

public interface MessagePublisher {
   // void publish(String topic, String eventName, JsonNode payload);
   void publish(String topic, String eventName, Object payload);

}//esta interface se usa en package com.main_group_ekn47.eventlib.producer; OutboxPublisher->publishEvent() para publicar el evento
/*package com.main_group_ekn47.eventlib.producer;

import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.producer.outbox.OutboxProcessor;
import com.main_group_ekn47.eventlib.broker.MessagePublisher;

import java.util.Optional;

public class DefaultEventPublisher implements EventPublisher {

    private final MessagePublisher messagePublisher;
    private final Optional<OutboxProcessor> outboxProcessor;

    public DefaultEventPublisher(
            MessagePublisher messagePublisher,
            Optional<OutboxProcessor> outboxProcessor
    ) {
        this.messagePublisher = messagePublisher;
        this.outboxProcessor = outboxProcessor;
    }

    @Override
    public void publish(IntegrationEvent event) {
        if (outboxProcessor.isPresent()) {
            outboxProcessor.get().store(event);
        } else {
            messagePublisher.publish(event);
        }
    }
}
*/

package com.main_group_ekn47.eventlib.producer;
/*
import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.core.MessageSerializer; // Importante
import com.main_group_ekn47.eventlib.producer.outbox.OutboxProcessor;
import com.main_group_ekn47.eventlib.broker.MessagePublisher;

import java.util.Optional;

public class DefaultEventPublisher implements EventPublisher {

    private final MessagePublisher messagePublisher;
    private final MessageSerializer serializer; // Necesitamos el serializador
    private final Optional<OutboxProcessor> outboxProcessor;

    public DefaultEventPublisher(
            MessagePublisher messagePublisher,
            MessageSerializer serializer,
            Optional<OutboxProcessor> outboxProcessor
    ) {
        this.messagePublisher = messagePublisher;
        this.serializer = serializer;
        this.outboxProcessor = outboxProcessor;
    }

    @Override
    public void publish(IntegrationEvent event) {
        if (outboxProcessor.isPresent()) {
            outboxProcessor.get().store(event);
        } else {
            // CORRECCIÓN: Desglosamos el evento para que coincida con la firma del método
            // required: String, String, byte[]
           /* messagePublisher.publish(
                    event.getEventName(),           // Exchange/Topic
                    "",                             // Routing Key (vacía por defecto)
                    serializer.serialize(event)     // Payload convertido a byte[]
            );*/
            // Cambiamos 'publish' por 'publishRaw'
          /*  messagePublisher.publishRaw(
                    event.getEventName(),           // String
                    serializer.serialize(event)     // String (Ya no hay error de tipos)
            );
        }
    }
}*/
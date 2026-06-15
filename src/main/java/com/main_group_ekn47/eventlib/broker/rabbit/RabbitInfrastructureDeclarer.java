/*
 * Licensed to Elkim Andres Medranda Caicedo under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Elkim Andres Medranda Caicedo licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.main_group_ekn47.eventlib.broker.rabbit;

import com.main_group_ekn47.eventlib.config.InfraProperties;
import com.main_group_ekn47.eventlib.config.RabbitProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Propósito: Declarar la infraestructura de RabbitMQ (Exchanges, Queues, Bindings).
 * Implementa el puente hacia la DLQ (Dead Letter Queue) mediante argumentos de cola.
 */
public class RabbitInfrastructureDeclarer {

    private final RabbitTemplate rabbitTemplate;
    private final InfraProperties infraProperties;

    public RabbitInfrastructureDeclarer(
            RabbitTemplate rabbitTemplate,
            InfraProperties infraProperties
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.infraProperties = infraProperties;
    }

    /**
     * Genera la lista de componentes que Spring AMQP registrará automáticamente en RabbitMQ.
     */
    public List<Declarable> declarables() {
        List<Declarable> declarables = new ArrayList<>();
        RabbitProperties rabbit = infraProperties.getRabbitmq();

        // Si no hay configuración de Rabbit, retornamos lista vacía para evitar NPE
        if (rabbit == null || rabbit.getQueues() == null) return declarables;

        // 1. Declarar el Topic Exchange principal
        TopicExchange exchange = new TopicExchange(rabbit.getExchange());
        declarables.add(exchange);

        // 2. Iterar sobre el mapa de colas configuradas
        for (Map.Entry<String, String> entry : rabbit.getQueues().entrySet()) {
            String routingKey = entry.getKey();
            String queueName = entry.getValue();

            // Determinar el sufijo y la llave de ruteo para la DLQ
            String suffix = (rabbit.getDlq() != null) ? rabbit.getDlq().getSuffix() : ".dlq";
            String dlqRoutingKey = routingKey + suffix;

            // --- COLA PRINCIPAL CON PUENTE A DLQ ---
            // El 'withArgument' conecta el nack(false) físicamente con el Exchange
            Queue mainQueue = QueueBuilder.durable(queueName)
                    .withArgument("x-dead-letter-exchange", rabbit.getExchange())
                    .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                    .build();

            Binding mainBinding = BindingBuilder
                    .bind(mainQueue)
                    .to(exchange)
                    .with(routingKey);

            declarables.add(mainQueue);
            declarables.add(mainBinding);

            // --- COLA DLQ (El destino final de los errores) ---
            Queue dlq = QueueBuilder.durable(queueName + suffix).build();

            Binding dlqBinding = BindingBuilder
                    .bind(dlq)
                    .to(exchange)
                    .with(dlqRoutingKey);

            declarables.add(dlq);
            declarables.add(dlqBinding);
        }

        return declarables;
    }
}
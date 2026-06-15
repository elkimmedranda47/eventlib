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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main_group_ekn47.eventlib.broker.MessagePublisher;
import com.main_group_ekn47.eventlib.config.InfraProperties;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RabbitMQPublisher implements MessagePublisher {

    private final Sender sender; // <--- CAMBIO: De RabbitTemplate a Sender
    private final InfraProperties infraProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor actualizado para recibir el Sender reactivo
    public RabbitMQPublisher(Sender sender, InfraProperties infraProperties) {
        this.sender = sender;
        this.infraProperties = infraProperties;
    }

   @Override
   public Mono<Void> publish(String exchange, String routingKey, byte[] payload) {
       OutboundMessage message = new OutboundMessage(exchange, routingKey, payload);

       return sender.send(Mono.just(message))
               // REINTENTO LOCAL: Si Rabbit se está levantando, reintenta 3 veces cada 2 segundos
               .retryWhen(reactor.util.retry.Retry.fixedDelay(3, Duration.ofSeconds(2))
                       .filter(ex -> isNetworkError(ex)))
               .timeout(Duration.ofSeconds(10))
               .then();
   }

    private boolean isNetworkError(Throwable ex) {
        // Reintenta solo si es un error de conexión, no si el mensaje está mal formado
        return ex instanceof java.net.ConnectException ||
                ex.getMessage().contains("connection") ||
                ex instanceof com.rabbitmq.client.AlreadyClosedException;
    }

    @Override
    public Mono<Void> publishRaw(String topic, String payload) {
        if (payload == null) return Mono.empty();

        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        String routingKey = "";

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (jsonNode.has("eventName")) {
                routingKey = jsonNode.get("eventName").asText();
            }
        } catch (Exception e) {
            // Loguear error pero permitir que el flujo continúe con routingKey vacía si es aceptable
        }

        // Llamamos al método reactivo de arriba
        return this.publish(topic, routingKey, data);
    }
}
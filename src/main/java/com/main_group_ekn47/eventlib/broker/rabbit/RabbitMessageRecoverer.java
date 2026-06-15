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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public class RabbitMessageRecoverer {
    private final RabbitTemplate rabbitTemplate;
    private final InfraProperties infraProperties;

    public RabbitMessageRecoverer(RabbitTemplate rabbitTemplate, InfraProperties infraProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.infraProperties = infraProperties;
    }

    public void recover(String queueName) {
        String dlqName = queueName + infraProperties.getRabbitmq().getDlq().getSuffix();
        String exchange = infraProperties.getRabbitmq().getExchange();

        log.info("♻️ Moviendo mensajes de {} hacia {}", dlqName, exchange);

        Message message;
        int count = 0;
        while ((message = rabbitTemplate.receive(dlqName)) != null) {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            rabbitTemplate.send(exchange, routingKey, message);
            count++;
        }
        log.info("✅ Recuperación completada: {} mensajes restaurados.", count);
    }
}
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
package com.main_group_ekn47.eventlib.autoconfigure;

import com.main_group_ekn47.eventlib.broker.rabbit.RabbitMessageReceiver;
import com.main_group_ekn47.eventlib.broker.rabbit.RabbitMessageRecoverer;
import com.main_group_ekn47.eventlib.config.InfraProperties;
import com.main_group_ekn47.eventlib.consumer.EventDispatcher;
import com.main_group_ekn47.eventlib.consumer.IntegrationEventHandler;
import com.main_group_ekn47.eventlib.consumer.idempotency.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.rabbitmq.Receiver;

import java.util.List;

/**
 * Auto-configuración del lado consumidor de eventlib.
 *
 * Se activa SOLO si hay al menos un IntegrationEventHandler registrado
 * en el contexto de Spring (@ConditionalOnBean). Esto evita que microservicios
 * que solo publican eventos carguen innecesariamente los beans de consumo.
 *
 * Orden de carga: después de RabbitAutoConfiguration para garantizar que
 * el Receiver ya esté disponible cuando se construya RabbitMessageReceiver.
 */
@AutoConfiguration(after = {RabbitAutoConfiguration.class})
@ConditionalOnBean(IntegrationEventHandler.class)
public class ConsumerAutoConfiguration {

    /**
     * EventDispatcher: enruta cada evento recibido al handler correcto
     * según el tipo de evento. También aplica idempotencia si hay un
     * IdempotencyStore disponible (Redis), evitando procesar duplicados.
     *
     * ObjectProvider<IdempotencyStore> es opcional — si no hay Redis
     * configurado, el dispatcher funciona sin idempotencia.
     */
    @Bean
    public EventDispatcher eventDispatcher(
            List<IntegrationEventHandler<?>> handlers,
            ObjectProvider<IdempotencyStore> storeProvider) {
        System.out.println("******************************* ConsumerAutoConfiguration activa ******************");
        return new EventDispatcher(handlers, storeProvider.getIfAvailable());
    }

    /**
     * RabbitMessageReceiver: wrapper reactivo sobre el Receiver de Reactor RabbitMQ.
     * Gestiona Manual Ack, reintentos por evento, reconexión automática del Flux
     * completo y enrutamiento a DLQ en caso de fallo definitivo.
     *
     * Ver RabbitMessageReceiver.consume() para detalle del flujo completo.
     */
    @Bean
    public RabbitMessageReceiver rabbitMessageReceiver(
            Receiver receiver,
            MessageSerializer serializer,
            EventDispatcher dispatcher) {
        return new RabbitMessageReceiver(receiver, serializer, dispatcher);
    }

    /**
     * RabbitMessageRecoverer: encargado de mover mensajes fallidos a la DLQ
     * o aplicar lógica de recuperación personalizada.
     * Solo se crea si no hay otro recoverer definido por el microservicio.
     */
    @Bean
    @ConditionalOnMissingBean
    public RabbitMessageRecoverer rabbitMessageRecoverer(
            RabbitTemplate rabbitTemplate,
            InfraProperties infraProperties) {
        return new RabbitMessageRecoverer(rabbitTemplate, infraProperties);
    }
}
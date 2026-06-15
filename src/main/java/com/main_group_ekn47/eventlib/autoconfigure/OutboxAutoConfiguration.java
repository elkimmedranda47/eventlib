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

import com.main_group_ekn47.eventlib.broker.MessagePublisher;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.producer.EventPublisher;
import com.main_group_ekn47.eventlib.producer.OutboxEventPublisher;
import com.main_group_ekn47.eventlib.producer.outbox.OutboxProcessor;
import com.main_group_ekn47.eventlib.producer.outbox.OutboxRepository;
import com.main_group_ekn47.eventlib.producer.outbox.OutboxScheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
// Forzamos a que primero existan los Serializers y el Publisher de Rabbit
@AutoConfigureAfter({EventLibAutoConfiguration.class, RabbitAutoConfiguration.class})
@ConditionalOnProperty(prefix = "eventlib.infra.outbox", name = "enabled", havingValue = "true")
// Solo activamos esto si MessagePublisher está presente en el contexto
@ConditionalOnBean({MessagePublisher.class, MessageSerializer.class})
@EnableR2dbcRepositories(basePackageClasses = OutboxRepository.class)
@EnableScheduling
public class OutboxAutoConfiguration {

    @Bean
    public OutboxProcessor outboxProcessor(
            OutboxRepository repository,
            @Lazy MessagePublisher publisher, // @Lazy quita el error rojo
            @Lazy MessageSerializer serializer // @Lazy quita el error rojo
            ) {
        return new OutboxProcessor(repository, publisher, serializer);
    }

    @Bean
    public OutboxScheduler outboxScheduler(
            OutboxRepository repository,
            OutboxProcessor processor) {
        return new OutboxScheduler(repository, processor);
    }

    @Bean
    public EventPublisher eventPublisher(OutboxProcessor outboxProcessor) {
        return new OutboxEventPublisher(outboxProcessor);
    }
}
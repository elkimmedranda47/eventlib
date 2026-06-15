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
package com.main_group_ekn47.eventlib.producer.outbox;

import com.main_group_ekn47.eventlib.broker.MessagePublisher;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
public class OutboxProcessor {

    private final OutboxRepository repository;
    private final MessagePublisher publisher;
    private final MessageSerializer serializer;

    public OutboxProcessor(OutboxRepository repository, MessagePublisher publisher, MessageSerializer serializer) {
        this.repository = repository;
        this.publisher = publisher;
        this.serializer = serializer;
    }

    public Mono<Void> store(IntegrationEvent event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventName(event.getEventName())
                .topic("eventlib.exchange")
                .payload(serializer.serialize(event))
                .createdAt(Instant.now())
                .isUpdate(false)
                .className(event.getClass().getName())
                .build();

        return repository.save(outboxEvent).then();
    }

    /**
     * Procesa un evento outbox con reintentos y marca como publicado solo cuando tiene éxito
     */
    public Mono<Void> process(OutboxEvent event) {
        log.debug("📤 Procesando evento outbox {} (reintentos habilitados)", event.getId());

        return publisher.publishRaw(event.getTopic(), event.getPayload())
                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(3))
                        .doBeforeRetry(retry ->
                                log.warn("♻️ Reintentando envío del evento {}... Intento: {}/5",
                                        event.getId(), retry.totalRetries() + 1))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("💀 Reintentos agotados para evento {} después de 5 intentos",
                                    event.getId());
                            return retrySignal.failure();
                        }))
                .then(Mono.defer(() -> {
                    // SOLO si la publicación fue exitosa, marcamos como publicado
                    log.info("✅ Evento {} publicado exitosamente, actualizando estado en DB", event.getId());
                    event.setPublishedAt(Instant.now());
                    event.setUpdate(true);
                    return repository.save(event)
                            .doOnSuccess(saved -> log.info("💾 Evento {} marcado como publicado", event.getId()))
                            .doOnError(e -> log.error("❌ Error al actualizar estado de {}: {}",
                                    event.getId(), e.getMessage()));
                }))
                .doOnError(e -> log.error("❌ Error procesando evento {}: {}",
                        event.getId(), e.getMessage()))
                .then();
    }
}
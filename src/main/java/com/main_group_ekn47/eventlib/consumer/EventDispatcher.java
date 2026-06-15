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
package com.main_group_ekn47.eventlib.consumer;

import com.main_group_ekn47.eventlib.consumer.idempotency.IdempotencyStore;
import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventDispatcher {
    // Usamos el nombre del evento como llave para el mapa de handlers
    private final Map<String, IntegrationEventHandler<IntegrationEvent>> handlers;
    private final IdempotencyStore idempotencyStore;

    @SuppressWarnings("unchecked")
    public EventDispatcher(List<IntegrationEventHandler<?>> handlerList, IdempotencyStore idempotencyStore) {
        // Mapeamos los handlers usando su método getEventName()
        // Nota: Asegúrate que tu interfaz IntegrationEventHandler tenga el método getEventName()
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        h -> h.getEventName(),
                        h -> (IntegrationEventHandler<IntegrationEvent>) h
                ));
        this.idempotencyStore = idempotencyStore;
    }

    public Mono<Void> dispatch(IntegrationEvent event) {
        String eventId = event.getEventId();
        String eventName = event.getEventName();

        return idempotencyStore.isProcessed(eventId)
                .flatMap(alreadyProcessed -> {
                    if (alreadyProcessed) {
                        log.warn("⏭️ Evento ignorado (duplicado detectado): {}", eventId);
                        return Mono.empty();
                    }

                    // 1. Buscamos el handler
                    IntegrationEventHandler<IntegrationEvent> handler = handlers.get(eventName);

                    if (handler == null) {
                        log.error("❌ No existe un Handler registrado para el evento: {}", eventName);
                        // Si no hay handler, no reintentamos (sería inútil),
                        // lanzamos error para que se vaya a la DLQ directamente.
                        return Mono.error(new RuntimeException("No handler found for " + eventName));
                    }

                    log.info("🎯 Despachando a Handler: [{}] ID: {}", eventName, eventId);

                    // 2. Ejecutamos Lógica -> 3. Marcamos como procesado
                    // IMPORTANTE: Si handle(event) falla, markProcessed NO se ejecuta.
                    return handler.handle(event)
                            .then(idempotencyStore.markProcessed(eventId))
                            .doOnSuccess(v -> log.info("✅ Procesado y marcado en Redis: {}", eventId));
                });
    }
}
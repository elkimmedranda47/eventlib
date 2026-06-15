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

import com.main_group_ekn47.eventlib.consumer.EventDispatcher;
import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.rabbitmq.Receiver;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class RabbitMessageReceiver {

    private final Receiver receiver;
    private final MessageSerializer serializer;
    private final EventDispatcher dispatcher;

    public RabbitMessageReceiver(Receiver receiver, MessageSerializer serializer, EventDispatcher dispatcher) {
        this.receiver = receiver;
        this.serializer = serializer;
        this.dispatcher = dispatcher;
    }

    public <T extends IntegrationEvent> Flux<T> consume(String queueName, Class<T> eventType) {
        log.info("🚀 [eventlib] Iniciando escucha (Manual Ack) en cola: {}", queueName);

        return Flux.defer(() -> {
                    log.debug("🔗 [eventlib] Abriendo canal de consumo en: {}", queueName);

                    // Sinks.many().unicast() actúa como un "interruptor de error manual".
                    // Cuando el Flux de consumo completa silenciosamente (broker caído),
                    // lo usamos para emitir un error explícito que active el retryWhen.
                    Sinks.Many<T> errorTrigger = Sinks.many().unicast().onBackpressureBuffer();

                    Flux<T> consumeFlux = receiver.consumeManualAck(queueName)
                            .flatMap(delivery -> {
                                String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
                                try {
                                    T event = serializer.deserialize(payload, eventType);

                                    return dispatcher.dispatch(event)
                                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                                    .doBeforeRetry(s -> log.warn(
                                                            "♻️ Reintentando handler para evento {} (intento {}/3)",
                                                            event.getEventId(), s.totalRetries() + 1)))
                                            .doOnSuccess(v -> {
                                                delivery.ack();
                                                log.info("✅ ACK enviado para evento: {}", event.getEventId());
                                            })
                                            .onErrorResume(e -> {
                                                log.error("❌ Fallo definitivo en evento {}. → DLQ. Causa: {}",
                                                        event.getEventId(), e.getMessage());
                                                delivery.nack(false);
                                                return Mono.empty();
                                            })
                                            .thenReturn(event);

                                } catch (Exception deserializationError) {
                                    log.error("❌ Error de deserialización en cola {}. → DLQ.", queueName);
                                    delivery.nack(false);
                                    return Mono.<T>empty();
                                }
                            })
                            // doOnComplete: el Receiver completó el Flux sin error (cancelación por broker).
                            // Convertimos esa compleción silenciosa en un error explícito
                            // para que retryWhen lo detecte y reinicie el consumidor.
                            .doOnComplete(() -> {
                                log.warn("⚠️ [{}] Flux de consumo completado inesperadamente " +
                                        "(posible caída del broker). Forzando reconexión...", queueName);
                                errorTrigger.tryEmitError(
                                        new IllegalStateException("Consumer flux completed unexpectedly on queue: " + queueName));
                            });

                    // Merge del flujo real con el trigger de error.
                    // En condiciones normales errorTrigger nunca emite nada.
                    // Solo se activa cuando doOnComplete detecta la caída del broker.
                    return Flux.merge(consumeFlux, errorTrigger.asFlux());
                })
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(s -> log.warn(
                                "🔄 [{}] Reconectando consumidor... (intento #{})",
                                queueName, s.totalRetries() + 1)))
                .doOnError(err -> log.error(
                        "💀 Error crítico irrecuperable en consumidor de {}: {}",
                        queueName, err.getMessage()));
    }
}
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class OutboxScheduler {

    private final OutboxRepository repository;
    private final OutboxProcessor processor;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    public OutboxScheduler(OutboxRepository repository, OutboxProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }
    @Scheduled(fixedDelay = 10000)
    public void processPendingEvents() {
        if (!isProcessing.compareAndSet(false, true)) {
            log.debug("⏭️ Omitiendo ejecución - procesamiento anterior en curso");
            return;
        }

        log.debug("🔍 Iniciando búsqueda de eventos pendientes");

        repository.findAllByPublishedAtIsNull()
                .doOnNext(event -> log.info("📬 Procesando evento: {} (creado: {})",
                        event.getId(), event.getCreatedAt()))
                // ✅ Procesamiento secuencial SIN .block() — usando concatMap
                .concatMap(event -> processor.process(event)
                        .doOnSuccess(v -> log.info("✅ Evento {} procesado exitosamente", event.getId()))
                        .doOnError(e -> log.error("❌ Error procesando evento {}: {}",
                                event.getId(), e.getMessage()))
                        .onErrorResume(e -> Mono.empty()) // continúa con el siguiente evento si uno falla
                )
                .doOnTerminate(() -> {
                    isProcessing.set(false);
                    log.debug("✅ Ciclo de procesamiento completado");
                })
                .doOnError(e -> {
                    consecutiveFailures.incrementAndGet();
                    log.error("💥 Error en ciclo (fallo #{}/3): {}", consecutiveFailures.get(), e.getMessage());
                    if (consecutiveFailures.get() >= 3) {
                        log.warn("⚠️ Múltiples fallos — RabbitMQ podría estar caído");
                    }
                    isProcessing.set(false);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
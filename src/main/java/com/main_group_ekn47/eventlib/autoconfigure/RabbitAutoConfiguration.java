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
import com.main_group_ekn47.eventlib.broker.rabbit.RabbitInfrastructureDeclarer;
import com.main_group_ekn47.eventlib.broker.rabbit.RabbitMQPublisher;
import com.main_group_ekn47.eventlib.config.InfraProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@AutoConfiguration(after = org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class)
@ConditionalOnClass({RabbitTemplate.class, RabbitFlux.class})
public class RabbitAutoConfiguration {

    private final AtomicReference<Mono<? extends Connection>> producerMonoRef
            = new AtomicReference<>();
    private final AtomicReference<Mono<? extends Connection>> consumerMonoRef
            = new AtomicReference<>();

    // =========================================================================
    // 1. FÁBRICA DE CONEXIÓN NATIVA
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory eventLibConnectionFactory(RabbitProperties springProps) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(springProps.getHost());
        factory.setPort(springProps.getPort());
        factory.setUsername(springProps.getUsername());
        factory.setPassword(springProps.getPassword());
        // Reactor RabbitMQ gestiona su propia resiliencia — desactivamos
        // la recuperación automática del driver para evitar conflictos.
        factory.setAutomaticRecoveryEnabled(false);
        factory.setExceptionHandler(new com.rabbitmq.client.impl.DefaultExceptionHandler() {
            @Override
            public void handleUnexpectedConnectionDriverException(
                    Connection conn, Throwable exception) {
                // Silenciado: resiliencia gestionada por Circuit Breaker + retryWhen
            }
        });
        return factory;
    }

    // =========================================================================
    // 2. CIRCUIT BREAKER — patrón Netflix
    //
    //    Estados:
    //    CLOSED    → todo normal, deja pasar las llamadas
    //    OPEN      → demasiados fallos, rechaza inmediatamente sin tocar el broker
    //    HALF-OPEN → deja pasar N llamadas de prueba para ver si el broker volvió
    //
    //    Ventaja sobre retry puro:
    //    Con solo retry, 100 requests esperan 5s cada una = 500s de espera acumulada.
    //    Con Circuit Breaker abierto, las 100 requests fallan en microsegundos
    //    y el sistema puede responder con un fallback en lugar de congelarse.
    // =========================================================================

    private CircuitBreaker buildCircuitBreaker(String name) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Abre el circuito si el 50% de los últimos 5 intentos fallan.
                // En producción Netflix usa ventanas más grandes (20-50 llamadas).
                .failureRateThreshold(50)
                .slidingWindowSize(5)
                // Tiempo en estado OPEN antes de pasar a HALF-OPEN.
                // Netflix recomienda alinearlo con el tiempo típico de restart
                // del servicio downstream — RabbitMQ tarda ~10s en reiniciar.
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // En HALF-OPEN: prueba con 2 conexiones antes de decidir
                // si cerrar el circuito o volver a abrirlo.
                .permittedNumberOfCallsInHalfOpenState(2)
                // Transición automática a HALF-OPEN sin esperar una llamada.
                // Sin esto el circuito queda en OPEN hasta que alguien intente
                // una llamada — con retry interno no hay problema, pero es
                // más predecible con automatic transition.
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreaker cb = CircuitBreaker.of(name, config);

        // Log de cada transición de estado — visible en producción
        cb.getEventPublisher()
                .onStateTransition(e -> log.warn(
                        "⚡ CircuitBreaker [{}]: {} → {}",
                        name,
                        e.getStateTransition().getFromState(),
                        e.getStateTransition().getToState()));

        return cb;
    }

    // =========================================================================
    // 3. BULKHEAD — aislamiento de recursos, patrón Netflix
    //
    //    Problema que resuelve:
    //    Si RabbitMQ está lento (no caído, solo lento), las llamadas se acumulan.
    //    Sin Bulkhead, pueden agotarse todos los hilos del pool esperando
    //    al broker, dejando sin recursos al resto de la aplicación.
    //
    //    Bulkhead semafórico (el que usamos):
    //    Limita cuántas llamadas concurrentes pueden estar en vuelo al broker.
    //    Si el límite se alcanza, nuevas llamadas fallan rápido (no esperan).
    //    Apropiado para operaciones no bloqueantes (Reactor/Netty).
    //
    //    Bulkhead de pool de hilos (alternativa):
    //    Para operaciones bloqueantes — no aplica aquí.
    // =========================================================================

    private Bulkhead buildBulkhead(String name) {
        BulkheadConfig config = BulkheadConfig.custom()
                // Máximo 10 intentos de conexión concurrentes al broker.
                // En un microservicio típico, nunca deberías tener más de 2-3
                // conexiones físicas a RabbitMQ (producer + consumer).
                // 10 es generoso para cubrir picos de reconexión.
                .maxConcurrentCalls(10)
                // Si ya hay 10 en vuelo, espera máximo 100ms antes de rechazar.
                // Sin timeout aquí, una cola de 1000 requests bloqueadas
                // esperando el Bulkhead consume memoria innecesariamente.
                .maxWaitDuration(Duration.ofMillis(100))
                .build();

        Bulkhead bulkhead = Bulkhead.of(name, config);

        bulkhead.getEventPublisher()
                .onCallRejected(e -> log.warn(
                        "🚧 Bulkhead [{}]: llamada rechazada — demasiadas conexiones concurrentes",
                        name));

        return bulkhead;
    }

    // =========================================================================
    // 4. CONSTRUCCIÓN DE CONEXIÓN RESILIENTE
    //
    //    Stack de resiliencia (orden de aplicación, de externo a interno):
    //
    //    Bulkhead → CircuitBreaker → fromCallable → retryWhen
    //
    //    ¿Por qué este orden?
    //    - Bulkhead es la primera puerta: si hay demasiadas conexiones en vuelo,
    //      rechaza inmediatamente sin gastar un slot del Circuit Breaker.
    //    - CircuitBreaker es la segunda puerta: si el broker está caído,
    //      rechaza sin hacer el fromCallable (sin tocar el broker).
    //    - fromCallable es la llamada real al broker.
    //    - retryWhen reintenta el fromCallable + CircuitBreaker + Bulkhead completo.
    //
    //    Una sola conexión activa por tipo (PRODUCER / CONSUMER):
    //    - AtomicReference garantiza que solo existe UN Mono cacheado a la vez.
    //    - Cuando ShutdownListener reemplaza el Mono, el anterior queda huérfano
    //      y el GC lo limpia. No hay conexiones fantasma acumulándose.
    // =========================================================================

    private Mono<? extends Connection> buildConnectionMono(
            ConnectionFactory factory, String appName, String type,
            AtomicReference<Mono<? extends Connection>> ref) {

        // Cada llamada a buildConnectionMono crea su propio CB y Bulkhead.
        // Esto es intencional: cuando el ShutdownListener reemplaza el Mono,
        // el nuevo Mono empieza con contadores limpios (0 fallos, CLOSED).
        CircuitBreaker circuitBreaker = buildCircuitBreaker(appName + "-" + type);
        Bulkhead bulkhead = buildBulkhead(appName + "-" + type);

        return Mono.fromCallable(() -> {
                    log.info("🔌 [{}] Conectando {} a RabbitMQ...", appName, type);
                    Connection conn = factory.newConnection(
                            appName + "-" + type.toLowerCase() + "-conn");

                    conn.addShutdownListener(cause -> {
                        if (!cause.isInitiatedByApplication()) {
                            log.warn("⚡ [{}] Conexión {} caída: {}. Preparando reconexión...",
                                    appName, type, cause.getReason());
                            // Reemplazamos el Mono con uno nuevo y limpio.
                            // AtomicReference.set() es atómico — si dos ShutdownListeners
                            // se disparan a la vez, el último gana pero ambos crean
                            // el mismo Mono lógicamente — no hay duplicados de conexión
                            // porque el nuevo Mono solo conecta cuando alguien se suscribe.
                            ref.set(buildConnectionMono(factory, appName, type, ref));
                        }
                    });

                    return conn;
                })
                // Orden Netflix: Bulkhead primero, CircuitBreaker segundo.
                // transformDeferred garantiza que el operador se aplica
                // en cada suscripción (cada reintento), no solo la primera vez.
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnNext(c -> log.info("✅ [{}] {} conectado exitosamente.", appName, type))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(s -> log.error(
                                "❌ [{}] RabbitMQ no disponible para {}. Reintento #{} en 5s...",
                                appName, type, s.totalRetries() + 1)))
                .cache(
                        conn -> Duration.ofDays(365),
                        err  -> Duration.ZERO,
                        ()   -> Duration.ZERO
                );
    }

    // =========================================================================
    // 5. BEANS DE CONEXIÓN
    // =========================================================================

    @Bean
    public Mono<? extends Connection> producerConnectionMono(
            ConnectionFactory factory,
            @Value("${spring.application.name:unknown-service}") String appName) {
        producerMonoRef.set(
                buildConnectionMono(factory, appName, "PRODUCER", producerMonoRef));
        return Mono.defer(producerMonoRef::get);
    }

    @Bean
    public Mono<? extends Connection> consumerConnectionMono(
            ConnectionFactory factory,
            @Value("${spring.application.name:unknown-service}") String appName) {
        consumerMonoRef.set(
                buildConnectionMono(factory, appName, "CONSUMER", consumerMonoRef));
        return Mono.defer(consumerMonoRef::get);
    }

    // =========================================================================
    // 6. COMPONENTES REACTIVOS
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean
    public Sender rabbitSender(Mono<? extends Connection> producerConnectionMono) {
        return RabbitFlux.createSender(
                new SenderOptions().connectionMono(producerConnectionMono));
    }

    @Bean
    @ConditionalOnMissingBean
    public Receiver rabbitReceiver(Mono<? extends Connection> consumerConnectionMono) {
        return RabbitFlux.createReceiver(
                new ReceiverOptions().connectionMono(consumerConnectionMono));
    }

    // =========================================================================
    // 7. INFRAESTRUCTURA SPRING AMQP
    // =========================================================================

    @Bean
    @ConditionalOnMissingBean
    public RabbitAdmin rabbitAdmin(
            org.springframework.amqp.rabbit.connection.ConnectionFactory springConnectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(springConnectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitInfrastructureDeclarer rabbitInfrastructureDeclarer(
            RabbitTemplate rabbitTemplate,
            InfraProperties infraProperties) {
        return new RabbitInfrastructureDeclarer(rabbitTemplate, infraProperties);
    }

    @Bean
    public Declarables rabbitEntities(RabbitInfrastructureDeclarer declarer) {
        return new Declarables(declarer.declarables());
    }

    @Bean
    @ConditionalOnMissingBean
    public MessagePublisher messagePublisher(Sender sender, InfraProperties infraProperties) {
        return new RabbitMQPublisher(sender, infraProperties);
    }

    @Bean
    public org.springframework.boot.ApplicationRunner initializeInfrastructure(
            RabbitAdmin rabbitAdmin,
            Declarables rabbitEntities) {
        return args -> {
            log.info("🛠️ [eventlib] Verificando infraestructura (exchanges, colas, DLQs)...");
            try {
                rabbitAdmin.initialize();
                log.info("🚀 [eventlib] Infraestructura declarada correctamente.");
            } catch (Exception e) {
                log.error("❌ [eventlib] Error al declarar infraestructura: {}. " +
                        "Se reintentará cuando RabbitMQ esté disponible.", e.getMessage());
            }
        };
    }
}
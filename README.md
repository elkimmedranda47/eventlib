# eventlib

Librería de integración de eventos para arquitecturas de microservicios basadas en Spring Boot WebFlux. Proporciona publicación y consumo de eventos de integración sobre **RabbitMQ** con soporte reactivo, idempotencia via **Redis**, y el patrón **Transactional Outbox** opcional.

> **Estado actual:** eventlib funciona como **módulo Maven** dentro del proyecto multi-módulo `microservices`. Los microservicios (`auth-services`, `email-service`, etc.) la consumen como dependencia interna desde el mismo proyecto padre.

---

## Estructura del proyecto

```
microservices/                          ← proyecto padre
├── pom.xml
├── eventlib/                           ← esta librería
│   └── src/main/java/com/main_group_ekn47/eventlib/
├── auth-services/                      ← productor de eventos  (puerto 8081)
│   └── src/main/java/com/main._group_ekn47/auth_services/
└── email-service/                      ← consumidor de eventos (puerto 8082)
    └── src/main/java/com/main._group_ekn47/email_service/
```

---

## Características

- **Publicación reactiva** de eventos de integración sobre RabbitMQ (Project Reactor / reactor-rabbitmq).
- **Consumo con idempotencia** automática usando Redis para evitar el procesamiento duplicado de mensajes.
- **Patrón Transactional Outbox** (opt-in): garantiza la entrega de eventos incluso ante fallos entre la base de datos y el broker.
- **Resiliencia** incorporada mediante Circuit Breaker y Bulkhead de Resilience4j.
- **Dead Letter Queue (DLQ)** configurada automáticamente por cada cola.
- **Autoconfiguración** Spring Boot — sin código de setup boilerplate.
- **Enrutamiento declarativo** de eventos a colas vía configuración YAML.

---

## Requisitos

- Java 17+
- Spring Boot 3.x
- RabbitMQ
- Redis (para idempotencia del consumidor)
- R2DBC + base de datos relacional (solo si se activa el Outbox)

---

## Instalación

Agrega la dependencia en el `pom.xml` del microservicio que la consume. La versión se hereda del `pom.xml` padre:

```xml
<dependency>
    <groupId>com.main_group_ekn47</groupId>
    <artifactId>eventlib</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

La librería se autoconfigura sola gracias al mecanismo de `AutoConfiguration.imports` de Spring Boot — no se requiere ninguna anotación adicional.

---

## Configuración

### Anatomía de la configuración de colas

```
eventlib.infra.rabbitmq.queues . user-created = user.created.queue
|____________________________|   |__________|   |__________________|
         Ruta al objeto          Key del mapa    Valor (nombre real de la cola)
```

---

## Uso — Productor (`auth-services`)

### `application.properties`

```properties
spring.application.name=auth-services
server.port=8081

# DATABASE - R2DBC PostgreSQL
spring.r2dbc.url=r2dbc:postgresql://localhost:5433/ms_auth_services
spring.r2dbc.username=ekn47
spring.r2dbc.password=password

# JWT CONFIG
jwt.secret=VjB3N2N5MjUzdDV6V2hLZHl4cDBtZmIyNnN2c3d0dzJ2MThhNm5XNGFjN3hRblp6Ym1vMDRjMWc4bEExYmUyMzJk
jwt.accessTokenExpiration=3600000
jwt.refreshTokenExpiration=86400000

# RABBITMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# EVENTLIB - EXCHANGE & QUEUES
eventlib.infra.rabbitmq.exchange=eventlib.exchange
eventlib.infra.rabbitmq.queues.user-created=user.created.queue
eventlib.infra.rabbitmq.queues.user-deleted=user.deleted.queue

# EVENTLIB - DLQ
eventlib.infra.rabbitmq.dlq.enabled=true
eventlib.infra.rabbitmq.dlq.suffix=.dlq

# EVENTLIB - OUTBOX
eventlib.infra.outbox.enabled=true
eventlib.infra.outbox.polling-interval=5000
```

### 1. Definir un evento de integración

Extiende `IntegrationEvent` e implementa `getEventName()`. Usa `@JsonProperty` si el nombre del campo en el JSON difiere del nombre del atributo Java:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent extends IntegrationEvent {

    private String userId;

    @JsonProperty("email")      // el JSON viaja con la clave "email"
    private String userEmail;

    public UserRegisteredEvent() {
        super(); // requerido por Jackson
    }

    public UserRegisteredEvent(String userId, String userEmail) {
        super(); // inicializa eventId, timestamp, etc.
        this.userId = userId;
        this.userEmail = userEmail;
    }

    @Override
    public String getEventName() {
        return "user-created";  // ⚠️ debe coincidir con el handler del consumidor
    }

    // getters y setters...
}
```

### 2. Publicar un evento

Inyecta `EventPublisher` en el servicio de aplicación y encadena la publicación dentro del flujo reactivo. El Outbox garantiza que si la publicación a RabbitMQ falla, el evento queda persistido y se reintenta:

```java
@Service
public class AuthenticationServiceHandler implements AuthenticationService {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public AuthenticationServiceHandler(UserRepository userRepository,
                                        /* ...otros repositorios... */,
                                        EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Mono<UserRegisteredEvent> registerUser(RegisterUserCommand command) {
        return userRepository.findByUsername(command.username())
                .flatMap(u -> Mono.<UserRegisteredEvent>error(
                        new IllegalArgumentException("El nombre de usuario ya existe")))

                .switchIfEmpty(userRepository.findByEmail(command.email())
                        .flatMap(u -> Mono.<UserRegisteredEvent>error(
                                new IllegalArgumentException("El email ya está registrado"))))

                .switchIfEmpty(
                        passwordEncoderService.encode(command.password())
                                .flatMap(encodedPassword -> {
                                    User newUser = User.create(
                                            command.username(),
                                            new EmailAddress(command.email()),
                                            new PasswordHash(encodedPassword)
                                    );
                                    return userRepository.save(newUser)
                                            .map(user -> new UserRegisteredEvent(
                                                    user.getId().value().toString(),
                                                    user.getEmail().value()
                                            ))
                                            .flatMap(event ->
                                                    eventPublisher.publish(event)
                                                            .thenReturn(event)  // continúa el flujo
                                            );
                                })
                );
    }
}
```

> **Nota:** No instancies `MessageSerializer` manualmente en el servicio. Es un bean interno de la librería — si lo necesitas en otro contexto, inyéctalo por constructor.

### 3. `main` del microservicio productor y el problema de `@EnableR2dbcRepositories`

#### Resumen

| Configuración | Resultado |
|---|---|
| Solo autodiscovery de Spring Boot | Puede presentar problemas cuando coexisten Spring Data R2DBC y Spring Data Redis |
| `@EnableR2dbcRepositories` apuntando al paquete R2DBC del microservicio | Recomendado ✅ |
| Agregar manualmente paquetes internos de EventLib | Generalmente innecesario ⚠️ |
| Redis + R2DBC sin configuración explícita | Spring puede entrar en *Strict Repository Configuration Mode* y no registrar algunos repositorios correctamente ⚠️ |
```java
package com.main._group_ekn47.auth_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
@SpringBootApplication
// Reemplaza esto con el paquete exacto donde están tus interfaces SpringData...Repository
@EnableR2dbcRepositories(basePackages = "com.main._group_ekn47.auth_services.infrastructure.adapter.repository.r2dbc")

public class AuthServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServicesApplication.class, args);
    }
}
```

> Si no usas el Outbox (`outbox.enabled=false`), puedes eliminar la anotación o dejar solo el paquete de tu servicio.

---

## Uso — Consumidor (`email-service`)

### `application.properties`

```properties
spring.application.name=email-service
server.port=8082

spring.r2dbc.url=r2dbc:postgresql://localhost:5433/ms_email_services_db
spring.r2dbc.username=ekn47
spring.r2dbc.password=password

# RABBITMQ - misma conexión que el productor
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# REDIS - configurado vía eventlib (no via spring.data.redis)
eventlib.infra.redis.host=localhost
eventlib.infra.redis.port=6379

# COLA A ESCUCHAR
eventlib.infra.rabbitmq.queues.user-created=user.created.queue

# IDEMPOTENCIA
messaging.idempotency.store=redis
eventlib.infra.redis.idempotency-prefix=eventlib:idempotency:email-service
```

> **Nota sobre Redis en el consumidor:** la librería expone su propia clave `eventlib.infra.redis.*` para configurar Redis. No es necesario declarar `spring.data.redis.*` en el consumidor si no se usa Redis directamente fuera de eventlib.

### 4. Implementar el handler del evento

Implementa `IntegrationEventHandler<T>` y regístrala como bean de Spring. El valor que retorna `getEventName()` **debe coincidir exactamente** con el `eventName` que el productor envía en el JSON:

```java
@Slf4j
@Component
public class WelcomeEmailHandler implements IntegrationEventHandler<UserRegisteredEvent> {

    @Override
    public String getEventName() {
        return "user-created"; // ⚠️ idéntico al getEventName() del evento del productor
    }

    @Override
    public Mono<Void> handle(UserRegisteredEvent event) {
        return Mono.fromRunnable(() -> {
            log.info("📧 Enviando email de bienvenida a {}", event.getUserEmail());
            log.info("🆔 Evento ID: {}", event.getEventId());
            // lógica de negocio...
        }).then();
    }
}
```

El `EventDispatcher` enruta automáticamente los mensajes entrantes al handler correcto según el `eventName`, y marca el evento como procesado en Redis para garantizar la idempotencia.

### 5. Suscribirse a la cola (ApplicationRunner)

Crea una clase de configuración que arranque la suscripción al iniciar la aplicación:

```java
@Slf4j
@Configuration
public class EventSubscriptionConfig {

    private final RabbitMessageReceiver receiver;

    @Value("${eventlib.infra.rabbitmq.queues.user-created}")
    private String queueName;

    public EventSubscriptionConfig(@Lazy RabbitMessageReceiver receiver) {
        this.receiver = receiver;
    }

    @Bean
    public ApplicationRunner setupSubscriptions() {
        return args -> {
            receiver.consume(queueName, UserRegisteredEvent.class)
                    .subscribe(
                            event -> log.debug("📨 [Email-Service] Evento procesado: {}",
                                    event.getEventId()),
                            err -> log.error("💀 [Email-Service] Flujo terminado: {}",
                                    err.getMessage())
                    );

            log.info("✅ [Email-Service] Escuchando en cola: {}", queueName);
        };
    }
}
```

> **⚠️ No usar `onErrorContinue` aquí.** Absorbe los errores antes de que lleguen al `retryWhen` interno de `RabbitMessageReceiver`, impidiendo la reconexión automática ante caídas del broker. La resiliencia ya está gestionada por la librería.

---

## Patrón Transactional Outbox

Cuando `eventlib.infra.outbox.enabled=true`, el `EventPublisher` persiste los eventos en la tabla `outbox_events` **dentro de la misma transacción del negocio**, en lugar de publicarlos directamente al broker. Un scheduler (`OutboxScheduler`) los procesa según el intervalo configurado (`polling-interval`) con hasta 5 reintentos (3 segundos de espera entre cada uno) y los publica en RabbitMQ.

Este patrón garantiza que **ningún evento se pierde** incluso si RabbitMQ no está disponible en el momento de la operación.

```properties
eventlib.infra.outbox.enabled=true
eventlib.infra.outbox.polling-interval=5000   # milisegundos
```

Requiere la tabla `outbox_events` en la base de datos R2DBC del microservicio productor y que `@EnableR2dbcRepositories` incluya el paquete `com.main_group_ekn47.eventlib.producer` (ver paso 3).

---

## Arquitectura interna

```
Producer side
─────────────────────────────────────────────────────────────
EventPublisher (interfaz)
  ├── DirectEventPublisher     → publica directo a RabbitMQ
  └── OutboxEventPublisher     → persiste en DB (Outbox pattern)
         └── OutboxScheduler   → publica en background (polling-interval)

Consumer side
─────────────────────────────────────────────────────────────
RabbitMessageReceiver
  └── EventDispatcher
        ├── IdempotencyStore (Redis) → descarta duplicados
        └── IntegrationEventHandler<T> → lógica de negocio
```

---

## Resiliencia

- **Circuit Breaker** (Resilience4j): abre el circuito si RabbitMQ no responde.
- **Bulkhead**: limita la concurrencia de publicaciones simultáneas.
- **DLQ automática**: los mensajes no procesables se mueven a `<queue-name>.dlq`.
- **Reintentos con backoff** (Outbox): 5 reintentos con 3 segundos de espera entre intentos.

---

## Licencia

Distribuido bajo la licencia [Apache 2.0](LICENSE). © Elkim Andres Medranda Caicedo.

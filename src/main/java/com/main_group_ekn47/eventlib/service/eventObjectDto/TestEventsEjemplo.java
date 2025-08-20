package com.main_group_ekn47.eventlib.service.eventObjectDto;

public class TestEventsEjemplo {
    /*
        https://gemini.google.com/app/5e259b6d0ae2182a?hl=es
        TestEvent.

        3. Corregir EventLibApplicationRunner.java
        Tu clase TestEvent necesita un constructor que reciba el nombre del evento y el tema para pasarlos a la clase base.

        Java

        // EventLibApplicationRunner.java
        import com.main_group_ekn47.eventlib.core.IntegrationEvent;
        import com.main_group_ekn47.eventlib.core.PublishEvent;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.stereotype.Component;
        import reactor.core.publisher.Mono;

        @Component
        public class EventLibApplicationRunner implements CommandLineRunner {

            private final EventPublisher eventPublisher;

            public EventLibApplicationRunner(EventPublisher eventPublisher) {
                this.eventPublisher = eventPublisher;
            }

            @Override
            public void run(String... args) throws Exception {
                System.out.println(">>> Generando evento de prueba a través del Outbox...");
                eventPublisher.publishTestEvent()
                        .doOnNext(event -> System.out.println("Evento de prueba generado. El OutboxPublisher lo gestionará pronto."))
                        .subscribe();
            }

            @Component
            public static class EventPublisher {
                @PublishEvent(topic = "test", eventName = "TestEvent")
                public Mono<TestEvent> publishTestEvent() {
                    // Pasa el nombre y el tema al constructor
                    return Mono.just(new TestEvent("TestEvent", "test", "Mi primer evento"));
                }
            }

            public static class TestEvent extends IntegrationEvent {
                private String message;

                public TestEvent(String eventName, String topic, String message) {
                    super(eventName, topic); // Llama al constructor de la clase base
                    this.message = message;
                }

                public String getMessage() {
                    return message;
                }
            }
        }
        Con estos cambios, la clase IntegrationEvent tendrá la información necesaria, y
        el PublishEventAspect podrá crear el OutboxEvent sin errores. Esto resolverá el
        problema de compilación y asegurará que tu patrón Outbox funcione como se espera.

     */

/*

Flujo de Implementación del email-service
El email-service actuará como un consumidor de eventos de la librería que creaste. El flujo es el siguiente:

El auth-service emite un evento (UserRegisteredEvent) cuando se registra un nuevo usuario.

La librería (event-lib) toma ese evento de la tabla outbox y lo publica al broker de mensajería (RabbitMQ, Kafka, etc.).

El email-service tiene un EventListener que escucha el evento UserRegisteredEvent.

Cuando el EventListener del email-service recibe el evento, llama al EmailSenderService para enviar el correo de bienvenida.

1. El auth-service publica el evento
En tu auth-service, dentro de tu UserManagementServiceHandler, tu método de registro de usuario podría verse así:

Java

// src/main/java/com/yourcompany/authservice/application/handler/UserManagementServiceHandler.java

@Service
public class UserManagementServiceHandler implements UserManagementService {

    private final UserRepository userRepository;

    // ... inyección de dependencias

    @Override
    // La anotación de la librería mágica que guarda el evento en la tabla Outbox
    @PublishEvent(topic = "user-events", eventName = "UserRegisteredEvent")
    public Mono<User> registerUser(RegisterUserCommand command) {
        return userRepository.save(User.create(command))
            .doOnSuccess(user -> {
                // Aquí se crea el evento de dominio/integración
                return Mono.just(new UserRegisteredEvent(user.getUserId().value(), user.getEmailAddress().value()));
            });
    }
}
2. El email-service recibe y procesa el evento
Primero, necesitas la clase del evento en tu email-service para poder deserializar el mensaje.

Java

// src/main/java/com/yourcompany/emailservice/domain/event/UserRegisteredEvent.java

import com.tuempresa.eventlib.core.IntegrationEvent;

public class UserRegisteredEvent extends IntegrationEvent {
    private String userId;
    private String userEmail;

    // ... constructor, getters y setters
}
Luego, creas el EventListener en tu email-service que se encargará de reaccionar al evento.

Java

// src/main/java/com/yourcompany/emailservice/infrastructure/listener/AuthEventListener.java

@Component
public class AuthEventListener {

    private final EmailSenderService emailSenderService;
    private final IdempotencyStore idempotencyStore;

    // ... inyección de dependencias

    @RabbitListener(queues = "${messaging.rabbitmq.queues.user-registered}") // O @KafkaListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event, Message message) {
        // La librería maneja la idempotencia por nosotros con el IdempotencyAspect
        try {
             // Lógica de negocio para enviar el correo
            emailSenderService.sendWelcomeEmail(event.getUserEmail());

            // Si todo fue bien, marcas el mensaje como procesado
            idempotencyStore.storeMessageId(event.getEventId());

        } catch (Exception e) {
            // La librería RetryHandler se encargará de los reintentos
            // O si es un error fatal, irá a la DLQ
        }
    }
}
3. El servicio de envío de correos
Este servicio es la implementación de tu lógica de negocio para mandar correos.

Java

// src/main/java/com/yourcompany/emailservice/infrastructure/adapter/service/SmtpEmailSenderAdapter.java

@Service
public class SmtpEmailSenderAdapter implements EmailSenderService {

    private final JavaMailSender javaMailSender;

    // ... inyección de dependencia

    @Override
    public Mono<Void> sendWelcomeEmail(String to) {
        // Lógica para construir y enviar el correo usando JavaMailSender
        return Mono.fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@yourcompany.com");
            message.setTo(to);
            message.setSubject("¡Bienvenido a nuestra plataforma!");
            message.setText("Hola,\n\nGracias por registrarte. ¡Estamos felices de tenerte aquí!");
            javaMailSender.send(message);
        });
    }
}
4. Configuración del email-service
Finalmente, necesitas un application.yml en tu email-service para configurar tanto el broker de mensajería
* como los detalles de tu servidor SMTP.

YAML

# src/main/resources/application.yml
spring:
  application:
    name: email-service
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  mail:
    host: smtp.gmail.com
    port: 587
    username: tu_correo@gmail.com
    password: tu_password_de_aplicacion
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# Configuración para la librería de eventos
messaging:
  rabbitmq:
    queues:
      user-registered: "user.registered"
Con estos pasos, el email-service estará configurado para escuchar
eventos de registro de usuarios y responder enviando un correo de bienvenida, utilizando todas las garantías de robustez que te ofrece tu librería de mensajería.
    *
    *
    * */
}

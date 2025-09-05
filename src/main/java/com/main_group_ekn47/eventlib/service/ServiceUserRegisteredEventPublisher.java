

package com.main_group_ekn47.eventlib.service;
import com.main_group_ekn47.eventlib.core.PublishEvent;
import com.main_group_ekn47.eventlib.service.eventObjectDto.UserRegisteredEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.Objects;

@Service
public class ServiceUserRegisteredEventPublisher {
    // Una instancia del generador de tokens para el ejemplo
    private final TokenGenerator tokenGenerator = new TokenGenerator();
    /**
     * Este método simula el registro de un usuario.
     * La anotación @PublishEvent se encarga de publicar el evento cuando
     * el Mono se completa exitosamente.
     *
     * @param command El comando que contiene los datos de registro.
     * @return Un Mono que emite el evento de registro.
     */
    /*@PublishEvent(topic = "user-events", eventName = "UserRegisteredEvent")
    //@PublishEvent(topic = "test", eventName = "TestEvent")
    public Mono<UserRegisteredEvent> registerUser() {
        // Lógica de ejemplo para crear el usuario.
        // En una aplicación real, aquí guardarías el usuario en la base de datos.
        RegisterUserCommand command = new RegisterUserCommand("correoejemplo@gmail.com");
        User user = new User(new UserId(UUID.randomUUID()), new EmailAddress(command.getEmail()));
        String token = tokenGenerator.generateActivationToken();
        // Retorna el evento que se publicará para que el aspecto lo capture
        //package com.main_group_ekn47.eventlib.producer.aop;PublishEventAspect{ se ejecuta public Object handlePublishEvent()}
        return Mono.just(new UserRegisteredEvent(user.getUserId().value().toString(), user.getEmailAddress().value(), token));
    }*/

    /**
     * Este método simula el registro de un usuario.
     * La anotación @PublishEvent se encarga de publicar el evento cuando
     * el método retorna exitosamente.
     *
     * @return El evento de registro que se publicará.
     */
    @PublishEvent(topic = "user-events", eventName = "UserRegisteredEvent")
    public UserRegisteredEvent registerUser() {
        // Lógica de ejemplo para crear el usuario.
        // En una aplicación real, aquí guardarías el usuario en la base de datos.
        RegisterUserCommand command = new RegisterUserCommand("correoejemploekn47@gmail.com");
        User user = new User(new UserId(UUID.randomUUID()), new EmailAddress(command.getEmail()));
        String token = tokenGenerator.generateActivationToken();

        // Retorna el objeto directamente, sin el Mono.
        return new UserRegisteredEvent(user.getUserId().value().toString(), user.getEmailAddress().value(), token);
    }







    // --- Clases Internas Necesarias para el Funcionamiento ---

    /**
     * Clase que simula el objeto de dominio del Usuario.
     */
    public static class User {
        private final UserId userId;
        private final EmailAddress emailAddress;

        public User(UserId userId, EmailAddress emailAddress) {
            this.userId = userId;
            this.emailAddress = emailAddress;
        }

        public UserId getUserId() {
            return userId;
        }

        public EmailAddress getEmailAddress() {
            return emailAddress;
        }
    }

    /**
     * Objeto de Valor (Value Object) para el ID del usuario.
     * Encapsula un valor de UUID.
     */
    public static class UserId {
        private final UUID value;

        public UserId(UUID value) {
            this.value = Objects.requireNonNull(value);
        }

        public UUID value() {
            return value;
        }
    }

    /**
     * Objeto de Valor (Value Object) para la dirección de correo electrónico.
     * Encapsula un valor de String.
     */
    public static class EmailAddress {
        private final String value;

        public EmailAddress(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /**
     * Objeto de Transferencia de Datos (DTO) para el comando de registro de usuario.
     */
    public static class RegisterUserCommand {
        private final String email;

        public RegisterUserCommand(String email) {
            this.email = Objects.requireNonNull(email);
        }

        public String getEmail() {
            return email;
        }
    }

    /**
     * El Evento de Integración que se publicará en el bus de mensajes.
     * Debe extender de una clase base de eventos.
     */
    /*
    public static class UserRegisteredEvent extends IntegrationEvent {
        private final String userId;
        private final String email;
        private final String token;

        public UserRegisteredEvent(String userId, String email, String token) {
            super("UserRegisteredEvent");
            this.userId = userId;
            this.email = email;
            this.token = token;
        }

        // Getters para la serialización del evento
        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }
    }
    */

    /**
     * Clase base abstracta para todos los eventos de integración.
     * Provee campos comunes como el nombre, ID y la marca de tiempo.
     */

    /**
     * Un generador de tokens de ejemplo. En una aplicación real podría ser un bean de Spring.
     */
    public static class TokenGenerator {
        public String generateActivationToken() {
            return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}

package com.main_group_ekn47.eventlib.config;

import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.service.EventPublisher; // Importa el nuevo servicio
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

        // Llama al método desde el bean del servicio para que el aspecto funcione
        eventPublisher.publishTestEvent()
                .doOnNext(event -> System.out.println("Evento de prueba generado. El OutboxPublisher lo gestionará pronto."))
                .subscribe();
    }

    public static class TestEvent extends IntegrationEvent {
        private String message;
        public TestEvent(String message) {
            super("TestEvent");
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
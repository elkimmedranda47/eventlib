package com.main_group_ekn47.eventlib.config;

import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import com.main_group_ekn47.eventlib.service.ServiceOneEventPublisher; // Importa el nuevo servicio
import com.main_group_ekn47.eventlib.service.ServiceUserRegisteredEventPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EventLibApplicationRunner implements CommandLineRunner {

    private final ServiceOneEventPublisher eventPublisher;

    private final ServiceUserRegisteredEventPublisher objetoEventRegisterdUsers;

    public EventLibApplicationRunner(ServiceOneEventPublisher eventPublisher,
                                     ServiceUserRegisteredEventPublisher objetoEventRegisterdUsers) {
        this.eventPublisher = eventPublisher;   //metodo publishTestEvent() viene de la clase ServiceOneEventPublisher{}
        this.objetoEventRegisterdUsers = objetoEventRegisterdUsers;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Generando evento de prueba a través del Outbox...");
        //El PublishEventAspect intercepta el método publishTestEvent().
        // Llama al método desde el bean del servicio para que el aspecto funcione
        /*
        eventPublisher.publishTestEvent()
                .doOnNext(event -> System.out.println("Evento de prueba generado. El OutboxPublisher lo gestionará pronto."))
                .subscribe();
        */

        /*
        objetoEventRegisterdUsers.registerUser()
                .doOnNext(event -> System.out.println("Evento de prueba generado. El registerUser lo gestionará pronto."))
                .subscribe();

         */
        // Llama al método de manera imperativa
        objetoEventRegisterdUsers.registerUser();
        System.out.println("Evento de prueba generado. El registerUser lo gestionará pronto.");

    }




}

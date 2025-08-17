package com.main_group_ekn47.eventlib.config;
/*
import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;



import com.main_group_ekn47.eventlib.monitoring.LoggingHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@EnableScheduling // <-- ¡Añade esto!

@Component
public class EventLibApplicationRunner implements ApplicationRunner {

    private final LoggingHandler loggingHandler;
    private final MessagingProperties properties;

    public EventLibApplicationRunner(LoggingHandler loggingHandler,
                                     MessagingProperties properties) {
        this.loggingHandler = loggingHandler;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        loggingHandler.logInfo("╔══════════════════════════════════════╗");
        loggingHandler.logInfo("║      EventLib Iniciada Correctamente ║");
        loggingHandler.logInfo("╚══════════════════════════════════════╝");
        loggingHandler.logInfo("Modo Outbox: " +
                (properties.getOutbox().isEnabled() ? "ACTIVADO" : "DESACTIVADO"));
        loggingHandler.logInfo("Broker: " + properties.getBroker().toUpperCase());

        if("rabbitmq".equalsIgnoreCase(properties.getBroker())) {
            loggingHandler.logInfo("Exchange: " + properties.getRabbitmq().getExchange());
            loggingHandler.logInfo("Routing Key: " +
                    (properties.getRabbitmq().getRoutingKey() != null ?
                            properties.getRabbitmq().getRoutingKey() :
                            properties.getRabbitmq().getQueue()));
        }
        loggingHandler.logInfo("╔══════════════════════════════════════╗");
        loggingHandler.logInfo("║        Detalles  EventLib            ║");
        loggingHandler.logInfo("╚══════════════════════════════════════╝");

        loggingHandler.logInfo("RabbitMQ Host: " + properties.getRabbitmq().getHost());
        loggingHandler.logInfo("RabbitMQ Port: " + properties.getRabbitmq().getPort());
        loggingHandler.logInfo("RabbitMQ Username: " + properties.getRabbitmq().getUsername());
        loggingHandler.logInfo("RabbitMQ Password: " + properties.getRabbitmq().getPassword());
    }
}*/
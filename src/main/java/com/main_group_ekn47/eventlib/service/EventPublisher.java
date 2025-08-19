package com.main_group_ekn47.eventlib.service;

import com.main_group_ekn47.eventlib.core.PublishEvent;
import com.main_group_ekn47.eventlib.config.EventLibApplicationRunner.TestEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventPublisher {

    @PublishEvent(topic = "test", eventName = "TestEvent")
    public Mono<TestEvent> publishTestEvent() {
        System.out.println("!!!!1**<0>**** ejecutando TestEvent() desde el servicio");
        return Mono.just(new TestEvent("Mi primer evento"));
    }
}
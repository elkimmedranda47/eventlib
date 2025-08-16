package com.main_group_ekn47.eventlib.core;

public abstract class IntegrationEvent extends DomainEvent {
    protected IntegrationEvent(String eventType) {
        super(eventType);
    }
}
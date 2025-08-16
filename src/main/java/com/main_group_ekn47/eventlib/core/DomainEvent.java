package com.main_group_ekn47.eventlib.core;

import lombok.Getter;

@Getter
public abstract class DomainEvent {
    private final EventMetadata metadata;

    protected DomainEvent(String eventType) {
        this.metadata = new EventMetadata(eventType);
    }
}
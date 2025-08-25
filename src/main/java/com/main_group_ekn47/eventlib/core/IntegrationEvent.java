package com.main_group_ekn47.eventlib.core;

import lombok.NoArgsConstructor;

@NoArgsConstructor // ✅ Ahora esto funcionará

public abstract class IntegrationEvent extends DomainEvent {
    protected IntegrationEvent(String eventType) {
        super(eventType);
    }
}
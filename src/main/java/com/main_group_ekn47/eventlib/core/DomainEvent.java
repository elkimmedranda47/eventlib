package com.main_group_ekn47.eventlib.core;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // ✅ Añade un constructor por defecto aquí

public abstract class DomainEvent {
    private  EventMetadata metadata;

    protected DomainEvent(String eventType) {
        this.metadata = new EventMetadata(eventType);
    }
}
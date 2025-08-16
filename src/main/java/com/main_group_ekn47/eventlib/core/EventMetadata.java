package com.main_group_ekn47.eventlib.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class EventMetadata {
    private String eventId;
    private Instant timestamp;
    private String eventType;

    public EventMetadata(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = eventType;
    }
}
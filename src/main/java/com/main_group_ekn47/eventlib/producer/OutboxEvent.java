package com.main_group_ekn47.eventlib.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;
    private String eventName;
    private String topic;
    private String payload;
    private Instant createdAt;
    private Instant publishedAt;
}
package com.main_group_ekn47.eventlib.producer;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OutboxRepository extends R2dbcRepository<OutboxEvent, UUID> {
    
    @Query("SELECT * FROM outbox_events WHERE published_at IS NULL ORDER BY created_at ASC")
    Flux<OutboxEvent> findUnpublishedEvents();
    
    @Query("UPDATE outbox_events SET published_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Void> markAsPublished(UUID id);
   //***************************************
   // Opción 2: Consulta manual (si mantienes el campo published)
  // @Query("SELECT COUNT(*) FROM outbox WHERE published = false")
   //Mono<Long> countUnpublishedEvents();

    // Opción 1: Usando query method (si tienes el campo publishedAt)
    Mono<Long> countByPublishedAtIsNull();

}

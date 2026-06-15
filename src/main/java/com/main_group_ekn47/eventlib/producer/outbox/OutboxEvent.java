/*
 * Licensed to Elkim Andres Medranda Caicedo under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Elkim Andres Medranda Caicedo licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.main_group_ekn47.eventlib.producer.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("outbox_events")
public class OutboxEvent implements Persistable<UUID> {
    @Id
    private UUID id;
    private String eventName;
    private String topic;
    private String payload;
    private Instant createdAt;
    private Instant publishedAt;
    private String className;
    @org.springframework.data.annotation.Transient
    @Builder.Default
    private boolean isUpdate = false; // Por defecto es falso para que sea INSERT

    @Override public UUID getId() { return id; }

    //@Override public boolean isNew() { return true; }
    @Override
    @org.springframework.data.annotation.Transient
    public boolean isNew() {
        // Si isUpdate es true, isNew() retorna false -> Ejecuta UPDATE
        // Si isUpdate es false, isNew() retorna true -> Ejecuta INSERT
        return !isUpdate;
    }
}
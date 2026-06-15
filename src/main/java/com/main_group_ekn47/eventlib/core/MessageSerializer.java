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
/**
 * Serializador JSON genérico.
 */



package com.main_group_ekn47.eventlib.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MessageSerializer {

    // Este es el "mapper" que definimos a nivel de clase
    private final ObjectMapper mapper;

    public MessageSerializer() {
        this.mapper = new ObjectMapper();
        // Aquí lo configuramos una sola vez
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Método que devuelve String (el que ya tenías)
    public String serialize(Object object) {
        try {
            return this.mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing to String: " + e.getMessage(), e);
        }
    }

    // Método que devuelve byte[] (el que necesita RabbitMQ)
    public byte[] serializev(Object event) {
        try {
            // USAMOS EL MISMO "this.mapper" de arriba
            return this.mapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing to bytes: " + e.getMessage(), e);
        }
    }

    public <T> T deserialize(String payload, Class<T> type) {
        try {
            return this.mapper.readValue(payload, type);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing: " + e.getMessage(), e);
        }
    }
}

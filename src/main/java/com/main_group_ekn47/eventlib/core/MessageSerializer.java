package com.main_group_ekn47.eventlib.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class MessageSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageSerializer() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing event", e);
        }
    }

    public JsonNode deserializeToJson(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON", e);
        }
    }

    public <T> T deserialize(String payload, Class<T> eventType) {
        try {
            return objectMapper.readValue(payload, eventType);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing event: " + eventType.getName(), e);
        }
    }
}
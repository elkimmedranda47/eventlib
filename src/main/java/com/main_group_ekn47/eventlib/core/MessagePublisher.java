package com.main_group_ekn47.eventlib.core;

import com.fasterxml.jackson.databind.JsonNode;

public interface MessagePublisher {
    void publish(String topic, String eventName, JsonNode payload);
}
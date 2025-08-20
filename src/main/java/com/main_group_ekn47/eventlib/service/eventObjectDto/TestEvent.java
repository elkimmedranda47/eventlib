package com.main_group_ekn47.eventlib.service.eventObjectDto;


import com.main_group_ekn47.eventlib.core.IntegrationEvent;

public  class TestEvent extends IntegrationEvent {
    private String message;
    public TestEvent(String message) {
        super("TestEvent");
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
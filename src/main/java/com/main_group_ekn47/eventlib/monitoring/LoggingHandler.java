package com.main_group_ekn47.eventlib.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoggingHandler.class);

    public void logInfo(String message) {
        logger.info("[EventLib] {}", message);
    }

    public void logWarning(String message) {
        logger.warn("[EventLib] {}", message);
    }

    public void logError(String message, Throwable error) {
        logger.error("[EventLib] {} - Error: {}", message, error.getMessage());
    }
}
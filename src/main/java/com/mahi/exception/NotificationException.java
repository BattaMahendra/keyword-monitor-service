package com.mahi.exception;

/**
 * Exception thrown when there's an issue with notification services
 */
public class NotificationException extends KeywordMonitorException {

    public NotificationException(String message) {
        super(message, "NOTIFICATION_ERROR");
    }

    public NotificationException(String message, Throwable cause) {
        super(message, "NOTIFICATION_ERROR", cause);
    }
}


package com.obed.notification.domain.exception;

public abstract class NotificationException extends RuntimeException {
    protected NotificationException(String message) {
        super(message);
    }

    protected NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
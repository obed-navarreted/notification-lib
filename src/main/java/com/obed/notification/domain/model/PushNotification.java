package com.obed.notification.domain.model;

import java.util.Map;

public record PushNotification(
        String recipient, // Device Token
        String title,
        String body,
        Map<String, String> data // Metadata extra para la app móvil
) implements Notification {
    public PushNotification {
        if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("Device token cannot be null");
    }
}

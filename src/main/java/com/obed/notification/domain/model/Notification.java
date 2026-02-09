package com.obed.notification.domain.model;

public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
    String recipient();
}

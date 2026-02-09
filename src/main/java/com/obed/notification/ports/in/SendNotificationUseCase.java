package com.obed.notification.ports.in;

import com.obed.notification.domain.model.Notification;

public interface SendNotificationUseCase {
    void send(Notification notification);
}
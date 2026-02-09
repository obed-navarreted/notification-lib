package com.obed.notification.ports.out;

import com.obed.notification.domain.model.Notification;

public interface NotificationPort<T extends Notification> {
    void send(T notification);

    Class<T> supports();
}

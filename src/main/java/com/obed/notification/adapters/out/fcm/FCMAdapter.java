package com.obed.notification.adapters.out.fcm;

import com.obed.notification.domain.model.PushNotification;
import com.obed.notification.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FCMAdapter implements NotificationPort<PushNotification> {
    private static final Logger log = LoggerFactory.getLogger(FCMAdapter.class);

    @Override
    public void send(PushNotification notification) {
        log.info("[FCM Provider] Sending message to device token: {}", notification.recipient());
        log.info("Notification: Title='{}', Body='{}'", notification.title(), notification.body());

        log.info("[FCM] Successfully sent message: projects/my-app/messages/0:123456789");
    }

    @Override
    public Class<PushNotification> supports() {
        return PushNotification.class;
    }
}

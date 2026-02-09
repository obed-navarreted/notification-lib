package com.obed.notification.adapters.out.twilio;

import com.obed.notification.domain.model.SmsNotification;
import com.obed.notification.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwilioSmsAdapter implements NotificationPort<SmsNotification> {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsAdapter.class);
    private final TwilioConfig config;

    public TwilioSmsAdapter(TwilioConfig config) {
        this.config = config;
    }

    @Override
    public void send(SmsNotification notification) {
        log.info("[Twilio Provider] POST to /2010-04-01/Accounts/{}/Messages.json", config.accountSid());
        log.info("From: {}", config.fromPhoneNumber());
        log.info("To:   {}", notification.recipient());
        log.info("Body: {}", notification.message());

        log.info("[Twilio] Response: SID SMxxxxxxxxxxxxxxxx (Queued)");
    }

    @Override
    public Class<SmsNotification> supports() {
        return SmsNotification.class;
    }
}
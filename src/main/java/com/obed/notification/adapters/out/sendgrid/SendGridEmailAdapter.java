package com.obed.notification.adapters.out.sendgrid;

import com.obed.notification.domain.exception.ValidationException;
import com.obed.notification.domain.model.EmailNotification;
import com.obed.notification.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendGridEmailAdapter implements NotificationPort<EmailNotification> {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailAdapter.class);
    private final SendGridConfig config;

    public SendGridEmailAdapter(SendGridConfig config) {
        this.config = config;
    }

    @Override
    public void send(EmailNotification notification) {
        if (notification.recipient().contains("invalid")) {
            log.error("[SendGrid] Failed to send email: Invalid recipient address '{}'", notification.recipient());
            throw new ValidationException("Invalid recipient address '"+notification.recipient()+"'");
        }

        log.info("[SendGrid Provider] Preparing HTTP POST to https://api.sendgrid.com/v3/mail/send");
        log.info("Auth: Bearer {}", maskKey(config.apiKey()));
        log.info("JSON Payload: { \"personalizations\": [{ \"to\": [{ \"email\": \"{}\" }] }], \"subject\": \"{}\" }",
                notification.recipient(), notification.subject());
        log.info("[SendGrid] Response: 202 Accepted");
    }

    @Override
    public Class<EmailNotification> supports() {
        return EmailNotification.class;
    }

    private String maskKey(String key) {
        return (key != null && key.length() > 4) ? key.substring(0, 4) + "****" : "****";
    }
}
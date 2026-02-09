package com.obed.notification.adapters.out.sendgrid;

public record SendGridConfig(
        String apiKey,
        String senderEmail
) {
}
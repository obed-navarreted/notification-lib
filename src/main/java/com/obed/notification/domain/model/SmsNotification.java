package com.obed.notification.domain.model;

import com.obed.notification.domain.exception.ValidationException;

import java.util.regex.Pattern;

public record SmsNotification(
        String recipient,
        String message
) implements Notification {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    public SmsNotification {
        if (recipient == null || !PHONE_PATTERN.matcher(recipient).matches())
            throw new ValidationException("Invalid phone number format. Expected E.164 format (e.g., +1234567890)");

        if (message == null || message.isBlank())
            throw new ValidationException("Message cannot be null or blank");
    }
}

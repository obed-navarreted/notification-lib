package com.obed.notification.domain.model;

import com.obed.notification.domain.exception.ValidationException;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public record EmailNotification(
        String recipient,
        String subject,
        String body,
        List<File> attachments
) implements Notification {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public EmailNotification {
        if (recipient == null || recipient.isBlank())
            throw new ValidationException("The recipient email cannot be null or blank");

        if (!EMAIL_PATTERN.matcher(recipient).matches())
            throw new ValidationException("Invalid email format for recipient: " + recipient);

        if (subject == null || subject.isBlank())
            throw new ValidationException("The email subject cannot be null or blank");
    }
}

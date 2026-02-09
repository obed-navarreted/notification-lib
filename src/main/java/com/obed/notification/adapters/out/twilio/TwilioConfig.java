package com.obed.notification.adapters.out.twilio;

public record TwilioConfig(
        String accountSid,
        String authToken,
        String fromPhoneNumber
) {
    public TwilioConfig {
        if (accountSid == null || authToken == null)
            throw new IllegalArgumentException("Twilio credentials cannot be null");
    }
}

package com.obed.notification;

import com.obed.notification.adapters.out.fcm.FCMAdapter;
import com.obed.notification.adapters.out.sendgrid.SendGridConfig;
import com.obed.notification.adapters.out.sendgrid.SendGridEmailAdapter;
import com.obed.notification.adapters.out.twilio.TwilioConfig;
import com.obed.notification.adapters.out.twilio.TwilioSmsAdapter;
import com.obed.notification.domain.model.EmailNotification;
import com.obed.notification.domain.model.PushNotification;
import com.obed.notification.domain.model.SmsNotification;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

class MainTest {
    @Test
    void manualIntegrationTest() {
        SendGridConfig sendGridConfig = new SendGridConfig("SG.xxxxx", "sender_email@emil.com");
        TwilioConfig twilioConfig = new TwilioConfig("ACxxxxxxxxxxxxxxxx", "uth_token", "+1234567890");

        NotificationClient client = NotificationClient.builder()
                .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                .registerProvider(new SendGridEmailAdapter(sendGridConfig))
                .registerProvider(new TwilioSmsAdapter(twilioConfig))
                .registerProvider(new FCMAdapter())
                .build();

        var email = new EmailNotification("obed@example.com", "Hello World", "This is a test", List.of());
        var sms = new SmsNotification("+50588888888", "Hola Obed!");
        var push = new PushNotification(UUID.randomUUID().toString(), "Title", "Body",
                Map.of("key1", "value1", "key2", "value2"));

        System.out.println("--- Sync Test ---");
        client.send(email);
        client.send(sms);
        client.send(push);
        System.out.println("--- Sync Test Completed ---");


        System.out.println("\n--- Async Test ---");
        var errorEmail = new EmailNotification("aasd@gmail.com", "Hello World", "This is a test", List.of());
        client.sendAsync(errorEmail).thenRun(() -> System.out.println("Async Test ---"))
                .exceptionally(ex -> {
                    System.err.println("@@@ Failed to send async email: " + ex.getMessage());
                    return null;
                });
        client.sendAsync(sms);
        client.sendAsync(push);
        System.out.println("--- Async Test Completed ---");
    }
}
package com.obed.notification;

import com.obed.notification.domain.exception.DeliveryException;
import com.obed.notification.domain.exception.ValidationException;
import com.obed.notification.domain.model.EmailNotification;
import com.obed.notification.domain.model.Notification;
import com.obed.notification.domain.model.PushNotification;
import com.obed.notification.domain.model.SmsNotification;
import com.obed.notification.ports.out.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {
    @Mock
    private NotificationPort<EmailNotification> emailProvider;
    @Mock
    private NotificationPort<SmsNotification> smsProvider;
    @Mock
    private NotificationPort<PushNotification> pushProvider;

    private NotificationClient client;

    @BeforeEach
    void setUp() {
        lenient().when(emailProvider.supports()).thenReturn(EmailNotification.class);
        lenient().when(smsProvider.supports()).thenReturn(SmsNotification.class);
        lenient().when(pushProvider.supports()).thenReturn(PushNotification.class);

        client = NotificationClient.builder()
                .registerProvider(emailProvider)
                .registerProvider(smsProvider)
                .registerProvider(pushProvider)
                .withExecutor(Executors.newSingleThreadExecutor())
                .build();
    }

    @Nested
    @DisplayName("Domain Validation Tests")
    class DomainValidationTests {

        @Test
        @DisplayName("EmailNotification: Must fail if recipient email is invalid")
        void shouldThrowExceptionForInvalidEmail() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new EmailNotification("correo-sin-arroba", "Subject", "Body", Collections.emptyList())
            );
            assertTrue(exception.getMessage().contains("Invalid email format for recipient:"));
        }

        @Test
        @DisplayName("EmailNotification: Must fail if subject is null or blank")
        void shouldThrowExceptionForNullSubject() {
            assertThrows(ValidationException.class, () ->
                    new EmailNotification("test@valid.com", null, "Body", Collections.emptyList())
            );
        }

        @Test
        @DisplayName("SmsNotification: Must fail if phone number is not in E.164 format")
        void shouldThrowExceptionForInvalidPhone() {
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    new SmsNotification("0505-8888", "Hola") // Falta el +
            );
            assertTrue(exception.getMessage().contains("E.164"));
        }
    }

    @Nested
    @DisplayName("Routing Tests, Strategy Pattern Verification")
    class RoutingTests {

        @Test
        @DisplayName("The app must route EmailNotification only to the email provider")
        void shouldRouteEmailCorrectly() {
            var email = new EmailNotification("test@test.com", "Sub", "Body", List.of());

            client.send(email);

            verify(emailProvider, times(1)).send(email);
            verify(smsProvider, never()).send(any());
            verify(pushProvider, never()).send(any());
        }

        @Test
        @DisplayName("The app must route SmsNotification only to the SMS provider")
        void shouldRouteSmsCorrectly() {
            var sms = new SmsNotification("+50588888888", "Hola mundo");

            client.send(sms);

            verify(smsProvider, times(1)).send(sms);
            verify(emailProvider, never()).send(any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Throw ValidationException if no provider is registered for the notification type")
        void shouldThrowWhenNoProviderRegistered() {
            var emptyClient = NotificationClient.builder().build();
            var email = new EmailNotification("test@test.com", "S", "B", List.of());

            assertThrows(ValidationException.class, () -> emptyClient.send(email));
        }

        @Test
        @DisplayName("Should wrap provider exceptions in a DeliveryException with a clear message")
        void shouldWrapProviderErrors() {
            var email = new EmailNotification("fail@test.com", "S", "B", List.of());

            doThrow(new RuntimeException("Connection Refused 500"))
                    .when(emailProvider).send(email);

            DeliveryException ex = assertThrows(DeliveryException.class, () -> client.send(email));

            assertEquals("Failed to send notification via " + emailProvider.getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Async Tests")
    class AsyncTests {

        @Test
        @DisplayName("sendAsync must send notifications without blocking and complete successfully")
        void shouldSendAsyncSuccessfully() {
            var push = new PushNotification("token123", "Title", "Body", Collections.emptyMap());

            CompletableFuture<Void> future = client.sendAsync(push);
            assertDoesNotThrow(future::join);
            verify(pushProvider, times(1)).send(push);
        }

        @Test
        @DisplayName("sendAsync must handle exceptions thrown by the provider and complete exceptionally")
        void shouldHandleAsyncErrors() {
            var sms = new SmsNotification("+50588888888", "Fail");

            doThrow(new RuntimeException("Async Error")).when(smsProvider).send(sms);
            CompletableFuture<Void> future = client.sendAsync(sms);
            ExecutionException ex = assertThrows(ExecutionException.class, future::get);
            assertInstanceOf(DeliveryException.class, ex.getCause());
        }
    }

    @Nested
    @DisplayName("Batch Sending Tests")
    class BatchTests {

        @Test
        @DisplayName("sendAll must send all notifications synchronously and route them to the correct providers")
        void shouldSendAllSynchronously() {
            var email = new EmailNotification("batch@test.com", "Sub", "Body", Collections.emptyList());
            var sms = new SmsNotification("+50588888888", "Batch SMS");
            var push = new PushNotification("token123", "Title", "Body", Collections.emptyMap());

            List<Notification> batch = List.of(email, sms, push);

            client.sendAll(batch);

            verify(emailProvider, times(1)).send(email);
            verify(smsProvider, times(1)).send(sms);
            verify(pushProvider, times(1)).send(push);
        }

        @Test
        @DisplayName("sendAllAsync must send all notifications asynchronously and complete successfully")
        void shouldSendAllAsynchronously() {
            var email1 = new EmailNotification("user1@test.com", "Sub", "Body", Collections.emptyList());
            var email2 = new EmailNotification("user2@test.com", "Sub", "Body", Collections.emptyList());
            var sms = new SmsNotification("+50588888888", "Async Batch");

            List<Notification> batch = List.of(email1, email2, sms);

            CompletableFuture<Void> future = client.sendAllAsync(batch);

            assertDoesNotThrow(future::join);

            verify(emailProvider, times(1)).send(email1);
            verify(emailProvider, times(1)).send(email2);
            verify(smsProvider, times(1)).send(sms);
        }

        @Test
        @DisplayName("sendAllAsync must handle empty batch without errors and complete successfully")
        void shouldHandleEmptyBatchAsync() {
            List<Notification> emptyBatch = Collections.emptyList();

            CompletableFuture<Void> future = client.sendAllAsync(emptyBatch);

            assertDoesNotThrow(future::join);
            assertTrue(future.isDone());
        }
    }
}
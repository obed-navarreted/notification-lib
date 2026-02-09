package com.obed.notification;

import com.obed.notification.domain.exception.DeliveryException;
import com.obed.notification.domain.exception.ValidationException;
import com.obed.notification.domain.model.Notification;
import com.obed.notification.ports.in.SendNotificationUseCase;
import com.obed.notification.ports.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationClient implements SendNotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final ExecutorService executor;
    private final List<NotificationPort<?>> providers;


    private NotificationClient(ExecutorService executor, List<NotificationPort<?>> providers) {
        this.executor = executor;
        this.providers = providers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void send(Notification notification) {
        NotificationPort<Notification> provider = (NotificationPort<Notification>) providers.stream()
                .filter(p -> p.supports().isInstance(notification))
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                        "No provider registered for notification type: " + notification.getClass().getSimpleName()));

        log.debug("Dispatching {} to provider: {}", notification.getClass().getSimpleName(), provider.getClass().getSimpleName());

        try {
            provider.send(notification);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send notification via {}", provider.getClass().getSimpleName(), e);
            throw new DeliveryException("Failed to send notification via " + provider.getClass().getSimpleName());
        }
    }

    public CompletableFuture<Void> sendAsync(Notification notification) {
        return CompletableFuture.runAsync(() -> {
                    log.debug("Async sending started in thread: {}", Thread.currentThread().getName());
                    this.send(notification);
                }, this.executor);
    }

    public void sendAll(List<Notification> notifications) {
        notifications.forEach(this::send);
    }

    public CompletableFuture<Void> sendAllAsync(List<Notification> notifications) {
        List<CompletableFuture<Void>> futures = notifications.stream()
                .map(this::sendAsync)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<NotificationPort<?>> providers = new ArrayList<>();
        private ExecutorService executor;

        public Builder withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder registerProvider(NotificationPort<?> provider) {
            this.providers.add(provider);
            return this;
        }

        public NotificationClient build() {
            if (executor == null) {
                executor = Executors.newFixedThreadPool(10);
            }
            return new NotificationClient(executor, providers);
        }
    }
}

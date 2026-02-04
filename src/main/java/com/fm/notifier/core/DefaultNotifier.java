package com.fm.notifier.core;

import com.fm.notifier.api.*;
import com.fm.notifier.channel.NotificationChannel;
import com.fm.notifier.exception.ValidationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


public final class DefaultNotifier implements Notifier {

    private final Map<String, NotificationChannel> channels;

    public DefaultNotifier(Map<String, NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new ValidationException("Al menos un canal debe ser registrado");
        }
        this.channels = Map.copyOf(channels);
    }

    @Override
    public NotificationResult notify(Notification notification) {

        if (notification == null) {
            return NotificationResult.failure(
                    NotificationStatus.VALIDATION_FAILED,
                    new NotificationError("NULL_NOTIFICATION", "Notification must not be null")
            ).build();
        }

        NotificationChannel channel = channels.values()
                .stream()
                .filter(c -> c.supports(notification))
                .findFirst()
                .orElse(null);

        if (channel == null) {
            return NotificationResult.failure(NotificationStatus.VALIDATION_FAILED,
                    new NotificationError("NO_CHANNEL_FOUND", "No channel supports this notification")
            ).build();
        }

        return channel.send(notification);
    }

    @Override
    public CompletableFuture<NotificationResult> notifyAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> notify(notification));
    }
}



package com.fm.notifier.channel;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationResult;

import java.util.concurrent.CompletableFuture;

public interface NotificationChannel {
    /**
     * Identificador lógico del canal (EMAIL, SMS, PUSH, etc.)
     */
    String getChannelName();

    /**
     * Verifica si la notificación es compatible con este canal.
     * No lanza excepción, solo indica compatibilidad.
     */
    boolean supports(Notification notification);

    /**
     * Envío síncrono.
     */
    NotificationResult send(Notification notification);

    /**
     * Envío asíncrono.
     * Implementación por defecto para no duplicar lógica.
     */
    default CompletableFuture<NotificationResult> sendAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> send(notification));
    }}

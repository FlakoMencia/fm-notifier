package com.fm.notifier.api;

import java.util.concurrent.CompletableFuture;

public interface Notifier {

    /**
     * Envía una notificación de forma síncrona.
     * El {@code Notifier} seleccionará el {@link com.fm.notifier.channel.NotificationChannel canal} apropiado
     * basándose en la metadata de la notificación.
     *
     * @param notification La {@link com.fm.notifier.api.Notification notificación} a enviar. No debe ser {@code null}.
     * @return Un {@link com.fm.notifier.api.NotificationResult} que contiene el estado del envío (éxito o fallo)
     *         y detalles adicionales como el canal utilizado o el error, si lo hubo.
     */
    NotificationResult notify(Notification notification);

    /**
     * Envía una notificación de forma asíncrona.
     * El procesamiento de la notificación se realiza en un hilo separado, devolviendo
     * inmediatamente un {@link java.util.concurrent.CompletableFuture} que se completará con el
     * {@link com.fm.notifier.api.NotificationResult resultado} cuando el envío haya finalizado.
     *
     * @param notification La {@link com.fm.notifier.api.Notification notificación} a enviar. No debe ser {@code null}.
     * @return Un {@link java.util.concurrent.CompletableFuture} que se resolverá con el {@link com.fm.notifier.api.NotificationResult}
     *         del envío de la notificación.
     */
    CompletableFuture<NotificationResult> notifyAsync(Notification notification);

    /**
     * Crea y devuelve una nueva instancia de {@link com.fm.notifier.api.NotifierBuilder} para configurar
     * y construir un {@link Notifier}. Este es el punto de entrada recomendado para
     * obtener una implementación de {@code Notifier}.
     *
     * @return Una nueva instancia de {@link com.fm.notifier.api.NotifierBuilder}.
     */
    static NotifierBuilder builder() {
        return new NotifierBuilder();
    }
}

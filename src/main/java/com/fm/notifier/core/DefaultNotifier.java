package com.fm.notifier.core;

import com.fm.notifier.api.*;
import com.fm.notifier.channel.NotificationChannel;
import com.fm.notifier.exception.ValidationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * {@code DefaultNotifier} es la implementación por defecto de la interfaz {@link com.fm.notifier.api.Notifier}.
 * Se encarga de recibir notificaciones, determinar el canal de envío adecuado y delegar
 * la operación de envío a dicho canal.
 * <p>
 * Esta clase central actúa como un orquestador, aplicando el patrón de diseño "Strategy"
 * al seleccionar dinámicamente un {@link com.fm.notifier.channel.NotificationChannel}
 * basado en la metadata de la {@link com.fm.notifier.api.Notification}.
 * También se adhiere a los principios SOLID, especialmente el de "Responsabilidad Única" (SRP)
 * al enfocarse solo en el enrutamiento y la delegación, y el de "Abierto/Cerrado" (OCP)
 * al permitir añadir nuevos canales de notificación sin modificar esta clase.
 * </p>
 *
 * <h3>Configuración y Uso:</h3>
 * <pre>{@code
 * // 1. Configurar los canales de notificación disponibles
 * Map<String, NotificationChannel> availableChannels = new HashMap<>();
 * availableChannels.put("email", new EmailChannel());
 * availableChannels.put("sms", new SmsChannel());
 * availableChannels.put("push", new PushNotificationChannel());
 *
 * // 2. Construir el Notifier usando el Builder
 * Notifier notifier = Notifier.builder()
 *                             .withChannels(availableChannels)
 *                             .build();
 *
 * // 3. Crear una notificación de ejemplo (ej. para email)
 * Notification emailNotification = Notification.builder()
 *                                             .withRecipient("recipient@example.com")
 *                                             .withSubject("¡Hola desde fm-notifier!")
 *                                             .withBody("Este es el cuerpo del email.")
 *                                             .withMetadata("channel", "email") // Importante para el enrutamiento
 *                                             .build();
 *
 * // 4. Enviar la notificación de forma síncrona
 * NotificationResult result = notifier.notify(emailNotification);
 *
 * if (result.isSuccess()) {
 *     System.out.println("Notificación enviada con éxito por el canal: " + result.getChannel());
 * } else {
 *     System.err.println("Error al enviar notificación: " + result.getError().getMessage());
 * }
 *
 * // Para envío asíncrono:
 * Notification smsNotification = Notification.builder()
 *                                            .withRecipient("+1234567890")
 *                                            .withBody("Mensaje SMS de prueba.")
 *                                            .withMetadata("channel", "sms")
 *                                            .build();
 *
 * notifier.notifyAsync(smsNotification)
 *         .thenAccept(asyncResult -> {
 *             if (asyncResult.isSuccess()) {
 *                 System.out.println("SMS enviado asíncronamente por el canal: " + asyncResult.getChannel());
 *             } else {
 *                 System.err.println("Error asíncrono al enviar SMS: " + asyncResult.getError().getMessage());
 *             }
 *         });
 * }</pre>
 *
 * @author El Flako Creador
 * @version 0.1.0
 * @see com.fm.notifier.api.Notifier
 * @see com.fm.notifier.api.Notification
 * @see com.fm.notifier.channel.NotificationChannel
 * @see com.fm.notifier.api.NotifierBuilder
 * @see com.fm.notifier.exception.ValidationException
 */
public final class DefaultNotifier implements Notifier {

    private final Map<String, NotificationChannel> channels;

    /**
     * Construye una nueva instancia de {@code DefaultNotifier} con los canales de notificación proporcionados.
     * Los canales se utilizarán para determinar cómo y a través de qué medio se enviará una notificación.
     *
     * @param channels Un mapa donde la clave es el nombre del canal (String) y el valor es la
     *                 implementación de {@link com.fm.notifier.channel.NotificationChannel} correspondiente.
     *                 Debe contener al menos un canal y no puede ser {@code null}.
     * @throws com.fm.notifier.exception.ValidationException Si el mapa de canales es {@code null} o está vacío, indicando
     *                             que el {@code Notifier} no puede operar sin canales configurados.
     */
    public DefaultNotifier(Map<String, NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new ValidationException("Al menos un canal debe ser registrado");
        }
        this.channels = Map.copyOf(channels);
    }

    /**
     * Procesa y envía una {@link com.fm.notifier.api.Notification notificación} de forma síncrona.
     * El método busca en los canales registrados aquel que {@link com.fm.notifier.channel.NotificationChannel#supports(Notification) soporte}
     * la notificación dada (generalmente por su metadata). Si se encuentra un canal,
     * la notificación se envía a través de él. Si la notificación es {@code null}
     * o no se encuentra ningún canal compatible, se devuelve un resultado de fallo.
     *
     * @param notification La {@link com.fm.notifier.api.Notification notificación} a ser enviada.
     * @return Un {@link com.fm.notifier.api.NotificationResult} que detalla el resultado del intento de envío.
     *         Contendrá éxito/fallo, el canal utilizado (si aplica), el proveedor (simulado),
     *         y un ID externo si fue exitoso, o un {@link com.fm.notifier.api.NotificationError} si falló.
     */
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

    /**
     * Procesa y envía una {@link com.fm.notifier.api.Notification notificación} de forma asíncrona.
     * Similar al método {@link #notify(Notification)} síncrono, pero la operación de envío
     * se ejecuta en un hilo separado, devolviendo un {@link java.util.concurrent.CompletableFuture} inmediatamente.
     * El resultado final del envío estará disponible cuando el {@code CompletableFuture} se complete.
     * Esto es útil para operaciones que no deben bloquear el hilo principal.
     *
     * @param notification La {@link com.fm.notifier.api.Notification notificación} a ser enviada asíncronamente.
     * @return Un {@link java.util.concurrent.CompletableFuture} que se completará con el {@link com.fm.notifier.api.NotificationResult}
     *         del intento de envío cuando este finalice.
     */
    @Override
    public CompletableFuture<NotificationResult> notifyAsync(Notification notification) {
        return CompletableFuture.supplyAsync(() -> notify(notification));
    }
}

package com.fm.notifier.channel.push;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationError;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import com.fm.notifier.channel.NotificationChannel;

import java.util.UUID;

public final class PushNotificationChannel implements NotificationChannel {

    private static final String CHANNEL_NAME = "push";
    private static final String METADATA_CHANNEL_KEY = "channel";
    private static final String DEVICE_TOKEN_KEY = "deviceToken";

    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
    }

    @Override
    public boolean supports(Notification notification) {
        Object channel = notification.getMetadata().get(METADATA_CHANNEL_KEY);
        return CHANNEL_NAME.equalsIgnoreCase(String.valueOf(channel));
    }

    @Override
    public NotificationResult send(Notification notification) {

        //Validar token
        String deviceToken = extractDeviceToken(notification);
        if (deviceToken == null || deviceToken.isBlank()) {
            return NotificationResult.failure(
                            NotificationStatus.VALIDATION_FAILED,
                            new NotificationError("MISSING_DEVICE_TOKEN",
                                    "Push notification requires a device token")
                    ).channel(CHANNEL_NAME)
                    .build();
        }

        //Validar contenido
        if (notification.getBody() == null || notification.getBody().isBlank()) {
            return NotificationResult.failure(
                            NotificationStatus.VALIDATION_FAILED,
                            new NotificationError("EMPTY_BODY",
                                    "Push notification body must not be empty")
                    ).channel(CHANNEL_NAME)
                    .build();
        }

        //Simulador
        try {
            simulatePushSend(notification, deviceToken);

            return NotificationResult
                    .success(CHANNEL_NAME, "SIMULATED_PUSH_PROVIDER")
                    .externalId(UUID.randomUUID().toString())
                    .build();

        } catch (Exception ex) {
            return NotificationResult.failure(
                            NotificationStatus.SEND_FAILED,
                            new NotificationError("PUSH_SEND_ERROR", ex.getMessage())
                    ).channel(CHANNEL_NAME)
                    .build();
        }
    }

    private String extractDeviceToken(Notification notification) {
        Object token = notification.getMetadata().get(DEVICE_TOKEN_KEY);
        return token != null ? token.toString() : null;
    }

    private void simulatePushSend(Notification notification, String deviceToken) {
        System.out.println("[PUSH] Sending push to token=" + deviceToken
                + " | message=" + notification.getBody()
        );
    }
}

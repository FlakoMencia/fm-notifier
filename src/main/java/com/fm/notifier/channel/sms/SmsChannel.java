package com.fm.notifier.channel.sms;

import com.fm.notifier.api.*;
import com.fm.notifier.channel.NotificationChannel;

import java.util.UUID;
import java.util.regex.Pattern;

public final class SmsChannel implements NotificationChannel {

    private static final String CHANNEL_NAME = "sms";
    private static final String METADATA_CHANNEL_KEY = "channel";

    // Regex simple para teléfonos internacionales
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{7,14}$");

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

        //Validar telefono
        if (!isValidPhone(notification.getRecipient())) {
            return NotificationResult.failure(NotificationStatus.VALIDATION_FAILED,
                            new NotificationError("INVALID_PHONE", "Recipient is not a valid phone number")
                    ).channel(CHANNEL_NAME)
                    .build();
        }

        //Validar body
        if (notification.getBody() == null || notification.getBody().isBlank()) {
            return NotificationResult.failure(NotificationStatus.VALIDATION_FAILED,
                            new NotificationError("EMPTY_BODY", "SMS body must not be empty")
                    ).channel(CHANNEL_NAME)
                    .build();
        }

        //Simulador
        try {
            simulateSmsSend(notification);

            return NotificationResult
                    .success(CHANNEL_NAME, "SIMULATED_SMS_PROVIDER")
                    .externalId(UUID.randomUUID().toString())
                    .build();

        } catch (Exception ex) {
            return NotificationResult.failure(NotificationStatus.SEND_FAILED,
                            new NotificationError("SMS_SEND_ERROR", ex.getMessage())
                    ).channel(CHANNEL_NAME)
                    .build();
        }
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private void simulateSmsSend(Notification notification) {
        System.out.println("[SMS] Sending SMS to " + notification.getRecipient()
                + " | message=" + notification.getBody()
        );
    }
}

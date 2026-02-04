package com.fm.notifier.channel.email;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationError;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import com.fm.notifier.channel.NotificationChannel;

import java.util.UUID;
import java.util.regex.Pattern;

public final class EmailChannel implements NotificationChannel {
    private static final String CHANNEL_NAME = "email";
    private static final String METADATA_CHANNEL_KEY = "channel";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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

        if (!isValidEmail(notification.getRecipient())) {
            return NotificationResult.failure(NotificationStatus.VALIDATION_FAILED,
                            new NotificationError("INVALID_EMAIL", "Recipient is not a valid email address"))
                            .channel(CHANNEL_NAME)
                            .build();
        }

        // Subject requerido
        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            return NotificationResult.failure(NotificationStatus.VALIDATION_FAILED,
                                                new NotificationError("MISSING_SUBJECT","Email subject is required" ))
                                                .channel(CHANNEL_NAME)
                                                .build();
        }

        // Simulación
        try {
            simulateEmailSend(notification);

            return NotificationResult
                    .success(CHANNEL_NAME, "SIMULATED_EMAIL_PROVIDER")
                    .externalId(UUID.randomUUID().toString())
                    .build();

        } catch (Exception ex) {
            return NotificationResult.failure(NotificationStatus.SEND_FAILED,
                            new NotificationError("EMAIL_SEND_ERROR", ex.getMessage()))
                            .channel(CHANNEL_NAME)
                            .build();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private void simulateEmailSend(Notification notification) {
        System.out.println( "[EMAIL] Sending email to " + notification.getRecipient() + " | subject=" + notification.getSubject()
        );
    }
}

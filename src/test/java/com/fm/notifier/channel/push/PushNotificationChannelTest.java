package com.fm.notifier.channel.push;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationChannelTest {

    private PushNotificationChannel pushNotificationChannel;

    @Mock
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        pushNotificationChannel = new PushNotificationChannel();
    }

    @Test
    @DisplayName("Should return failure for missing or blank device token")
    void testSendMissingDeviceToken() {
        when(mockNotification.getMetadata()).thenReturn(new HashMap<>()); // No device token
        when(mockNotification.getBody()).thenReturn("Test message"); // Body is required

        NotificationResult result = pushNotificationChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("MISSING_DEVICE_TOKEN", result.getError().getCode());
        assertEquals("Push notification requires a device token", result.getError().getMessage());
        assertEquals("push", result.getChannel());

        Map<String, Object> metadataWithBlankToken = new HashMap<>();
        metadataWithBlankToken.put("deviceToken", "   "); // Blank device token
        when(mockNotification.getMetadata()).thenReturn(metadataWithBlankToken);

        result = pushNotificationChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("MISSING_DEVICE_TOKEN", result.getError().getCode());
        assertEquals("Push notification requires a device token", result.getError().getMessage());
        assertEquals("push", result.getChannel());
    }

    @Test
    @DisplayName("Should return failure for empty or blank body")
    void testSendEmptyBody() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "validDeviceToken");
        when(mockNotification.getMetadata()).thenReturn(metadata);
        when(mockNotification.getBody()).thenReturn(null); // Empty body

        NotificationResult result = pushNotificationChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("EMPTY_BODY", result.getError().getCode());
        assertEquals("Push notification body must not be empty", result.getError().getMessage());
        assertEquals("push", result.getChannel());

        when(mockNotification.getBody()).thenReturn("   "); // Blank body
        result = pushNotificationChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("EMPTY_BODY", result.getError().getCode());
        assertEquals("Push notification body must not be empty", result.getError().getMessage());
        assertEquals("push", result.getChannel());
    }

    @Test
    @DisplayName("Should successfully send push notification with valid device token and body")
    void testSendSuccess() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "validDeviceToken");
        metadata.put("channel", "push");
        when(mockNotification.getMetadata()).thenReturn(metadata);
        when(mockNotification.getBody()).thenReturn("Hello, push notification!");

        NotificationResult result = pushNotificationChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals("push", result.getChannel());
        assertEquals("SIMULATED_PUSH_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
    }

    @Test
    @DisplayName("Should support notifications with 'push' channel in metadata")
    void testSupportsPushChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "push");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(pushNotificationChannel.supports(mockNotification));
    }

    @Test
    @DisplayName("Should not support notifications without 'push' channel in metadata")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "email");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(pushNotificationChannel.supports(mockNotification));

        when(mockNotification.getMetadata()).thenReturn(Collections.emptyMap());
        assertFalse(pushNotificationChannel.supports(mockNotification));
    }
}

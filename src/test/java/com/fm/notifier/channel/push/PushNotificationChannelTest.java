package com.fm.notifier.channel.push;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PushNotificationChannelTest {

    private PushNotificationChannel pushChannel;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        pushChannel = new PushNotificationChannel();
        mockNotification = Mockito.mock(Notification.class);
    }

    @Test
    @DisplayName("supports() should return true for 'push' channel")
    void testSupportsPushChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "push");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(pushChannel.supports(mockNotification), "Debería soportar el canal 'push'.");
    }

    @Test
    @DisplayName("supports() should return false for other channels")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "sms");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(pushChannel.supports(mockNotification), "No debería soportar canales diferentes a 'push'.");
    }

    @Test
    @DisplayName("send() should fail for missing or blank device token")
    void testSendMissingDeviceToken() {
        when(mockNotification.getBody()).thenReturn("Test message");

        // Test with missing device token
        when(mockNotification.getMetadata()).thenReturn(Collections.emptyMap());
        NotificationResult resultMissing = pushChannel.send(mockNotification);

        assertNotNull(resultMissing);
        assertFalse(resultMissing.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultMissing.getStatus());
        assertEquals("MISSING_DEVICE_TOKEN", resultMissing.getError().getCode());

        // Test with blank device token
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "   ");
        when(mockNotification.getMetadata()).thenReturn(metadata);
        NotificationResult resultBlank = pushChannel.send(mockNotification);

        assertNotNull(resultBlank);
        assertFalse(resultBlank.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultBlank.getStatus());
        assertEquals("MISSING_DEVICE_TOKEN", resultBlank.getError().getCode());
    }

    @Test
    @DisplayName("send() should fail for empty or blank body")
    void testSendEmptyBody() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "valid-token");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        // Test with null body
        when(mockNotification.getBody()).thenReturn(null);
        NotificationResult resultNull = pushChannel.send(mockNotification);

        assertNotNull(resultNull);
        assertFalse(resultNull.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultNull.getStatus());
        assertEquals("EMPTY_BODY", resultNull.getError().getCode());

        // Test with blank body
        when(mockNotification.getBody()).thenReturn("   ");
        NotificationResult resultBlank = pushChannel.send(mockNotification);

        assertNotNull(resultBlank);
        assertFalse(resultBlank.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultBlank.getStatus());
        assertEquals("EMPTY_BODY", resultBlank.getError().getCode());
    }

    @Test
    @DisplayName("send() should succeed with valid device token and body")
    void testSendSuccess() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deviceToken", "valid-token-123");
        when(mockNotification.getMetadata()).thenReturn(metadata);
        when(mockNotification.getBody()).thenReturn("Hello from push!");

        NotificationResult result = pushChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals(pushChannel.getChannelName(), result.getChannel());
        assertEquals("SIMULATED_PUSH_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
        assertNull(result.getError());
    }
}

package com.fm.notifier.channel.sms;

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
class SmsChannelTest {

    private SmsChannel smsChannel;

    @Mock
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        smsChannel = new SmsChannel();
    }

    @Test
    @DisplayName("Should return failure for invalid phone number recipient")
    void testSendInvalidRecipient() {
        when(mockNotification.getRecipient()).thenReturn("invalid-phone");
        when(mockNotification.getBody()).thenReturn("Test message"); // Body is required
        when(mockNotification.getMetadata()).thenReturn(new HashMap<>()); // Mock metadata to avoid NPE

        NotificationResult result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("INVALID_PHONE", result.getError().getCode());
        assertEquals("Recipient is not a valid phone number", result.getError().getMessage());
        assertEquals("sms", result.getChannel());
    }

    @Test
    @DisplayName("Should return failure for empty or blank body")
    void testSendEmptyBody() {
        when(mockNotification.getRecipient()).thenReturn("+1234567890");
        when(mockNotification.getBody()).thenReturn(null); // Empty body
        when(mockNotification.getMetadata()).thenReturn(new HashMap<>()); // Mock metadata to avoid NPE

        NotificationResult result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("EMPTY_BODY", result.getError().getCode());
        assertEquals("SMS body must not be empty", result.getError().getMessage());
        assertEquals("sms", result.getChannel());

        when(mockNotification.getBody()).thenReturn("   "); // Blank body
        result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("EMPTY_BODY", result.getError().getCode());
        assertEquals("SMS body must not be empty", result.getError().getMessage());
        assertEquals("sms", result.getChannel());
    }

    @Test
    @DisplayName("Should successfully send SMS with valid recipient and body")
    void testSendSuccess() {
        when(mockNotification.getRecipient()).thenReturn("+1234567890");
        when(mockNotification.getBody()).thenReturn("Hello, world!");
        when(mockNotification.getMetadata()).thenReturn(Collections.singletonMap("channel", "sms"));

        NotificationResult result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals("sms", result.getChannel());
        assertEquals("SIMULATED_SMS_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
    }

    @Test
    @DisplayName("Should support notifications with 'sms' channel in metadata")
    void testSupportsSmsChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "sms");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(smsChannel.supports(mockNotification));
    }

    @Test
    @DisplayName("Should not support notifications without 'sms' channel in metadata")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "email");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(smsChannel.supports(mockNotification));

        when(mockNotification.getMetadata()).thenReturn(Collections.emptyMap());
        assertFalse(smsChannel.supports(mockNotification));
    }
}

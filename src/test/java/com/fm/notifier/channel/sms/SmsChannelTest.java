package com.fm.notifier.channel.sms;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SmsChannelTest {

    private SmsChannel smsChannel;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        smsChannel = new SmsChannel();
        mockNotification = Mockito.mock(Notification.class);
    }

    @Test
    @DisplayName("supports() should return true for 'sms' channel")
    void testSupportsSmsChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "sms");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(smsChannel.supports(mockNotification), "Debería soportar el canal 'sms'.");
    }

    @Test
    @DisplayName("supports() should return false for other channels")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "email");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(smsChannel.supports(mockNotification), "No debería soportar canales diferentes a 'sms'.");
    }

    @Test
    @DisplayName("send() should fail for invalid phone number recipient")
    void testSendInvalidRecipient() {
        when(mockNotification.getRecipient()).thenReturn("invalid-phone");
        when(mockNotification.getBody()).thenReturn("Test message");

        NotificationResult result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("INVALID_PHONE", result.getError().getCode());
        assertEquals("Recipient is not a valid phone number", result.getError().getMessage());
        assertEquals(smsChannel.getChannelName(), result.getChannel());
    }

    @Test
    @DisplayName("send() should fail for empty or blank body")
    void testSendEmptyBody() {
        when(mockNotification.getRecipient()).thenReturn("+1234567890");

        // Test with null body
        when(mockNotification.getBody()).thenReturn(null);
        NotificationResult resultNullBody = smsChannel.send(mockNotification);

        assertNotNull(resultNullBody);
        assertFalse(resultNullBody.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultNullBody.getStatus());
        assertEquals("EMPTY_BODY", resultNullBody.getError().getCode());

        // Test with blank body
        when(mockNotification.getBody()).thenReturn("   ");
        NotificationResult resultBlankBody = smsChannel.send(mockNotification);

        assertNotNull(resultBlankBody);
        assertFalse(resultBlankBody.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultBlankBody.getStatus());
        assertEquals("EMPTY_BODY", resultBlankBody.getError().getCode());
    }

    @Test
    @DisplayName("send() should succeed with valid recipient and body")
    void testSendSuccess() {
        when(mockNotification.getRecipient()).thenReturn("+1234567890");
        when(mockNotification.getBody()).thenReturn("Hello, world!");

        NotificationResult result = smsChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals(smsChannel.getChannelName(), result.getChannel());
        assertEquals("SIMULATED_SMS_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
        assertNull(result.getError());
    }
}

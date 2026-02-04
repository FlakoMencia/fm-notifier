package com.fm.notifier.channel.email;

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

class EmailChannelTest {

    private EmailChannel emailChannel;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        emailChannel = new EmailChannel();
        mockNotification = Mockito.mock(Notification.class);
    }

    @Test
    @DisplayName("supports() should return true for 'email' channel")
    void testSupportsEmailChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "email");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(emailChannel.supports(mockNotification), "Debería soportar el canal 'email'.");
    }

    @Test
    @DisplayName("supports() should return false for other channels")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "sms");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(emailChannel.supports(mockNotification), "No debería soportar canales diferentes a 'email'.");
    }

    @Test
    @DisplayName("send() should fail for invalid email recipient")
    void testSendInvalidRecipient() {
        when(mockNotification.getRecipient()).thenReturn("invalid-email");
        when(mockNotification.getSubject()).thenReturn("Test Subject");

        NotificationResult result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("INVALID_EMAIL", result.getError().orElseThrow().getCode());
        assertEquals("Recipient is not a valid email address", result.getError().orElseThrow().getMessage());
        assertEquals(emailChannel.getChannelName(), result.getChannel());
    }

    @Test
    @DisplayName("send() should fail for missing or blank subject")
    void testSendMissingSubject() {
        when(mockNotification.getRecipient()).thenReturn("test@example.com");

        // Test with null subject
        when(mockNotification.getSubject()).thenReturn(null);
        NotificationResult resultNullSubject = emailChannel.send(mockNotification);

        assertNotNull(resultNullSubject);
        assertFalse(resultNullSubject.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultNullSubject.getStatus());
        assertEquals("MISSING_SUBJECT", resultNullSubject.getError().orElseThrow().getCode());

        // Test with blank subject
        when(mockNotification.getSubject()).thenReturn("   ");
        NotificationResult resultBlankSubject = emailChannel.send(mockNotification);

        assertNotNull(resultBlankSubject);
        assertFalse(resultBlankSubject.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, resultBlankSubject.getStatus());
        assertEquals("MISSING_SUBJECT", resultBlankSubject.getError().orElseThrow().getCode());
    }

    @Test
    @DisplayName("send() should succeed with valid recipient and subject")
    void testSendSuccess() {
        when(mockNotification.getRecipient()).thenReturn("valid@example.com");
        when(mockNotification.getSubject()).thenReturn("Success Subject");
        when(mockNotification.getBody()).thenReturn("This is the body.");

        NotificationResult result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SUCCESS, result.getStatus());
        assertEquals(emailChannel.getChannelName(), result.getChannel());
        assertEquals("SIMULATED_EMAIL_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
        assertNull(result.getError());
    }
}

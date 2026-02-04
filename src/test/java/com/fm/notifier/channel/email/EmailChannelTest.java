package com.fm.notifier.channel.email;

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
class EmailChannelTest {

    private EmailChannel emailChannel;

    @Mock
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        emailChannel = new EmailChannel();
    }

    @Test
    @DisplayName("Should return failure for invalid email recipient")
    void testSendInvalidRecipient() {
        when(mockNotification.getRecipient()).thenReturn("invalid-email");
        when(mockNotification.getSubject()).thenReturn("Test Subject"); // Subject is required
        when(mockNotification.getMetadata()).thenReturn(new HashMap<>()); // Mock metadata to avoid NPE

        NotificationResult result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("INVALID_EMAIL", result.getError().getCode());
        assertEquals("Recipient is not a valid email address", result.getError().getMessage());
        assertEquals("email", result.getChannel());
    }

    @Test
    @DisplayName("Should return failure for missing or blank subject")
    void testSendMissingSubject() {
        when(mockNotification.getRecipient()).thenReturn("test@example.com");
        when(mockNotification.getSubject()).thenReturn(null); // Missing subject
        when(mockNotification.getMetadata()).thenReturn(new HashMap<>()); // Mock metadata to avoid NPE

        NotificationResult result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("MISSING_SUBJECT", result.getError().getCode());
        assertEquals("Email subject is required", result.getError().getMessage());
        assertEquals("email", result.getChannel());

        when(mockNotification.getSubject()).thenReturn("   "); // Blank subject
        result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("MISSING_SUBJECT", result.getError().getCode());
        assertEquals("Email subject is required", result.getError().getMessage());
        assertEquals("email", result.getChannel());
    }

    @Test
    @DisplayName("Should successfully send email with valid recipient and subject")
    void testSendSuccess() {
        when(mockNotification.getRecipient()).thenReturn("valid@example.com");
        when(mockNotification.getSubject()).thenReturn("Success Subject");
        when(mockNotification.getMetadata()).thenReturn(Collections.singletonMap("channel", "email")); // Important for supports method

        NotificationResult result = emailChannel.send(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals("email", result.getChannel());
        assertEquals("SIMULATED_EMAIL_PROVIDER", result.getProvider());
        assertNotNull(result.getExternalId());
    }

    @Test
    @DisplayName("Should support notifications with 'email' channel in metadata")
    void testSupportsEmailChannel() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "email");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertTrue(emailChannel.supports(mockNotification));
    }

    @Test
    @DisplayName("Should not support notifications without 'email' channel in metadata")
    void testDoesNotSupportOtherChannels() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channel", "sms");
        when(mockNotification.getMetadata()).thenReturn(metadata);

        assertFalse(emailChannel.supports(mockNotification));

        when(mockNotification.getMetadata()).thenReturn(Collections.emptyMap());
        assertFalse(emailChannel.supports(mockNotification));
    }
}

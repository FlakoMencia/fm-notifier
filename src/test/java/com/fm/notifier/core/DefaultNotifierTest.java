package com.fm.notifier.core;

import com.fm.notifier.api.Notification;
import com.fm.notifier.api.NotificationChannel;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import com.fm.notifier.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNotifierTest {

    private DefaultNotifier defaultNotifier;

    @Mock
    private NotificationChannel mockEmailChannel;
    @Mock
    private NotificationChannel mockSmsChannel;
    @Mock
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        when(mockEmailChannel.getChannelName()).thenReturn("email");
        when(mockSmsChannel.getChannelName()).thenReturn("sms");
    }

    @Test
    @DisplayName("Should throw ValidationException if channels map is null")
    void testConstructorWithNullChannels() {
        assertThrows(ValidationException.class, () -> new DefaultNotifier(null));
    }

    @Test
    @DisplayName("Should throw ValidationException if channels map is empty")
    void testConstructorWithEmptyChannels() {
        assertThrows(ValidationException.class, () -> new DefaultNotifier(Collections.emptyMap()));
    }

    @Test
    @DisplayName("Should handle null notification gracefully")
    void testNotifyNullNotification() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        NotificationResult result = defaultNotifier.notify(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("NULL_NOTIFICATION", result.getError().getCode());
        assertEquals("Notification must not be null", result.getError().getMessage());
    }

    @Test
    @DisplayName("Should return failure if no channel supports the notification")
    void testNotifyUnknownChannel() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockEmailChannel.supports(mockNotification)).thenReturn(false);

        NotificationResult result = defaultNotifier.notify(mockNotification);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("NO_CHANNEL_FOUND", result.getError().getCode());
        assertEquals("No channel supports this notification", result.getError().getMessage());
        verify(mockEmailChannel, times(1)).supports(mockNotification);
        verify(mockEmailChannel, never()).send(any());
    }

    @Test
    @DisplayName("Should successfully notify using a supported channel")
    void testNotifySupportedChannelSuccess() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockEmailChannel.supports(mockNotification)).thenReturn(true);
        when(mockEmailChannel.send(mockNotification)).thenReturn(
                NotificationResult.success("email", "mockProvider").externalId("123").build()
        );

        NotificationResult result = defaultNotifier.notify(mockNotification);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals("email", result.getChannel());
        assertEquals("mockProvider", result.getProvider());
        assertEquals("123", result.getExternalId());
        verify(mockEmailChannel, times(1)).supports(mockNotification);
        verify(mockEmailChannel, times(1)).send(mockNotification);
    }

    @Test
    @DisplayName("Should successfully notify asynchronously using a supported channel")
    void testNotifyAsyncSuccess() throws ExecutionException, InterruptedException {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("sms", mockSmsChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockSmsChannel.supports(mockNotification)).thenReturn(true);
        when(mockSmsChannel.send(mockNotification)).thenReturn(
                NotificationResult.success("sms", "mockProvider").externalId("456").build()
        );

        CompletableFuture<NotificationResult> futureResult = defaultNotifier.notifyAsync(mockNotification);
        NotificationResult result = futureResult.get(); // Wait for the async operation to complete

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.DELIVERED, result.getStatus());
        assertEquals("sms", result.getChannel());
        assertEquals("mockProvider", result.getProvider());
        assertEquals("456", result.getExternalId());
        verify(mockSmsChannel, times(1)).supports(mockNotification);
        verify(mockSmsChannel, times(1)).send(mockNotification);
    }
}

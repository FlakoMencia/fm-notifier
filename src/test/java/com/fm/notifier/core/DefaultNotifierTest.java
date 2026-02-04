package com.fm.notifier.core;

import com.fm.notifier.api.Notification;

import com.fm.notifier.api.NotificationError;
import com.fm.notifier.api.NotificationResult;
import com.fm.notifier.api.NotificationStatus;
import com.fm.notifier.channel.NotificationChannel;
import com.fm.notifier.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultNotifierTest {

    private DefaultNotifier defaultNotifier;
    private NotificationChannel mockEmailChannel;
    private NotificationChannel mockSmsChannel;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        // Inicialización manual de mocks
        mockEmailChannel = Mockito.mock(NotificationChannel.class);
        mockSmsChannel = Mockito.mock(NotificationChannel.class);
        mockNotification = Mockito.mock(Notification.class);

        // Comportamiento común para los mocks de canal
        when(mockEmailChannel.getChannelName()).thenReturn("email");
        when(mockSmsChannel.getChannelName()).thenReturn("sms");
    }

    @Test
    @DisplayName("Constructor should throw ValidationException if channels map is null")
    void testConstructorWithNullChannels() {
        assertThrows(ValidationException.class, () -> new DefaultNotifier(null),
                "El constructor debería lanzar ValidationException para un mapa de canales nulo.");
    }

    @Test
    @DisplayName("Constructor should throw ValidationException if channels map is empty")
    void testConstructorWithEmptyChannels() {
        assertThrows(ValidationException.class, () -> new DefaultNotifier(Collections.emptyMap()),
                "El constructor debería lanzar ValidationException para un mapa de canales vacío.");
    }

    @Test
    @DisplayName("notify() should handle null notification gracefully")
    void testNotifyNullNotification() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        NotificationResult result = defaultNotifier.notify(null);

        assertNotNull(result, "El resultado no debe ser nulo.");
        assertFalse(result.isSuccess(), "El envío debería fallar.");
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus(), "El estado debería ser VALIDATION_FAILED.");
        assertNotNull(result.getError(), "El resultado debería contener un error.");
        assertEquals("NULL_NOTIFICATION", result.getError().orElseThrow().getCode(), "El código de error es incorrecto.");
        assertEquals("Notification must not be null", result.getError().orElseThrow().getMessage(), "El mensaje de error es incorrecto.");
    }

    @Test
    @DisplayName("notify() should return failure if no channel supports the notification")
    void testNotifyUnknownChannel() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockEmailChannel.supports(mockNotification)).thenReturn(false);

        NotificationResult result = defaultNotifier.notify(mockNotification);

        assertNotNull(result, "El resultado no debe ser nulo.");
        assertFalse(result.isSuccess(), "El envío debería fallar.");
        assertEquals(NotificationStatus.VALIDATION_FAILED, result.getStatus(), "El estado debería ser VALIDATION_FAILED.");
        assertNotNull(result.getError(), "El resultado debería contener un error.");
        assertEquals("NO_CHANNEL_FOUND", result.getError().orElseThrow().getCode(), "El código de error es incorrecto.");
        assertEquals("No channel supports this notification", result.getError().orElseThrow().getMessage(), "El mensaje de error es incorrecto.");

        verify(mockEmailChannel, times(1)).supports(mockNotification);
        verify(mockEmailChannel, never()).send(any(Notification.class));
    }

    @Test
    @DisplayName("notify() should successfully notify using a supported channel")
    void testNotifySupportedChannelSuccess() {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("email", mockEmailChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockEmailChannel.supports(mockNotification)).thenReturn(true);
        NotificationResult expectedResult = NotificationResult.success("email", "mockProvider").externalId("123").build();
        when(mockEmailChannel.send(mockNotification)).thenReturn(expectedResult);

        NotificationResult result = defaultNotifier.notify(mockNotification);

        assertNotNull(result, "El resultado no debe ser nulo.");
        assertTrue(result.isSuccess(), "El envío debería ser exitoso.");
        assertEquals(NotificationStatus.SUCCESS, result.getStatus(), "El estado debería ser DELIVERED.");
        assertEquals("email", result.getChannel(), "El nombre del canal es incorrecto.");
        assertEquals("mockProvider", result.getProvider(), "El nombre del proveedor es incorrecto.");
        assertEquals("123", result.getExternalId(), "El ID externo es incorrecto.");
        assertNull(result.getError(), "No debería haber error en un envío exitoso.");

        verify(mockEmailChannel, times(1)).supports(mockNotification);
        verify(mockEmailChannel, times(1)).send(mockNotification);
    }

    @Test
    @DisplayName("notifyAsync() should successfully notify asynchronously using a supported channel")
    void testNotifyAsyncSuccess() throws ExecutionException, InterruptedException {
        Map<String, NotificationChannel> channels = new HashMap<>();
        channels.put("sms", mockSmsChannel);
        defaultNotifier = new DefaultNotifier(channels);

        when(mockSmsChannel.supports(mockNotification)).thenReturn(true);
        NotificationResult expectedResult = NotificationResult.success("sms", "mockSmsProvider").externalId("456").build();
        when(mockSmsChannel.send(mockNotification)).thenReturn(expectedResult);

        CompletableFuture<NotificationResult> futureResult = defaultNotifier.notifyAsync(mockNotification);
        NotificationResult result = futureResult.get(); // Esperar a que la operación asíncrona se complete

        assertNotNull(result, "El resultado no debe ser nulo.");
        assertTrue(result.isSuccess(), "El envío asíncrono debería ser exitoso.");
        assertEquals(NotificationStatus.SUCCESS, result.getStatus(), "El estado debería ser SUCCESS.");
        assertEquals("sms", result.getChannel(), "El nombre del canal es incorrecto.");
        assertEquals("mockSmsProvider", result.getProvider(), "El nombre del proveedor es incorrecto.");
        assertEquals("456", result.getExternalId(), "El ID externo es incorrecto.");
        assertNull(result.getError(), "No debería haber error en un envío asíncrono exitoso.");

        verify(mockSmsChannel, times(1)).supports(mockNotification);
        verify(mockSmsChannel, times(1)).send(mockNotification);
    }
}

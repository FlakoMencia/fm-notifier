package com.fm.notifier.exception;

public sealed class DeliveryException extends NotificationException permits ProviderException, ChannelException {
    protected DeliveryException(String message) {
        super(message);
    }
}

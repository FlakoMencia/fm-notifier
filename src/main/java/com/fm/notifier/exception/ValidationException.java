package com.fm.notifier.exception;

public sealed class ValidationException extends NotificationException permits InvalidNotificationException {
    protected ValidationException(String message) {
        super(message);
    }
}

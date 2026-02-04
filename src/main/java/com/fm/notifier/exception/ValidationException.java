package com.fm.notifier.exception;

public sealed class ValidationException extends NotificationException permits InvalidNotificationException {

    public ValidationException(String message) {
        super(message);
    }

}

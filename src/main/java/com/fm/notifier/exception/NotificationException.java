package com.fm.notifier.exception;

public abstract class NotificationException extends RuntimeException {
    protected NotificationException(String message) {
        super(message);
    }
}

package com.fm.notifier.api;

import java.util.Objects;

public final class NotificationError {

    private final String code;
    private final String message;

    public NotificationError(String code, String message) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}

package com.fm.notifier.exception;

public enum ErrorCode {
    // Validation Error Codes
    VALIDATION_INVALID_INPUT,
    VALIDATION_MISSING_REQUIRED_FIELD,

    // Delivery Error Codes
    DELIVERY_CHANNEL_UNAVAILABLE,
    DELIVERY_PROVIDER_ERROR,
    DELIVERY_TIMEOUT
}

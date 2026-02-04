package com.fm.notifier.api;

import java.time.Instant;
import java.util.Optional;

public final class NotificationResult {

    private final NotificationStatus status;
    private final String channel;
    private final String provider;
    private final String externalId;
    private final NotificationError error;
    private final Instant timestamp;

    private NotificationResult(Builder builder) {
        this.status = builder.status;
        this.channel = builder.channel;
        this.provider = builder.provider;
        this.externalId = builder.externalId;
        this.error = builder.error;
        this.timestamp = builder.timestamp != null
                ? builder.timestamp
                : Instant.now();
    }

    public NotificationStatus getStatus() { return status; }
    public String getChannel() { return channel; }
    public String getProvider() { return provider; }
    public Optional<String> getExternalId() { return Optional.ofNullable(externalId); }
    public Optional<NotificationError> getError() { return Optional.ofNullable(error); }
    public Instant getTimestamp() { return timestamp; }

    public boolean isSuccess() {
        return status == NotificationStatus.SUCCESS;
    }

    public static Builder success(String channel, String provider) {
        return new Builder()
                .status(NotificationStatus.SUCCESS)
                .channel(channel)
                .provider(provider);
    }

    public static Builder failure(NotificationStatus status, NotificationError error) {
        return new Builder()
                .status(status)
                .error(error);
    }

    public static final class Builder {
        private NotificationStatus status;
        private String channel;
        private String provider;
        private String externalId;
        private NotificationError error;
        private Instant timestamp;

        public Builder status(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder error(NotificationError error) {
            this.error = error;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public NotificationResult build() {
            return new NotificationResult(this);
        }
    }
}

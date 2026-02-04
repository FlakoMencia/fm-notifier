package com.fm.notifier.api;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Notification {

    private final String recipient;
    private final String subject;
    private final String body;
    private final Map<String, Object> metadata;
//    private final String channel;   // lo Remuevo channelpara mejor enviarlo por metadata y extender la posibilidad de usar mas canales de los implementados


    private Notification(Builder builder) {
        this.recipient = Objects.requireNonNull(builder.recipient);
        this.subject = builder.subject;
        this.body = Objects.requireNonNull(builder.body);
        this.metadata = builder.metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.metadata);
//        this.channel = builder.channel;
    }

    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Map<String, Object> getMetadata() { return metadata; }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String recipient;
        private String subject;
        private String body;
        private Map<String, Object> metadata;


        public Builder to(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }

    }
}

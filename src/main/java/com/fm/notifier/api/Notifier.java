package com.fm.notifier.api;

import java.util.concurrent.CompletableFuture;

public interface Notifier {

    NotificationResult notify(Notification notification);

    CompletableFuture<NotificationResult> notifyAsync(Notification notification);

    static NotifierBuilder builder() {
        return new NotifierBuilder();
    }
}

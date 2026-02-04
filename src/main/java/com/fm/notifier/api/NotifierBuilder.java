package com.fm.notifier.api;

import com.fm.notifier.channel.NotificationChannel;
import com.fm.notifier.core.DefaultNotifier;

import java.util.HashMap;
import java.util.Map;

public final class NotifierBuilder {

    private final Map<String, NotificationChannel> channels = new HashMap<>();

    public NotifierBuilder registerChannel(NotificationChannel channel) {
        channels.put(channel.getChannelName(), channel);
        return this;
    }

    public Notifier build() {
        return new DefaultNotifier(channels);
    }
}

# Welcome to the solution by Mario Mencía


🌍 Read this in ( Lee esto en) : [English](README.md) | [Español](README_ES.md)

# 📩 fm-notifier

A lightweight, noneframework Java notification library designed with clean architecture and extensibility in mind.

## Version 0.1.0 (Simulator Release)

This first release focuses on architecture, design patterns, and extensibility.  
Real provider integrations (SendGrid, Twilio, FCM, etc.) are intentionally simulated.

---

## ✨ Overview

fm-notifier provides a unified abstraction to send notifications through multiple channels such as:

- 📧 Email
- 📩 SMS
- 📲 Push Notifications
- (Optional) Slack, WhatsApp, Webhooks…

The main goal is to allow any client application to send notifications without worrying about the underlying channel or provider.

---

## 🎯 Design Goals

- ✅ Framework-agnostic (no Spring, no Quarkus, no annotations)
- ✅ Configuration 100% via Java code (no YAML/properties)
- ✅ SOLID principles applied correctly
- ✅ Extensible (Open/Closed): add channels without modifying the core
- ✅ Clear separation of responsibilities
- ✅ Sync and Async support
- ✅ Easy to test (mockable providers)

---

## 📦 Installation

### Maven

```
<dependency>
<groupId>com.fm</groupId>
<artifactId>fm-notifier</artifactId>
<version>0.1.0</version>
</dependency>
```

(Real publication on GitHub Packages will be enabled in future releases.)

---

## 🚀 Quick Start

### 1. Create Channels

```
NotificationChannel email = new EmailChannel();
NotificationChannel sms   = new SmsChannel();
NotificationChannel push  = new PushNotificationChannel();
```

---

### 2. Register Channels in the Notifier

```
Notifier notifier = Notifier.builder()
.registerChannel(email)
.registerChannel(sms)
.registerChannel(push)
.build();
```

---

### 3. Send a Notification

#### Example: Email

```
Notification emailNotification = Notification.builder()
.to("user@example.com")
.subject("Welcome!")
.body("Thanks for using fm-notifier.")
.metadata(Map.of("channel", "email"))
.build();

NotificationResult result = notifier.notify(emailNotification);
System.out.println(result);
```

---

#### Example: SMS

```
Notification smsNotification = Notification.builder()
.to("+50371234567")
.body("Your code is 123456")
.metadata(Map.of("channel", "sms"))
.build();

notifier.notify(smsNotification);
```

---

#### Example: Push Notification

```
Notification pushNotification = Notification.builder()
.body("You have a new message!")
.metadata(Map.of(
"channel", "push",
"deviceToken", "abc123-device-token"
))
.build();

notifier.notify(pushNotification);
```

---

## 🧠 Channel Routing via Metadata

The library uses a metadata-based routing the way:

```
.metadata(Map.of("channel", "sms"))
```

This avoids rigid enums or hardcoded logic, making it easy to add future channels like:

- Slack
- WhatsApp
- Discord
- Webhooks

without modifying existing code.

---

## 🏗️ Architecture & Design Patterns

### Strategy Pattern (NotificationChannel)

Each channel implements the same interface:

```
public interface NotificationChannel {
boolean supports(Notification notification);
NotificationResult send(Notification notification);
}
```

---

### Builder Pattern (Notifier + Notification)

```
Notifier notifier = Notifier.builder()
.registerChannel(new EmailChannel())
.build();
```

---

### Open/Closed Principle

To add a new channel:

- ✅ Create a new implementation
- ❌ Do not modify DefaultNotifier (please >.<)

---

### Dependency Inversion

The core depends only on abstractions:

- Notifier
- NotificationChannel

Never on concrete providers.

---

## ⚠️ Error Handling

Errors are separated by domain:

### Validation Errors

- ValidationException
- InvalidNotificationException

Examples:

- Invalid email
- Missing subject
- Missing device token

---

### Delivery Errors

- DeliveryException
- ProviderException
- ChannelException

Examples:

- Provider failure
- Channel unavailable

---

### Configuration Errors

- NotifierConfigurationException

Example:

- No channel registered

---

## ⏳ Async Support

Any notification can be sent asynchronously:

```
notifier.notifyAsync(notification)
.thenAccept(System.out::println);
```

Based on CompletableFuture.

---

## 🧪 Testing Philosophy

The library is designed to be tested without real HTTP calls.

Tests should focus on:

- Validations
- Correct routing via supports()
- Expected NotificationResult
- Clear error propagation

Providers can be replaced with mocks or stubs.

---

## 🔌 Extending the Library

### Add a New Channel (Example: Slack)

Create the channel:

```
public final class SlackChannel implements NotificationChannel {
...
}
```

Register it:

```
notifier.registerChannel(new SlackChannel());
```

Use it:

```
.metadata(Map.of("channel", "slack"))
```

No core changes required.

---

## 🔐 Security Notes

Recommended best practices:

- Never hardcode API keys
- Inject credentials via environment variables
- Keep providers decoupled from the core

---

## 📌 Roadmap

Planned improvements:

- Real adapters (SendGrid, Twilio, Firebase, etc)
- Retry system
- Message templates
- Pub/Sub notification status
- Batch sending

---

## 📄 License

MIT License — Free to use and extend.

---

## ⭐ Final Note

fm-notifier demonstrates real backend architecture well think it:

- Clean abstractions
- Extensible design
- Classic patterns
- Swappable providers

This is not a framework — it is a portable Java library.

---

## ✅ Recommended Next Steps

- Full unit tests for Email/SMS/Push
- Real publishing on GitHub Packages
- Retry + Templates (optional)

---

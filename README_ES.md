# Bienvenidos a la solucion desarrollada por  Mario Mencía

🌍 Read this in ( Lee esto en) : [English](README.md) | [Español](README_ES.md)





# 📩 fm-notifier

Una librería ligera y sin ataduras de frameworks para envío de notificaciones en Java, diseñada con arquitectura limpia y extensibilidad en mente.

## Versión 0.1.0 (Release Simulador)

Esta primera versión está enfocada en diseño, patrones de arquitectura y extensibilidad.  
Las integraciones reales con proveedores (SendGrid, Twilio, FCM, etc.) están simuladas intencionalmente.

---

## ✨ Descripción General

fm-notifier provee una abstracción unificada para enviar notificaciones a través de múltiples canales como:

- 📧 Email
- 📩 SMS
- 📲 Push Notifications
- (Opcional) Slack, WhatsApp, Webhooks…

El objetivo principal es permitir que cualquier aplicación cliente envíe notificaciones sin preocuparse por el canal o proveedor subyacente.

---

## 🎯 Objetivos de Diseño

- ✅ Agnóstica a frameworks (sin Spring, sin Quarkus, sin anotaciones)
- ✅ Configuración 100% mediante código Java (sin YAML ni properties)
- ✅ Principios SOLID correctamente aplicados
- ✅ Extensible (Open/Closed): agregar canales sin modificar el core
- ✅ Separación clara de responsabilidades
- ✅ Soporte síncrono y asíncrono
- ✅ Fácil de testear (providers simulados o mockeables)

---

## 📦 Instalación

### Maven

```
<dependency>
<groupId>com.fm</groupId>
<artifactId>fm-notifier</artifactId>
<version>0.1.0</version>
</dependency>
```

(La publicación en GitHub Packages se habilitará en versiones futuras.)

---

## 🚀 Quick Start

### 1. Crear Canales

```
NotificationChannel email = new EmailChannel();
NotificationChannel sms   = new SmsChannel();
NotificationChannel push  = new PushNotificationChannel();
```

---

### 2. Registrar Canales en el Notifier

```
Notifier notifier = Notifier.builder()
.registerChannel(email)
.registerChannel(sms)
.registerChannel(push)
.build();
```

---

### 3. Enviar una Notificación

#### Ejemplo: Email

```
Notification emailNotification = Notification.builder()
.to("user@example.com")
.subject("Bienvenido!")
.body("Gracias por usar fm-notifier.")
.metadata(Map.of("channel", "email"))
.build();

NotificationResult result = notifier.notify(emailNotification);
System.out.println(result);
```

---

#### Ejemplo: SMS

```
Notification smsNotification = Notification.builder()
.to("+50371234567")
.body("Tu código es 123456")
.metadata(Map.of("channel", "sms"))
.build();

notifier.notify(smsNotification);
```

---

#### Ejemplo: Push Notification

```
Notification pushNotification = Notification.builder()
.body("Tienes un nuevo mensaje!")
.metadata(Map.of(
"channel", "push",
"deviceToken", "abc123-device-token"
))
.build();

notifier.notify(pushNotification);
```

---

## 🧠 Enrutamiento de Canales mediante Metadata

La librería utiliza una estrategia basada en metadata:

```
.metadata(Map.of("channel", "sms"))
```

Esto evita enums rígidos o lógica hardcodeada, permitiendo agregar fácilmente canales futuros como:

- Slack
- WhatsApp
- Discord
- Webhooks

sin modificar código existente.

---

## 🏗️ Arquitectura y Patrones de Diseño

### Patron Strategy (NotificationChannel)

Cada canal implementa la misma interfaz:

```
public interface NotificationChannel {
boolean supports(Notification notification);
NotificationResult send(Notification notification);
}
```

---

### Patron Builder (Notifier + Notification)

```
Notifier notifier = Notifier.builder()
.registerChannel(new EmailChannel())
.build();
```

---

### Principio Open/Closed 

Para agregar un canal nuevo:

- ✅ Crear una nueva implementación
- ❌ No modificar la clase DefaultNotifier (Porfavor >.<)

---

### Dependency Inversion

El core depende únicamente de abstracciones:

- Notifier
- NotificationChannel

Nunca de proveedores concretos.

---

## ⚠️ Manejo de Errores

Los errores están separados por dominio:

### Errores de Validación

- ValidationException
- InvalidNotificationException

Ejemplos:

- Email inválido
- Subject faltante
- Device token requerido

---

### Errores de Envío (Delivery)

- DeliveryException
- ProviderException
- ChannelException

Ejemplos:

- Fallo del proveedor
- Canal no disponible

---

### Errores de Configuración

- NotifierConfigurationException

Ejemplo:

- No se registró ningún canal

---

## ⏳ Soporte Asíncrono

Toda notificación puede enviarse async:

```
notifier.notifyAsync(notification)
.thenAccept(System.out::println);
```

Basado en CompletableFuture.

---

## 🧪 Filosofía de Testing

La librería está diseñada para testearse sin llamadas HTTP reales.

Los tests deben enfocarse en:

- Validaciones
- Enrutamiento correcto con supports()
- Resultados esperados (NotificationResult)
- Propagación clara de errores

Providers reales pueden reemplazarse por mocks o stubs.

---

## 🔌 Extender la Librería

### Agregar un Canal Nuevo (Ej: Slack)

Crear el canal:

```
public final class SlackChannel implements NotificationChannel {
...
}
```

Registrarlo:

```
notifier.registerChannel(new SlackChannel());
```

Usarlo:

```
.metadata(Map.of("channel", "slack"))
```

Sin cambios en el core.

---

## 🔐 Seguridad

Buenas prácticas recomendadas:

- Nunca hardcodear API keys
- Inyectar credenciales desde variables de entorno
- Mantener providers desacoplados del core

---

## 📌 Roadmap

Mejoras planeadas:

- Adaptadores reales (SendGrid, Twilio, Firebase, etc)
- Sistema de reintentos
- Templates de mensajes
- Pub/Sub para estado de notificación
- Envío en lote

---

## 📄 Licencia

MIT License — Libre para usar y extender.

---

## ⭐ Nota Final

fm-notifier está construido para facilitar el envio de comunicaciones atravez de un backend real bien pensado:

- Abstracciones limpias
- Diseño extensible
- Patrones clásicos
- Providers intercambiables

Esto no es un framework — es una librería portable en Java.

---
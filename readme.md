# Notification Library - Backend Challenge

Una librería Java **agnóstica a frameworks**, diseñada bajo **Arquitectura Hexagonal (Ports & Adapters)** para el envío unificado de notificaciones a través de múltiples canales (Email, SMS, Push).

Esta librería permite a las aplicaciones enviar mensajes sin acoplarse a proveedores específicos (como Twilio o SendGrid), facilitando la mantenibilidad y la escalabilidad del sistema.

---

## 🚀 Características Principales

* **Arquitectura Hexagonal Pura:** Separación estricta entre el Dominio (Lógica de negocio), Puertos (Interfaces) y Adaptadores (Infraestructura).
* **Zero-Dependency Core:** El núcleo de la librería no tiene dependencias externas, garantizando un dominio limpio.
* **Configuración Type-Safe:** Configuración 100% mediante código Java (Records) e inyección de dependencias, eliminando la necesidad de archivos `application.properties` o magia de frameworks.
* **Extensibilidad (Open/Closed Principle):** Fácil adición de nuevos proveedores sin modificar el código base.
* **Soporte Asíncrono:** Envío no bloqueante utilizando `CompletableFuture` y gestión de hilos personalizada.
* **Validación Robusta:** Validaciones de dominio (Email, Teléfono E.164) mediante Java Records[cite: 32].

---

## 🛠️ Tecnologías

* **Java 21** (Uso de `Records`, `Sealed Interfaces` y `Pattern Matching`).
* **Maven** (Gestión de dependencias).
* **JUnit 5 & Mockito** (Testing unitario y simulaciones).
* **SLF4J** (Logging).

---

## 🏛️ Arquitectura y Diseño

El proyecto sigue estrictamente los principios **SOLID** para asegurar calidad de software:

### 1. Estructura de Paquetes (Hexagonal)
```text
com.obed.notification
├── domain            # EL NÚCLEO (Agnóstico)
│   ├── model         # Records inmutables (EmailNotification, SmsNotification...)
│   └── exception     # Excepciones de negocio (ValidationException)
├── ports             # LAS FRONTERAS (Contratos)
│   ├── in            # Casos de Uso (API Pública)
│   └── out           # Interfaces para proveedores (SPI)
└── adapters.out      # LA INFRAESTRUCTURA (Implementaciones)
    ├── sendgrid      # Adaptador para Email
    ├── twilio        # Adaptador para SMS
    └── fcm           # Adaptador para Push (Firebase)

```

## 📦 Instalación e Integración

Esta librería está diseñada para ser utilizada como dependencia en otros proyectos Java, no como una aplicación ejecutable independiente.

### 1. Instalar en el repositorio local
Para que la librería esté disponible para tus otros proyectos, primero debes compilarla e instalarla en tu repositorio local de Maven (`~/.m2/repository`):

```bash
mvn clean install
```

### 2. Agregar como dependencia en tu proyecto
Luego, en el `pom.xml` de tu proyecto, agrega la siguiente dependencia:
```xml
<dependency>
    <groupId>com.obed</groupId>
    <artifactId>notification-lib</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## ⚡ Quick Start
Ejemplo completo de cómo configurar y enviar un correo electrónico:

```java
import com.obed.notification.NotificationClient;
import com.obed.notification.adapters.out.sendgrid.*;
import com.obed.notification.domain.model.EmailNotification;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Configurar el Proveedor (Adapter)
        // Recomendación: Usar variables de entorno para las claves
        var config = new SendGridConfig(System.getenv("SENDGRID_API_KEY"), "no-reply@miempresa.com");
        var emailAdapter = new SendGridEmailAdapter(config);

        // 2. Construir el Cliente (Facade)
        var client = NotificationClient.builder()
                .registerProvider(emailAdapter)
                .build();

        // 3. Enviar Notificación
        var email = new EmailNotification("usuario@gmail.com", "Bienvenido", "Hola mundo", List.of());
        
        try {
            client.send(email);
            System.out.println("✅ Correo enviado.");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
```

## ⚙️ Configuración y Proveedores Soportados
La librería utiliza el patrón de Inyección de Dependencias para la configuración. No busca archivos ocultos; tú debes instanciar la configuración y pasarla al adaptador.

- **Email:** SendGrid (Config class: `SendGridConfig` con API Key y email de origen).
- **SMS:** Twilio (Config class: `TwilioConfig` con Account SID, Auth Token y número de origen).
- **Push:** Firebase Cloud Messaging (Sin configuración adicional para fines de prueba).


-- **Ejemplos de Configuración para cada proveedor:**

```java
// SendGrid
var config = new SendGridConfig("SG.YOUR_KEY", "sender@domain.com");
var adapter = new SendGridEmailAdapter(config);
````

```java
// Twilio
var config = new TwilioConfig("AC_SID", "AUTH_TOKEN", "+15551234567");
var adapter = new TwilioSmsAdapter(config);
```

```java
// Firebase Cloud Messaging (FCM)
// Para fines de prueba, no se requiere configuración adicional.
var adapter = new FcmPushAdapter();
```

## 🏛️ Arquitectura Interna
El proyecto sigue los principios SOLID y una arquitectura de Puertos y Adaptadores:
- **Domain (com.obed.notification.domain):** Reglas de negocio puras. Sin dependencias de frameworks ni librerías HTTP.
- **Ports (com.obed.notification.ports):** Interfaces que definen los contratos de entrada (API) y salida (SPI).
- **Adapters (com.obed.notification.adapters.out):** Implementaciones concretas que hablan con APIs externas (Twilio, SendGrid).
- **NotificationClient:** Actúa como una fachada unificada para enviar notificaciones, desacoplando a los consumidores de los detalles de implementación.
- **Configuración Type-Safe:** Utiliza Java Records para representar configuraciones inmutables, garantizando seguridad de tipos y claridad en la configuración.
- **Validación de Dominio:** Implementa validaciones robustas para asegurar que los datos de notificación sean correctos antes de intentar enviarlos, lanzando excepciones específicas en caso de errores.
- **Extensibilidad:** Siguiendo el principio Open/Closed, la librería permite agregar nuevos proveedores sin modificar el código existente, simplemente implementando nuevas clases de adaptadores que cumplan con las interfaces definidas en los puertos.
- **Asincronía:** El envío de notificaciones se maneja de manera asíncrona utilizando `CompletableFuture`, permitiendo a los consumidores no bloquear sus hilos mientras esperan la respuesta del proveedor.
- **Logging:** Utiliza SLF4J para registrar eventos importantes, errores y depuración, facilitando el monitoreo y la resolución de problemas en producción.
- **Testing:** Incluye pruebas unitarias exhaustivas utilizando JUnit 5 y Mockito para garantizar la calidad y confiabilidad de la librería, cubriendo tanto el dominio como las interacciones con los adaptadores.

## 🧪 Testing
La librería incluye pruebas unitarias utilizando JUnit 5 y Mockito para garantizar la calidad del código. Se prueban tanto las validaciones de dominio como las interacciones con los adaptadores, asegurando que los casos de éxito y error se manejen correctamente.

## Autor
**Obed Navarrete** - [GitHub](
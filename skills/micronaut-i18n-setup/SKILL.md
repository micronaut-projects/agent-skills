---
name: micronaut-i18n-setup
description: Set up internationalization (i18n) in a Micronaut application with a MessageSource backed by resource bundles. Use when the user wants to add translations, configure a MessageSource, create message.properties bundles, or localize an application's text.
license: Apache-2.0
compatibility: Requires Micronaut framework (io.micronaut.context) on the classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To set up internationalization in a Micronaut application:

### 1. Create the message bundles

Place a base bundle and one file per language under `src/main/resources/i18n/`. The base file
holds the default language; each `messages_<lang>.properties` overrides it for that locale.

`src/main/resources/i18n/messages.properties`

```properties
logout=Logout
```

`src/main/resources/i18n/messages_es.properties`

```properties
logout=Salir
```

### 2. Expose a MessageSource

Create a `@Factory` that registers a `ResourceBundleMessageSource` pointing at the bundle base
name. The base name uses a **dot-separated, package-style path** relative to the classpath
(`i18n.messages` → `classpath:i18n/messages*.properties`) — not a slash path or a `.properties`
suffix.

```java
package com.micronauttodo.i18n;

import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import jakarta.inject.Singleton;

@Factory
class MessageSourceFactory {
    @Singleton
    MessageSource createMessageSource() {
        return new ResourceBundleMessageSource("i18n.messages");
    }
}
```

The `MessageSource` bean can now be injected anywhere, and `messageSource.getMessage(key,
locale)` resolves the localized text. It is also used by view layers (e.g. Thymeleaf
`#{key}`) and by `@Bindable`/validation messages.

### 3. Test the MessageSource

Use `@MicronautTest(startApplication = false)` — only the context is needed, no server. Inject
the `MessageSource` and assert each key resolves per locale:

```java
package com.micronauttodo.i18n;

import io.micronaut.context.MessageSource;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class MessageSourceFactoryTest {
    @Test
    void messageSource(MessageSource messageSource) {
        Optional<String> message = messageSource.getMessage("logout", Locale.ENGLISH);
        assertTrue(message.isPresent());
        assertEquals("Logout", message.get());

        message = messageSource.getMessage("logout", Locale.forLanguageTag("es"));
        assertTrue(message.isPresent());
        assertEquals("Salir", message.get());
    }
}
```

## Notes

- The bundle base name passed to `ResourceBundleMessageSource` is dot-separated
  (`i18n.messages`), mirroring the `src/main/resources/i18n/` directory — make sure the
  directory is `i18n` (not `i8n`).
- In modern Java (Java 9+), `ResourceBundle` reads `.properties` bundles as UTF-8 by default,
  so you can write non-ASCII text such as `Español` directly in the file. Use `\uXXXX`
  escapes only if you must support legacy Java 8 behavior or are loading properties through a
  non-UTF-8 path.
- A key missing from a localized bundle falls back to the base `messages.properties`. To
  guarantee every key is translated in every language, add a test that compares the key sets
  of each bundle (see the `micronaut-test-keys-localized` skill).
- To control which locale a request resolves to, configure
  `micronaut.server.locale-resolution` (e.g. `fixed`, `cookie-name`, `header`, or
  `default-locale`).

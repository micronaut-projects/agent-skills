---
name: micronaut-test-serializable
description: Write a test that verifies a class is serializable and/or deserializable by Micronaut Serialization (Serde). Use when the user wants to check a type is @Serdeable, can be serialized/deserialized, or has the right serde introspections.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Serialization (io.micronaut.serde) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To verify Micronaut Serialization can (de)serialize a type, inject the `SerdeIntrospections`
bean and assert that obtaining the serializable / deserializable introspection does not throw:

- `getDeserializableIntrospection(Argument.of(Type.class))` → the type can be **deserialized**
  (read from JSON into an object).
- `getSerializableIntrospection(Argument.of(Type.class))` → the type can be **serialized**
  (written from an object to JSON).

Both throw if the type lacks the required serde metadata (e.g. it is not `@Serdeable`, or not
introspected for the needed direction). Wrap each call in `assertDoesNotThrow`.

Use `@MicronautTest(startApplication = false)` — only the application context is needed, no
server.

### Example

Type under test:

```java
package io.micronaut.guides.core;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record App(String packageName) {
}
```

Test:

```java
package io.micronaut.guides.core;

import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.SerdeIntrospections;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@MicronautTest(startApplication = false)
class AppTest {

    @Inject
    BeanContext beanContext;

    @Test
    void typeAppIsDeserializable() {
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections.class);
        assertDoesNotThrow(() -> serdeIntrospections.getDeserializableIntrospection(Argument.of(App.class)));
    }

    @Test
    void typeAppIsSerializable() {
        SerdeIntrospections serdeIntrospections = beanContext.getBean(SerdeIntrospections.class);
        assertDoesNotThrow(() -> serdeIntrospections.getSerializableIntrospection(Argument.of(App.class)));
    }
}
```

You can also inject `SerdeIntrospections` directly instead of going through `BeanContext`:

```java
@Inject
SerdeIntrospections serdeIntrospections;
```

## Notes

- Name the tests after what they assert: `getSerializableIntrospection` verifies the type **is
  serializable**, so the method should be `typeAppIsSerializable` (not `...IsNotSerializable`).
- `@Serdeable` makes a type both serializable and deserializable. To allow only one direction,
  use `@Serdeable.Serializable` or `@Serdeable.Deserializable`, and assert only the direction
  you enabled — the other call is then expected to throw.
- `@Introspected` alone provides the bean introspection serde builds on; whether both
  introspection directions resolve depends on the serde configuration/annotations on the type.
- This verifies the serde *metadata* is present. To assert actual JSON output, use an
  `ObjectMapper`/`JsonMapper` round-trip test instead.

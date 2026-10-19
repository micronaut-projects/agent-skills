---
name: micronaut-test-introspected
description: Write a test that verifies a class has Micronaut bean introspection metadata (is @Introspected). Use when the user wants to check a type is introspectable, confirm @Introspected is present, or assert BeanIntrospection metadata exists for a class.
license: Apache-2.0
compatibility: Requires Micronaut Core (io.micronaut.core) on the test classpath. No running context needed.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

Introspection is a foundational capability behind many Micronaut features (serialization,
validation, bean mapping, configuration, etc.). To verify a class is introspectable, assert
that retrieving its `BeanIntrospection` does not throw.

`BeanIntrospection.getIntrospection(Type.class)` throws `IntrospectionException` if the type has
no compile-time introspection metadata (i.e. it is not `@Introspected`, nor annotated with
something that implies it such as `@Serdeable` or `@ConfigurationProperties`). Wrap the call in
`assertDoesNotThrow`.

- No application context is needed — introspection metadata is generated at compile time, so
  this is a **plain JUnit test** (no `@MicronautTest`).

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

import io.micronaut.core.beans.BeanIntrospection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppTest {

    @Test
    void typeAppIsAnnotatedWithIntrospected() {
        assertDoesNotThrow(() -> BeanIntrospection.getIntrospection(App.class));
    }
}
```

## Notes

- This is a plain unit test — do **not** add `@MicronautTest`. Introspections are resolved from
  compile-time-generated metadata via `BeanIntrospection`, with no need for a bean context or
  server.
- `@Introspected` is implied by other annotations (e.g. `@Serdeable`, `@ConfigurationProperties`,
  `@EachProperty`), so a type carrying one of those is also introspectable.
- Beyond existence, you can assert on the introspection itself, e.g.
  `BeanIntrospection.getIntrospection(App.class).getPropertyNames()` contains `"packageName"`,
  or instantiate via `introspection.instantiate(...)`.
- Introspection is the metadata layer underneath Serde and Validation — see `micronaut-test-serializable`
  (serde introspections) and `micronaut-test-field-required` / `micronaut-test-field-optional` (validation), which
  build on an introspected type.

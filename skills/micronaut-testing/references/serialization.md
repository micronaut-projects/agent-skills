# Introspection and serialization

## A type is introspected

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

### Notes

- This is a plain unit test — do **not** add `@MicronautTest`. Introspections are resolved from
  compile-time-generated metadata via `BeanIntrospection`, with no need for a bean context or
  server.
- `@Introspected` is implied by other annotations (e.g. `@Serdeable`, `@ConfigurationProperties`,
  `@EachProperty`), so a type carrying one of those is also introspectable.
- Beyond existence, you can assert on the introspection itself, e.g.
  `BeanIntrospection.getIntrospection(App.class).getPropertyNames()` contains `"packageName"`,
  or instantiate via `introspection.instantiate(...)`.
- Introspection is the metadata layer underneath Serde and Validation — see the sections below (serde) and
  [validation.md](validation.md), which build on an introspected type.

## A type is serializable / deserializable

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

### Notes

- Name the tests after what they assert: `getSerializableIntrospection` verifies the type **is
  serializable**, so the method should be `typeAppIsSerializable` (not `...IsNotSerializable`).
- `@Serdeable` makes a type both serializable and deserializable. To allow only one direction,
  use `@Serdeable.Serializable` or `@Serdeable.Deserializable`, and assert only the direction
  you enabled — the other call is then expected to throw.
- `@Introspected` alone provides the bean introspection serde builds on; whether both
  introspection directions resolve depends on the serde configuration/annotations on the type.
- This verifies the serde *metadata* is present. To assert actual JSON output, use an
  `JsonMapper` test instead — see "Object to JSON" / "JSON to object" below.

## Object to JSON

To verify a Java object serializes to the expected JSON, inject `JsonMapper` and call
`writeValueAsString(obj)`, then assert the produced JSON string equals the expected payload.

The `JsonMapper` API mirrors Jackson Databind's `ObjectMapper`, so `writeValueAsString` writes
an object to a JSON `String`.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject `JsonMapper` as a test-method parameter (or with `@Inject`).
- `writeValueAsString` throws `IOException`, so declare `throws IOException` on the test method.

### Example

Type under test (serialization enabled with `@Serdeable`):

```java
package training.questionnaire.repositories;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record Topic(
        @NonNull @NotBlank String id,
        @NonNull @NotBlank String name) {
}
```

Test:

```java
package training.questionnaire.repositories;

import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false)
class TopicTest {

    @Test
    void topicJsonSerialization(JsonMapper jsonMapper) throws IOException {
        Topic topic = new Topic("1", "Oracle Cloud Infrastructure Fundamentals");
        String json = jsonMapper.writeValueAsString(topic);
        assertEquals("""
                {"id":"1","name":"Oracle Cloud Infrastructure Fundamentals"}""", json);
    }
}
```

### Notes

- The type must be serializable by Serde — annotate it `@Serdeable` (both directions) or
  `@Serdeable.Serializable` (write-only). Without it, `writeValueAsString` throws.
- Serde preserves the declaration order of record components, so the JSON field order is
  deterministic — a literal string equality assertion is reliable. Keep the expected JSON
  compact (no spaces after `:` or `,`) to match the default output.
- To test the opposite direction (JSON → object), use
  `jsonMapper.readValue(json, Argument.of(Topic.class))` — see "JSON to object" below.
- To check only that the serde metadata exists (not actual output), assert the introspections
  instead — see "A type is serializable / deserializable" above.

## JSON to object

To verify JSON deserializes into the expected Java object, inject `JsonMapper` and call
`readValue(json, Argument.of(Type.class))`, then assert the result equals the expected object.

The `JsonMapper` API mirrors Jackson Databind's `ObjectMapper`, so `readValue` reads a JSON
string (or bytes) into an instance of the target type described by an `Argument`.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject `JsonMapper` as a test-method parameter (or with `@Inject`).
- `readValue` throws `IOException`, so declare `throws IOException` on the test method.
- Equality relies on the type's `equals` — Java `record`s get this for free.

### Example

Type under test (deserialization enabled with `@Serdeable.Deserializable`):

```java
package training.questionnaire.repositories;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable.Deserializable
public record TopicSave(@NonNull @NotBlank String name) {
}
```

Test:

```java
package training.questionnaire.repositories;

import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false)
class TopicSaveTest {

    @Test
    void topicSaveJsonDeserialization(JsonMapper jsonMapper) throws IOException {
        TopicSave topic = new TopicSave("Oracle Cloud Infrastructure Fundamentals");
        assertEquals(
                topic,
                jsonMapper.readValue("""
                        {"name":"Oracle Cloud Infrastructure Fundamentals"}""", Argument.of(TopicSave.class)));
    }
}
```

### Notes

- The type must be deserializable by Serde — annotate it `@Serdeable` (both directions) or
  `@Serdeable.Deserializable` (read-only). Without it, `readValue` throws.
- `Argument.of(TopicSave.class)` describes the target type; use the `Argument` overload (rather
  than `readValue(json, Class)`) when the type has generics, e.g.
  `Argument.listOf(TopicSave.class)`.
- To test the opposite direction (object → JSON), use `jsonMapper.writeValueAsString(obj)` and
  assert on the produced JSON.
- To check only that the serde metadata exists (not actual mapping), assert the introspections
  instead — see "A type is serializable / deserializable" above.

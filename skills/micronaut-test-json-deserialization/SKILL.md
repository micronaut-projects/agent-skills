---
name: micronaut-test-json-deserialization
description: Write a test that verifies JSON deserializes into the expected Java object using Micronaut's JsonMapper. Use when the user wants to assert JSON-to-object mapping, check field binding from a JSON payload, or verify a @Serdeable.Deserializable type reads correctly.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Serialization (io.micronaut.serde / io.micronaut.json) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

- The type must be deserializable by Serde — annotate it `@Serdeable` (both directions) or
  `@Serdeable.Deserializable` (read-only). Without it, `readValue` throws.
- `Argument.of(TopicSave.class)` describes the target type; use the `Argument` overload (rather
  than `readValue(json, Class)`) when the type has generics, e.g.
  `Argument.listOf(TopicSave.class)`.
- To test the opposite direction (object → JSON), use `jsonMapper.writeValueAsString(obj)` and
  assert on the produced JSON.
- To check only that the serde metadata exists (not actual mapping), assert the introspections
  instead — see the `micronaut-test-serializable` skill.

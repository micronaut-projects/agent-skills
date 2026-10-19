---
name: micronaut-test-json-serialization
description: Write a test that verifies a Java object serializes to the expected JSON using Micronaut's JsonMapper. Use when the user wants to assert object-to-JSON output, check the produced JSON payload, or verify a @Serdeable type writes correctly.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Serialization (io.micronaut.serde / io.micronaut.json) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

- The type must be serializable by Serde — annotate it `@Serdeable` (both directions) or
  `@Serdeable.Serializable` (write-only). Without it, `writeValueAsString` throws.
- Serde preserves the declaration order of record components, so the JSON field order is
  deterministic — a literal string equality assertion is reliable. Keep the expected JSON
  compact (no spaces after `:` or `,`) to match the default output.
- To test the opposite direction (JSON → object), use
  `jsonMapper.readValue(json, Argument.of(Topic.class))` — see the `micronaut-test-json-deserialization`
  skill.
- To check only that the serde metadata exists (not actual output), assert the introspections
  instead — see the `micronaut-test-serializable` skill.

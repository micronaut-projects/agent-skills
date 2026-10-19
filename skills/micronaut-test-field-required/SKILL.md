---
name: micronaut-test-field-required
description: Write a test that verifies a field is required by validating an instance with that field blank/null and asserting there are constraint violations. Use when the user wants to confirm a property is mandatory, check a @NotBlank/@NotNull constraint, or test bean validation for a required field.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Validation (io.micronaut.validation) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To verify a field is required, inject a `Validator`, build an instance with that field set to
an invalid value (e.g. `null` and/or `""`), validate it, and assert there **are** constraint
violations.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject `jakarta.validation.Validator`.
- `validator.validate(instance)` returns a `Set<ConstraintViolation<T>>`; a **non-empty** set
  means the instance is invalid.
- For a `@NotBlank String`, test both the empty string `""` and `null`, since `@NotBlank`
  rejects both.

### Example

Type under test (the `name` field is `@NotBlank`):

```java
package training.questionnaire.repositories.jdbc;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;

@Introspected
record TopicEntity(@NonNull @NotBlank String name) {
}
```

Test:

```java
package training.questionnaire.repositories.jdbc;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@MicronautTest(startApplication = false)
class TopicEntityTest {

    @Inject
    Validator validator;

    @Test
    void nameCannotBeBlank() {
        TopicEntity topicEntity = new TopicEntity("");
        assertFalse(validator.validate(topicEntity).isEmpty());

        topicEntity = new TopicEntity(null);
        assertFalse(validator.validate(topicEntity).isEmpty());
    }
}
```

## Notes

- This is the **opposite** of an optional field: a required field produces violations when
  blank/null, whereas an optional field produces none (see the `micronaut-test-field-optional` skill).
- Choose the constraint and invalid values to match: `@NotNull` → test `null`; `@NotBlank`
  (Strings) → test `null` and `""` (and whitespace-only); `@NotEmpty` (collections/strings) →
  test `null` and empty.
- The type must be introspected for validation to apply — annotate it `@Introspected` (or
  `@Serdeable`, which implies it). Without an introspection the validator has no metadata to
  check.
- You can inject either `jakarta.validation.Validator` or
  `io.micronaut.validation.validator.Validator` (which extends it); both use Micronaut's
  introspection-based validation.
- To assert *which* field or constraint failed, inspect the violations, e.g.
  `violations.iterator().next().getPropertyPath().toString()` equals `"name"`.

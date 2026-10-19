---
name: micronaut-test-field-optional
description: Write a test that verifies a field is optional (nullable) by validating an instance with that field set to null and asserting there are no constraint violations. Use when the user wants to confirm a property may be null, check a field is not required, or test bean validation for an optional field.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Validation (io.micronaut.validation) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To verify a field is optional, inject the `Validator`, build an instance with that field set to
`null`, validate it, and assert there are **no** constraint violations.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject `io.micronaut.validation.validator.Validator`.
- `validator.validate(instance)` returns a `Set<ConstraintViolation<T>>`; an **empty** set
  means the instance is valid.
- A field with no `@NotNull`/`@NotBlank`/`@NonNull` constraint (often marked `@Nullable`) is
  optional, so a `null` value produces no violations.

### Example

Type under test (the `packageName` field is `@Nullable`):

```java
package io.micronaut.guides.core;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
public record App(@Nullable String packageName) {
}
```

Test:

```java
package io.micronaut.guides.core;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AppTest {

    @Inject
    Validator validator;

    @Test
    void typeAppPackageNameCanBeNull() {
        String packageName = null;
        Set<ConstraintViolation<App>> violations = validator.validate(new App(packageName));
        assertTrue(violations.isEmpty());
    }
}
```

## Notes

- This verifies the **opposite** of a required field: for a required field you would assert
  `assertFalse(violations.isEmpty())` when the value is `null` (see the related "required field"
  case).
- The type must be introspected for validation to apply — annotate it `@Introspected` (or
  `@Serdeable`, which implies it). Without an introspection the validator has no metadata to
  check.
- Validation only triggers automatically on beans crossing a validated boundary (controller
  `@Body`, `@Valid` parameters, etc.). In a unit test, call `validator.validate(...)`
  explicitly as shown.
- Inject `io.micronaut.validation.validator.Validator` (Micronaut's validator) rather than
  constructing a `jakarta.validation.Validator` yourself, so Micronaut's introspection-based
  validation is used.

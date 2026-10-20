# Validation

## A field is required

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

### Notes

- This is the **opposite** of an optional field: a required field produces violations when
  blank/null, whereas an optional field produces none (see "A field is optional" below).
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

## A field is optional

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

### Notes

- This verifies the **opposite** of a required field: for a required field you would assert
  `assertFalse(violations.isEmpty())` when the value is `null` (see "A field is required" above).
- The type must be introspected for validation to apply — annotate it `@Introspected` (or
  `@Serdeable`, which implies it). Without an introspection the validator has no metadata to
  check.
- Validation only triggers automatically on beans crossing a validated boundary (controller
  `@Body`, `@Valid` parameters, etc.). In a unit test, call `validator.validate(...)`
  explicitly as shown.
- Inject `io.micronaut.validation.validator.Validator` (Micronaut's validator) rather than
  constructing a `jakarta.validation.Validator` yourself, so Micronaut's introspection-based
  validation is used.

## Method parameter constraints are enforced

Micronaut validates constraint annotations on the parameters of methods on injected beans. It
is easy to forget such a constraint when defining an API, so write a test that calls the method
with an invalid argument and asserts a `ConstraintViolationException` is thrown.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject the interface/bean under test and invoke the method with invalid arguments
  (e.g. `""` and `null` for a `@NotBlank` parameter).
- Assert `ConstraintViolationException` with `assertThrows`. The exception is raised **before**
  the method body runs, so the implementation can be a stub.
- Provide a stub implementation scoped to this test with the `spec.name` + `@Requires` pattern,
  so it does not pollute other tests.

### The `spec.name` pattern (avoid bean pollution)

- Annotate the test class with `@Property(name = "spec.name", value = "TopicRepositoryTest")`.
- Declare a `@Singleton` bean that `@Replaces` the interface and is guarded by
  `@Requires(property = "spec.name", value = "TopicRepositoryTest")`. The `@Requires` ensures
  the stub is only loaded for this test.

### Example

API with a required parameter:

```java
package training.questionnaire.repositories;

import jakarta.validation.constraints.NotBlank;

public interface TopicRepository {
    void save(@NotBlank String name);
}
```

Test:

```java
package training.questionnaire.repositories;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Property(name = "spec.name", value = "TopicRepositoryTest")
@MicronautTest(startApplication = false)
class TopicRepositoryTest {

    @Test
    void test(TopicRepository topicRepository) {
        assertThrows(ConstraintViolationException.class,
                () -> topicRepository.save(""));
        assertThrows(ConstraintViolationException.class,
                () -> topicRepository.save(null));
    }

    @Requires(property = "spec.name", value = "TopicRepositoryTest")
    @Singleton
    @Replaces(TopicRepository.class)
    static class CustomTopicRepository implements TopicRepository {
        @Override
        public void save(String name) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
```

### Notes

- Parameter validation only applies when the method is called on a **bean** that Micronaut
  proxies (inject the bean — don't `new` it). Calling `new CustomTopicRepository().save("")`
  would bypass validation.
- The constraint lives on the **interface** method parameter; the implementation does not repeat
  it. Because validation runs on the proxy before dispatch, the stub body never executes for
  invalid input — hence it can safely throw `UnsupportedOperationException`.
- This complements field/bean validation tests ("A field is required" /
  "A field is optional" above) by covering method-parameter constraints on an API boundary.
- The `spec.name` + `@Requires` pair keeps the stub local to this test; without the matching
  `@Requires`, the `@Replaces` bean would replace the real implementation across the whole test
  suite.

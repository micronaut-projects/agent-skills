---
name: micronaut-test-interface-parameter-validation
description: Write a test that verifies constraint annotations on an interface/API method parameter are enforced at runtime. Use when the user wants to confirm a method parameter constraint (e.g. @NotBlank, @NotNull) triggers a ConstraintViolationException, or test that a validated service/repository API rejects invalid arguments.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Validation (io.micronaut.validation) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

- Parameter validation only applies when the method is called on a **bean** that Micronaut
  proxies (inject the bean — don't `new` it). Calling `new CustomTopicRepository().save("")`
  would bypass validation.
- The constraint lives on the **interface** method parameter; the implementation does not repeat
  it. Because validation runs on the proxy before dispatch, the stub body never executes for
  invalid input — hence it can safely throw `UnsupportedOperationException`.
- This complements field/bean validation tests (`micronaut-test-field-required`,
  `micronaut-test-field-optional`) by covering method-parameter constraints on an API boundary.
- The `spec.name` + `@Requires` pair keeps the stub local to this test; without the matching
  `@Requires`, the `@Replaces` bean would replace the real implementation across the whole test
  suite.

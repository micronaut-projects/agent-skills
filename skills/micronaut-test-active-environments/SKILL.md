---
name: micronaut-test-active-environments
description: Write a test that asserts which Micronaut environments are active. Use when the user wants to verify active environments, check that the "test" environment is enabled, or assert environment activation in a Micronaut test.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To test which environments are active, inject the `ApplicationContext` and read the
active environment names from its `Environment`:

```java
applicationContext.getEnvironment().getActiveNames();
```

`getActiveNames()` returns a `Set<String>` of the environments Micronaut resolved for the
running context.

When a test class is annotated with `@MicronautTest`, Micronaut activates the `test`
environment by default. The following test asserts that:

```java
package example.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class TestEnvByDefault {
    @Inject
    ApplicationContext applicationContext;

    @Test
    void testIsAddedByDefault() {
        assertEquals(Set.of("test"),
                applicationContext.getEnvironment().getActiveNames());
    }
}
```

## Notes

- Use `assertEquals(Set.of("test"), ...)` when you want to assert the *exact* set of active
  environments. Use `assertTrue(activeNames.contains("test"))` when you only care that a
  specific environment is present and other environments may also be active.
- Additional environments can be activated with `@MicronautTest(environments = {"foo", "bar"})`,
  via the `micronaut.environments` system property, or the `MICRONAUT_ENVIRONMENTS`
  environment variable — assert against the full expected set when any of these are in play.

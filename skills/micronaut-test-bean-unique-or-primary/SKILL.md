---
name: micronaut-test-bean-unique-or-primary
description: Write a test that verifies a single or primary bean of a given type resolves (no NonUniqueBeanException). Use when the user wants to confirm a type resolves to one bean, guard against multiple candidate beans, or assert @Primary/@Secondary disambiguation works.
license: Apache-2.0
compatibility: Requires Micronaut Core (io.micronaut.context) and Micronaut Test (io.micronaut.test) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

When an injection point requests a type that has **multiple** bean candidates and none is marked
primary, Micronaut cannot decide which to inject and throws:

```
io.micronaut.context.exceptions.NonUniqueBeanException: Multiple possible bean candidates found
```

To guard against this, write a test that resolves the type with `BeanContext::getBean` and
asserts it does **not** throw — `getBean(Type.class)` throws `NonUniqueBeanException` when the
candidate is ambiguous, so `assertDoesNotThrow` fails the test until disambiguation is in place.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject `BeanContext` and call `getBean(Type.class)` inside `assertDoesNotThrow`.

### Example

Two beans implement the same interface:

```java
package example.micronaut;

public interface Calculator {
}
```

```java
package example.micronaut;

import jakarta.inject.Singleton;

@Singleton
public class MacOsCalculator implements Calculator {
}
```

```java
package example.micronaut;

import jakarta.inject.Singleton;

@Singleton
public class Pcalc implements Calculator {
}
```

Test (fails with `NonUniqueBeanException` until one bean is made primary):

```java
package example.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@MicronautTest(startApplication = false)
class CalculatorTest {

    @Inject
    BeanContext beanContext;

    @Test
    void noMultipleBeansOfTypeCalculator() {
        assertDoesNotThrow(() -> beanContext.getBean(Calculator.class));
    }
}
```

## Making the test pass

Disambiguate the candidates so exactly one is selected for the bare-type injection point:

- Annotate one bean with `@Primary` — it becomes the preferred candidate.
- Or annotate the other bean(s) with `@Secondary` — they are de-prioritized, leaving one
  primary candidate.

## Notes

- `getBean(Type.class)` resolves a **single** bean: it throws `NonUniqueBeanException` for
  ambiguous candidates and `NoSuchBeanException` when none exist. Wrapping it in
  `assertDoesNotThrow` asserts there is exactly one resolvable (or primary) bean.
- This differs from `containsBean(Type.class)` (a `boolean` existence check that does not care
  about uniqueness) — see the `micronaut-test-bean-exists` skill.
- To get **all** candidates instead of a single one, use `getBeansOfType(Type.class)`; assert on
  its size when you intend to verify the exact number of implementations.
- `@Primary`/`@Secondary` only affect resolution of the bare type. Qualified lookups (e.g.
  `@Named`) select a specific candidate regardless.

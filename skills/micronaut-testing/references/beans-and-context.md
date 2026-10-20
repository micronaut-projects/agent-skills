# Beans and application context

## A bean exists

To verify a bean of a certain type exists, ask the context with `containsBean(Type.class)` and
assert the result is `true`. There are two ways to obtain the context.

### Option 1 — with Micronaut Test (recommended)

Inject the `BeanContext` with `@Inject` and use `@MicronautTest(startApplication = false)` so no
server starts:

```java
package com.example;

import io.micronaut.context.BeanContext;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AuthenticationFetcherTest {
    @Inject
    BeanContext beanContext;

    @Test
    void beanOfTypeAuthenticationFetcherExists() {
        assertTrue(beanContext.containsBean(AuthenticationFetcher.class));
    }
}
```

### Option 2 — without Micronaut Test

Start an `ApplicationContext` manually inside a try-with-resources block so it is closed after
the test:

```java
package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.security.filters.AuthenticationFetcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationFetcherTest {

    @Test
    void beanOfTypeAuthenticationFetcherExists() {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            assertTrue(applicationContext.containsBean(AuthenticationFetcher.class));
        }
    }
}
```

### Notes

- `containsBean(Type.class)` returns a `boolean` and does not instantiate the bean; assert
  `true` for existence, `false` to verify a bean is **absent**.
- Prefer Option 1 (`@MicronautTest`) — Micronaut Test manages the context lifecycle for you. Use
  `startApplication = false` since checking for a bean does not need the embedded server.
- In Option 2, always start the context in a try-with-resources statement so it is closed when
  the test finishes; an unclosed context leaks resources across tests.
- `BeanContext` (Option 1) and `ApplicationContext` (Option 2, which extends `BeanContext`) both
  expose `containsBean`. To assert a specific instance can be resolved instead, use
  `getBean(Type.class)` and wrap it in `assertDoesNotThrow`.
- Conditional beans (`@Requires`) only exist when their conditions hold — set the relevant
  properties/environment in the test if the bean you expect is guarded.

## A type resolves to a single or primary bean

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

### Making the test pass

Disambiguate the candidates so exactly one is selected for the bare-type injection point:

- Annotate one bean with `@Primary` — it becomes the preferred candidate.
- Or annotate the other bean(s) with `@Secondary` — they are de-prioritized, leaving one
  primary candidate.

### Notes

- `getBean(Type.class)` resolves a **single** bean: it throws `NonUniqueBeanException` for
  ambiguous candidates and `NoSuchBeanException` when none exist. Wrapping it in
  `assertDoesNotThrow` asserts there is exactly one resolvable (or primary) bean.
- This differs from `containsBean(Type.class)` (a `boolean` existence check that does not care
  about uniqueness) — see "A bean exists" above.
- To get **all** candidates instead of a single one, use `getBeansOfType(Type.class)`; assert on
  its size when you intend to verify the exact number of implementations.
- `@Primary`/`@Secondary` only affect resolution of the bare type. Qualified lookups (e.g.
  `@Named`) select a specific candidate regardless.

## A bean has an annotation

To verify a bean carries a particular annotation, obtain its `BeanDefinition` from the context
and query the annotation metadata.

- Get the definition with `applicationContext.getBeanDefinition(MyBean.class)` (or
  `beanContext.getBeanDefinition(...)`).
- `BeanDefinition` exposes the bean's `AnnotationMetadata`. Common checks:
  - `hasAnnotation(MyAnnotation.class)` — the annotation is declared directly on the bean.
  - `hasStereotype(MyAnnotation.class)` — the annotation is present directly **or** as a
    meta-annotation (stereotype).
  - `getAnnotationNameByStereotype(MyAnnotation.class)` — returns an `Optional<String>` with the
    name of the annotation that carries the given stereotype.
- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.

### Example

Bean annotated with `@BootstrapContextCompatible` (and `@EachProperty`):

```java
@BootstrapContextCompatible
@EachProperty(AWSServiceConfigurationProperties.SERVICE_PREFIX)
public class AWSServiceConfigurationProperties implements AWSServiceConfiguration {
    // ...
}
```

Test:

```java
package example.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AWSServiceConfigurationPropertiesTest {

    @Inject
    BeanContext beanContext;

    @Test
    void beanIsBootstrapContextCompatible() {
        BeanDefinition<AWSServiceConfigurationProperties> beanDefinition =
                beanContext.getBeanDefinition(AWSServiceConfigurationProperties.class);
        assertTrue(beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible.class).isPresent());
    }
}
```

### Notes

- Prefer querying the `BeanDefinition` (compile-time `AnnotationMetadata`) over Java reflection
  (`clazz.getAnnotation(...)`). Micronaut's metadata captures meta-annotations/stereotypes and
  retains annotations that are not `RUNTIME`-retained, which reflection would miss.
- `hasAnnotation` vs `hasStereotype`: use `hasAnnotation` for a directly-declared annotation;
  use `hasStereotype` (or `getAnnotationNameByStereotype`) when the annotation may be applied
  indirectly via another annotation that is meta-annotated with it.
- `getBeanDefinition(Type.class)` throws if the type is not a bean or is ambiguous — it asserts
  the bean exists as a side effect. For a pure existence check use `containsBean` (see "A bean exists");
  for uniqueness see "A type resolves to a single or primary bean".
- To assert on an annotation's member values, read them from the metadata, e.g.
  `beanDefinition.stringValue(MyAnnotation.class, "value")` or
  `beanDefinition.getValue(MyAnnotation.class, Integer.class)`.

## Active environments

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

### Notes

- Use `assertEquals(Set.of("test"), ...)` when you want to assert the *exact* set of active
  environments. Use `assertTrue(activeNames.contains("test"))` when you only care that a
  specific environment is present and other environments may also be active.
- Additional environments can be activated with `@MicronautTest(environments = {"foo", "bar"})`,
  via the `micronaut.environments` system property, or the `MICRONAUT_ENVIRONMENTS`
  environment variable — assert against the full expected set when any of these are in play.

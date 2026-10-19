---
name: micronaut-test-bean-exists
description: Write a test that verifies a bean of a given type exists in the Micronaut context. Use when the user wants to confirm a bean is registered, check a type is available for injection, or assert the application context contains a bean.
license: Apache-2.0
compatibility: Requires Micronaut Core (io.micronaut.context); the @MicronautTest variant also requires Micronaut Test (io.micronaut.test).
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

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

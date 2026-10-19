---
name: micronaut-test-static-resources
description: Write a test that verifies static resources (assets such as images, CSS, JS, favicon) are served and reachable. Use when the user wants to confirm a static-resource mapping works, check an asset is accessible, or assert a file under classpath assets is served over HTTP.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut HTTP Client on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To verify a static resource is served, send a `GET` to its mapped URL and assert the request
succeeds. `client.exchange(...)` throws an `HttpClientResponseException` on a non-2xx status
(e.g. 404 if the mapping is wrong or the file is missing), so wrapping it in
`assertDoesNotThrow` asserts the asset is reachable.

- Use `@MicronautTest` (the server must be running so the static-resource route is served).
- Request the exact mapped path of each asset you want to verify.

This test exercises both the static-resource configuration and — if the path is secured — the
security rule that grants access.

### Configuration under test

```properties
micronaut.router.static-resources.images.paths=classpath:assets/images
micronaut.router.static-resources.images.mapping=/assets/images/**
micronaut.security.intercept-url-map[0].pattern=/assets/images/**
micronaut.security.intercept-url-map[0].http-method=GET
micronaut.security.intercept-url-map[0].access[0]=isAnonymous()
```

### Test

```java
package dev.contactvault.assets;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@MicronautTest
class ImagesAssetsTest {
    @Test
    void imagesAreAccessible(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        assertDoesNotThrow(() -> client.exchange(HttpRequest.GET("/assets/images/sign-in-with-google-dark.svg")));
        assertDoesNotThrow(() -> client.exchange(HttpRequest.GET("/assets/images/sign-in-with-google-light.svg")));
        assertDoesNotThrow(() -> client.exchange(HttpRequest.GET("/assets/images/sign-in-with-microsoft-dark.svg")));
        assertDoesNotThrow(() -> client.exchange(HttpRequest.GET("/assets/images/sign-in-with-microsoft-light.svg")));
    }
}
```

## Notes

- Use `@MicronautTest` (not `startApplication = false`) — serving a static resource requires the
  embedded server and its router.
- `assertDoesNotThrow(() -> client.exchange(...))` is the assertion: a missing file or wrong
  mapping yields a 404, which throws and fails the test. To be stricter, capture the response
  and assert `HttpStatus.OK` and/or the `Content-Type`.
- The request path must match the `mapping` (`/assets/images/**`); the file must exist on the
  classpath under the configured `paths` (`classpath:assets/images`), i.e.
  `src/main/resources/assets/images/...`.
- When the static path is secured, the asset is only reachable if a matching
  `intercept-url-map` grants access (`isAnonymous()` here). An unauthenticated test request to an
  un-whitelisted path would be redirected/forbidden, so this test also guards that rule.
- You can also assert with `client.retrieve(...)` when you want the body, or `client.exchange(...,
  byte[].class)` for binary assets.

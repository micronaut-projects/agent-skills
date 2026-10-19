---
name: micronaut-test-controller-returns-cookie
description: Write a test that asserts a Micronaut controller sets a Cookie on the HTTP response. Use when the user wants to verify a Set-Cookie header, assert a cookie's value, or test an endpoint that returns a cookie (often alongside a redirect).
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut HTTP Client on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To test that a controller returns a cookie, use `response.getCookie(name)` on the
`HttpResponse` and assert its presence and value.

When the cookie is set alongside a redirect (e.g. `HttpResponse.seeOther(uri).cookie(...)`),
the Micronaut HTTP Client follows redirects by default — and the redirected-to response will
**not** carry the `Set-Cookie` header. Configure the client **not** to follow redirects so you
observe the original response that sets the cookie:

```java
@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@MicronautTest
```

Then:

1. Send the request and `exchange` for the response.
2. Assert the status (e.g. `HttpStatus.SEE_OTHER`).
3. Assert `response.getCookie(name).isPresent()` and check `getValue()`.

### Example

Controller returning a redirect with a cookie:

```java
@Hidden
@Secured(SecurityRule.IS_ANONYMOUS)
@Produces(MediaType.TEXT_HTML)
@Post
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
HttpResponse<?> changeLocale(@Body("locale") String locale) {
    return HttpResponse.seeOther(uri)
        .cookie(cookieBuilder.apply(cookieName, locale));
}
```

Test:

```java
package com.micronauttodo.controllers.locale;

import com.micronauttodo.BrowserRequest;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@MicronautTest
class LocaleControllerTest {

    @Test
    void localeSavesACookie(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpRequest<?> request = BrowserRequest.POST("/locale", Map.of("locale", "es"));
        HttpResponse<?> response = assertDoesNotThrow(() -> client.exchange(request));
        assertEquals(HttpStatus.SEE_OTHER, response.status());
        final String cookieName = "locale";
        assertTrue(response.getCookie(cookieName).isPresent());
        assertEquals("es", response.getCookie(cookieName).get().getValue());
    }
}
```

## Notes

- `response.getCookie(name)` returns an `Optional<Cookie>`; assert `isPresent()` before calling
  `get().getValue()`.
- Without `micronaut.http.client.follow-redirects=false`, the client transparently follows the
  redirect, and the cookie set on the `SEE_OTHER` response is lost from what the test observes.
- Use `client.exchange(...)` (not `retrieve`) so you have the full `HttpResponse` to inspect
  status, headers, and cookies.
- This pairs naturally with the redirect-testing pattern — see the `micronaut-test-redirection` skill for
  asserting the `Location` header on the same response.

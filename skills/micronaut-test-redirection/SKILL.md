---
name: micronaut-test-redirection
description: Write a test that asserts an HTTP redirection (e.g. a controller returning seeOther/redirect). Use when the user wants to verify a redirect, assert a Location header, or test that an endpoint redirects to another path.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut HTTP Client on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

The Micronaut HTTP Client follows redirections by default. To assert a redirection, you must
configure the client **not** to follow redirects, then verify the response status and the
`Location` HTTP header.

Disable redirect-following with the `micronaut.http.client.follow-redirects` property set to
`false`:

```java
@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@MicronautTest
```

Then assert the response status is a redirect (e.g. `HttpStatus.SEE_OTHER`) and that the
`Location` header holds the target path.

### Example

Given a controller that redirects `/` to `/todo/list`:

```java
package com.micronauttodo.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;

import static com.micronauttodo.controllers.TodoController.URI_TODO_LIST;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
class HomeController {
    @Hidden
    @Produces(MediaType.TEXT_HTML)
    @Get
    HttpResponse<?> index() {
       return HttpResponse.seeOther(URI_TODO_LIST);
    }
}
```

The test configures the client not to follow redirects and verifies the `Location` header:

```java
package com.micronauttodo.controllers;

import com.micronauttodo.BrowserRequest;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@MicronautTest
class HomeControllerTest {

    @Test
    void homeRedirects(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpRequest<?> request = BrowserRequest.GET("/");
        HttpResponse<?> response = assertDoesNotThrow(() -> client.exchange(request));
        assertEquals(HttpStatus.SEE_OTHER, response.status());
        assertTrue(response.getHeaders().contains(HttpHeaders.LOCATION));
        assertEquals("/todo/list", response.getHeaders().get(HttpHeaders.LOCATION));
    }
}
```

## Notes

- Without `micronaut.http.client.follow-redirects=false`, the client transparently follows the
  redirect and the test would observe the final response (e.g. `/todo/list`) instead of the
  `SEE_OTHER` redirect — the assertions on status and `Location` would fail.
- Match the asserted status to what the controller returns: `HttpResponse.seeOther(...)` →
  `HttpStatus.SEE_OTHER` (303), `HttpResponse.redirect(...)` → `HttpStatus.MOVED_PERMANENTLY`
  (301), `HttpResponse.temporaryRedirect(...)` → `HttpStatus.TEMPORARY_REDIRECT` (307).
- Use the `Set.of`/exact-path assertion on the `Location` header to verify the precise
  redirect target.

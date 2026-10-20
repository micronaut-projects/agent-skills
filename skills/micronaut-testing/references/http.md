# HTTP endpoints

## A controller returns HTML

To test a controller that renders HTML with Micronaut Views:

1. Send a `GET` request with `.accept(MediaType.TEXT_HTML)` and retrieve the response as a
   `String`.
2. Assert the returned HTML contains the expected markup and data.
3. Replace the controller's collaborators (e.g. a repository) with a stub so the rendered data
   is deterministic. Scope the replacement to the test with `spec.name` + `@Requires` so it
   does not leak into other tests.

### Stubbing a collaborator with `spec.name`

- Annotate the test class with `@Property(name = "spec.name", value = "ContactControllerTest")`.
- Declare a `@Singleton` bean that `@Replaces` the real collaborator and is guarded by
  `@Requires(property = "spec.name", value = "ContactControllerTest")`. The `@Requires` ensures
  the stub is only active for this test.

### Example

Controller under test (uses `@View` to render `contact/list.html`):

```java
@Controller
class ContactController {
    public static final String PATH_CONTACT = "/contact";
    public static final String PATH_ACTION_LIST = "/list";
    private static final String VIEW_CONTACT = "contact";
    private static final String KEY_CONTACTS = "contacts";

    private final ContactRepository contactRepository;

    ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Produces(MediaType.TEXT_HTML)
    @Get(PATH_CONTACT + PATH_ACTION_LIST)
    @View(VIEW_CONTACT + PATH_ACTION_LIST)
    Map<String, Object> index() {
        return Collections.singletonMap(KEY_CONTACTS, contactRepository.findAll());
    }
}
```

Test:

```java
package dev.contactvault.controllers;

import dev.contactvault.repositories.Contact;
import dev.contactvault.repositories.ContactRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "ContactControllerTest")
@MicronautTest
class ContactControllerTest {

    @Test
    void contact(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpRequest<?> request = HttpRequest.GET("/contact/list").accept(MediaType.TEXT_HTML);
        String html = assertDoesNotThrow(() -> client.retrieve(request, String.class));
        assertNotNull(html);
        assertTrue(html.contains("DOCTYPE html"));
        assertTrue(html.contains("Katherine"));
        assertTrue(html.contains("Adams"));
        assertTrue(html.contains("Tim"));
        assertTrue(html.contains("Cook"));
    }

    @Requires(property = "spec.name", value = "ContactControllerTest")
    @Replaces(ContactRepository.class)
    @Singleton
    static class ContactRepositoryReplacement implements ContactRepository {
        @Override
        public List<Contact> findAll() {
            return List.of(
                    new Contact("Katherine", "Adams", "Senior Vice President and General Counsel", "Apple Inc."),
                    new Contact("Tim", "Cook", "CEO", "Apple Inc.")
            );
        }
    }
}
```

### Notes

- Use `client.retrieve(request, String.class)` to get the rendered HTML as a `String` so you
  can assert on its content. `retrieve` throws on non-2xx responses, so wrapping it in
  `assertDoesNotThrow` also asserts the request succeeded.
- Always set `.accept(MediaType.TEXT_HTML)` — it matches the controller's
  `@Produces(MediaType.TEXT_HTML)` and ensures the view is rendered.
- The `spec.name` pattern keeps the stub local to this test class. Without the matching
  `@Requires`, a `@Replaces` bean would replace the real collaborator across the whole test
  suite.
- Prefer asserting on stable, meaningful content (names, labels) rather than brittle full-markup
  matches, so the test survives template tweaks.

## A redirection

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

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
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
        HttpRequest<?> request = HttpRequest.GET("/").accept(MediaType.TEXT_HTML);
        HttpResponse<?> response = assertDoesNotThrow(() -> client.exchange(request));
        assertEquals(HttpStatus.SEE_OTHER, response.status());
        assertTrue(response.getHeaders().contains(HttpHeaders.LOCATION));
        assertEquals("/todo/list", response.getHeaders().get(HttpHeaders.LOCATION));
    }
}
```

### Notes

- Without `micronaut.http.client.follow-redirects=false`, the client transparently follows the
  redirect and the test would observe the final response (e.g. `/todo/list`) instead of the
  `SEE_OTHER` redirect — the assertions on status and `Location` would fail.
- Match the asserted status to what the controller returns: `HttpResponse.seeOther(...)` →
  `HttpStatus.SEE_OTHER` (303), `HttpResponse.redirect(...)` → `HttpStatus.MOVED_PERMANENTLY`
  (301), `HttpResponse.temporaryRedirect(...)` → `HttpStatus.TEMPORARY_REDIRECT` (307).
- Assert the exact path in the `Location` header to verify the precise redirect target.

## A controller sets a cookie

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

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
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
        HttpRequest<?> request = HttpRequest.POST("/locale", Map.of("locale", "es"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpResponse<?> response = assertDoesNotThrow(() -> client.exchange(request));
        assertEquals(HttpStatus.SEE_OTHER, response.status());
        final String cookieName = "locale";
        assertTrue(response.getCookie(cookieName).isPresent());
        assertEquals("es", response.getCookie(cookieName).get().getValue());
    }
}
```

### Notes

- `response.getCookie(name)` returns an `Optional<Cookie>`; assert `isPresent()` before calling
  `get().getValue()`.
- Without `micronaut.http.client.follow-redirects=false`, the client transparently follows the
  redirect, and the cookie set on the `SEE_OTHER` response is lost from what the test observes.
- Use `client.exchange(...)` (not `retrieve`) so you have the full `HttpResponse` to inspect
  status, headers, and cookies.
- This pairs naturally with the redirect-testing pattern — see "A redirection" above for
  asserting the `Location` header on the same response.

## Static resources are served

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

### Notes

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

## Disabling security in a test

To disable security in a test, set the `micronaut.security.filter.enabled` property to
`false`. This turns off the Micronaut Security filter so secured endpoints can be exercised
without authentication.

Apply it with `@Property` on the test class:

```java
@Property(name = "micronaut.security.filter.enabled", value = StringUtils.FALSE)
```

### Example

```java
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;

@Property(name = "micronaut.security.filter.enabled", value = StringUtils.FALSE)
@Property(name = "spec.name", value = "ContactControllerTest")
@MicronautTest
class ContactControllerTest {

    @Test
    void contact(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpRequest<?> request = HttpRequest.GET("/contact/list").accept(MediaType.TEXT_HTML);
        // ...
    }
}
```

### Notes

- Use `io.micronaut.core.util.StringUtils.FALSE` for the property value instead of the string
  literal `"false"`.
- The `spec.name` property is unrelated to disabling security — it scopes
  `@Requires(property = "spec.name", value = "...")` beans (such as mocks or stub
  authentication providers) to a single test so they don't leak into other tests.
- Disabling the security filter bypasses authentication **and** authorization, so `@Secured`
  rules are not enforced. When you instead want to test as an authenticated user (and verify
  authorization rules), keep security enabled and supply credentials or a mock authentication
  rather than disabling the filter.

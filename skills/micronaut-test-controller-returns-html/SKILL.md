---
name: micronaut-test-controller-returns-html
description: Write a test that asserts a Micronaut Views controller returns the expected HTML. Use when the user wants to test an HTML endpoint, verify rendered view output, assert page content, or replace a repository/collaborator with a stub in a controller test.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test), Micronaut HTTP Client, and Micronaut Views on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

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

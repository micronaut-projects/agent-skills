---
name: micronaut-test-disable-security
description: Disable Micronaut Security in a test so endpoints can be exercised without authentication. Use when the user wants to turn off security/authentication in a test, bypass the security filter, or test a secured controller without logging in.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Security on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

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

## Notes

- Use `io.micronaut.core.util.StringUtils.FALSE` for the property value instead of the string
  literal `"false"`.
- The `spec.name` property is unrelated to disabling security — it scopes
  `@Requires(property = "spec.name", value = "...")` beans (such as mocks or stub
  authentication providers) to a single test so they don't leak into other tests.
- Disabling the security filter bypasses authentication **and** authorization, so `@Secured`
  rules are not enforced. When you instead want to test as an authenticated user (and verify
  authorization rules), keep security enabled and supply credentials or a mock authentication
  rather than disabling the filter.

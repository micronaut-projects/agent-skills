---
name: micronaut-testing
description: Write tests for a Micronaut application with Micronaut Test and JUnit 5. Use when the user wants to test beans and the application context, bean validation, introspection or JSON serialization with JsonMapper, HTTP endpoints (HTML, redirects, cookies, static resources, security), or view rendering.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) with JUnit 5 on the test classpath. Some patterns also need Micronaut HTTP Client, Validation, Serialization, Security or Views.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Conventions for every Micronaut test

- **Start only what you need.** Use `@MicronautTest(startApplication = false)` when the test only
  needs beans from the context (validation, serde, bean metadata, view rendering). Use plain
  `@MicronautTest` only when the embedded server must run (HTTP client tests). Use no annotation
  at all for compile-time metadata such as `BeanIntrospection`.
- **Inject, don't instantiate.** Get collaborators from the context (`@Inject` fields or test-method
  parameters) so Micronaut's proxies, validation, and configuration apply.
- **Scope test doubles with `spec.name`.** Annotate the test with
  `@Property(name = "spec.name", value = "MyTest")` and guard any `@Replaces` stub with
  `@Requires(property = "spec.name", value = "MyTest")`, so the stub doesn't leak into other tests.
- **Use Micronaut APIs over third-party equivalents:** `io.micronaut.json.JsonMapper` (not
  Jackson's `ObjectMapper`), the Micronaut `HttpClient` (`@Client("/")`, `.toBlocking()`), and
  `StringUtils.TRUE` / `StringUtils.FALSE` for boolean `@Property` values.
- **Assert outcomes with `assertDoesNotThrow` / `assertThrows`** where Micronaut signals failure
  by throwing (missing beans, introspections, non-2xx responses, constraint violations).

## Patterns

Read only the reference that matches the task:

| Testing… | Reference |
| --- | --- |
| A bean exists, resolves uniquely (`@Primary`/`@Secondary`), carries an annotation; active environments | [references/beans-and-context.md](references/beans-and-context.md) |
| A field is required or optional; method-parameter constraints throw `ConstraintViolationException` | [references/validation.md](references/validation.md) |
| `@Introspected`, `@Serdeable` metadata; object → JSON and JSON → object with `JsonMapper` | [references/serialization.md](references/serialization.md) |
| Controllers returning HTML, redirects (`Location`), cookies, static resources; disabling security | [references/http.md](references/http.md) |
| Rendering a template with `ViewsRenderer` without the server | [references/views.md](references/views.md) |

---
name: micronaut-testing
description: Conventions for writing tests for a Micronaut application with Micronaut Test and JUnit 5. Use when writing or reviewing tests for beans and the application context, bean validation, introspection or JSON serialization, HTTP endpoints (redirects, cookies, static resources, security), or view rendering.
license: Apache-2.0
compatibility: Requires JUnit 5. Most conventions assume Micronaut Test (io.micronaut.test); some patterns also need Micronaut HTTP Client, Validation, Serialization, Security or Views.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

These are the conventions we look for when reviewing Micronaut tests. Follow them in addition to
normal JUnit 5 practice.

## Context and lifecycle

- **Start only what you need.** Use `@MicronautTest(startApplication = false)` when the test only
  needs beans (validation, serde, bean metadata, view rendering), because starting the embedded
  server is slow and adds nothing. Use plain `@MicronautTest` only when an HTTP client must reach
  the server. `BeanIntrospection` needs no context at all, so test it in a plain JUnit class.
- **Without Micronaut Test**, start the context with `ApplicationContext.run()` in a
  try-with-resources block so it is always closed; leaked contexts slow down and pollute the
  suite.
- **Inject, don't instantiate.** Get beans from the context (`@Inject` fields or test-method
  parameters). Calling `new` bypasses Micronaut's proxies, so method validation, AOP and
  configuration silently don't apply.
- **Scope test doubles with `spec.name`.** Put `@Property(name = "spec.name", value = "<TestClass>")`
  on the test and `@Requires(property = "spec.name", value = "<TestClass>")` on every `@Replaces`
  stub, test controller or test `AuthenticationProvider`, so it can't leak into other tests.
- **Set boolean properties with `StringUtils.TRUE` / `StringUtils.FALSE`** (`io.micronaut.core.util`)
  instead of string literals.
- **Never change production code to make a test pass** (for example, raising an annotation's
  retention so reflection can see it). Use Micronaut's own metadata instead.

## Beans and annotation metadata

- **Presence or absence:** assert `beanContext.containsBean(Type.class)`. Don't inject the type to
  prove it's absent — that fails context setup instead of asserting anything.
- **Exactly one candidate:** wrap `beanContext.getBean(Type.class)` in `assertDoesNotThrow`; it
  throws `NonUniqueBeanException` for ambiguous candidates. The fix is `@Primary` or `@Secondary`.
- **Conditional beans:** set the property the `@Requires` checks, otherwise the bean doesn't exist.
- **Annotations:** read them from `beanContext.getBeanDefinition(Type.class)`, never from Java
  reflection. Use `hasStereotype` / `getAnnotationNameByStereotype` when the annotation may come
  through a meta-annotation; Micronaut's metadata also sees annotations that aren't
  `RUNTIME`-retained.

## Serialization

- **Use Micronaut Serialization APIs** — `io.micronaut.json.JsonMapper`, not Jackson's
  `ObjectMapper` — so the test exercises the same serde the application uses.
- **Checking serde support, not JSON shape:** assert
  `SerdeIntrospections.getSerializableIntrospection` / `getDeserializableIntrospection` don't
  throw. For a one-direction type (`@Serdeable.Serializable` or `.Deserializable`), also assert
  the other direction throws.
- **Generic types:** pass `Argument.listOf(Type.class)` (or another generic `Argument`) to
  `readValue`, not `List.class`.

## Validation

- The type must be `@Introspected` (or `@Serdeable`); otherwise the validator has no metadata.
  If it's missing, add it and tell the user why.
- Inject `Validator` rather than building one with `Validation.buildDefaultValidatorFactory()`.
- For `@NotBlank`, test `null`, `""` and whitespace-only. To prove *which* field failed, assert
  on the violation's `getPropertyPath()`.
- Method-parameter constraints only run on an injected bean: assert `ConstraintViolationException`
  with `assertThrows`.

## HTTP

- Use the Micronaut `HttpClient` (`@Client("/")`, `.toBlocking()`); use `exchange` when you need
  status, headers or cookies, and `retrieve` when you only need the body.
- **Redirects and cookies on a 3xx:** set `micronaut.http.client.follow-redirects` to
  `StringUtils.FALSE`; otherwise the client follows the redirect and the test sees the final
  response. Assert the status that matches the factory method (`seeOther` → 303, `redirect` →
  301, `temporaryRedirect` → 307) and the exact `Location`.
- **Skipping authentication** when security is tested elsewhere: set
  `micronaut.security.filter.enabled` to `StringUtils.FALSE`. This also bypasses `@Secured`.
- **Testing security itself** (anonymous rejected, authenticated allowed, anonymous access to a
  static path): keep security enabled and send real credentials, e.g. basic auth backed by a
  `spec.name`-scoped `AuthenticationProvider`.

## Views

- To test a template without HTTP, inject the `ViewsRenderer` for the project's engine (Thymeleaf,
  Handlebars, Velocity, …), call `render(view, model, null)` and write the `Writable` to a
  `StringWriter`.

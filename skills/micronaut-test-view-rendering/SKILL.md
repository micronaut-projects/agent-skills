---
name: micronaut-test-view-rendering
description: Write a test that renders a Micronaut view (template) with a model and asserts on the produced HTML, without starting the server. Use when the user wants to unit-test how a template renders, verify view output for a given model, or check rendered markup with the ViewsRenderer.
license: Apache-2.0
compatibility: Requires Micronaut Test (io.micronaut.test) and Micronaut Views (e.g. io.micronaut.views.thymeleaf) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To test how a view renders, inject the `ViewsRenderer` (e.g. `ThymeleafViewsRenderer`), call
`render(viewName, model, request)`, convert the resulting `Writable` to a `String`, and assert
on the HTML. This renders the template **directly**, without HTTP or the embedded server.

- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.
- Inject the renderer for your view engine, e.g.
  `ThymeleafViewsRenderer<Map<String, Object>>`.
- `render(...)` returns a `Writable`; pass `null` for the request when the template does not need
  one.

### Step 1 — helper to turn a `Writable` into a `String`

```java
package com.micronauttodo.views;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.Writable;

import java.io.IOException;
import java.io.StringWriter;

public final class WritableUtils {
    private WritableUtils() {
    }

    @NonNull
    public static String writableToString(@NonNull Writable writable) throws IOException {
        StringWriter stringWriter = new StringWriter();
        writable.writeTo(stringWriter);
        return stringWriter.toString();
    }
}
```

### Step 2 — render the view and assert on the HTML

```java
package com.micronauttodo.views;

import io.micronaut.core.io.Writable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.views.thymeleaf.ThymeleafViewsRenderer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class TodoIndexRenderTest {
    private static final Logger LOG = LoggerFactory.getLogger(TodoIndexRenderTest.class);

    @Inject
    ThymeleafViewsRenderer<Map<String, Object>> viewsRenderer;

    @Test
    void testRenderTodoIndex() throws IOException {
        Writable writable = viewsRenderer.render("todo/index", Map.of("todos", Collections.emptyList()), null);
        String html = WritableUtils.writableToString(writable);
        boolean isHtml5 = html.contains("<!DOCTYPE html>");
        if (!isHtml5) {
            LOG.trace("{}", html);
        }
        assertTrue(isHtml5);
    }
}
```

## Notes

- This renders the template directly via the `ViewsRenderer`, so it is faster than an HTTP test
  and needs no server (`startApplication = false`). Use it to unit-test template output for a
  given model.
- Inject the renderer matching your view engine — `ThymeleafViewsRenderer`,
  `VelocityViewsRenderer`, `HandlebarsViewsRenderer`, `SoyViewsRenderer`, etc. The view name is
  the template path without the engine's extension (e.g. `todo/index`).
- Pass the model as the second argument; pass `null` (or a stub `HttpRequest`) as the third when
  the template doesn't depend on the request.
- Logging the HTML at `trace` on failure makes diagnosing a bad render easier without spamming
  output on success.
- To instead exercise the full controller + view over HTTP (status, security, content
  assertions), use an HTTP client test — see the `micronaut-test-controller-returns-html` skill.

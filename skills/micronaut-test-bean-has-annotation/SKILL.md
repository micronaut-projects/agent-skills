---
name: micronaut-test-bean-has-annotation
description: Write a test that verifies a bean is annotated with (or meta-annotated by) a particular annotation, using its BeanDefinition. Use when the user wants to confirm a bean carries an annotation/stereotype, check @BootstrapContextCompatible or similar metadata, or assert annotation presence on a bean.
license: Apache-2.0
compatibility: Requires Micronaut Core (io.micronaut.context, io.micronaut.inject) and Micronaut Test (io.micronaut.test) on the test classpath.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read Write Edit
---

## Instructions

To verify a bean carries a particular annotation, obtain its `BeanDefinition` from the context
and query the annotation metadata.

- Get the definition with `applicationContext.getBeanDefinition(MyBean.class)` (or
  `beanContext.getBeanDefinition(...)`).
- `BeanDefinition` exposes the bean's `AnnotationMetadata`. Common checks:
  - `hasAnnotation(MyAnnotation.class)` — the annotation is declared directly on the bean.
  - `hasStereotype(MyAnnotation.class)` — the annotation is present directly **or** as a
    meta-annotation (stereotype).
  - `getAnnotationNameByStereotype(MyAnnotation.class)` — returns an `Optional<String>` with the
    name of the annotation that carries the given stereotype.
- Use `@MicronautTest(startApplication = false)` — only the context is needed, no server.

### Example

Bean annotated with `@BootstrapContextCompatible` (and `@EachProperty`):

```java
@BootstrapContextCompatible
@EachProperty(AWSServiceConfigurationProperties.SERVICE_PREFIX)
public class AWSServiceConfigurationProperties implements AWSServiceConfiguration {
    // ...
}
```

Test:

```java
package example.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AWSServiceConfigurationPropertiesTest {

    @Inject
    BeanContext beanContext;

    @Test
    void beanIsBootstrapContextCompatible() {
        BeanDefinition<AWSServiceConfigurationProperties> beanDefinition =
                beanContext.getBeanDefinition(AWSServiceConfigurationProperties.class);
        assertTrue(beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible.class).isPresent());
    }
}
```

## Notes

- Prefer querying the `BeanDefinition` (compile-time `AnnotationMetadata`) over Java reflection
  (`clazz.getAnnotation(...)`). Micronaut's metadata captures meta-annotations/stereotypes and
  retains annotations that are not `RUNTIME`-retained, which reflection would miss.
- `hasAnnotation` vs `hasStereotype`: use `hasAnnotation` for a directly-declared annotation;
  use `hasStereotype` (or `getAnnotationNameByStereotype`) when the annotation may be applied
  indirectly via another annotation that is meta-annotated with it.
- `getBeanDefinition(Type.class)` throws if the type is not a bean or is ambiguous — it asserts
  the bean exists as a side effect. For a pure existence check use `containsBean` (see
  `micronaut-test-bean-exists`); for uniqueness see `micronaut-test-bean-unique-or-primary`.
- To assert on an annotation's member values, read them from the metadata, e.g.
  `beanDefinition.stringValue(MyAnnotation.class, "value")` or
  `beanDefinition.getValue(MyAnnotation.class, Integer.class)`.

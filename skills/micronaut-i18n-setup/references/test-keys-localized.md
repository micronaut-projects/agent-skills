# Test every key is localized in every language

To verify every key in the base `messages.properties` is also present in each localized
bundle (e.g. `messages_es.properties`), load both files as `Properties` and assert their key
sets are equal.

- Use a `@MicronautTest(startApplication = false)` test — no server is needed, only the
  application context to inject a `ResourceLoader`.
- Inject `ResourceLoader` and read each bundle with
  `getResourceAsStream("classpath:i18n/...")`.
- Compare the **key sets** of the base bundle and each localized bundle with `assertEquals`.
  Asserting equality (rather than `containsAll`) catches both *missing* translations and
  *extra* localized keys that don't exist in the base bundle.

### Example

```java
package com.micronauttodo.i18n;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class PropertiesKeysLocalizedInEveryLanguageTest {

    @Test
    void keysAreLocalized(ResourceLoader resourceLoader) throws IOException {
        List<String> suffixes = List.of("es");
        Optional<InputStream> messagesInputStreamOptional =
                resourceLoader.getResourceAsStream("classpath:i18n/messages.properties");
        assertTrue(messagesInputStreamOptional.isPresent());
        try (InputStream inputStream = messagesInputStreamOptional.get()) {
            Properties messages = new Properties();
            messages.load(inputStream);
            Set<String> keys = messages.keySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
            for (String suffix : suffixes) {
                Optional<InputStream> messagesLocalizedInputStreamOptional =
                        resourceLoader.getResourceAsStream("classpath:i18n/messages_" + suffix + ".properties");
                assertTrue(messagesLocalizedInputStreamOptional.isPresent());
                try (InputStream messagesLocalizedInputStream = messagesLocalizedInputStreamOptional.get()) {
                    Properties messagesLocalized = new Properties();
                    messagesLocalized.load(messagesLocalizedInputStream);
                    Set<String> keysLocalized = messagesLocalized.keySet().stream()
                            .map(Object::toString)
                            .collect(Collectors.toSet());
                    assertEquals(keys, keysLocalized);
                }
            }
        }
    }
}
```

## Notes

- **Compute `keysLocalized` from the localized bundle** (`messagesLocalized.keySet()`), not
  from the base `messages` — building it from `messages` makes the assertion compare a set
  against itself and always pass, defeating the test.
- Add every language suffix you ship to the `suffixes` list (e.g.
  `List.of("es", "fr", "de")`) so each localized bundle is checked.
- `assertEquals(keys, keysLocalized)` fails if a localized bundle is missing a key *or* has an
  extra key. When the failure message is hard to read, assert the symmetric differences
  instead to report exactly which keys are missing or extra.

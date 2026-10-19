# Micronaut Gradle Plugin — `run` task

The `run` task is contributed by the `io.micronaut.application` Gradle plugin, which configures it automatically.

Full documentation: https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/

## Basic usage

```bash
./gradlew run
```

By default, the application starts in port 8080, unless the property `micronaut.server.port` is set to a different value in configuration.

## Continuous build mode

Use the `-t` flag to enable continuous mode. The application automatically rebuilds and restarts whenever source files change — useful during development:

```bash
./gradlew run -t
```

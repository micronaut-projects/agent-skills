---
name: micronaut-gradle-run
description: Run a Micronaut Gradle application locally. Use when the user wants to start, launch, or run the application.
license: Apache-2.0
compatibility: Requires Gradle wrapper (./gradlew) in the project root.
metadata:
  author: Sergio del Amo
allowed-tools: Bash Read
---

## Instructions

1. Run the application using the Gradle `run` task:

```bash
./gradlew run
```

If you use Windows, use

```bash
gradlew.bat run
```

See [Micronaut Gradle Plugin reference](references/micronaut-gradle-plugin.md) for more details.


## Notes

- To stop the server, the user should press `Ctrl+C` in the terminal running the process.

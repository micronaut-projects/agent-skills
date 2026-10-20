package com.example.core;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

@Introspected
public record App(@Nullable String packageName) {
}

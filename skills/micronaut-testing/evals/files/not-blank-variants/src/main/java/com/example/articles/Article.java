package com.example.articles;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;

@Introspected
public record Article(@NonNull @NotBlank String title, String body) {
}

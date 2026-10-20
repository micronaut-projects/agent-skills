package com.example.topics;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record Topic(@NonNull @NotBlank String id, @NonNull @NotBlank String name) {
}

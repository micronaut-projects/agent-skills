package com.example.addresses;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;

@Introspected
public record Address(@NonNull @NotBlank String street,
                      @NonNull @NotBlank String city,
                      @NonNull @NotBlank String postalCode) {
}

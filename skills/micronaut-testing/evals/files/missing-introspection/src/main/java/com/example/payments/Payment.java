package com.example.payments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record Payment(@NotNull @Positive BigDecimal amount, String reference) {
}

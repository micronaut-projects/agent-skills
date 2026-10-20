package com.example.reports;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Report(String name, int total) {
}

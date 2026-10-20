package com.example.contacts;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record Contact(String firstName, String lastName, String title, String company) {
}

package com.example.sms;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Requires(property = "sms.enabled", value = "true")
@Singleton
public class SmsSender {
    public void send(String phoneNumber, String text) {
        // calls the SMS gateway
    }
}

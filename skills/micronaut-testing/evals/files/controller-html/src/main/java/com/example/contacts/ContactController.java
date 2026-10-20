package com.example.contacts;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.View;

import java.util.Collections;
import java.util.Map;

@Controller("/contact")
class ContactController {
    private final ContactRepository contactRepository;

    ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Produces(MediaType.TEXT_HTML)
    @Get("/list")
    @View("contact/list")
    Map<String, Object> list() {
        return Collections.singletonMap("contacts", contactRepository.findAll());
    }
}

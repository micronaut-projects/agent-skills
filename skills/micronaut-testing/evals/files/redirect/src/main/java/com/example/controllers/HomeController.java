package com.example.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller
class HomeController {
    static final String URI_TODO_LIST = "/todo/list";

    @Produces(MediaType.TEXT_HTML)
    @Get
    HttpResponse<?> index() {
        return HttpResponse.seeOther(java.net.URI.create(URI_TODO_LIST));
    }
}

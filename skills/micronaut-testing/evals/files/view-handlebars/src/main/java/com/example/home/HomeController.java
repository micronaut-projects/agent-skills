package com.example.home;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

import java.util.Map;

@Controller
class HomeController {
    @View("home")
    @Get
    Map<String, Object> index() {
        return Map.of("title", "Welcome");
    }
}

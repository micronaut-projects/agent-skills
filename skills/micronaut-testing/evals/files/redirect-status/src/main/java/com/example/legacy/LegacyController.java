package com.example.legacy;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.net.URI;

@Controller
class LegacyController {
    @Get("/old-pricing")
    HttpResponse<?> oldPricing() {
        return HttpResponse.redirect(URI.create("/pricing"));
    }

    @Get("/maintenance")
    HttpResponse<?> maintenance() {
        return HttpResponse.temporaryRedirect(URI.create("/status"));
    }
}

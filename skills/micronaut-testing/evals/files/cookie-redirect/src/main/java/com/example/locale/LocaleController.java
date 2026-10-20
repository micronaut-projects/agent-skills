package com.example.locale;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.cookie.Cookie;

import java.net.URI;

@Controller("/locale")
class LocaleController {
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Post
    HttpResponse<?> changeLocale(@Body("locale") String locale) {
        return HttpResponse.seeOther(URI.create("/"))
                .cookie(Cookie.of("locale", locale));
    }
}

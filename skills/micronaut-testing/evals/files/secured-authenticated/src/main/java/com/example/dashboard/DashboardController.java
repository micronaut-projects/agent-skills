package com.example.dashboard;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/dashboard")
class DashboardController {
    @Produces(MediaType.TEXT_PLAIN)
    @Get
    String index(Principal principal) {
        return "Welcome " + principal.getName();
    }
}

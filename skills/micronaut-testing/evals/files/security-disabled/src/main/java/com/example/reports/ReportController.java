package com.example.reports;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/reports")
class ReportController {
    @Get
    List<Report> index() {
        return List.of(new Report("sales", 42), new Report("refunds", 3));
    }
}

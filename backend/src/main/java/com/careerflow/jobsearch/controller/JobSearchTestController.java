package com.careerflow.jobsearch.controller;

import com.careerflow.jobsearch.provider.AdzunaJobSearchProvider;
import com.careerflow.jobsearch.provider.BundesagenturJobSearchProvider;
import com.careerflow.jobsearch.provider.JoobleJobSearchProvider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobSearchTestController {

    private final BundesagenturJobSearchProvider bundesagenturProvider;
    private final AdzunaJobSearchProvider adzunaProvider;
    private final JoobleJobSearchProvider joobleProvider;

    public JobSearchTestController(
            BundesagenturJobSearchProvider bundesagenturProvider,
            AdzunaJobSearchProvider adzunaProvider,
            JoobleJobSearchProvider joobleProvider
    ) {
        this.bundesagenturProvider = bundesagenturProvider;
        this.adzunaProvider = adzunaProvider;
        this.joobleProvider = joobleProvider;
    }

    @GetMapping("/api/job-search/test/bundesagentur")
    public String testBundesagenturSearch(
            @RequestParam String query,
            @RequestParam String location
    ) {
        return bundesagenturProvider.searchJobs(query, location, 1, 5);
    }

    @GetMapping("/api/job-search/test/adzuna")
    public String testAdzunaSearch(
            @RequestParam String query,
            @RequestParam String location
    ) {
        return adzunaProvider.searchJobs(query, location, 1, 5);
    }

    @GetMapping("/api/job-search/test/jooble")
    public String testJoobleSearch(
            @RequestParam String query,
            @RequestParam String location,
            @RequestParam(defaultValue = "1") int page
    ) {
        return joobleProvider.searchJobs(query, location, page);
    }
}
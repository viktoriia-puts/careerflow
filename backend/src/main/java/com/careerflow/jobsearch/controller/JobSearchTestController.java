package com.careerflow.jobsearch.controller;

import com.careerflow.jobsearch.provider.AdzunaJobSearchProvider;
import com.careerflow.jobsearch.provider.BundesagenturJobSearchProvider;
import com.careerflow.jobsearch.provider.JoobleJobSearchProvider;
import com.careerflow.jobsearch.provider.ArbeitnowJobSearchProvider;
import com.careerflow.jobsearch.service.JobPrefilterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.careerflow.jobsearch.provider.RemotiveJobSearchProvider;
import com.careerflow.jobsearch.dto.JobSearchResult;
import java.util.List;
import org.springframework.http.MediaType;


@RestController
public class JobSearchTestController {

    private final BundesagenturJobSearchProvider bundesagenturProvider;
    private final AdzunaJobSearchProvider adzunaProvider;
    private final JoobleJobSearchProvider joobleProvider;
    private final ArbeitnowJobSearchProvider arbeitnowProvider;
    private final RemotiveJobSearchProvider remotiveProvider;
    private final JobPrefilterService jobPrefilterService;

    public JobSearchTestController(
            BundesagenturJobSearchProvider bundesagenturProvider,
            AdzunaJobSearchProvider adzunaProvider,
            JoobleJobSearchProvider joobleProvider,
            ArbeitnowJobSearchProvider arbeitnowProvider,
            RemotiveJobSearchProvider remotiveProvider,
            JobPrefilterService jobPrefilterService) {
        this.bundesagenturProvider = bundesagenturProvider;
        this.adzunaProvider = adzunaProvider;
        this.joobleProvider = joobleProvider;
        this.arbeitnowProvider = arbeitnowProvider;
        this.remotiveProvider = remotiveProvider;
        this.jobPrefilterService = jobPrefilterService;
    }

    @GetMapping(
            value = "/api/job-search/test/arbeitnow/prefiltered",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JobSearchResult> testArbeitnowPrefiltered(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String roles,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "30") int target
    ) {
        List<JobSearchResult> locationFilteredJobs =
                arbeitnowProvider.searchCachedJobsByLocation(location);

        return jobPrefilterService.prefilter(
                locationFilteredJobs,
                roles,
                keywords,
                target
        );
    }

    @GetMapping("/api/job-search/test/remotive")
    public String testRemotiveSearch(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return remotiveProvider.searchJobs(query, limit);
    }

    @GetMapping(
            value = "/api/job-search/test/arbeitnow/filtered",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JobSearchResult> testArbeitnowFiltered(
            @RequestParam(required = false) String location
    ) {
        return arbeitnowProvider.searchCachedJobsByLocation(location);
    }

    @GetMapping(
            value = "/api/job-search/test/arbeitnow",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String testArbeitnowSearch(
            @RequestParam(defaultValue = "1") int page
    ) {
        return arbeitnowProvider.searchJobs(page);
    }

    @GetMapping("/api/job-search/test/bundesagentur")
    public String testBundesagenturSearch(
            @RequestParam String query,
            @RequestParam String location
    ) {
        return bundesagenturProvider.searchJobs(query, location, 1, 5);
    }

    @GetMapping("/api/job-search/test/bundesagentur/details")
    public String testBundesagenturDetails(
            @RequestParam String refnr
    ) {
        return bundesagenturProvider.getJobDetails(refnr);
    }

    @GetMapping(
            value = "/api/job-search/test/arbeitnow/normalized",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JobSearchResult> testArbeitnowNormalized(
            @RequestParam(defaultValue = "1") int page
    ) {
        return arbeitnowProvider.searchJobResults(page);
    }

    @GetMapping("/api/job-search/test/remotive/normalized")
    public List<JobSearchResult> testRemotiveNormalized(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return remotiveProvider.searchJobResults(query, limit);
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
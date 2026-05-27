package com.careerflow.jobsearch.controller;

import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.jobsearch.service.JobSearchRankingApplicationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobSearchRankingController {

    private final JobSearchRankingApplicationService jobSearchRankingApplicationService;

    public JobSearchRankingController(
            JobSearchRankingApplicationService jobSearchRankingApplicationService
    ) {
        this.jobSearchRankingApplicationService = jobSearchRankingApplicationService;
    }

    @GetMapping(
            value = "/api/job-search/ranked",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<RankedJobSearchResult> getRankedJobs(
            @RequestParam Long profileId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer targetPerProvider,
            @RequestParam(required = false) Integer target,
            @RequestParam(required = false) String jobLevel
    ) {
        return jobSearchRankingApplicationService.searchAndRankJobs(
                profileId,
                location,
                targetPerProvider,
                target,
                jobLevel
        );
    }
}

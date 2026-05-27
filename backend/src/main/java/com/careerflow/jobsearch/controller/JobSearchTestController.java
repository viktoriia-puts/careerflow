package com.careerflow.jobsearch.controller;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.provider.ArbeitnowJobSearchProvider;
import com.careerflow.jobsearch.provider.BundesagenturJobSearchProvider;
import com.careerflow.jobsearch.provider.RemotiveJobSearchProvider;
import com.careerflow.jobsearch.service.ArbeitnowPrefilteredSearchService;
import com.careerflow.jobsearch.service.JobPrefilterService;
import com.careerflow.jobsearch.service.JobSeniorityPreference;
import com.careerflow.jobsearch.service.MultiProviderPrefilteredJobSearchService;
import com.careerflow.querygeneration.service.SearchQueryGenerationService;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.service.SearchProfileService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobSearchTestController {

    private final BundesagenturJobSearchProvider bundesagenturProvider;
    private final ArbeitnowJobSearchProvider arbeitnowProvider;
    private final RemotiveJobSearchProvider remotiveProvider;
    private final JobPrefilterService jobPrefilterService;
    private final ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService;
    private final SearchProfileService searchProfileService;
    private final MultiProviderPrefilteredJobSearchService multiProviderPrefilteredJobSearchService;
    private final SearchQueryGenerationService searchQueryGenerationService;

    public JobSearchTestController(
            BundesagenturJobSearchProvider bundesagenturProvider,
            ArbeitnowJobSearchProvider arbeitnowProvider,
            RemotiveJobSearchProvider remotiveProvider,
            JobPrefilterService jobPrefilterService,
            ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService,
            SearchProfileService searchProfileService,
            MultiProviderPrefilteredJobSearchService multiProviderPrefilteredJobSearchService,
            SearchQueryGenerationService searchQueryGenerationService
    ) {
        this.bundesagenturProvider = bundesagenturProvider;
        this.arbeitnowProvider = arbeitnowProvider;
        this.remotiveProvider = remotiveProvider;
        this.jobPrefilterService = jobPrefilterService;
        this.arbeitnowPrefilteredSearchService = arbeitnowPrefilteredSearchService;
        this.searchProfileService = searchProfileService;
        this.multiProviderPrefilteredJobSearchService = multiProviderPrefilteredJobSearchService;
        this.searchQueryGenerationService = searchQueryGenerationService;
    }

    @GetMapping(
            value = "/api/job-search/test/arbeitnow/prefiltered",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JobSearchResult> testArbeitnowPrefiltered(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String roles,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "10") int target
    ) {
        return arbeitnowPrefilteredSearchService.searchPrefilteredArbeitnowJobs(
                location,
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
            value = "/api/job-search/test/prefiltered",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<JobSearchResult> testMultiProviderPrefiltered(
            @RequestParam Long profileId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer targetPerProvider,
            @RequestParam(required = false) Integer target,
            @RequestParam(required = false) String jobLevel
    ) {
        SearchProfile profile =
                searchProfileService.getSearchProfileEntityById(profileId);

        int resolvedTargetPerProvider = resolveTargetPerProvider(
                targetPerProvider,
                target
        );

        List<String> jobSearchQueries = buildJobSearchQueries(
                profile.getSearchRoles(),
                searchQueryGenerationService.getSavedJobSearchQueriesForProfile(profileId)
        );

        return multiProviderPrefilteredJobSearchService.searchPrefilteredJobs(
                location,
                jobSearchQueries,
                profile.getSearchRoles(),
                profile.getKeywords(),
                resolvedTargetPerProvider,
                JobSeniorityPreference.from(jobLevel)
        );
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

    private List<String> buildJobSearchQueries(
            List<String> profileRoles,
            List<String> savedGeneratedQueries
    ) {
        Set<String> queries = new LinkedHashSet<>();

        addQueries(queries, profileRoles);
        addQueries(queries, savedGeneratedQueries);

        return new ArrayList<>(queries);
    }

    private int resolveTargetPerProvider(
            Integer targetPerProvider,
            Integer legacyTarget
    ) {
        if (targetPerProvider != null && targetPerProvider > 0) {
            return targetPerProvider;
        }

        if (legacyTarget != null && legacyTarget > 0) {
            return legacyTarget;
        }

        return 25;
    }

    private void addQueries(Set<String> queries, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            queries.add(value.trim());
        }
    }

}

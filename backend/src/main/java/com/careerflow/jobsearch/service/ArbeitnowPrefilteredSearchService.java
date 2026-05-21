package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.provider.ArbeitnowJobSearchProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArbeitnowPrefilteredSearchService {

    private static final int PAGE_BATCH_SIZE = 5;

    private final ArbeitnowJobSearchProvider arbeitnowProvider;
    private final JobPrefilterService jobPrefilterService;

    public ArbeitnowPrefilteredSearchService(
            ArbeitnowJobSearchProvider arbeitnowProvider,
            JobPrefilterService jobPrefilterService
    ) {
        this.arbeitnowProvider = arbeitnowProvider;
        this.jobPrefilterService = jobPrefilterService;
    }

    public List<JobSearchResult> searchPrefilteredArbeitnowJobs(
            String location,
            String roles,
            String keywords,
            int target
    ) {
        if (target <= 0) {
            return List.of();
        }

        System.out.println("========== ARBEITNOW PROFILE PREFILTER SEARCH ==========");
        System.out.println("Location: " + location);
        System.out.println("Roles: " + roles);
        System.out.println("Keywords: " + keywords);
        System.out.println("Target final matches: " + target);
        System.out.println("Page batch size: " + PAGE_BATCH_SIZE);
        System.out.println("========================================================");

        List<JobSearchResult> locationFilteredJobs =
                arbeitnowProvider.filterCachedJobsByLocation(location);

        List<JobSearchResult> prefilteredJobs =
                jobPrefilterService.prefilter(
                        locationFilteredJobs,
                        roles,
                        keywords,
                        target
                );

        System.out.println("Initial location-filtered jobs: " + locationFilteredJobs.size());
        System.out.println("Initial profile-prefiltered jobs: " + prefilteredJobs.size());

        while (prefilteredJobs.size() < target) {
            System.out.println("========== PROFILE PREFILTER FALLBACK ==========");
            System.out.println("Only found " + prefilteredJobs.size() + " final matches.");
            System.out.println("Fetching next " + PAGE_BATCH_SIZE + " Arbeitnow pages.");
            System.out.println("================================================");

            List<JobSearchResult> newJobs =
                    arbeitnowProvider.addNextCachedJobPages(PAGE_BATCH_SIZE);

            if (newJobs.isEmpty()) {
                System.out.println("No new Arbeitnow jobs returned. Stopping search.");
                break;
            }

            locationFilteredJobs =
                    arbeitnowProvider.filterCachedJobsByLocation(location);

            prefilteredJobs =
                    jobPrefilterService.prefilter(
                            locationFilteredJobs,
                            roles,
                            keywords,
                            target
                    );

            System.out.println("Location-filtered jobs after new batch: " + locationFilteredJobs.size());
            System.out.println("Profile-prefiltered jobs after new batch: " + prefilteredJobs.size());
        }

        System.out.println("========== ARBEITNOW PROFILE PREFILTER RESULT ==========");
        System.out.println("Returned final matches: " + prefilteredJobs.size());
        System.out.println("========================================================");

        return prefilteredJobs;
    }

    public List<JobSearchResult> searchPrefilteredArbeitnowJobs(
            String location,
            List<String> roles,
            List<String> keywords,
            int target
    ) {
        if (target <= 0) {
            return List.of();
        }

        System.out.println("========== ARBEITNOW PROFILE PREFILTER SEARCH ==========");
        System.out.println("Location: " + location);
        System.out.println("Roles: " + roles);
        System.out.println("Keywords: " + keywords);
        System.out.println("Target final matches: " + target);
        System.out.println("Page batch size: " + PAGE_BATCH_SIZE);
        System.out.println("========================================================");

        List<JobSearchResult> locationFilteredJobs =
                arbeitnowProvider.filterCachedJobsByLocation(location);

        List<JobSearchResult> prefilteredJobs =
                jobPrefilterService.prefilter(
                        locationFilteredJobs,
                        roles,
                        keywords,
                        target
                );

        System.out.println("Initial location-filtered jobs: " + locationFilteredJobs.size());
        System.out.println("Initial profile-prefiltered jobs: " + prefilteredJobs.size());

        while (prefilteredJobs.size() < target) {
            System.out.println("========== PROFILE PREFILTER FALLBACK ==========");
            System.out.println("Only found " + prefilteredJobs.size() + " final matches.");
            System.out.println("Fetching next " + PAGE_BATCH_SIZE + " Arbeitnow pages.");
            System.out.println("================================================");

            List<JobSearchResult> newJobs =
                    arbeitnowProvider.addNextCachedJobPages(PAGE_BATCH_SIZE);

            if (newJobs.isEmpty()) {
                System.out.println("No new Arbeitnow jobs returned. Stopping search.");
                break;
            }

            locationFilteredJobs =
                    arbeitnowProvider.filterCachedJobsByLocation(location);

            prefilteredJobs =
                    jobPrefilterService.prefilter(
                            locationFilteredJobs,
                            roles,
                            keywords,
                            target
                    );

            System.out.println("Location-filtered jobs after new batch: " + locationFilteredJobs.size());
            System.out.println("Profile-prefiltered jobs after new batch: " + prefilteredJobs.size());
        }

        System.out.println("========== ARBEITNOW PROFILE PREFILTER RESULT ==========");
        System.out.println("Returned final matches: " + prefilteredJobs.size());
        System.out.println("========================================================");

        return prefilteredJobs;
    }
}
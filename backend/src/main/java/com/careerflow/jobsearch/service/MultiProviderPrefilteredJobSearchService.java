package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.provider.RemotiveJobSearchProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MultiProviderPrefilteredJobSearchService {

    private final ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService;
    private final RemotiveJobSearchProvider remotiveProvider;
    private final JobPrefilterService jobPrefilterService;
    private final BundesagenturPrefilteredSearchService bundesagenturPrefilteredSearchService;

    public MultiProviderPrefilteredJobSearchService(
            ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService,
            RemotiveJobSearchProvider remotiveProvider,
            JobPrefilterService jobPrefilterService, BundesagenturPrefilteredSearchService bundesagenturPrefilteredSearchService
    ) {
        this.arbeitnowPrefilteredSearchService = arbeitnowPrefilteredSearchService;
        this.remotiveProvider = remotiveProvider;
        this.jobPrefilterService = jobPrefilterService;
        this.bundesagenturPrefilteredSearchService = bundesagenturPrefilteredSearchService;
    }

    public List<JobSearchResult> searchPrefilteredJobs(
            String location,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider
    ) {
        List<JobSearchResult> allJobs = new ArrayList<>();

        System.out.println("========== MULTI PROVIDER PREFILTER SEARCH ==========");
        System.out.println("Location: " + location);
        System.out.println("Target per provider: " + targetPerProvider);
        System.out.println("=====================================================");

        List<JobSearchResult> arbeitnowJobs =
                arbeitnowPrefilteredSearchService.searchPrefilteredArbeitnowJobs(
                        location,
                        roles,
                        keywords,
                        targetPerProvider
                );

        System.out.println("Arbeitnow prefiltered jobs: " + arbeitnowJobs.size());

        allJobs.addAll(arbeitnowJobs);

        List<JobSearchResult> remotiveCachedJobs =
                remotiveProvider.getCachedJobResults();

        System.out.println("Remotive cached jobs: " + remotiveCachedJobs.size());

        List<JobSearchResult> remotiveLocationAllowedJobs =
                filterRemotiveJobsByCandidateLocation(remotiveCachedJobs);

        List<JobSearchResult> remotivePrefilteredJobs =
                jobPrefilterService.prefilter(
                        remotiveLocationAllowedJobs,
                        roles,
                        keywords,
                        targetPerProvider
                );

        System.out.println("Remotive prefiltered jobs: " + remotivePrefilteredJobs.size());

        allJobs.addAll(remotivePrefilteredJobs);

        List<JobSearchResult> bundesagenturJobs =
                bundesagenturPrefilteredSearchService.searchPrefilteredBundesagenturJobs(
                        location,
                        roles,
                        keywords,
                        targetPerProvider
                );

        System.out.println("Bundesagentur prefiltered jobs: " + bundesagenturJobs.size());

        allJobs.addAll(bundesagenturJobs);

        List<JobSearchResult> deduplicatedJobs = removeDuplicates(allJobs);

        System.out.println("Total prefiltered jobs after deduplication: " + deduplicatedJobs.size());

        return deduplicatedJobs;
    }

    private List<JobSearchResult> removeDuplicates(List<JobSearchResult> jobs) {
        List<JobSearchResult> uniqueJobs = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (JobSearchResult job : jobs) {
            String key = job.getSource() + "|" + job.getReferenceId();

            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                uniqueJobs.add(job);
            }
        }

        return uniqueJobs;
    }

    private List<JobSearchResult> filterRemotiveJobsByCandidateLocation(List<JobSearchResult> jobs) {
        List<JobSearchResult> filteredJobs = new ArrayList<>();

        for (JobSearchResult job : jobs) {
            String location = normalizeLocation(job.getLocation());

            if (isRemotiveLocationAllowedForGermany(location)) {
                filteredJobs.add(job);
            } else {
                System.out.println("REMOTIVE LOCATION SKIPPED:");
                System.out.println("Title: " + job.getTitle());
                System.out.println("Company: " + job.getCompany());
                System.out.println("Candidate required location: " + job.getLocation());
            }
        }

        System.out.println("Remotive jobs after candidate location filter: " + filteredJobs.size());

        return filteredJobs;
    }

    private boolean isRemotiveLocationAllowedForGermany(String location) {
        if (location.isBlank()) {
            return true;
        }

        return location.contains("worldwide")
                || location.contains("anywhere")
                || location.contains("global")
                || location.contains("europe")
                || location.contains("emea")
                || location.contains("european")
                || location.contains("germany")
                || location.contains("deutschland")
                || location.contains("german")
                || containsWholeWord(location, "eu");
    }

    private boolean containsWholeWord(String text, String word) {
        String normalizedText = " " + text + " ";
        String normalizedWord = " " + word + " ";

        return normalizedText.contains(normalizedWord);
    }

    private String normalizeLocation(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
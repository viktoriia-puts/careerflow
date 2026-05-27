package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobPrefilterResult;
import com.careerflow.jobsearch.dto.JobSearchPrefilterStatistics;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.PrefilteredJobSearchResult;
import com.careerflow.jobsearch.dto.ProviderPrefilterStatistics;
import com.careerflow.jobsearch.dto.ProviderSearchResult;
import com.careerflow.jobsearch.provider.RemotiveJobSearchProvider;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MultiProviderPrefilteredJobSearchService {

    private final ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService;
    private final RemotiveJobSearchProvider remotiveProvider;
    private final JobPrefilterService jobPrefilterService;
    private final BundesagenturPrefilteredSearchService bundesagenturPrefilteredSearchService;
    private final ExecutorService providerSearchExecutor;

    public MultiProviderPrefilteredJobSearchService(
            ArbeitnowPrefilteredSearchService arbeitnowPrefilteredSearchService,
            RemotiveJobSearchProvider remotiveProvider,
            JobPrefilterService jobPrefilterService, BundesagenturPrefilteredSearchService bundesagenturPrefilteredSearchService
    ) {
        this.arbeitnowPrefilteredSearchService = arbeitnowPrefilteredSearchService;
        this.remotiveProvider = remotiveProvider;
        this.jobPrefilterService = jobPrefilterService;
        this.bundesagenturPrefilteredSearchService = bundesagenturPrefilteredSearchService;
        this.providerSearchExecutor = Executors.newFixedThreadPool(
                3,
                providerSearchThreadFactory()
        );
    }

    @PreDestroy
    public void shutdownProviderSearchExecutor() {
        providerSearchExecutor.shutdown();
    }

    public List<JobSearchResult> searchPrefilteredJobs(
            String location,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider
    ) {
        return searchPrefilteredJobs(
                location,
                roles,
                roles,
                keywords,
                targetPerProvider
        );
    }

    public List<JobSearchResult> searchPrefilteredJobs(
            String location,
            List<String> jobSearchQueries,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider
    ) {
        return searchPrefilteredJobs(
                location,
                jobSearchQueries,
                roles,
                keywords,
                targetPerProvider,
                JobSeniorityPreference.JUNIOR
        );
    }

    public List<JobSearchResult> searchPrefilteredJobs(
            String location,
            List<String> jobSearchQueries,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider,
            JobSeniorityPreference seniorityPreference
    ) {
        return searchPrefilteredJobsWithStatistics(
                location,
                jobSearchQueries,
                roles,
                keywords,
                targetPerProvider,
                seniorityPreference
        ).getJobs();
    }

    public PrefilteredJobSearchResult searchPrefilteredJobsWithStatistics(
            String location,
            List<String> jobSearchQueries,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider
    ) {
        return searchPrefilteredJobsWithStatistics(
                location,
                jobSearchQueries,
                roles,
                keywords,
                targetPerProvider,
                JobSeniorityPreference.JUNIOR
        );
    }

    public PrefilteredJobSearchResult searchPrefilteredJobsWithStatistics(
            String location,
            List<String> jobSearchQueries,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider,
            JobSeniorityPreference seniorityPreference
    ) {
        List<JobSearchResult> allJobs = new ArrayList<>();

        System.out.println("========== MULTI PROVIDER PREFILTER SEARCH ==========");
        System.out.println("Location: " + location);
        System.out.println("Job search queries: " + jobSearchQueries);
        System.out.println("Profile roles: " + roles);
        System.out.println("Target per provider: " + targetPerProvider);
        System.out.println("Seniority preference: " + seniorityPreference);
        System.out.println("=====================================================");

        CompletableFuture<ProviderSearchResult> arbeitnowFuture =
                CompletableFuture.supplyAsync(() -> searchArbeitnowJobs(
                        location,
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                ), providerSearchExecutor);

        CompletableFuture<ProviderSearchResult> remotiveFuture =
                CompletableFuture.supplyAsync(() -> searchRemotiveJobs(
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                ), providerSearchExecutor);

        CompletableFuture<ProviderSearchResult> bundesagenturFuture =
                CompletableFuture.supplyAsync(() -> searchBundesagenturJobs(
                        location,
                        jobSearchQueries,
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                ), providerSearchExecutor);

        System.out.println("Provider searches started in parallel.");

        ProviderSearchResult arbeitnowResult =
                awaitProviderResults("Arbeitnow", arbeitnowFuture);
        ProviderSearchResult remotiveResult =
                awaitProviderResults("Remotive", remotiveFuture);
        ProviderSearchResult bundesagenturResult =
                awaitProviderResults("Bundesagentur", bundesagenturFuture);

        List<ProviderSearchResult> providerResults = List.of(
                arbeitnowResult,
                remotiveResult,
                bundesagenturResult
        );

        if (providerResults.stream()
                .allMatch(result -> result.getStatistics().isFailed())) {
            throw new IllegalStateException("All job providers failed. Cannot search and rank jobs.");
        }

        allJobs.addAll(arbeitnowResult.getJobs());
        allJobs.addAll(remotiveResult.getJobs());
        allJobs.addAll(bundesagenturResult.getJobs());

        List<JobSearchResult> deduplicatedJobs = removeDuplicates(allJobs);

        System.out.println("Total prefiltered jobs after deduplication: " + deduplicatedJobs.size());

        JobSearchPrefilterStatistics statistics = new JobSearchPrefilterStatistics(
                providerResults.stream()
                        .map(ProviderSearchResult::getStatistics)
                        .toList(),
                deduplicatedJobs.size()
        );

        logPrefilterStatistics(statistics);

        return new PrefilteredJobSearchResult(deduplicatedJobs, statistics);
    }

    private ProviderSearchResult searchArbeitnowJobs(
            String location,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider,
            JobSeniorityPreference seniorityPreference
    ) {
        return arbeitnowPrefilteredSearchService.searchPrefilteredArbeitnowJobsWithStatistics(
                        location,
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                );
    }

    private ProviderSearchResult searchRemotiveJobs(
            List<String> roles,
            List<String> keywords,
            int targetPerProvider,
            JobSeniorityPreference seniorityPreference
    ) {
        List<JobSearchResult> remotiveCachedJobs =
                remotiveProvider.getCachedJobResults();

        System.out.println("Remotive cached jobs: " + remotiveCachedJobs.size());

        List<JobSearchResult> remotiveLocationAllowedJobs =
                filterRemotiveJobsByCandidateLocation(remotiveCachedJobs);

        JobPrefilterResult prefilterResult =
                jobPrefilterService.prefilterWithStatistics(
                        remotiveLocationAllowedJobs,
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                );
        List<JobSearchResult> remotivePrefilteredJobs = prefilterResult.getJobs();

        System.out.println("Remotive prefiltered jobs: " + remotivePrefilteredJobs.size());

        ProviderPrefilterStatistics statistics = new ProviderPrefilterStatistics(
                "Remotive",
                remotiveCachedJobs.size(),
                remotiveLocationAllowedJobs.size(),
                prefilterResult.getAfterSeniorFilterCount(),
                prefilterResult.getAfterProfileFilterCount(),
                remotivePrefilteredJobs.size(),
                false,
                null
        );

        return new ProviderSearchResult(remotivePrefilteredJobs, statistics);
    }

    private ProviderSearchResult searchBundesagenturJobs(
            String location,
            List<String> jobSearchQueries,
            List<String> roles,
            List<String> keywords,
            int targetPerProvider,
            JobSeniorityPreference seniorityPreference
    ) {
        return bundesagenturPrefilteredSearchService.searchPrefilteredBundesagenturJobsWithStatistics(
                        location,
                        jobSearchQueries,
                        roles,
                        keywords,
                        targetPerProvider,
                        seniorityPreference
                );
    }

    private ProviderSearchResult awaitProviderResults(
            String providerName,
            CompletableFuture<ProviderSearchResult> future
    ) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.out.println(providerName + " provider search failed.");
            System.out.println("Reason: " + cause.getMessage());
            return new ProviderSearchResult(
                    List.of(),
                    ProviderPrefilterStatistics.failed(
                            providerName,
                            cause.getMessage()
                    )
            );
        }
    }

    private void logPrefilterStatistics(JobSearchPrefilterStatistics statistics) {
        System.out.println("========== PREFILTER STATISTICS ==========");
        for (ProviderPrefilterStatistics providerStatistics : statistics.getProviderStatistics()) {
            System.out.println("Provider: " + providerStatistics.getProviderName());
            System.out.println("  failed: " + providerStatistics.isFailed());
            if (providerStatistics.isFailed()) {
                System.out.println("  error: " + providerStatistics.getErrorMessage());
            }
            System.out.println("  candidates: " + providerStatistics.getCandidates());
            System.out.println("  after location filter: " + providerStatistics.getAfterLocationFilter());
            System.out.println("  after senior filter: " + providerStatistics.getAfterSeniorFilter());
            System.out.println("  after profile filter: " + providerStatistics.getAfterProfileFilter());
            System.out.println("  returned jobs: " + providerStatistics.getReturnedJobs());
        }
        System.out.println("Total candidates: " + statistics.getTotalCandidates());
        System.out.println("Total after location filter: " + statistics.getTotalAfterLocationFilter());
        System.out.println("Total after senior filter: " + statistics.getTotalAfterSeniorFilter());
        System.out.println("Total after profile filter: " + statistics.getTotalAfterProfileFilter());
        System.out.println("Total after deduplication: " + statistics.getTotalAfterDeduplication());
        System.out.println("==========================================");
    }

    private ThreadFactory providerSearchThreadFactory() {
        AtomicInteger counter = new AtomicInteger(1);

        return runnable -> {
            Thread thread = new Thread(
                    runnable,
                    "provider-search-" + counter.getAndIncrement()
            );
            thread.setDaemon(true);
            return thread;
        };
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

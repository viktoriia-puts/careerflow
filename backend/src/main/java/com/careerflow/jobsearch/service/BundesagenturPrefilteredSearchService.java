package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.provider.BundesagenturJobSearchProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BundesagenturPrefilteredSearchService {

    private static final int SEARCH_PAGE = 1;
    private static final int SEARCH_SIZE = 15;
    private static final long CACHE_VALIDITY_HOURS = 24;

    private final BundesagenturJobSearchProvider bundesagenturProvider;
    private final JobPrefilterService jobPrefilterService;

    private final Map<String, BundesagenturCacheEntry> cache = new HashMap<>();

    public BundesagenturPrefilteredSearchService(
            BundesagenturJobSearchProvider bundesagenturProvider,
            JobPrefilterService jobPrefilterService
    ) {
        this.bundesagenturProvider = bundesagenturProvider;
        this.jobPrefilterService = jobPrefilterService;
    }

    public List<JobSearchResult> searchPrefilteredBundesagenturJobs(
            String location,
            List<String> roles,
            List<String> keywords,
            int target
    ) {
        if (target <= 0) {
            return List.of();
        }

        List<String> roleQueries = buildRoleQueries(roles);

        if (roleQueries.isEmpty()) {
            System.out.println("No Bundesagentur role queries available.");
            return List.of();
        }

        String cacheKey = buildCacheKey(location, roleQueries);

        List<JobSearchResult> candidates = getCachedOrFetchCandidates(
                cacheKey,
                location,
                roleQueries
        );

        List<JobSearchResult> prefilteredJobs =
                jobPrefilterService.prefilter(
                        candidates,
                        roles,
                        keywords,
                        target
                );

        System.out.println("Bundesagentur prefiltered jobs: " + prefilteredJobs.size());

        return prefilteredJobs;
    }

    private synchronized List<JobSearchResult> getCachedOrFetchCandidates(
            String cacheKey,
            String location,
            List<String> roleQueries
    ) {
        BundesagenturCacheEntry cacheEntry = cache.get(cacheKey);

        if (cacheEntry != null && !isCacheExpired(cacheEntry)) {
            System.out.println("========== BUNDESAGENTUR CACHE DEBUG ==========");
            System.out.println("Cache hit. Using cached Bundesagentur jobs.");
            System.out.println("Cache key: " + cacheKey);
            System.out.println("Cached jobs: " + cacheEntry.getJobs().size());
            System.out.println("Cached at: " + cacheEntry.getCreatedAt());
            System.out.println("================================================");

            return new ArrayList<>(cacheEntry.getJobs());
        }

        System.out.println("========== BUNDESAGENTUR PREFILTER SEARCH ==========");
        System.out.println("Cache miss or expired. Fetching Bundesagentur jobs.");
        System.out.println("Location: " + location);
        System.out.println("Role queries: " + roleQueries);
        System.out.println("Search page: " + SEARCH_PAGE);
        System.out.println("Search size: " + SEARCH_SIZE);
        System.out.println("====================================================");

        List<JobSearchResult> candidates = new ArrayList<>();

        for (String roleQuery : roleQueries) {
            System.out.println("Searching Bundesagentur with role query: " + roleQuery);

            try {
                List<JobSearchResult> jobs =
                        bundesagenturProvider.searchJobResults(
                                roleQuery,
                                location,
                                SEARCH_PAGE,
                                SEARCH_SIZE
                        );

                System.out.println("Bundesagentur jobs found for '" + roleQuery + "': " + jobs.size());

                candidates.addAll(jobs);
            } catch (Exception e) {
                System.out.println("Bundesagentur search failed for query: " + roleQuery);
                System.out.println("Reason: " + e.getMessage());

                if (e.getCause() != null) {
                    System.out.println("Root cause: " + e.getCause().getMessage());
                }
            }
        }

        List<JobSearchResult> uniqueCandidates = removeDuplicates(candidates);

        System.out.println("Bundesagentur candidates before deduplication: " + candidates.size());
        System.out.println("Bundesagentur candidates after deduplication: " + uniqueCandidates.size());

        cache.put(
                cacheKey,
                new BundesagenturCacheEntry(uniqueCandidates, LocalDateTime.now())
        );

        return new ArrayList<>(uniqueCandidates);
    }

    private boolean isCacheExpired(BundesagenturCacheEntry cacheEntry) {
        return ChronoUnit.HOURS.between(
                cacheEntry.getCreatedAt(),
                LocalDateTime.now()
        ) >= CACHE_VALIDITY_HOURS;
    }

    private List<String> buildRoleQueries(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String buildCacheKey(String location, List<String> roleQueries) {
        String normalizedLocation = normalizeForCache(location);

        String normalizedRoles = roleQueries.stream()
                .map(this::normalizeForCache)
                .sorted()
                .reduce("", (left, right) -> left + "|" + right);

        return normalizedLocation + "::" + normalizedRoles;
    }

    private String normalizeForCache(String value) {
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

    private static class BundesagenturCacheEntry {

        private final List<JobSearchResult> jobs;
        private final LocalDateTime createdAt;

        private BundesagenturCacheEntry(
                List<JobSearchResult> jobs,
                LocalDateTime createdAt
        ) {
            this.jobs = jobs;
            this.createdAt = createdAt;
        }

        private List<JobSearchResult> getJobs() {
            return jobs;
        }

        private LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
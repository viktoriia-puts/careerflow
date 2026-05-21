package com.careerflow.jobsearch.provider;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import com.careerflow.jobsearch.service.TextCleaningService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArbeitnowJobSearchProvider {
    private final TextCleaningService textCleaningService;
    private static final int INITIAL_CACHE_PAGES = 5;
    private static final int MAX_LOCATION_SEARCH_PAGES = 30;
    private static final int MIN_LOCATION_MATCHES = 5;
    private static final int FALLBACK_PAGE_BATCH_SIZE = 5;

    public ArbeitnowJobSearchProvider(TextCleaningService textCleaningService) {
        this.textCleaningService = textCleaningService;
    }

    private static final String BASE_URL =
            "https://www.arbeitnow.com/api/job-board-api";

    private static final long CACHE_VALIDITY_HOURS = 6;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // In-memory cache for normalized jobs
    private List<JobSearchResult> cachedJobs = new ArrayList<>();
    private LocalDateTime cacheUpdatedAt = null;

    public String searchJobs(int page) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("page", page)
                    .build()
                    .encode()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            String responseBody = new String(response.body(), StandardCharsets.UTF_8);

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Arbeitnow API returned status "
                                + response.statusCode()
                                + ": "
                                + responseBody
                );
            }

            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search jobs via Arbeitnow API", e);
        }
    }

    public List<JobSearchResult> searchJobResults(int page) {
        try {
            String rawJson = searchJobs(page);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode data = root.path("data");

            List<JobSearchResult> results = new ArrayList<>();

            for (JsonNode item : data) {
                JobSearchResult result = new JobSearchResult(
                        "ARBEITNOW",
                        textCleaningService.cleanJobText(item.path("title").asText()),
                        textCleaningService.cleanJobText(item.path("company_name").asText()),
                        textCleaningService.cleanJobText(item.path("location").asText()),
                        textCleaningService.cleanJobText(item.path("description").asText()),
                        item.path("url").asText(),
                        item.path("slug").asText(),
                        String.valueOf(item.path("created_at").asLong()),
                        true
                );

                results.add(result);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to normalize Arbeitnow jobs", e);
        }
    }

    public List<JobSearchResult> getCachedJobResults() {
        boolean cacheExpired = cacheUpdatedAt == null
                || ChronoUnit.HOURS.between(cacheUpdatedAt, LocalDateTime.now()) >= CACHE_VALIDITY_HOURS;

        if (cachedJobs.isEmpty() || cacheExpired) {
            System.out.println("========== ARBEITNOW CACHE DEBUG ==========");
            System.out.println("Cache miss or expired. Fetching fresh Arbeitnow jobs...");
            System.out.println("cachedJobs empty: " + cachedJobs.isEmpty());
            System.out.println("cacheUpdatedAt: " + cacheUpdatedAt);
            System.out.println("CACHE_VALIDITY_HOURS: " + CACHE_VALIDITY_HOURS);
            System.out.println("===========================================");

            cachedJobs = searchJobResultsFromFirstPages(INITIAL_CACHE_PAGES);
            cacheUpdatedAt = LocalDateTime.now();
        } else {
            System.out.println("========== ARBEITNOW CACHE DEBUG ==========");
            System.out.println("Cache hit. Using cached Arbeitnow jobs.");
            System.out.println("cachedJobs size: " + cachedJobs.size());
            System.out.println("cacheUpdatedAt: " + cacheUpdatedAt);
            System.out.println("===========================================");
        }

        return new ArrayList<>(cachedJobs);
    }

    public List<JobSearchResult> searchJobResultsFromFirstPages(int pages) {
        List<JobSearchResult> allResults = new ArrayList<>();

        for (int page = 1; page <= pages; page++) {
            allResults.addAll(searchJobResults(page));
        }

        return allResults;
    }

    public List<JobSearchResult> searchCachedJobsByLocation(String location) {
        List<JobSearchResult> jobs = getCachedJobResults();

        if (location == null || location.isBlank()) {
            System.out.println("No location provided. Returning all Arbeitnow jobs: " + jobs.size());
            return jobs;
        }

        List<String> normalizedLocationAliases = getLocationAliases(location);

        System.out.println("========== LOCATION FILTER DEBUG ==========");
        System.out.println("User location raw: " + location);
        System.out.println("User location aliases: " + normalizedLocationAliases);
        System.out.println("Initial cached jobs: " + jobs.size());
        System.out.println("Minimum location matches wanted: " + MIN_LOCATION_MATCHES);
        System.out.println("Maximum pages to search: " + MAX_LOCATION_SEARCH_PAGES);
        System.out.println("Fallback page batch size: " + FALLBACK_PAGE_BATCH_SIZE);
        System.out.println("===========================================");

        List<JobSearchResult> filtered = filterJobsByLocation(jobs, normalizedLocationAliases);

        int nextPage = INITIAL_CACHE_PAGES + 1;

        while (filtered.size() < MIN_LOCATION_MATCHES && nextPage <= MAX_LOCATION_SEARCH_PAGES) {
            int batchStartPage = nextPage;
            int batchEndPage = Math.min(
                    batchStartPage + FALLBACK_PAGE_BATCH_SIZE - 1,
                    MAX_LOCATION_SEARCH_PAGES
            );

            System.out.println("========== LOCATION FALLBACK BATCH DEBUG ==========");
            System.out.println("Only found " + filtered.size() + " jobs for location '" + location + "'.");
            System.out.println("Fetching additional Arbeitnow pages: " + batchStartPage + "-" + batchEndPage);
            System.out.println("Current cache size before fetch: " + cachedJobs.size());
            System.out.println("===================================================");

            List<JobSearchResult> newJobs = searchJobResultsFromPageRange(batchStartPage, batchEndPage);

            if (newJobs.isEmpty()) {
                System.out.println("No jobs returned from pages " + batchStartPage + "-" + batchEndPage
                        + ". Stopping fallback search.");
                break;
            }

            cachedJobs.addAll(newJobs);
            cacheUpdatedAt = LocalDateTime.now();

            System.out.println("Fetched jobs from pages " + batchStartPage + "-" + batchEndPage + ": " + newJobs.size());
            System.out.println("Current cache size after fetch: " + cachedJobs.size());

            filtered = filterJobsByLocation(cachedJobs, normalizedLocationAliases);

            System.out.println("Jobs matching location after pages "
                    + batchStartPage + "-" + batchEndPage + ": " + filtered.size());

            nextPage = batchEndPage + 1;
        }

        System.out.println("========== LOCATION FILTER RESULT ==========");
        System.out.println("Location: " + location);
        System.out.println("Location aliases used: " + normalizedLocationAliases);
        System.out.println("Returned jobs: " + filtered.size());
        System.out.println("Cached jobs total: " + cachedJobs.size());
        System.out.println("============================================");

        return filtered;
    }

    private List<JobSearchResult> filterJobsByLocation(
            List<JobSearchResult> jobs,
            List<String> normalizedLocationAliases
    ) {
        List<JobSearchResult> filtered = new ArrayList<>();

        System.out.println("---------- LOCATION FILTER PASS ----------");
        System.out.println("Jobs before location filter: " + jobs.size());
        System.out.println("Location aliases: " + normalizedLocationAliases);

        for (JobSearchResult job : jobs) {
            String normalizedJobLocation = normalizeLocation(job.getLocation());

            boolean matches = false;
            String matchedAlias = null;

            for (String alias : normalizedLocationAliases) {
                if (normalizedJobLocation.contains(alias)) {
                    matches = true;
                    matchedAlias = alias;
                    break;
                }
            }

            if (matches) {
                System.out.println("LOCATION MATCH:");
                System.out.println("Title: " + job.getTitle());
                System.out.println("Company: " + job.getCompany());
                System.out.println("Job location raw: " + job.getLocation());
                System.out.println("Job location normalized: " + normalizedJobLocation);
                System.out.println("Matched location alias: " + matchedAlias);

                filtered.add(job);
            }
        }

        System.out.println("Jobs after location filter: " + filtered.size());
        System.out.println("------------------------------------------");

        return filtered;
    }

    private List<JobSearchResult> searchJobResultsFromPageRange(int startPage, int endPage) {
        List<JobSearchResult> allResults = new ArrayList<>();

        for (int page = startPage; page <= endPage; page++) {
            System.out.println("Fetching Arbeitnow page: " + page);

            List<JobSearchResult> pageResults = searchJobResults(page);

            System.out.println("Fetched jobs from page " + page + ": " + pageResults.size());

            allResults.addAll(pageResults);

            if (pageResults.isEmpty()) {
                System.out.println("Page " + page + " returned 0 jobs. Stopping this batch early.");
                break;
            }
        }

        return allResults;
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

    private List<String> getLocationAliases(String location) {
        String normalized = normalizeLocation(location);

        if (normalized.equals("nuernberg") || normalized.equals("nuremberg")) {
            return List.of("nuernberg", "nuremberg");
        }

        if (normalized.equals("muenchen") || normalized.equals("munich")) {
            return List.of("muenchen", "munich");
        }

        if (normalized.equals("koeln") || normalized.equals("cologne")) {
            return List.of("koeln", "cologne");
        }

        return List.of(normalized);
    }

}
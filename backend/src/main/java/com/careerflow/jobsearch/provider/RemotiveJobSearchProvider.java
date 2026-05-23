package com.careerflow.jobsearch.provider;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.service.TextCleaningService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
public class RemotiveJobSearchProvider {

    private static final String BASE_URL =
            "https://remotive.com/api/remote-jobs";

    private static final int REMOTIVE_CACHE_LIMIT = 100;
    private static final long CACHE_VALIDITY_HOURS = 24;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TextCleaningService textCleaningService;

    private List<JobSearchResult> cachedJobs = new ArrayList<>();
    private LocalDateTime cacheUpdatedAt = null;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public RemotiveJobSearchProvider(TextCleaningService textCleaningService) {
        this.textCleaningService = textCleaningService;
    }

    public String searchJobs(String query, int limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("limit", limit);

            if (query != null && !query.isBlank()) {
                builder.queryParam("search", query);
            }

            URI uri = builder
                    .build()
                    .encode()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Remotive API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search jobs via Remotive API", e);
        }
    }

    public List<JobSearchResult> searchJobResults(String query, int limit) {
        try {
            String rawJson = searchJobs(query, limit);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode jobs = root.path("jobs");

            List<JobSearchResult> results = new ArrayList<>();

            for (JsonNode item : jobs) {
                JobSearchResult result = new JobSearchResult(
                        "REMOTIVE",
                        textCleaningService.cleanJobText(item.path("title").asText()),
                        textCleaningService.cleanJobText(item.path("company_name").asText()),
                        textCleaningService.cleanJobText(item.path("candidate_required_location").asText()),
                        textCleaningService.cleanJobText(item.path("description").asText()),
                        item.path("url").asText(),
                        String.valueOf(item.path("id").asLong()),
                        item.path("publication_date").asText(),
                        true
                );

                results.add(result);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to normalize Remotive jobs", e);
        }
    }

    public synchronized List<JobSearchResult> getCachedJobResults() {
        boolean cacheExpired = cacheUpdatedAt == null
                || ChronoUnit.HOURS.between(cacheUpdatedAt, LocalDateTime.now()) >= CACHE_VALIDITY_HOURS;

        if (!cachedJobs.isEmpty() && !cacheExpired) {
            System.out.println("========== REMOTIVE CACHE DEBUG ==========");
            System.out.println("Cache hit. Using cached Remotive jobs.");
            System.out.println("cachedJobs size: " + cachedJobs.size());
            System.out.println("cacheUpdatedAt: " + cacheUpdatedAt);
            System.out.println("==========================================");

            return new ArrayList<>(cachedJobs);
        }

        System.out.println("========== REMOTIVE CACHE DEBUG ==========");
        System.out.println("Cache miss or expired. Fetching Remotive jobs ONCE.");
        System.out.println("Limit: " + REMOTIVE_CACHE_LIMIT);
        System.out.println("==========================================");

        try {
            cachedJobs = searchJobResults(null, REMOTIVE_CACHE_LIMIT);
            cacheUpdatedAt = LocalDateTime.now();

            System.out.println("Fetched Remotive jobs: " + cachedJobs.size());

            return new ArrayList<>(cachedJobs);
        } catch (Exception e) {
            System.out.println("Failed to fetch Remotive jobs.");
            System.out.println("Reason: " + e.getMessage());

            if (!cachedJobs.isEmpty()) {
                System.out.println("Returning stale Remotive cache: " + cachedJobs.size());
                return new ArrayList<>(cachedJobs);
            }

            throw e;
        }
    }
}
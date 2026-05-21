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

            cachedJobs = searchJobResults(1);
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

    public List<JobSearchResult> searchCachedJobsByLocation(String location) {
        List<JobSearchResult> jobs = getCachedJobResults();

        if (location == null || location.isBlank()) {
            System.out.println("No location provided. Returning all Arbeitnow jobs: " + jobs.size());
            return jobs;
        }

        String normalizedUserLocation = normalizeLocation(location);

        System.out.println("========== LOCATION FILTER DEBUG ==========");
        System.out.println("User location raw: " + location);
        System.out.println("User location normalized: " + normalizedUserLocation);
        System.out.println("Jobs before filter: " + jobs.size());

        List<JobSearchResult> filtered = jobs.stream()
                .filter(job -> {
                    String normalizedJobLocation = normalizeLocation(job.getLocation());
                    boolean matches = normalizedJobLocation.contains(normalizedUserLocation);

                    if (matches) {
                        System.out.println("MATCH: job location raw = " + job.getLocation()
                                + " | normalized = " + normalizedJobLocation);
                    }

                    return matches;
                })
                .toList();

        System.out.println("Jobs after filter: " + filtered.size());
        System.out.println("===========================================");

        return filtered;
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
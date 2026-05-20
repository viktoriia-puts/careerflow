package com.careerflow.jobsearch.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class RemotiveJobSearchProvider {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL =
            "https://remotive.com/api/remote-jobs";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

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
                        item.path("title").asText(),
                        item.path("company_name").asText(),
                        item.path("candidate_required_location").asText(),
                        item.path("description").asText(),
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
}
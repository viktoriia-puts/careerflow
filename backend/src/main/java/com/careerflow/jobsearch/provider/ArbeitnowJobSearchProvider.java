package com.careerflow.jobsearch.provider;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArbeitnowJobSearchProvider {

    private static final String BASE_URL =
            "https://www.arbeitnow.com/api/job-board-api";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

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

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Arbeitnow API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
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
                        item.path("title").asText(),
                        item.path("company_name").asText(),
                        item.path("location").asText(),
                        item.path("description").asText(),
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
}
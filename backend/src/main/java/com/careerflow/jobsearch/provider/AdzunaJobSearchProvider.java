package com.careerflow.jobsearch.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class AdzunaJobSearchProvider {

    private static final String BASE_URL =
            "https://api.adzuna.com/v1/api/jobs/de/search";

    private final String appId;
    private final String appKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public AdzunaJobSearchProvider(
            @Value("${adzuna.app.id}") String appId,
            @Value("${adzuna.app.key}") String appKey
    ) {
        this.appId = appId;
        this.appKey = appKey;
    }

    public String searchJobs(String query, String location, int page, int size) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/" + page)
                    .queryParam("app_id", appId)
                    .queryParam("app_key", appKey)
                    .queryParam("what", query)
                    .queryParam("where", location)
                    .queryParam("results_per_page", size)
                    .queryParam("content-type", "application/json")
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
                        "Adzuna API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search jobs via Adzuna API", e);
        }
    }
}
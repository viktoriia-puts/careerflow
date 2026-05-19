package com.careerflow.jobsearch.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class JoobleJobSearchProvider {

    private static final String BASE_URL = "https://de.jooble.org/api/";

    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public JoobleJobSearchProvider(@Value("${jooble.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String searchJobs(String query, String location, int page) {
        try {
            String url = BASE_URL + apiKey;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("keywords", query);
            requestBody.put("location", location);
            requestBody.put("page", page);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Jooble API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search jobs via Jooble API", e);
        }
    }
}
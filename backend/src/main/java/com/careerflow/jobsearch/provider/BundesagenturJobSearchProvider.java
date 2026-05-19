package com.careerflow.jobsearch.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class BundesagenturJobSearchProvider {

    private static final String BASE_URL =
            "https://rest.arbeitsagentur.de/jobboerse/jobsuche-service/pc/v4/jobs";

    private static final String API_KEY = "jobboerse-jobsuche";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String searchJobs(String query, String location, int page, int size) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("was", query)
                    .queryParam("wo", location)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("angebotsart", "1")
                    .queryParam("pav", "false")
                    .build()
                    .encode()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("X-API-Key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Bundesagentur API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search jobs via Bundesagentur API", e);
        }
    }
}
package com.careerflow.jobsearch.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class BundesagenturJobSearchProvider {

    private static final String SEARCH_URL =
            "https://rest.arbeitsagentur.de/jobboerse/jobsuche-service/pc/v4/jobs";

    private static final String DETAILS_URL =
            "https://rest.arbeitsagentur.de/jobboerse/jobsuche-service/pc/v4/jobdetails";

    private static final String API_KEY = "jobboerse-jobsuche";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String searchJobs(String query, String location, int page, int size) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(SEARCH_URL)
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

    public String getJobDetails(String refnr) {
        try {
            String encodedRefnr = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(refnr.getBytes(StandardCharsets.UTF_8));

            URI uri = UriComponentsBuilder
                    .fromUriString(DETAILS_URL + "/" + encodedRefnr)
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
                        "Bundesagentur details API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Bundesagentur job details", e);
        }
    }
}
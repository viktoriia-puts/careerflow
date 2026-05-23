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
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.service.TextCleaningService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class BundesagenturJobSearchProvider {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TextCleaningService textCleaningService;

    public BundesagenturJobSearchProvider(TextCleaningService textCleaningService) {
        this.textCleaningService = textCleaningService;
    }

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

    public List<JobSearchResult> searchJobResults(
            String query,
            String location,
            int page,
            int size
    ) {
        try {
            String rawJson = searchJobs(query, location, page, size);

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode jobs = root.path("stellenangebote");

            List<JobSearchResult> results = new ArrayList<>();

            for (JsonNode item : jobs) {
                String referenceId = item.path("refnr").asText();

                JobSearchResult result = new JobSearchResult(
                        "BUNDESAGENTUR",
                        textCleaningService.cleanJobText(item.path("titel").asText()),
                        textCleaningService.cleanJobText(item.path("arbeitgeber").asText()),
                        textCleaningService.cleanJobText(extractLocation(item)),
                        textCleaningService.cleanJobText(buildShortDescription(item)),
                        item.path("externeUrl").asText(),
                        referenceId,
                        item.path("aktuelleVeroeffentlichungsdatum").asText(),
                        false
                );

                results.add(result);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to normalize Bundesagentur jobs", e);
        }
    }

    private String extractLocation(JsonNode item) {
        JsonNode arbeitsort = item.path("arbeitsort");

        String city = arbeitsort.path("ort").asText("");
        String region = arbeitsort.path("region").asText("");
        String country = arbeitsort.path("land").asText("");

        String location = String.join(" ", city, region, country)
                .replaceAll("\\s+", " ")
                .trim();

        if (!location.isBlank()) {
            return location;
        }

        return item.path("arbeitsort").asText("");
    }

    private String buildShortDescription(JsonNode item) {
        StringBuilder description = new StringBuilder();

        appendIfPresent(description, "Title", item.path("titel").asText(""));
        appendIfPresent(description, "Employer", item.path("arbeitgeber").asText(""));
        appendIfPresent(description, "Profession", item.path("beruf").asText(""));
        appendIfPresent(description, "Location", extractLocation(item));

        return description.toString().trim();
    }

    private void appendIfPresent(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        builder.append(label)
                .append(": ")
                .append(value)
                .append("\n");
    }
}
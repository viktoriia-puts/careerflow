package com.careerflow.querygeneration.controller;

import com.careerflow.querygeneration.dto.SearchQueryGenerationResponse;
import com.careerflow.querygeneration.service.SearchQueryGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/search-profiles")
public class SearchQueryGenerationController {

    private final SearchQueryGenerationService searchQueryGenerationService;

    public SearchQueryGenerationController(SearchQueryGenerationService searchQueryGenerationService) {
        this.searchQueryGenerationService = searchQueryGenerationService;
    }

    @PostMapping("/{id}/generate-queries")
    public ResponseEntity<?> generateQueries(@PathVariable Long id) {
        try {
            SearchQueryGenerationResponse response =
                    searchQueryGenerationService.generateQueriesForProfile(id);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("{\"error\": \"" + e.getReason() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to generate queries: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/queries")
    public ResponseEntity<?> getQueries(@PathVariable Long id) {
        try {
            SearchQueryGenerationResponse response =
                    searchQueryGenerationService.getQueriesForProfile(id);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("{\"error\": \"" + e.getReason() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to load queries: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/queries")
    public ResponseEntity<?> updateQueries(
            @PathVariable Long id,
            @RequestBody SearchQueryGenerationResponse request
    ) {
        try {
            SearchQueryGenerationResponse response =
                    searchQueryGenerationService.updateQueriesForProfile(id, request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("{\"error\": \"" + e.getReason() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update queries: " + e.getMessage() + "\"}");
        }
    }
}

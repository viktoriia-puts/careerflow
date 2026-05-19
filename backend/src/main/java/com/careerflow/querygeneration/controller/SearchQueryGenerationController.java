package com.careerflow.querygeneration.controller;

import com.careerflow.querygeneration.dto.SearchQueryGenerationResponse;
import com.careerflow.querygeneration.service.SearchQueryGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            SearchQueryGenerationResponse response = searchQueryGenerationService.generateQueriesForProfile(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to generate queries: " + e.getMessage() + "\"}");
        }
    }
}



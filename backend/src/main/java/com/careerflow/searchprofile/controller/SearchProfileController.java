package com.careerflow.searchprofile.controller;

import com.careerflow.searchprofile.dto.SearchProfileCreateRequest;
import com.careerflow.searchprofile.dto.SearchProfileResponse;
import com.careerflow.searchprofile.service.SearchProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search-profiles")
public class SearchProfileController {

    private final SearchProfileService searchProfileService;

    public SearchProfileController(SearchProfileService searchProfileService) {
        this.searchProfileService = searchProfileService;
    }

    /**
     * Create a new search profile.
     *
     * @param request the search profile creation request with validated fields
     * @return ResponseEntity with the created profile and HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<SearchProfileResponse> createSearchProfile(@Valid @RequestBody SearchProfileCreateRequest request) {
        SearchProfileResponse response = searchProfileService.createSearchProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}


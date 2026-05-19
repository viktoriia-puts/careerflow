package com.careerflow.querygeneration.service;

import com.careerflow.ai.GeminiSearchQueryClient;
import com.careerflow.querygeneration.dto.SearchQueryGenerationResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.repository.SearchProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SearchQueryGenerationService {

    private final SearchProfileRepository searchProfileRepository;
    private final GeminiSearchQueryClient geminiSearchQueryClient;

    public SearchQueryGenerationService(
            SearchProfileRepository searchProfileRepository,
            GeminiSearchQueryClient geminiSearchQueryClient
    ) {
        this.searchProfileRepository = searchProfileRepository;
        this.geminiSearchQueryClient = geminiSearchQueryClient;
    }

    @Transactional(readOnly = true)
    public SearchQueryGenerationResponse generateQueriesForProfile(Long profileId) {
        SearchProfile profile = searchProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Search profile with id " + profileId + " not found"
                ));

        return geminiSearchQueryClient.generateQueries(profile);
    }
}
package com.careerflow.jobmatch.service;

import com.careerflow.ai.GeminiJobMatchClient;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.service.SearchProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobMatchAnalysisService {

    private final SearchProfileService searchProfileService;
    private final GeminiJobMatchClient geminiJobMatchClient;

    public JobMatchAnalysisService(
            SearchProfileService searchProfileService,
            GeminiJobMatchClient geminiJobMatchClient
    ) {
        this.searchProfileService = searchProfileService;
        this.geminiJobMatchClient = geminiJobMatchClient;
    }

    @Transactional(readOnly = true)
    public JobMatchAnalysisResponse analyzeJobMatch(Long profileId, String jobDescription) {
        try {
            SearchProfile profile = searchProfileService.getSearchProfileEntityById(profileId);
            return geminiJobMatchClient.analyzeJobMatch(profile, jobDescription);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Search profile with id " + profileId + " not found");
        }
    }
}

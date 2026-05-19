package com.careerflow.jobmatch.service;

import com.careerflow.ai.GeminiJobMatchClient;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.repository.SearchProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class JobMatchAnalysisService {

    private final SearchProfileRepository searchProfileRepository;
    private final GeminiJobMatchClient geminiJobMatchClient;

    public JobMatchAnalysisService(
            SearchProfileRepository searchProfileRepository,
            GeminiJobMatchClient geminiJobMatchClient
    ) {
        this.searchProfileRepository = searchProfileRepository;
        this.geminiJobMatchClient = geminiJobMatchClient;
    }

    @Transactional(readOnly = true)
    public JobMatchAnalysisResponse analyzeJobMatch(Long profileId, String jobDescription) {
        Optional<SearchProfile> profileOptional = searchProfileRepository.findById(profileId);

        if (profileOptional.isEmpty()) {
            throw new IllegalArgumentException("Search profile with id " + profileId + " not found");
        }

        SearchProfile profile = profileOptional.get();
        return geminiJobMatchClient.analyzeJobMatch(profile, jobDescription);
    }
}
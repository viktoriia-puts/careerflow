package com.careerflow.jobsearch.service;

import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.jobsearch.entity.RankedJobSearchResultEntity;
import com.careerflow.jobsearch.entity.RankedJobSearchRun;
import com.careerflow.jobsearch.repository.RankedJobSearchResultRepository;
import com.careerflow.jobsearch.repository.RankedJobSearchRunRepository;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankedJobSearchResultPersistenceService {

    private final RankedJobSearchRunRepository runRepository;
    private final RankedJobSearchResultRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RankedJobSearchResultPersistenceService(
            RankedJobSearchRunRepository runRepository,
            RankedJobSearchResultRepository resultRepository
    ) {
        this.runRepository = runRepository;
        this.resultRepository = resultRepository;
    }
    @Transactional
    public void saveRankedResults(
            SearchProfile profile,
            String location,
            List<RankedJobSearchResult> rankedResults
    ) {
        if (profile == null || rankedResults == null) {
            return;
        }

        RankedJobSearchRun run =
                runRepository.save(new RankedJobSearchRun(profile, location));

        List<RankedJobSearchResultEntity> entities = new ArrayList<>();

        for (int index = 0; index < rankedResults.size(); index++) {
            entities.add(toEntity(run, rankedResults.get(index), index));
        }

        resultRepository.saveAll(entities);
    }

    private RankedJobSearchResultEntity toEntity(
            RankedJobSearchRun run,
            RankedJobSearchResult rankedResult,
            int positionIndex
    ) {
        RankedJobSearchResultEntity entity = new RankedJobSearchResultEntity();
        entity.setSearchRun(run);
        entity.setPositionIndex(positionIndex);

        JobSearchResult job = rankedResult.getJob();
        if (job != null) {
            entity.setJobSource(truncate(job.getSource(), 100));
            entity.setJobTitle(truncate(job.getTitle(), 500));
            entity.setJobCompany(truncate(job.getCompany(), 500));
            entity.setJobLocation(truncate(job.getLocation(), 500));
            entity.setJobDescription(job.getDescription());
            entity.setJobUrl(job.getUrl());
            entity.setJobReferenceId(truncate(job.getReferenceId(), 500));
            entity.setJobPublishedAt(truncate(job.getPublishedAt(), 100));
            entity.setFullDescriptionAvailable(job.isFullDescriptionAvailable());
        }

        JobMatchAnalysisResponse matchAnalysis = rankedResult.getMatchAnalysis();
        if (matchAnalysis != null) {
            entity.setMatchScore(matchAnalysis.getMatchScore());
            entity.setRecommendation(truncate(matchAnalysis.getRecommendation(), 100));
            entity.setMatchSummary(matchAnalysis.getSummary());
            entity.setMatchingSkillsJson(toJson(matchAnalysis.getMatchingSkills()));
            entity.setMissingSkillsJson(toJson(matchAnalysis.getMissingSkills()));
            entity.setConcernsJson(toJson(matchAnalysis.getConcerns()));
            entity.setSuggestedApplicationFocusJson(
                    toJson(matchAnalysis.getSuggestedApplicationFocus())
            );
        }

        return entity;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(
                    values == null ? List.of() : values
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ranked job list field", e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}

package com.careerflow.jobsearch.service;

import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchHistoryResult;
import com.careerflow.jobsearch.dto.RankedJobSearchRunDetailResponse;
import com.careerflow.jobsearch.dto.RankedJobSearchRunSummaryResponse;
import com.careerflow.jobsearch.entity.RankedJobSearchResultEntity;
import com.careerflow.jobsearch.entity.RankedJobSearchRun;
import com.careerflow.jobsearch.repository.RankedJobSearchResultRepository;
import com.careerflow.jobsearch.repository.RankedJobSearchRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RankedJobSearchHistoryService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE =
            new TypeReference<>() {
            };

    private final RankedJobSearchRunRepository runRepository;
    private final RankedJobSearchResultRepository resultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RankedJobSearchHistoryService(
            RankedJobSearchRunRepository runRepository,
            RankedJobSearchResultRepository resultRepository
    ) {
        this.runRepository = runRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public List<RankedJobSearchRunSummaryResponse> getRuns(Long profileId) {
        return runRepository.findBySearchProfile_IdOrderByCreatedAtDesc(profileId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RankedJobSearchRunDetailResponse getRun(Long profileId, Long runId) {
        RankedJobSearchRun run = runRepository.findById(runId)
                .filter(savedRun -> savedRun.getSearchProfile().getId().equals(profileId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ranked job search run not found"
                ));

        List<RankedJobSearchHistoryResult> results =
                resultRepository.findBySearchRun_IdOrderByPositionIndexAsc(runId)
                        .stream()
                        .map(this::toHistoryResult)
                        .toList();

        return new RankedJobSearchRunDetailResponse(
                run.getId(),
                run.getSearchProfile().getId(),
                run.getLocation(),
                run.getCreatedAt(),
                results
        );
    }

    @Transactional
    public void deleteResult(Long profileId, Long resultId) {
        RankedJobSearchResultEntity result =
                resultRepository.findByIdAndSearchRun_SearchProfile_Id(resultId, profileId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ranked job search result not found"
                        ));

        resultRepository.delete(result);
    }

    @Transactional(readOnly = true)
    public List<JobSearchResult> removeJobsAlreadySavedInHistory(
            Long profileId,
            List<JobSearchResult> candidates
    ) {
        if (profileId == null || candidates == null || candidates.isEmpty()) {
            return candidates == null ? List.of() : candidates;
        }

        Set<String> savedJobKeys = resultRepository.findBySearchRun_SearchProfile_Id(profileId)
                .stream()
                .flatMap(result -> savedHistoryKeys(result).stream())
                .collect(Collectors.toSet());

        if (savedJobKeys.isEmpty()) {
            return candidates;
        }

        return candidates.stream()
                .filter(candidate -> candidateKeys(candidate).stream()
                        .noneMatch(savedJobKeys::contains))
                .toList();
    }

    private RankedJobSearchRunSummaryResponse toSummary(RankedJobSearchRun run) {
        List<RankedJobSearchResultEntity> results =
                resultRepository.findBySearchRun_IdOrderByPositionIndexAsc(run.getId());

        Integer topMatchScore = results.stream()
                .map(RankedJobSearchResultEntity::getMatchScore)
                .filter(score -> score != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new RankedJobSearchRunSummaryResponse(
                run.getId(),
                run.getSearchProfile().getId(),
                run.getLocation(),
                run.getCreatedAt(),
                results.size(),
                topMatchScore
        );
    }

    private RankedJobSearchHistoryResult toHistoryResult(RankedJobSearchResultEntity entity) {
        return new RankedJobSearchHistoryResult(
                entity.getId(),
                entity.getPositionIndex(),
                toJob(entity),
                toMatchAnalysis(entity)
        );
    }

    private JobSearchResult toJob(RankedJobSearchResultEntity entity) {
        return new JobSearchResult(
                entity.getJobSource(),
                entity.getJobTitle(),
                entity.getJobCompany(),
                entity.getJobLocation(),
                entity.getJobDescription(),
                entity.getJobUrl(),
                entity.getJobReferenceId(),
                entity.getJobPublishedAt(),
                entity.isFullDescriptionAvailable()
        );
    }

    private JobMatchAnalysisResponse toMatchAnalysis(RankedJobSearchResultEntity entity) {
        return new JobMatchAnalysisResponse(
                entity.getMatchScore(),
                entity.getRecommendation(),
                entity.getMatchSummary(),
                fromJson(entity.getMatchingSkillsJson()),
                fromJson(entity.getMissingSkillsJson()),
                fromJson(entity.getConcernsJson()),
                fromJson(entity.getSuggestedApplicationFocusJson())
        );
    }

    private List<String> fromJson(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(value, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize ranked job list field", e);
        }
    }

    private Set<String> savedHistoryKeys(RankedJobSearchResultEntity result) {
        Set<String> keys = new HashSet<>();

        addKey(keys, referenceKey(result.getJobSource(), result.getJobReferenceId()));
        addKey(keys, urlKey(result.getJobUrl()));
        addKey(keys, fallbackKey(
                result.getJobCompany(),
                result.getJobTitle(),
                result.getJobLocation()
        ));

        return keys;
    }

    private Set<String> candidateKeys(JobSearchResult job) {
        if (job == null) {
            return Set.of();
        }

        Set<String> keys = new HashSet<>();

        addKey(keys, referenceKey(job.getSource(), job.getReferenceId()));
        addKey(keys, urlKey(job.getUrl()));
        addKey(keys, fallbackKey(job.getCompany(), job.getTitle(), job.getLocation()));

        return keys;
    }

    private void addKey(Set<String> keys, String key) {
        if (key != null && !key.isBlank()) {
            keys.add(key);
        }
    }

    private String referenceKey(String source, String referenceId) {
        if (isBlank(source) || isBlank(referenceId)) {
            return "";
        }

        return identityKey("ref", source, referenceId);
    }

    private String urlKey(String url) {
        if (isBlank(url)) {
            return "";
        }

        return identityKey("url", url);
    }

    private String fallbackKey(String company, String title, String location) {
        if (isBlank(company) || isBlank(title)) {
            return "";
        }

        return identityKey("fallback", company, title, location);
    }

    private String identityKey(String prefix, String... parts) {
        String joined = List.of(parts)
                .stream()
                .map(this::normalizeKeyPart)
                .filter(part -> !part.isBlank())
                .collect(Collectors.joining("|"));

        return joined.isBlank() ? "" : prefix + ":" + joined;
    }

    private String normalizeKeyPart(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

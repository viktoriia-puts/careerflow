package com.careerflow.querygeneration.service;

import com.careerflow.ai.GeminiSearchQueryClient;
import com.careerflow.querygeneration.dto.SearchQueryGenerationResponse;
import com.careerflow.querygeneration.entity.SearchProfileGeneratedQuery;
import com.careerflow.querygeneration.entity.SearchProfileGeneratedQueryType;
import com.careerflow.querygeneration.repository.SearchProfileGeneratedQueryRepository;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.repository.SearchProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchQueryGenerationService {

    private final SearchProfileRepository searchProfileRepository;
    private final SearchProfileGeneratedQueryRepository generatedQueryRepository;
    private final GeminiSearchQueryClient geminiSearchQueryClient;

    public SearchQueryGenerationService(
            SearchProfileRepository searchProfileRepository,
            SearchProfileGeneratedQueryRepository generatedQueryRepository,
            GeminiSearchQueryClient geminiSearchQueryClient
    ) {
        this.searchProfileRepository = searchProfileRepository;
        this.generatedQueryRepository = generatedQueryRepository;
        this.geminiSearchQueryClient = geminiSearchQueryClient;
    }

    @Transactional
    public SearchQueryGenerationResponse generateQueriesForProfile(Long profileId) {
        SearchProfile profile = getProfile(profileId);

        SearchQueryGenerationResponse savedQueries = mapToResponse(
                generatedQueryRepository.findBySearchProfileIdOrderByQueryTypeAscPositionIndexAsc(
                        profileId
                )
        );

        if (hasAnyQueries(savedQueries)) {
            return savedQueries;
        }

        SearchQueryGenerationResponse generatedQueries =
                geminiSearchQueryClient.generateQueries(profile);

        replaceQueries(profile, generatedQueries);

        return generatedQueries;
    }

    @Transactional(readOnly = true)
    public SearchQueryGenerationResponse getQueriesForProfile(Long profileId) {
        getProfile(profileId);

        List<SearchProfileGeneratedQuery> savedQueries =
                generatedQueryRepository.findBySearchProfileIdOrderByQueryTypeAscPositionIndexAsc(
                        profileId
                );

        return mapToResponse(savedQueries);
    }

    @Transactional
    public SearchQueryGenerationResponse updateQueriesForProfile(
            Long profileId,
            SearchQueryGenerationResponse request
    ) {
        SearchProfile profile = getProfile(profileId);

        SearchQueryGenerationResponse normalizedRequest =
                new SearchQueryGenerationResponse(
                        normalizeQueries(request.getRoleTitleQueries()),
                        normalizeQueries(request.getRequirementBasedQueries()),
                        normalizeQueries(request.getAlternativeDirectionQueries())
                );

        replaceQueries(profile, normalizedRequest);

        return normalizedRequest;
    }

    @Transactional(readOnly = true)
    public List<String> getSavedJobSearchQueriesForProfile(Long profileId) {
        SearchQueryGenerationResponse savedQueries = getQueriesForProfile(profileId);

        List<String> jobSearchQueries = new ArrayList<>();
        jobSearchQueries.addAll(savedQueries.getRoleTitleQueries());
        jobSearchQueries.addAll(savedQueries.getRequirementBasedQueries());

        return jobSearchQueries;
    }

    private SearchProfile getProfile(Long profileId) {
        return searchProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Search profile with id " + profileId + " not found"
                ));
    }

    private void replaceQueries(
            SearchProfile profile,
            SearchQueryGenerationResponse generatedQueries
    ) {
        generatedQueryRepository.deleteBySearchProfileId(profile.getId());

        List<SearchProfileGeneratedQuery> entities = new ArrayList<>();

        addEntities(
                entities,
                profile,
                SearchProfileGeneratedQueryType.ROLE_TITLE,
                normalizeQueries(generatedQueries.getRoleTitleQueries())
        );
        addEntities(
                entities,
                profile,
                SearchProfileGeneratedQueryType.REQUIREMENT_BASED,
                normalizeQueries(generatedQueries.getRequirementBasedQueries())
        );
        addEntities(
                entities,
                profile,
                SearchProfileGeneratedQueryType.ALTERNATIVE_DIRECTION,
                normalizeQueries(generatedQueries.getAlternativeDirectionQueries())
        );

        generatedQueryRepository.saveAll(entities);
    }

    private void addEntities(
            List<SearchProfileGeneratedQuery> entities,
            SearchProfile profile,
            SearchProfileGeneratedQueryType queryType,
            List<String> queries
    ) {
        for (int index = 0; index < queries.size(); index++) {
            entities.add(new SearchProfileGeneratedQuery(
                    profile,
                    queryType,
                    queries.get(index),
                    index
            ));
        }
    }

    private SearchQueryGenerationResponse mapToResponse(
            List<SearchProfileGeneratedQuery> savedQueries
    ) {
        List<String> roleTitleQueries = new ArrayList<>();
        List<String> requirementBasedQueries = new ArrayList<>();
        List<String> alternativeDirectionQueries = new ArrayList<>();

        for (SearchProfileGeneratedQuery savedQuery : savedQueries) {
            if (savedQuery.getQueryType() == SearchProfileGeneratedQueryType.ROLE_TITLE) {
                roleTitleQueries.add(savedQuery.getQueryText());
            } else if (savedQuery.getQueryType() == SearchProfileGeneratedQueryType.REQUIREMENT_BASED) {
                requirementBasedQueries.add(savedQuery.getQueryText());
            } else if (savedQuery.getQueryType() == SearchProfileGeneratedQueryType.ALTERNATIVE_DIRECTION) {
                alternativeDirectionQueries.add(savedQuery.getQueryText());
            }
        }

        return new SearchQueryGenerationResponse(
                roleTitleQueries,
                requirementBasedQueries,
                alternativeDirectionQueries
        );
    }

    private boolean hasAnyQueries(SearchQueryGenerationResponse queries) {
        return !queries.getRoleTitleQueries().isEmpty()
                || !queries.getRequirementBasedQueries().isEmpty()
                || !queries.getAlternativeDirectionQueries().isEmpty();
    }

    private List<String> normalizeQueries(List<String> queries) {
        List<String> normalizedQueries = new ArrayList<>();

        if (queries == null || queries.isEmpty()) {
            return normalizedQueries;
        }

        for (String query : queries) {
            if (query == null || query.isBlank()) {
                continue;
            }

            String normalizedQuery = query.trim();

            if (!normalizedQueries.contains(normalizedQuery)) {
                normalizedQueries.add(normalizedQuery);
            }
        }

        return normalizedQueries;
    }
}

package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.JobSearchPrefilterStatistics;
import com.careerflow.jobsearch.dto.PrefilteredJobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.querygeneration.service.SearchQueryGenerationService;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.service.SearchProfileService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobSearchRankingApplicationService {

    private static final int DEFAULT_TARGET_PER_PROVIDER = 25;

    private final SearchProfileService searchProfileService;
    private final SearchQueryGenerationService searchQueryGenerationService;
    private final MultiProviderPrefilteredJobSearchService multiProviderPrefilteredJobSearchService;
    private final RankedJobSearchHistoryService rankedJobSearchHistoryService;
    private final JobMatchRankingService jobMatchRankingService;
    private final RankedJobSearchResultPersistenceService rankedJobSearchResultPersistenceService;

    public JobSearchRankingApplicationService(
            SearchProfileService searchProfileService,
            SearchQueryGenerationService searchQueryGenerationService,
            MultiProviderPrefilteredJobSearchService multiProviderPrefilteredJobSearchService,
            RankedJobSearchHistoryService rankedJobSearchHistoryService,
            JobMatchRankingService jobMatchRankingService,
            RankedJobSearchResultPersistenceService rankedJobSearchResultPersistenceService
    ) {
        this.searchProfileService = searchProfileService;
        this.searchQueryGenerationService = searchQueryGenerationService;
        this.multiProviderPrefilteredJobSearchService = multiProviderPrefilteredJobSearchService;
        this.rankedJobSearchHistoryService = rankedJobSearchHistoryService;
        this.jobMatchRankingService = jobMatchRankingService;
        this.rankedJobSearchResultPersistenceService = rankedJobSearchResultPersistenceService;
    }

    public List<RankedJobSearchResult> searchAndRankJobs(
            Long profileId,
            String location,
            Integer targetPerProvider,
            Integer legacyTarget,
            String jobLevel
    ) {
        SearchProfile profile = searchProfileService.getSearchProfileEntityById(profileId);
        int resolvedTargetPerProvider = resolveTargetPerProvider(targetPerProvider, legacyTarget);
        JobSeniorityPreference seniorityPreference = JobSeniorityPreference.from(jobLevel);

        List<String> jobSearchQueries = buildJobSearchQueries(
                profile.getSearchRoles(),
                searchQueryGenerationService.getSavedJobSearchQueriesForProfile(profileId)
        );

        PrefilteredJobSearchResult prefilterResult =
                multiProviderPrefilteredJobSearchService.searchPrefilteredJobsWithStatistics(
                        location,
                        jobSearchQueries,
                        profile.getSearchRoles(),
                        profile.getKeywords(),
                        resolvedTargetPerProvider,
                        seniorityPreference
                );
        List<JobSearchResult> prefilteredJobs = prefilterResult.getJobs();

        List<JobSearchResult> jobsNotSeenInMatchHistory =
                rankedJobSearchHistoryService.removeJobsAlreadySavedInHistory(
                        profileId,
                        prefilteredJobs
                );

        JobSearchPrefilterStatistics statistics = prefilterResult.getStatistics();
        statistics.setAfterMatchHistoryExclusion(jobsNotSeenInMatchHistory.size());
        statistics.setSentToGemini(jobsNotSeenInMatchHistory.size());

        System.out.println("Prefiltered jobs before match history exclusion: " + prefilteredJobs.size());
        System.out.println("Prefiltered jobs after match history exclusion: " + jobsNotSeenInMatchHistory.size());
        logRankingPrefilterStatistics(statistics);

        List<RankedJobSearchResult> rankedJobs =
                jobMatchRankingService.rankJobs(profile, jobsNotSeenInMatchHistory);

        rankedJobSearchResultPersistenceService.saveRankedResults(
                profile,
                location,
                rankedJobs
        );

        return rankedJobs;
    }

    private List<String> buildJobSearchQueries(
            List<String> profileRoles,
            List<String> savedGeneratedQueries
    ) {
        Set<String> queries = new LinkedHashSet<>();

        addQueries(queries, profileRoles);
        addQueries(queries, savedGeneratedQueries);

        return new ArrayList<>(queries);
    }

    private int resolveTargetPerProvider(
            Integer targetPerProvider,
            Integer legacyTarget
    ) {
        if (targetPerProvider != null && targetPerProvider > 0) {
            return targetPerProvider;
        }

        if (legacyTarget != null && legacyTarget > 0) {
            return legacyTarget;
        }

        return DEFAULT_TARGET_PER_PROVIDER;
    }

    private void addQueries(Set<String> queries, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            queries.add(value.trim());
        }
    }

    private void logRankingPrefilterStatistics(JobSearchPrefilterStatistics statistics) {
        System.out.println("========== RANKING PREFILTER SUMMARY ==========");
        System.out.println("Total candidates: " + statistics.getTotalCandidates());
        System.out.println("After location filter: " + statistics.getTotalAfterLocationFilter());
        System.out.println("After senior filter: " + statistics.getTotalAfterSeniorFilter());
        System.out.println("After profile filter: " + statistics.getTotalAfterProfileFilter());
        System.out.println("After provider deduplication: " + statistics.getTotalAfterDeduplication());
        System.out.println("After match history exclusion: " + statistics.getAfterMatchHistoryExclusion());
        System.out.println("Sent to Gemini: " + statistics.getSentToGemini());
        System.out.println("===============================================");
    }
}

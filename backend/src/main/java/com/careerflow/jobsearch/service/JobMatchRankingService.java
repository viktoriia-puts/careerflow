package com.careerflow.jobsearch.service;

import com.careerflow.ai.GeminiJobMatchClient;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.searchprofile.entity.SearchProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class JobMatchRankingService {

    private static final int MINIMUM_MATCH_SCORE = 40;

    private final GeminiJobMatchClient geminiJobMatchClient;
    private final long geminiDelayMs;

    @Autowired
    public JobMatchRankingService(
            GeminiJobMatchClient geminiJobMatchClient,
            @Value("${jobmatch.ranking.gemini-delay-ms:5000}") long geminiDelayMs
    ) {
        this.geminiJobMatchClient = geminiJobMatchClient;
        this.geminiDelayMs = geminiDelayMs;
    }

    public JobMatchRankingService(GeminiJobMatchClient geminiJobMatchClient) {
        this(geminiJobMatchClient, 0);
    }

    public List<RankedJobSearchResult> rankJobs(
            SearchProfile profile,
            List<JobSearchResult> jobs
    ) {
        List<RankedJobSearchResult> rankedJobs = new ArrayList<>();

        if (profile == null || jobs == null || jobs.isEmpty()) {
            return rankedJobs;
        }

        for (int index = 0; index < jobs.size(); index++) {
            delayBeforeGeminiCall(index);

            JobSearchResult job = jobs.get(index);
            String jobDescription = buildJobDescriptionForGemini(job);

            JobMatchAnalysisResponse matchAnalysis =
                    geminiJobMatchClient.analyzeJobMatch(profile, jobDescription);

            if (isAboveMinimumMatchScore(matchAnalysis)) {
                rankedJobs.add(new RankedJobSearchResult(job, matchAnalysis));
            }
        }

        rankedJobs.sort(
                Comparator.comparingInt(
                        (RankedJobSearchResult rankedJob) ->
                                getMatchScore(rankedJob)
                ).reversed()
        );

        return rankedJobs;
    }

    private void delayBeforeGeminiCall(int jobIndex) {
        if (jobIndex <= 0 || geminiDelayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(geminiDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting between Gemini calls", e);
        }
    }

    public List<RankedJobSearchResult> rankJobs(
            SearchProfile profile,
            List<JobSearchResult> jobs,
            int limit
    ) {
        List<RankedJobSearchResult> rankedJobs = rankJobs(profile, jobs);

        if (limit <= 0 || rankedJobs.size() <= limit) {
            return rankedJobs;
        }

        return new ArrayList<>(rankedJobs.subList(0, limit));
    }

    private boolean isAboveMinimumMatchScore(JobMatchAnalysisResponse matchAnalysis) {
        if (matchAnalysis == null || matchAnalysis.getMatchScore() == null) {
            return false;
        }

        return matchAnalysis.getMatchScore() > MINIMUM_MATCH_SCORE;
    }

    private int getMatchScore(RankedJobSearchResult rankedJob) {
        if (rankedJob == null || rankedJob.getMatchAnalysis() == null) {
            return 0;
        }

        Integer matchScore = rankedJob.getMatchAnalysis().getMatchScore();

        if (matchScore == null) {
            return 0;
        }

        return matchScore;
    }

    private String buildJobDescriptionForGemini(JobSearchResult job) {
        if (job == null) {
            return "";
        }

        return """
                Title: %s
                Company: %s
                Location: %s

                Description:
                %s
                """.formatted(
                safeString(job.getTitle()),
                safeString(job.getCompany()),
                safeString(job.getLocation()),
                safeString(job.getDescription())
        );
    }

    private String safeString(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}

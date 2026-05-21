package com.careerflow.jobsearch.service;

import com.careerflow.ai.GeminiJobMatchClient;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.searchprofile.entity.SearchProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class JobMatchRankingService {

    private final GeminiJobMatchClient geminiJobMatchClient;

    public JobMatchRankingService(GeminiJobMatchClient geminiJobMatchClient) {
        this.geminiJobMatchClient = geminiJobMatchClient;
    }

    public List<RankedJobSearchResult> rankJobs(
            SearchProfile profile,
            List<JobSearchResult> jobs
    ) {
        List<RankedJobSearchResult> rankedJobs = new ArrayList<>();

        if (profile == null || jobs == null || jobs.isEmpty()) {
            return rankedJobs;
        }

        for (JobSearchResult job : jobs) {
            String jobDescription = buildJobDescriptionForGemini(job);

            JobMatchAnalysisResponse matchAnalysis =
                    geminiJobMatchClient.analyzeJobMatch(profile, jobDescription);

            rankedJobs.add(new RankedJobSearchResult(job, matchAnalysis));
        }

        rankedJobs.sort(
                Comparator.comparingInt(
                        (RankedJobSearchResult rankedJob) ->
                                rankedJob.getMatchAnalysis().getMatchScore()
                ).reversed()
        );

        return rankedJobs;
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
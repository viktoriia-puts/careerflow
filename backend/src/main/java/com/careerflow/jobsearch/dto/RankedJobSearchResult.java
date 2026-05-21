package com.careerflow.jobsearch.dto;

import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;

public class RankedJobSearchResult {

    private JobSearchResult job;
    private JobMatchAnalysisResponse matchAnalysis;

    public RankedJobSearchResult() {
    }

    public RankedJobSearchResult(
            JobSearchResult job,
            JobMatchAnalysisResponse matchAnalysis
    ) {
        this.job = job;
        this.matchAnalysis = matchAnalysis;
    }

    public JobSearchResult getJob() {
        return job;
    }

    public void setJob(JobSearchResult job) {
        this.job = job;
    }

    public JobMatchAnalysisResponse getMatchAnalysis() {
        return matchAnalysis;
    }

    public void setMatchAnalysis(JobMatchAnalysisResponse matchAnalysis) {
        this.matchAnalysis = matchAnalysis;
    }
}
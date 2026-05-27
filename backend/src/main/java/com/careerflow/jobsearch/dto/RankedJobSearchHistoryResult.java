package com.careerflow.jobsearch.dto;

import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;

public class RankedJobSearchHistoryResult {

    private Long id;
    private int positionIndex;
    private JobSearchResult job;
    private JobMatchAnalysisResponse matchAnalysis;

    public RankedJobSearchHistoryResult() {
    }

    public RankedJobSearchHistoryResult(
            Long id,
            int positionIndex,
            JobSearchResult job,
            JobMatchAnalysisResponse matchAnalysis
    ) {
        this.id = id;
        this.positionIndex = positionIndex;
        this.job = job;
        this.matchAnalysis = matchAnalysis;
    }

    public Long getId() {
        return id;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public JobSearchResult getJob() {
        return job;
    }

    public JobMatchAnalysisResponse getMatchAnalysis() {
        return matchAnalysis;
    }
}

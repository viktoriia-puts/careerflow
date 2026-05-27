package com.careerflow.jobsearch.dto;

import java.util.List;

public class JobPrefilterResult {

    private final List<JobSearchResult> jobs;
    private final int candidateCount;
    private final int afterSeniorFilterCount;
    private final int afterProfileFilterCount;

    public JobPrefilterResult(
            List<JobSearchResult> jobs,
            int candidateCount,
            int afterSeniorFilterCount,
            int afterProfileFilterCount
    ) {
        this.jobs = jobs;
        this.candidateCount = candidateCount;
        this.afterSeniorFilterCount = afterSeniorFilterCount;
        this.afterProfileFilterCount = afterProfileFilterCount;
    }

    public List<JobSearchResult> getJobs() {
        return jobs;
    }

    public int getCandidateCount() {
        return candidateCount;
    }

    public int getAfterSeniorFilterCount() {
        return afterSeniorFilterCount;
    }

    public int getAfterProfileFilterCount() {
        return afterProfileFilterCount;
    }
}

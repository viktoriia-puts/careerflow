package com.careerflow.jobsearch.dto;

import java.util.List;

public class ProviderSearchResult {

    private final List<JobSearchResult> jobs;
    private final ProviderPrefilterStatistics statistics;

    public ProviderSearchResult(
            List<JobSearchResult> jobs,
            ProviderPrefilterStatistics statistics
    ) {
        this.jobs = jobs;
        this.statistics = statistics;
    }

    public List<JobSearchResult> getJobs() {
        return jobs;
    }

    public ProviderPrefilterStatistics getStatistics() {
        return statistics;
    }
}

package com.careerflow.jobsearch.dto;

import java.util.List;

public class PrefilteredJobSearchResult {

    private final List<JobSearchResult> jobs;
    private final JobSearchPrefilterStatistics statistics;

    public PrefilteredJobSearchResult(
            List<JobSearchResult> jobs,
            JobSearchPrefilterStatistics statistics
    ) {
        this.jobs = jobs;
        this.statistics = statistics;
    }

    public List<JobSearchResult> getJobs() {
        return jobs;
    }

    public JobSearchPrefilterStatistics getStatistics() {
        return statistics;
    }
}

package com.careerflow.jobmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JobMatchAnalysisRequest {

    @NotNull(message = "Search profile ID must not be null")
    private Long searchProfileId;

    @NotBlank(message = "Job description must not be blank")
    private String jobDescription;

    public JobMatchAnalysisRequest() {
    }

    public JobMatchAnalysisRequest(Long searchProfileId, String jobDescription) {
        this.searchProfileId = searchProfileId;
        this.jobDescription = jobDescription;
    }

    public Long getSearchProfileId() {
        return searchProfileId;
    }

    public void setSearchProfileId(Long searchProfileId) {
        this.searchProfileId = searchProfileId;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}


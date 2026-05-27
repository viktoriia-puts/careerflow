package com.careerflow.jobsearch.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RankedJobSearchRunDetailResponse {

    private Long id;
    private Long searchProfileId;
    private String location;
    private LocalDateTime createdAt;
    private List<RankedJobSearchHistoryResult> results;

    public RankedJobSearchRunDetailResponse() {
    }

    public RankedJobSearchRunDetailResponse(
            Long id,
            Long searchProfileId,
            String location,
            LocalDateTime createdAt,
            List<RankedJobSearchHistoryResult> results
    ) {
        this.id = id;
        this.searchProfileId = searchProfileId;
        this.location = location;
        this.createdAt = createdAt;
        this.results = results;
    }

    public Long getId() {
        return id;
    }

    public Long getSearchProfileId() {
        return searchProfileId;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<RankedJobSearchHistoryResult> getResults() {
        return results;
    }
}

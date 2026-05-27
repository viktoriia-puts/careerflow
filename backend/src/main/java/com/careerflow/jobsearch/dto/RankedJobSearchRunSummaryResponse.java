package com.careerflow.jobsearch.dto;

import java.time.LocalDateTime;

public class RankedJobSearchRunSummaryResponse {

    private Long id;
    private Long searchProfileId;
    private String location;
    private LocalDateTime createdAt;
    private int resultCount;
    private Integer topMatchScore;

    public RankedJobSearchRunSummaryResponse() {
    }

    public RankedJobSearchRunSummaryResponse(
            Long id,
            Long searchProfileId,
            String location,
            LocalDateTime createdAt,
            int resultCount,
            Integer topMatchScore
    ) {
        this.id = id;
        this.searchProfileId = searchProfileId;
        this.location = location;
        this.createdAt = createdAt;
        this.resultCount = resultCount;
        this.topMatchScore = topMatchScore;
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

    public int getResultCount() {
        return resultCount;
    }

    public Integer getTopMatchScore() {
        return topMatchScore;
    }
}

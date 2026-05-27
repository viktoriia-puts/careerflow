package com.careerflow.jobtracker.dto;

import com.careerflow.jobtracker.entity.TrackedJobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrackedJobResponse {

    private Long id;
    private Long searchProfileId;
    private String company;
    private String positionTitle;
    private String location;
    private String source;
    private String jobUrl;
    private String referenceId;
    private Integer matchScore;
    private TrackedJobStatus status;
    private LocalDate appliedDate;
    private String resultNote;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TrackedJobResponse() {
    }

    public TrackedJobResponse(
            Long id,
            Long searchProfileId,
            String company,
            String positionTitle,
            String location,
            String source,
            String jobUrl,
            String referenceId,
            Integer matchScore,
            TrackedJobStatus status,
            LocalDate appliedDate,
            String resultNote,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.searchProfileId = searchProfileId;
        this.company = company;
        this.positionTitle = positionTitle;
        this.location = location;
        this.source = source;
        this.jobUrl = jobUrl;
        this.referenceId = referenceId;
        this.matchScore = matchScore;
        this.status = status;
        this.appliedDate = appliedDate;
        this.resultNote = resultNote;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSearchProfileId() {
        return searchProfileId;
    }

    public String getCompany() {
        return company;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public String getLocation() {
        return location;
    }

    public String getSource() {
        return source;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public TrackedJobStatus getStatus() {
        return status;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public String getResultNote() {
        return resultNote;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

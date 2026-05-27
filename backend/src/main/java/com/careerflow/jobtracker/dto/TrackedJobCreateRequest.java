package com.careerflow.jobtracker.dto;

import com.careerflow.jobtracker.entity.TrackedJobStatus;

public class TrackedJobCreateRequest {

    private Long searchProfileId;
    private String company;
    private String positionTitle;
    private String location;
    private String source;
    private String jobUrl;
    private String referenceId;
    private Integer matchScore;
    private TrackedJobStatus status;
    private String notes;

    public Long getSearchProfileId() {
        return searchProfileId;
    }

    public void setSearchProfileId(Long searchProfileId) {
        this.searchProfileId = searchProfileId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public TrackedJobStatus getStatus() {
        return status;
    }

    public void setStatus(TrackedJobStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

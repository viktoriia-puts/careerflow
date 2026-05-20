package com.careerflow.jobsearch.dto;

public class JobSearchResult {

    private String source;
    private String title;
    private String company;
    private String location;
    private String description;
    private String url;
    private String referenceId;
    private String publishedAt;
    private boolean fullDescriptionAvailable;

    public JobSearchResult() {
    }

    public JobSearchResult(
            String source,
            String title,
            String company,
            String location,
            String description,
            String url,
            String referenceId,
            String publishedAt,
            boolean fullDescriptionAvailable
    ) {
        this.source = source;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.url = url;
        this.referenceId = referenceId;
        this.publishedAt = publishedAt;
        this.fullDescriptionAvailable = fullDescriptionAvailable;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isFullDescriptionAvailable() {
        return fullDescriptionAvailable;
    }

    public void setFullDescriptionAvailable(boolean fullDescriptionAvailable) {
        this.fullDescriptionAvailable = fullDescriptionAvailable;
    }
}
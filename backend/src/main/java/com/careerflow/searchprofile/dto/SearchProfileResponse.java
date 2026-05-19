package com.careerflow.searchprofile.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SearchProfileResponse {

    private Long id;
    private String summary;
    private List<String> searchRoles;
    private List<String> alternativeCareerRoles;
    private List<String> keywords;
    private LocalDateTime createdAt;

    // Constructors
    public SearchProfileResponse() {
    }

    public SearchProfileResponse(Long id, String summary, List<String> searchRoles, List<String> alternativeCareerRoles, List<String> keywords, LocalDateTime createdAt) {
        this.id = id;
        this.summary = summary;
        this.searchRoles = searchRoles;
        this.alternativeCareerRoles = alternativeCareerRoles;
        this.keywords = keywords;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getSearchRoles() {
        return searchRoles;
    }

    public void setSearchRoles(List<String> searchRoles) {
        this.searchRoles = searchRoles;
    }

    public List<String> getAlternativeCareerRoles() {
        return alternativeCareerRoles;
    }

    public void setAlternativeCareerRoles(List<String> alternativeCareerRoles) {
        this.alternativeCareerRoles = alternativeCareerRoles;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


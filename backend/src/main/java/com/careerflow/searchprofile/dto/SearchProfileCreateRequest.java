package com.careerflow.searchprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SearchProfileCreateRequest {

    @NotBlank(message = "Summary must not be blank")
    private String summary;

    @NotEmpty(message = "SearchRoles must not be empty")
    private List<String> searchRoles;

    private List<String> alternativeCareerRoles;

    @NotEmpty(message = "Keywords must not be empty")
    private List<String> keywords;

    // Constructors
    public SearchProfileCreateRequest() {
    }

    public SearchProfileCreateRequest(String summary, List<String> searchRoles, List<String> alternativeCareerRoles, List<String> keywords) {
        this.summary = summary;
        this.searchRoles = searchRoles;
        this.alternativeCareerRoles = alternativeCareerRoles;
        this.keywords = keywords;
    }

    // Getters and Setters
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
}


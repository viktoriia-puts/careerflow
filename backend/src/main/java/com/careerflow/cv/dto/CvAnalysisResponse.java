package com.careerflow.cv.dto;

import java.util.List;

public class CvAnalysisResponse {

    private String summary;
    private List<String> suggestedRoles;
    private List<String> keywords;

    // Constructor
    public CvAnalysisResponse() {
    }

    public CvAnalysisResponse(String summary, List<String> suggestedRoles, List<String> keywords) {
        this.summary = summary;
        this.suggestedRoles = suggestedRoles;
        this.keywords = keywords;
    }

    // Getters
    public String getSummary() {
        return summary;
    }

    public List<String> getSuggestedRoles() {
        return suggestedRoles;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    // Setters
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setSuggestedRoles(List<String> suggestedRoles) {
        this.suggestedRoles = suggestedRoles;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}


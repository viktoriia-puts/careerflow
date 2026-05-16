package com.careerflow.cv.dto;

import java.util.List;

public class CvAnalysisResponse {

    private String summary;
    private List<String> searchRoles;
    private List<String> alternativeCareerRoles;
    private List<String> keywords;

    public CvAnalysisResponse() {
    }

    public CvAnalysisResponse(
            String summary,
            List<String> searchRoles,
            List<String> alternativeCareerRoles,
            List<String> keywords
    ) {
        this.summary = summary;
        this.searchRoles = searchRoles;
        this.alternativeCareerRoles = alternativeCareerRoles;
        this.keywords = keywords;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getSearchRoles() {
        return searchRoles;
    }

    public List<String> getAlternativeCareerRoles() {
        return alternativeCareerRoles;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setSearchRoles(List<String> searchRoles) {
        this.searchRoles = searchRoles;
    }

    public void setAlternativeCareerRoles(List<String> alternativeCareerRoles) {
        this.alternativeCareerRoles = alternativeCareerRoles;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
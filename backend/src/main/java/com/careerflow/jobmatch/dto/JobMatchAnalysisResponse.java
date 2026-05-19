package com.careerflow.jobmatch.dto;

import java.util.List;

public class JobMatchAnalysisResponse {

    private Integer matchScore;
    private String recommendation;
    private String summary;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> concerns;
    private List<String> suggestedApplicationFocus;

    public JobMatchAnalysisResponse() {
    }

    public JobMatchAnalysisResponse(
            Integer matchScore,
            String recommendation,
            String summary,
            List<String> matchingSkills,
            List<String> missingSkills,
            List<String> concerns,
            List<String> suggestedApplicationFocus) {
        this.matchScore = matchScore;
        this.recommendation = recommendation;
        this.summary = summary;
        this.matchingSkills = matchingSkills;
        this.missingSkills = missingSkills;
        this.concerns = concerns;
        this.suggestedApplicationFocus = suggestedApplicationFocus;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(List<String> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(List<String> concerns) {
        this.concerns = concerns;
    }

    public List<String> getSuggestedApplicationFocus() {
        return suggestedApplicationFocus;
    }

    public void setSuggestedApplicationFocus(List<String> suggestedApplicationFocus) {
        this.suggestedApplicationFocus = suggestedApplicationFocus;
    }
}


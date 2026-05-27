package com.careerflow.jobsearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ranked_job_search_results")
public class RankedJobSearchResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_run_id", nullable = false)
    private RankedJobSearchRun searchRun;

    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    @Column(name = "job_source", length = 100)
    private String jobSource;

    @Column(name = "job_title", length = 500)
    private String jobTitle;

    @Column(name = "job_company", length = 500)
    private String jobCompany;

    @Column(name = "job_location", length = 500)
    private String jobLocation;

    @Column(name = "job_description", columnDefinition = "LONGTEXT")
    private String jobDescription;

    @Column(name = "job_url", columnDefinition = "LONGTEXT")
    private String jobUrl;

    @Column(name = "job_reference_id", length = 500)
    private String jobReferenceId;

    @Column(name = "job_published_at", length = 100)
    private String jobPublishedAt;

    @Column(name = "full_description_available", nullable = false)
    private boolean fullDescriptionAvailable;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "recommendation", length = 100)
    private String recommendation;

    @Column(name = "match_summary", columnDefinition = "LONGTEXT")
    private String matchSummary;

    @Column(name = "matching_skills_json", columnDefinition = "LONGTEXT")
    private String matchingSkillsJson;

    @Column(name = "missing_skills_json", columnDefinition = "LONGTEXT")
    private String missingSkillsJson;

    @Column(name = "concerns_json", columnDefinition = "LONGTEXT")
    private String concernsJson;

    @Column(name = "suggested_application_focus_json", columnDefinition = "LONGTEXT")
    private String suggestedApplicationFocusJson;

    public Long getId() {
        return id;
    }

    public RankedJobSearchRun getSearchRun() {
        return searchRun;
    }

    public void setSearchRun(RankedJobSearchRun searchRun) {
        this.searchRun = searchRun;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public String getJobSource() {
        return jobSource;
    }

    public void setJobSource(String jobSource) {
        this.jobSource = jobSource;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobCompany() {
        return jobCompany;
    }

    public void setJobCompany(String jobCompany) {
        this.jobCompany = jobCompany;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobReferenceId() {
        return jobReferenceId;
    }

    public void setJobReferenceId(String jobReferenceId) {
        this.jobReferenceId = jobReferenceId;
    }

    public String getJobPublishedAt() {
        return jobPublishedAt;
    }

    public void setJobPublishedAt(String jobPublishedAt) {
        this.jobPublishedAt = jobPublishedAt;
    }

    public boolean isFullDescriptionAvailable() {
        return fullDescriptionAvailable;
    }

    public void setFullDescriptionAvailable(boolean fullDescriptionAvailable) {
        this.fullDescriptionAvailable = fullDescriptionAvailable;
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

    public String getMatchSummary() {
        return matchSummary;
    }

    public void setMatchSummary(String matchSummary) {
        this.matchSummary = matchSummary;
    }

    public String getMatchingSkillsJson() {
        return matchingSkillsJson;
    }

    public void setMatchingSkillsJson(String matchingSkillsJson) {
        this.matchingSkillsJson = matchingSkillsJson;
    }

    public String getMissingSkillsJson() {
        return missingSkillsJson;
    }

    public void setMissingSkillsJson(String missingSkillsJson) {
        this.missingSkillsJson = missingSkillsJson;
    }

    public String getConcernsJson() {
        return concernsJson;
    }

    public void setConcernsJson(String concernsJson) {
        this.concernsJson = concernsJson;
    }

    public String getSuggestedApplicationFocusJson() {
        return suggestedApplicationFocusJson;
    }

    public void setSuggestedApplicationFocusJson(String suggestedApplicationFocusJson) {
        this.suggestedApplicationFocusJson = suggestedApplicationFocusJson;
    }
}

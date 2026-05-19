package com.careerflow.searchprofile.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "search_profiles")
public class SearchProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @ElementCollection
    @CollectionTable(name = "search_profile_search_roles", joinColumns = @JoinColumn(name = "search_profile_id"))
    @Column(name = "role")
    private List<String> searchRoles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "search_profile_alternative_career_roles", joinColumns = @JoinColumn(name = "search_profile_id"))
    @Column(name = "role")
    private List<String> alternativeCareerRoles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "search_profile_keywords", joinColumns = @JoinColumn(name = "search_profile_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public SearchProfile() {
    }

    public SearchProfile(String summary, List<String> searchRoles, List<String> alternativeCareerRoles, List<String> keywords) {
        this.summary = summary;
        this.searchRoles = searchRoles != null ? new ArrayList<>(searchRoles) : new ArrayList<>();
        this.alternativeCareerRoles = alternativeCareerRoles != null ? new ArrayList<>(alternativeCareerRoles) : new ArrayList<>();
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
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
        this.searchRoles = searchRoles != null ? searchRoles : new ArrayList<>();
    }

    public List<String> getAlternativeCareerRoles() {
        return alternativeCareerRoles;
    }

    public void setAlternativeCareerRoles(List<String> alternativeCareerRoles) {
        this.alternativeCareerRoles = alternativeCareerRoles != null ? alternativeCareerRoles : new ArrayList<>();
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


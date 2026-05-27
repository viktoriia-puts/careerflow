package com.careerflow.querygeneration.entity;

import com.careerflow.searchprofile.entity.SearchProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "search_profile_generated_queries")
public class SearchProfileGeneratedQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_profile_id", nullable = false)
    private SearchProfile searchProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false, length = 50)
    private SearchProfileGeneratedQueryType queryType;

    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;

    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    public SearchProfileGeneratedQuery() {
    }

    public SearchProfileGeneratedQuery(
            SearchProfile searchProfile,
            SearchProfileGeneratedQueryType queryType,
            String queryText,
            int positionIndex
    ) {
        this.searchProfile = searchProfile;
        this.queryType = queryType;
        this.queryText = queryText;
        this.positionIndex = positionIndex;
    }

    public Long getId() {
        return id;
    }

    public SearchProfile getSearchProfile() {
        return searchProfile;
    }

    public void setSearchProfile(SearchProfile searchProfile) {
        this.searchProfile = searchProfile;
    }

    public SearchProfileGeneratedQueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(SearchProfileGeneratedQueryType queryType) {
        this.queryType = queryType;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }
}

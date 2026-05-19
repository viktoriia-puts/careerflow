package com.careerflow.querygeneration.dto;

import java.util.List;

public class SearchQueryGenerationResponse {

    private List<String> roleTitleQueries;
    private List<String> requirementBasedQueries;
    private List<String> alternativeDirectionQueries;

    public SearchQueryGenerationResponse() {
    }

    public SearchQueryGenerationResponse(
            List<String> roleTitleQueries,
            List<String> requirementBasedQueries,
            List<String> alternativeDirectionQueries) {
        this.roleTitleQueries = roleTitleQueries;
        this.requirementBasedQueries = requirementBasedQueries;
        this.alternativeDirectionQueries = alternativeDirectionQueries;
    }

    public List<String> getRoleTitleQueries() {
        return roleTitleQueries;
    }

    public void setRoleTitleQueries(List<String> roleTitleQueries) {
        this.roleTitleQueries = roleTitleQueries;
    }

    public List<String> getRequirementBasedQueries() {
        return requirementBasedQueries;
    }

    public void setRequirementBasedQueries(List<String> requirementBasedQueries) {
        this.requirementBasedQueries = requirementBasedQueries;
    }

    public List<String> getAlternativeDirectionQueries() {
        return alternativeDirectionQueries;
    }

    public void setAlternativeDirectionQueries(List<String> alternativeDirectionQueries) {
        this.alternativeDirectionQueries = alternativeDirectionQueries;
    }
}


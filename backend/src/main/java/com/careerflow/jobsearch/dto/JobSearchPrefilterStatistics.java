package com.careerflow.jobsearch.dto;

import java.util.List;

public class JobSearchPrefilterStatistics {

    private final List<ProviderPrefilterStatistics> providerStatistics;
    private final int totalCandidates;
    private final int totalAfterLocationFilter;
    private final int totalAfterSeniorFilter;
    private final int totalAfterProfileFilter;
    private final int totalAfterDeduplication;
    private int afterMatchHistoryExclusion;
    private int sentToGemini;

    public JobSearchPrefilterStatistics(
            List<ProviderPrefilterStatistics> providerStatistics,
            int totalAfterDeduplication
    ) {
        this.providerStatistics = providerStatistics;
        this.totalCandidates = providerStatistics.stream()
                .mapToInt(ProviderPrefilterStatistics::getCandidates)
                .sum();
        this.totalAfterLocationFilter = providerStatistics.stream()
                .mapToInt(ProviderPrefilterStatistics::getAfterLocationFilter)
                .sum();
        this.totalAfterSeniorFilter = providerStatistics.stream()
                .mapToInt(ProviderPrefilterStatistics::getAfterSeniorFilter)
                .sum();
        this.totalAfterProfileFilter = providerStatistics.stream()
                .mapToInt(ProviderPrefilterStatistics::getAfterProfileFilter)
                .sum();
        this.totalAfterDeduplication = totalAfterDeduplication;
    }

    public List<ProviderPrefilterStatistics> getProviderStatistics() {
        return providerStatistics;
    }

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public int getTotalAfterLocationFilter() {
        return totalAfterLocationFilter;
    }

    public int getTotalAfterSeniorFilter() {
        return totalAfterSeniorFilter;
    }

    public int getTotalAfterProfileFilter() {
        return totalAfterProfileFilter;
    }

    public int getTotalAfterDeduplication() {
        return totalAfterDeduplication;
    }

    public int getAfterMatchHistoryExclusion() {
        return afterMatchHistoryExclusion;
    }

    public void setAfterMatchHistoryExclusion(int afterMatchHistoryExclusion) {
        this.afterMatchHistoryExclusion = afterMatchHistoryExclusion;
    }

    public int getSentToGemini() {
        return sentToGemini;
    }

    public void setSentToGemini(int sentToGemini) {
        this.sentToGemini = sentToGemini;
    }
}

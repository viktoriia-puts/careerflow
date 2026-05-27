package com.careerflow.jobsearch.dto;

public class ProviderPrefilterStatistics {

    private final String providerName;
    private final int candidates;
    private final int afterLocationFilter;
    private final int afterSeniorFilter;
    private final int afterProfileFilter;
    private final int returnedJobs;
    private final boolean failed;
    private final String errorMessage;

    public ProviderPrefilterStatistics(
            String providerName,
            int candidates,
            int afterLocationFilter,
            int afterSeniorFilter,
            int afterProfileFilter,
            int returnedJobs,
            boolean failed,
            String errorMessage
    ) {
        this.providerName = providerName;
        this.candidates = candidates;
        this.afterLocationFilter = afterLocationFilter;
        this.afterSeniorFilter = afterSeniorFilter;
        this.afterProfileFilter = afterProfileFilter;
        this.returnedJobs = returnedJobs;
        this.failed = failed;
        this.errorMessage = errorMessage;
    }

    public static ProviderPrefilterStatistics failed(String providerName, String errorMessage) {
        return new ProviderPrefilterStatistics(
                providerName,
                0,
                0,
                0,
                0,
                0,
                true,
                errorMessage
        );
    }

    public String getProviderName() {
        return providerName;
    }

    public int getCandidates() {
        return candidates;
    }

    public int getAfterLocationFilter() {
        return afterLocationFilter;
    }

    public int getAfterSeniorFilter() {
        return afterSeniorFilter;
    }

    public int getAfterProfileFilter() {
        return afterProfileFilter;
    }

    public int getReturnedJobs() {
        return returnedJobs;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

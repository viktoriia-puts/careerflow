package com.careerflow.jobsearch.service;

import java.util.Locale;

public enum JobSeniorityPreference {
    JUNIOR,
    SENIOR_MIDDLE;

    public static JobSeniorityPreference from(String value) {
        if (value == null || value.isBlank()) {
            return JUNIOR;
        }

        String normalizedValue = value.trim()
                .replace("-", "_")
                .toUpperCase(Locale.ROOT);

        if (normalizedValue.equals("SENIOR")
                || normalizedValue.equals("MIDDLE")
                || normalizedValue.equals("SENIOR_MIDDLE")
                || normalizedValue.equals("MIDDLE_SENIOR")) {
            return SENIOR_MIDDLE;
        }

        return JUNIOR;
    }
}

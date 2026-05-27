package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobPrefilterServiceTests {

    private final JobPrefilterService jobPrefilterService = new JobPrefilterService();

    @Test
    void prefilterWithoutTargetReturnsAllMatchingNonSeniorJobs() {
        List<JobSearchResult> jobs = List.of(
                job("Junior Java Developer", "Java Spring onboarding and learning support"),
                job("Senior Java Developer", "Java Spring backend development"),
                job("Backend Developer", "Java backend APIs")
        );

        List<JobSearchResult> result = jobPrefilterService.prefilter(
                jobs,
                List.of("Developer"),
                List.of("Java")
        );

        assertThat(result)
                .extracting(JobSearchResult::getTitle)
                .containsExactly("Junior Java Developer", "Backend Developer");
    }

    @Test
    void prefilterWithTargetKeepsExistingLimitBehavior() {
        List<JobSearchResult> jobs = List.of(
                job("Junior Java Developer", "Java Spring onboarding and learning support"),
                job("Backend Developer", "Java backend APIs")
        );

        List<JobSearchResult> result = jobPrefilterService.prefilter(
                jobs,
                List.of("Developer"),
                List.of("Java"),
                1
        );

        assertThat(result)
                .extracting(JobSearchResult::getTitle)
                .containsExactly("Junior Java Developer");
    }

    @Test
    void prefilterRemovesJobsWithGermanSeniorExperienceSignals() {
        List<JobSearchResult> jobs = List.of(
                job("Java Entwickler", "Java Spring und mehrjährige Erfahrung im Backend"),
                job("Junior Java Entwickler", "Java Spring mit strukturierter Einarbeitung")
        );

        List<JobSearchResult> result = jobPrefilterService.prefilter(
                jobs,
                List.of("Entwickler"),
                List.of("Java")
        );

        assertThat(result)
                .extracting(JobSearchResult::getTitle)
                .containsExactly("Junior Java Entwickler");
    }

    @Test
    void prefilterKeepsJobsWithExpandedJuniorFriendlySignals() {
        List<JobSearchResult> jobs = List.of(
                job("Java Support Specialist", "Berufseinstieg mit Java Grundkenntnissen und Weiterbildung"),
                job("Java Support Specialist Senior", "Java und langjährige Erfahrung")
        );

        List<JobSearchResult> result = jobPrefilterService.prefilter(
                jobs,
                List.of("Developer"),
                List.of("Java")
        );

        assertThat(result)
                .extracting(JobSearchResult::getTitle)
                .containsExactly("Java Support Specialist");
    }

    private JobSearchResult job(String title, String description) {
        return new JobSearchResult(
                "TEST",
                title,
                "Company",
                "Berlin",
                description,
                "https://example.com",
                title,
                "2026-05-24",
                false
        );
    }
}

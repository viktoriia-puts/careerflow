package com.careerflow.jobsearch.service;

import com.careerflow.ai.GeminiJobMatchClient;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobsearch.dto.JobSearchResult;
import com.careerflow.jobsearch.dto.RankedJobSearchResult;
import com.careerflow.searchprofile.entity.SearchProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class JobMatchRankingServiceTests {

    @Test
    void rankJobsReturnsOnlyMatchesAboveFortyPercent() {
        JobMatchRankingService service = new JobMatchRankingService(
                new FakeGeminiJobMatchClient(40, 41, 80)
        );

        List<RankedJobSearchResult> result = service.rankJobs(
                profile(),
                List.of(
                        job("Weak match"),
                        job("Partial match"),
                        job("Strong match")
                )
        );

        assertThat(result)
                .extracting(rankedJob -> rankedJob.getJob().getTitle())
                .containsExactly("Strong match", "Partial match");
    }

    @Test
    void rankJobsAppliesLimitAfterFilteringWeakMatches() {
        JobMatchRankingService service = new JobMatchRankingService(
                new FakeGeminiJobMatchClient(40, 75, 60)
        );

        List<RankedJobSearchResult> result = service.rankJobs(
                profile(),
                List.of(
                        job("Weak match"),
                        job("Best match"),
                        job("Good match")
                ),
                1
        );

        assertThat(result)
                .extracting(rankedJob -> rankedJob.getJob().getTitle())
                .containsExactly("Best match");
    }

    private SearchProfile profile() {
        return new SearchProfile(
                "Junior Java developer",
                List.of("Developer"),
                List.of(),
                List.of("Java")
        );
    }

    private JobSearchResult job(String title) {
        return new JobSearchResult(
                "TEST",
                title,
                "Company",
                "Berlin",
                "Java Spring APIs",
                "https://example.com",
                title,
                "2026-05-24",
                false
        );
    }

    private static class FakeGeminiJobMatchClient extends GeminiJobMatchClient {

        private final Queue<Integer> scores;

        private FakeGeminiJobMatchClient(Integer... scores) {
            super("test-model");
            this.scores = new ArrayDeque<>(List.of(scores));
        }

        @Override
        public JobMatchAnalysisResponse analyzeJobMatch(
                SearchProfile profile,
                String jobDescription
        ) {
            return new JobMatchAnalysisResponse(
                    scores.remove(),
                    "Partial match",
                    "summary",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }
    }
}

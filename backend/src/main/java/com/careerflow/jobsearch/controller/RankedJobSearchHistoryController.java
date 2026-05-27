package com.careerflow.jobsearch.controller;

import com.careerflow.jobsearch.dto.RankedJobSearchRunDetailResponse;
import com.careerflow.jobsearch.dto.RankedJobSearchRunSummaryResponse;
import com.careerflow.jobsearch.service.RankedJobSearchHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-search/match-history")
public class RankedJobSearchHistoryController {

    private final RankedJobSearchHistoryService historyService;

    public RankedJobSearchHistoryController(RankedJobSearchHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<RankedJobSearchRunSummaryResponse>> getRuns(
            @RequestParam Long profileId
    ) {
        return ResponseEntity.ok(historyService.getRuns(profileId));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<RankedJobSearchRunDetailResponse> getRun(
            @PathVariable Long runId,
            @RequestParam Long profileId
    ) {
        return ResponseEntity.ok(historyService.getRun(profileId, runId));
    }

    @DeleteMapping("/results/{resultId}")
    public ResponseEntity<Void> deleteResult(
            @PathVariable Long resultId,
            @RequestParam Long profileId
    ) {
        historyService.deleteResult(profileId, resultId);
        return ResponseEntity.noContent().build();
    }
}

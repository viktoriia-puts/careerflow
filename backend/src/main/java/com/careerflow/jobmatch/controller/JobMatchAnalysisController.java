package com.careerflow.jobmatch.controller;

import com.careerflow.jobmatch.dto.JobMatchAnalysisRequest;
import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.jobmatch.service.JobMatchAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-matches")
public class JobMatchAnalysisController {

    private final JobMatchAnalysisService jobMatchAnalysisService;

    public JobMatchAnalysisController(JobMatchAnalysisService jobMatchAnalysisService) {
        this.jobMatchAnalysisService = jobMatchAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeJobMatch(@Valid @RequestBody JobMatchAnalysisRequest request) {
        try {
            JobMatchAnalysisResponse response = jobMatchAnalysisService.analyzeJobMatch(
                    request.getSearchProfileId(),
                    request.getJobDescription()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to analyze job match: " + e.getMessage() + "\"}");
        }
    }
}


package com.careerflow.cv.controller;

import com.careerflow.cv.dto.CvAnalysisRequest;
import com.careerflow.cv.dto.CvAnalysisResponse;
import com.careerflow.cv.service.CvAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cv")
public class CvAnalysisController {

    private final CvAnalysisService cvAnalysisService;

    public CvAnalysisController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    /**
     * Analyzes a CV text and returns analysis results.
     *
     * @param request the CV analysis request containing the CV text
     * @return ResponseEntity containing the CV analysis response
     */
    @PostMapping("/analyze")
    public ResponseEntity<CvAnalysisResponse> analyzeCv(@Valid @RequestBody CvAnalysisRequest request) {
        CvAnalysisResponse response = cvAnalysisService.analyzeCv(request.getCvText());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}


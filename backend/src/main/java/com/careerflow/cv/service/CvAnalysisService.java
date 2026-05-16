package com.careerflow.cv.service;

import com.careerflow.ai.GeminiCvAnalysisClient;
import com.careerflow.cv.dto.CvAnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class CvAnalysisService {

    private final GeminiCvAnalysisClient geminiCvAnalysisClient;

    public CvAnalysisService(GeminiCvAnalysisClient geminiCvAnalysisClient) {
        this.geminiCvAnalysisClient = geminiCvAnalysisClient;
    }

    public CvAnalysisResponse analyzeCv(String cvText) {
        return geminiCvAnalysisClient.analyzeCv(cvText);
    }
}
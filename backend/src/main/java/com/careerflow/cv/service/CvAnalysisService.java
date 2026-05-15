package com.careerflow.cv.service;

import com.careerflow.cv.dto.CvAnalysisResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CvAnalysisService {

    /**
     * Analyzes CV text and returns mock analysis results.
     * In the future, this will be replaced with real AI analysis.
     *
     * @param cvText the CV text to analyze
     * @return CvAnalysisResponse with mock analysis data
     */
    public CvAnalysisResponse analyzeCv(String cvText) {
        String summary = generateMockSummary(cvText);

        List<String> suggestedRoles = List.of(
                "Junior Java Developer",
                "Backend Developer",
                "Full Stack Developer",
                "Cloud Application Developer"
        );

        List<String> keywords = List.of(
                "Java",
                "Spring Boot",
                "REST API",
                "SQL",
                "MySQL",
                "Angular",
                "Docker",
                "AWS",
                "Git"
        );

        return new CvAnalysisResponse(summary, suggestedRoles, keywords);
    }

    private String generateMockSummary(String cvText) {
        int wordCount = cvText.trim().split("\\s+").length;
        int charCount = cvText.length();

        return String.format(
                "CV text received successfully. The text contains %d words and %d characters. " +
                        "This is a mock analysis result. Later, the system will use an AI service to extract skills, keywords and suitable target roles.",
                wordCount,
                charCount
        );
    }
}
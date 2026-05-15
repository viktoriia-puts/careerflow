package com.careerflow.cv.dto;

import jakarta.validation.constraints.NotBlank;

public class CvAnalysisRequest {

    @NotBlank(message = "CV text cannot be blank")
    private String cvText;

    // Constructor
    public CvAnalysisRequest() {
    }

    public CvAnalysisRequest(String cvText) {
        this.cvText = cvText;
    }

    // Getter
    public String getCvText() {
        return cvText;
    }

    // Setter
    public void setCvText(String cvText) {
        this.cvText = cvText;
    }
}


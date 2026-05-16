package com.careerflow.ai;

import com.careerflow.cv.dto.CvAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiCvAnalysisClient {

    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiCvAnalysisClient(@Value("${gemini.model}") String model) {
        this.model = model;
    }

    public CvAnalysisResponse analyzeCv(String cvText) {
        try (Client client = new Client()) {
            String prompt = buildPrompt(cvText);

            GenerateContentResponse response =
                    client.models.generateContent(model, prompt, null);

            String responseText = response.text();

            if (responseText == null || responseText.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response");
            }

            String cleanedJson = cleanJson(responseText);

            return objectMapper.readValue(cleanedJson, CvAnalysisResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze CV with Gemini", e);
        }
    }

    private String buildPrompt(String cvText) {
        return """
                Analyze the following CV text.

                Return only valid JSON.
                Do not include markdown.
                Do not include explanations outside JSON.

                JSON structure:
                {
                  "summary": "short summary of the candidate profile",
                  "suggestedRoles": ["role 1", "role 2", "role 3"],
                  "keywords": ["keyword 1", "keyword 2", "keyword 3"]
                }

                CV text:
                %s
                """.formatted(cvText);
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
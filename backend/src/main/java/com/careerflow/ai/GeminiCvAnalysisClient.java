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
        Analyze the following CV text for a job matching application.

        Return only valid JSON.
        Do not include markdown.
        Do not include explanations outside JSON.

        Extract information useful for searching, matching job postings and suggesting possible career directions.

        The application searches job postings in Germany.
        German job postings often use both English and German job titles and keywords.
        Therefore, generate search roles, alternative career roles and keywords in both English and German where useful.
        Put English and German variants into the same arrays.
        Do not create separate arrays for German terms.

        JSON structure:
        {
          "summary": "2-3 sentence summary of the candidate profile",
          "searchRoles": [],
          "alternativeCareerRoles": [],
          "keywords": []
        }

        Rules:
        - searchRoles must contain 8 to 14 realistic job titles that directly match the candidate's current profile, experience and stated skills.
        - searchRoles should include both English and German job-title variants where useful.
        - Example: if "Backend Developer" is relevant, also include a realistic German equivalent such as "Backend Entwickler" or "Softwareentwickler Backend".
        - alternativeCareerRoles must contain 8 to 14 broader career directions where the candidate's skills, experience, education or transferable strengths could be useful.
        - alternativeCareerRoles should also include both English and German variants where useful.
        - Alternative career roles do not need to contain the same technologies, tools or exact terms from the CV, but they must still be logically supported by the CV.
        - Consider both hard skills and transferable skills, such as technical knowledge, analysis, communication, documentation, organization, languages, domain knowledge, project experience or customer-facing experience, if present in the CV.
        - keywords must contain 40 to 60 relevant technical, domain, method, tool, transferable-skill and job-search keywords.
        - keywords should include English and German variants where useful.
        - Prefer concrete skills, technologies, tools, methods, domains and job-search terms.
        - Include German job-search terms that commonly appear in German postings, if relevant, such as "Berufseinsteiger", "Junior", "Trainee", "Einarbeitung", "Weiterbildung" or "Lernbereitschaft".
        - Do not invent experience that is not present in the CV.
        - Keep the suggested roles realistic for the candidate's apparent experience level.
        - Avoid duplicates and near-duplicates unless they are useful English/German variants.
        - Keep each item short and useful for search/filtering.

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
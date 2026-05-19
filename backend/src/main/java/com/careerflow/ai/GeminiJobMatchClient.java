package com.careerflow.ai;

import com.careerflow.jobmatch.dto.JobMatchAnalysisResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiJobMatchClient {

    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiJobMatchClient(@Value("${gemini.model}") String model) {
        this.model = model;
    }

    public JobMatchAnalysisResponse analyzeJobMatch(SearchProfile profile, String jobDescription) {
        try (Client client = new Client()) {
            String prompt = buildPrompt(profile, jobDescription);

            GenerateContentResponse response =
                    client.models.generateContent(model, prompt, null);

            String responseText = response.text();

            if (responseText == null || responseText.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response");
            }

            String cleanedJson = cleanJson(responseText);

            return objectMapper.readValue(cleanedJson, JobMatchAnalysisResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze job match with Gemini", e);
        }
    }

    private String buildPrompt(SearchProfile profile, String jobDescription) {
        String searchRolesList = joinList(profile.getSearchRoles());
        String alternativeRolesList = joinList(profile.getAlternativeCareerRoles());
        String keywordsList = joinList(profile.getKeywords());

        return """
                You are a professional career coach and recruiter.

                Compare the saved candidate profile with the job description.

                Return only valid JSON.
                Do not include markdown.
                Do not include explanations outside JSON.

                Candidate profile:
                Summary: %s
                Search roles: %s
                Alternative career directions: %s
                Keywords and skills: %s

                Job description:
                %s

                JSON structure:
                {
                  "matchScore": 0,
                  "recommendation": "Good match",
                  "summary": "Brief summary of fit",
                  "matchingSkills": [],
                  "missingSkills": [],
                  "concerns": [],
                  "suggestedApplicationFocus": []
                }

                Rules:
                - matchScore must be an integer from 0 to 100.
                - recommendation must be exactly one of:
                  "Strong match", "Good match", "Partial match", "Weak match".
                - Use "Strong match" for matchScore >= 81.
                - Use "Good match" for matchScore 61-80.
                - Use "Partial match" for matchScore 41-60.
                - Use "Weak match" for matchScore <= 40.
                - summary should be 1-2 sentences.
                - matchingSkills should contain 3-8 items.
                - missingSkills should contain 0-8 items.
                - concerns should contain 0-5 items.
                - suggestedApplicationFocus should contain 3-5 items.
                - Do not invent matching skills.
                - Do not invent missing skills.
                - Be realistic and objective.
                - Return only JSON.
                """.formatted(
                profile.getSummary(),
                searchRolesList,
                alternativeRolesList,
                keywordsList,
                jobDescription
        );
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "none";
        }
        return String.join(", ", values);
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
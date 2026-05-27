package com.careerflow.ai;

import com.careerflow.querygeneration.dto.SearchQueryGenerationResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiSearchQueryClient {

    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiSearchQueryClient(@Value("${gemini.model}") String model) {
        this.model = model;
    }

    public SearchQueryGenerationResponse generateQueries(SearchProfile profile) {
        try (Client client = new Client()) {
            String prompt = buildPrompt(profile);

            GenerateContentResponse response =
                    client.models.generateContent(model, prompt, null);

            String responseText = response.text();

            if (responseText == null || responseText.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty response");
            }

            String cleanedJson = cleanJson(responseText);

            return objectMapper.readValue(cleanedJson, SearchQueryGenerationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate search queries with Gemini", e);
        }
    }

    private String joinList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return "none";
        }
        return String.join(", ", values);
    }

    private String buildPrompt(SearchProfile profile) {
        String searchRolesList = joinList(profile.getSearchRoles());
        String alternativeRolesList = joinList(profile.getAlternativeCareerRoles());
        String keywordsList = joinList(profile.getKeywords());
        return """
                You are a professional job search strategist and recruiter.

                Based on the saved search profile below, generate practical search queries for job boards.

                Return only valid JSON.
                Do not include markdown.
                Do not include explanations outside JSON.

                Profile summary:
                %s

                Search roles:
                %s

                Alternative career directions:
                %s

                Keywords and skills:
                %s

                JSON structure:
                {
                  "roleTitleQueries": [],
                  "requirementBasedQueries": [],
                  "alternativeDirectionQueries": []
                }

                Rules:
                - roleTitleQueries must contain 5 to 10 direct job title queries.
                - requirementBasedQueries must contain 8 to 15 skill-based queries that can find jobs with unusual titles but matching requirements.
                - alternativeDirectionQueries must contain 5 to 10 queries based on alternative career directions and transferable skills.
                - Queries should be practical for job boards like LinkedIn, Indeed, StepStone or company career pages.
                - Use concise search phrases, not full sentences.
                - Do not invent skills that are not supported by the profile.
                - Do not generate senior-level queries such as senior, lead, staff, principal, manager, head, architect, or similar.
                - Keep roleTitleQueries and requirementBasedQueries focused on first real junior jobs, not internships, apprenticeships, or student jobs.
                - Return only JSON.
                """.formatted(
                profile.getSummary(),
                searchRolesList,
                alternativeRolesList,
                keywordsList
        );
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}

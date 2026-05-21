package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobPrefilterService {

    private static final List<String> JUNIOR_FRIENDLY_TERMS = List.of(
            "junior",
            "trainee",
            "graduate",
            "entry level",
            "berufseinsteiger",
            "absolvent",
            "quereinsteiger",
            "einstieg",
            "einarbeitung",
            "lernbereitschaft",
            "erste erfahrung",
            "weiterbildung"
    );

    public List<JobSearchResult> prefilter(
            List<JobSearchResult> jobs,
            String rolesParam,
            String keywordsParam,
            int target
    ) {
        List<String> roles = parseCommaSeparatedParam(rolesParam);
        List<String> keywords = parseCommaSeparatedParam(keywordsParam);

        return prefilter(jobs, roles, keywords, target);
    }

    public List<JobSearchResult> prefilter(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            int target
    ) {
        List<String> roleWords = parseRoleWords(roles);
        List<String> keywordTerms = parseKeywordTerms(keywords);
        List<String> juniorTerms = normalizeTerms(JUNIOR_FRIENDLY_TERMS);

        List<JobSearchResult> matchingJobs = new ArrayList<>();

        if (jobs == null || jobs.isEmpty()) {
            return matchingJobs;
        }

        if (target <= 0) {
            return matchingJobs;
        }

        for (JobSearchResult job : jobs) {
            String searchableText = buildSearchableText(job);

            boolean hasRoleMatch = containsAnyTerm(searchableText, roleWords);
            boolean hasKeywordMatch = containsAnyTerm(searchableText, keywordTerms);
            boolean hasJuniorFriendlyMatch = containsAnyTerm(searchableText, juniorTerms);

            boolean passesProfilePrefilter =
                    (hasRoleMatch && hasKeywordMatch)
                            || (hasJuniorFriendlyMatch && hasKeywordMatch);

            if (passesProfilePrefilter) {
                matchingJobs.add(job);
            }

            if (matchingJobs.size() >= target) {
                break;
            }
        }

        return matchingJobs;
    }

    private List<String> parseCommaSeparatedParam(String value) {
        List<String> items = new ArrayList<>();

        if (value == null || value.isBlank()) {
            return items;
        }

        String[] parts = value.split(",");

        for (String part : parts) {
            String cleanedPart = part.trim();

            if (!cleanedPart.isBlank()) {
                items.add(cleanedPart);
            }
        }

        return items;
    }

    private String buildSearchableText(JobSearchResult job) {
        if (job == null) {
            return "";
        }

        String title = safeString(job.getTitle());
        String description = safeString(job.getDescription());

        return normalizeText(title + " " + description);
    }

    private List<String> parseRoleWords(List<String> roles) {
        List<String> words = new ArrayList<>();

        if (roles == null || roles.isEmpty()) {
            return words;
        }

        for (String rolePhrase : roles) {
            String normalizedRolePhrase = normalizeText(rolePhrase);

            String[] roleWords = normalizedRolePhrase.split(" ");

            for (String word : roleWords) {
                addIfUseful(words, word);
            }
        }

        return words;
    }

    private List<String> parseKeywordTerms(List<String> keywords) {
        List<String> terms = new ArrayList<>();

        if (keywords == null || keywords.isEmpty()) {
            return terms;
        }

        for (String keywordPhrase : keywords) {
            String normalizedKeywordPhrase = normalizeText(keywordPhrase);

            addIfUseful(terms, normalizedKeywordPhrase);

            String[] keywordWords = normalizedKeywordPhrase.split(" ");

            for (String word : keywordWords) {
                addIfUseful(terms, word);
            }
        }

        return terms;
    }

    private List<String> normalizeTerms(List<String> rawTerms) {
        List<String> normalizedTerms = new ArrayList<>();

        for (String rawTerm : rawTerms) {
            String normalizedTerm = normalizeText(rawTerm);
            addIfUseful(normalizedTerms, normalizedTerm);
        }

        return normalizedTerms;
    }

    private boolean containsAnyTerm(String text, List<String> terms) {
        if (text == null || text.isBlank()) {
            return false;
        }

        if (terms == null || terms.isEmpty()) {
            return false;
        }

        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private void addIfUseful(List<String> terms, String term) {
        if (term == null) {
            return;
        }

        String cleanedTerm = term.trim();

        if (cleanedTerm.length() < 3) {
            return;
        }

        if (!terms.contains(cleanedTerm)) {
            terms.add(cleanedTerm);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replaceAll("[^a-z0-9+#. ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safeString(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}
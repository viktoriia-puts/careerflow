package com.careerflow.jobsearch.service;

import com.careerflow.jobsearch.dto.JobPrefilterResult;
import com.careerflow.jobsearch.dto.JobSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobPrefilterService {

    private static final List<String> JUNIOR_FRIENDLY_TERMS = List.of(
            "junior",
            "jr",
            "trainee",
            "graduate trainee",
            "graduate",
            "new graduate",
            "recent graduate",
            "entry level",
            "entry-level",
            "entrylevel",
            "beginner",
            "no experience required",
            "no prior experience",
            "career starter",
            "career changer",
            "berufseinsteiger",
            "berufseinstieg",
            "absolvent",
            "absolventin",
            "hochschulabsolvent",
            "hochschulabsolventin",
            "quereinsteiger",
            "quereinsteigerin",
            "quereinstieg",
            "einstieg",
            "einstiegsposition",
            "einstiegsstelle",
            "nachwuchs",
            "nachwuchskraft",
            "einarbeitung",
            "lernbereitschaft",
            "erste erfahrung",
            "erste praktische erfahrung",
            "erste berufserfahrung",
            "erste kenntnisse",
            "grundkenntnisse",
            "basiskenntnisse",
            "weiterbildung"
    );

    private static final List<String> SENIOR_LEVEL_TITLE_TERMS = List.of(
            "senior",
            "sr",
            "principal",
            "staff engineer",
            "staff developer",
            "tech lead",
            "team lead",
            "lead engineer",
            "lead developer",
            "lead frontend",
            "lead backend",
            "lead software",
            "lead fullstack",
            "lead full stack",
            "architect",
            "software architect",
            "solution architect",
            "solutions architect",
            "technical architect",
            "engineering manager",
            "manager engineering",
            "head of",
            "head",
            "leiter",
            "leiterin",
            "leitung",
            "fuehrungskraft",
            "führungskraft"
    );

    private static final List<String> SENIOR_LEVEL_DESCRIPTION_TERMS = List.of(
            "senior",
            "sr",
            "mehrjährige erfahrung",
            "mehrjaehrige erfahrung",
            "mehrjährige berufserfahrung",
            "mehrjaehrige berufserfahrung",
            "langjährige erfahrung",
            "langjaehrige erfahrung",
            "langjährige berufserfahrung",
            "langjaehrige berufserfahrung",
            "einschlägige berufserfahrung",
            "einschlaegige berufserfahrung",
            "fundierte erfahrung",
            "umfangreiche erfahrung",
            "ausgeprägte erfahrung",
            "ausgepraegte erfahrung",
            "mehrere jahre erfahrung",
            "several years of experience",
            "many years of experience",
            "extensive experience",
            "proven experience",
            "strong experience",
            "deep experience",
            "leadership experience",
            "führungserfahrung",
            "fuehrungserfahrung",
            "fachliche führung",
            "fachliche fuehrung",
            "personalverantwortung"
    );

    private static final List<String> MIDDLE_LEVEL_TITLE_TERMS = List.of(
            "middle",
            "mid",
            "mid level",
            "mid-level",
            "medior",
            "professional",
            "experienced"
    );

    private static final List<String> MIDDLE_LEVEL_DESCRIPTION_TERMS = List.of(
            "professional experience",
            "professional software development experience",
            "berufserfahrung",
            "praktische berufserfahrung",
            "erfahrung in der softwareentwicklung",
            "erfahrung mit java",
            "sicherer umgang",
            "gute kenntnisse",
            "fundierte kenntnisse",
            "vertiefte kenntnisse"
    );

    public List<JobSearchResult> prefilter(
            List<JobSearchResult> jobs,
            String rolesParam,
            String keywordsParam
    ) {
        List<String> roles = parseCommaSeparatedParam(rolesParam);
        List<String> keywords = parseCommaSeparatedParam(keywordsParam);

        return prefilter(jobs, roles, keywords);
    }

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
            List<String> keywords
    ) {
        return prefilterWithStatistics(jobs, roles, keywords, JobSeniorityPreference.JUNIOR).getJobs();
    }

    public List<JobSearchResult> prefilter(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            int target
    ) {
        if (target <= 0) {
            return new ArrayList<>();
        }

        return prefilterWithStatistics(
                jobs,
                roles,
                keywords,
                target,
                JobSeniorityPreference.JUNIOR
        ).getJobs();
    }

    public JobPrefilterResult prefilterWithStatistics(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords
    ) {
        return prefilterWithStatistics(
                jobs,
                roles,
                keywords,
                JobSeniorityPreference.JUNIOR
        );
    }

    public JobPrefilterResult prefilterWithStatistics(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            JobSeniorityPreference seniorityPreference
    ) {
        return prefilterInternal(jobs, roles, keywords, null, seniorityPreference);
    }

    public JobPrefilterResult prefilterWithStatistics(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            int target
    ) {
        return prefilterWithStatistics(
                jobs,
                roles,
                keywords,
                target,
                JobSeniorityPreference.JUNIOR
        );
    }

    public JobPrefilterResult prefilterWithStatistics(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            int target,
            JobSeniorityPreference seniorityPreference
    ) {
        if (target <= 0) {
            return new JobPrefilterResult(new ArrayList<>(), countJobs(jobs), 0, 0);
        }

        return prefilterInternal(jobs, roles, keywords, target, seniorityPreference);
    }

    private JobPrefilterResult prefilterInternal(
            List<JobSearchResult> jobs,
            List<String> roles,
            List<String> keywords,
            Integer target,
            JobSeniorityPreference seniorityPreference
    ) {
        List<String> roleWords = parseRoleWords(roles);
        List<String> keywordTerms = parseKeywordTerms(keywords);
        List<String> juniorTerms = normalizeTerms(JUNIOR_FRIENDLY_TERMS);
        List<String> seniorTitleTerms = normalizeTerms(SENIOR_LEVEL_TITLE_TERMS);
        List<String> seniorDescriptionTerms = normalizeTerms(SENIOR_LEVEL_DESCRIPTION_TERMS);
        List<String> middleTitleTerms = normalizeTerms(MIDDLE_LEVEL_TITLE_TERMS);
        List<String> middleDescriptionTerms = normalizeTerms(MIDDLE_LEVEL_DESCRIPTION_TERMS);
        JobSeniorityPreference resolvedSeniorityPreference =
                seniorityPreference == null ? JobSeniorityPreference.JUNIOR : seniorityPreference;

        List<JobSearchResult> matchingJobs = new ArrayList<>();

        if (jobs == null || jobs.isEmpty()) {
            return new JobPrefilterResult(matchingJobs, 0, 0, 0);
        }

        int afterSeniorFilterCount = 0;
        int afterProfileFilterCount = 0;

        for (JobSearchResult job : jobs) {
            String titleText = buildTitleText(job);
            String searchableText = buildSearchableText(job);

            if (!matchesSeniorityPreference(
                    titleText,
                    searchableText,
                    juniorTerms,
                    seniorTitleTerms,
                    seniorDescriptionTerms,
                    middleTitleTerms,
                    middleDescriptionTerms,
                    resolvedSeniorityPreference
            )) {
                continue;
            }

            afterSeniorFilterCount++;

            boolean hasRoleMatch = containsAnyTerm(searchableText, roleWords);
            boolean hasKeywordMatch = containsAnyTerm(searchableText, keywordTerms);
            boolean hasJuniorFriendlyMatch = containsAnyTerm(searchableText, juniorTerms);

            boolean passesProfilePrefilter =
                    (hasRoleMatch && hasKeywordMatch)
                            || (hasJuniorFriendlyMatch && hasKeywordMatch);

            if (passesProfilePrefilter) {
                afterProfileFilterCount++;
                if (target == null || matchingJobs.size() < target) {
                    matchingJobs.add(job);
                }
            }
        }

        return new JobPrefilterResult(
                matchingJobs,
                jobs.size(),
                afterSeniorFilterCount,
                afterProfileFilterCount
        );
    }

    private int countJobs(List<JobSearchResult> jobs) {
        return jobs == null ? 0 : jobs.size();
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

    private String buildTitleText(JobSearchResult job) {
        if (job == null) {
            return "";
        }

        return normalizeText(safeString(job.getTitle()));
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

    private boolean isSeniorLevelJob(
            String titleText,
            String searchableText,
            List<String> seniorTitleTerms,
            List<String> seniorDescriptionTerms
    ) {
        return containsAnyWholeTerm(titleText, seniorTitleTerms)
                || containsAnyWholeTerm(searchableText, seniorDescriptionTerms);
    }

    private boolean isMiddleLevelJob(
            String titleText,
            String searchableText,
            List<String> middleTitleTerms,
            List<String> middleDescriptionTerms
    ) {
        return containsAnyWholeTerm(titleText, middleTitleTerms)
                || containsAnyWholeTerm(searchableText, middleDescriptionTerms);
    }

    private boolean matchesSeniorityPreference(
            String titleText,
            String searchableText,
            List<String> juniorTerms,
            List<String> seniorTitleTerms,
            List<String> seniorDescriptionTerms,
            List<String> middleTitleTerms,
            List<String> middleDescriptionTerms,
            JobSeniorityPreference seniorityPreference
    ) {
        boolean isJuniorFriendly = containsAnyWholeTerm(searchableText, juniorTerms);
        boolean isSeniorLevel = isSeniorLevelJob(
                titleText,
                searchableText,
                seniorTitleTerms,
                seniorDescriptionTerms
        );
        boolean isMiddleLevel = isMiddleLevelJob(
                titleText,
                searchableText,
                middleTitleTerms,
                middleDescriptionTerms
        );

        if (seniorityPreference == JobSeniorityPreference.SENIOR_MIDDLE) {
            return !isJuniorFriendly && (isSeniorLevel || isMiddleLevel);
        }

        return !isSeniorLevel;
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

    private boolean containsAnyWholeTerm(String text, List<String> terms) {
        if (text == null || text.isBlank()) {
            return false;
        }

        if (terms == null || terms.isEmpty()) {
            return false;
        }

        String paddedText = " " + text + " ";

        for (String term : terms) {
            String paddedTerm = " " + term + " ";

            if (paddedText.contains(paddedTerm)) {
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

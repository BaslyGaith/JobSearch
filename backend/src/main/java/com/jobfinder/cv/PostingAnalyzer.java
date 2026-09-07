package com.jobfinder.cv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a posting and works out what it tests, before any prose is written.
 * Deliberately deterministic: the model may phrase the CV, but what counts as a
 * requirement - and whether the fact bank covers it - is decided here.
 */
@Slf4j
@Component
public class PostingAnalyzer {

    /** Keyword -> fact-bank tag. Keys are matched case-insensitively. */
    private static final Map<String, String> KEYWORD_TAGS = new LinkedHashMap<>();

    /** Job family -> accent colour, per the CV agent brief. */
    private static final Map<String, String> FAMILY_COLORS = Map.of(
            "java", "#1d4ed8",
            "data", "#0f766e",
            "power-platform", "#6d28d9",
            "security", "#3b5b7a",
            "general", "#1d4ed8"
    );

    static {
        KEYWORD_TAGS.put("power bi", "bi");
        KEYWORD_TAGS.put("dax", "bi");
        KEYWORD_TAGS.put("power query", "bi");
        KEYWORD_TAGS.put("business intelligence", "bi");
        KEYWORD_TAGS.put("dashboard", "bi");
        KEYWORD_TAGS.put("reporting", "bi");
        KEYWORD_TAGS.put("fabric", "fabric");
        KEYWORD_TAGS.put("lakehouse", "fabric");
        KEYWORD_TAGS.put("medallion", "fabric");
        KEYWORD_TAGS.put("data model", "data-modelling");
        KEYWORD_TAGS.put("star schema", "data-modelling");
        KEYWORD_TAGS.put("semantic model", "data-modelling");
        KEYWORD_TAGS.put("etl", "etl");
        KEYWORD_TAGS.put("pipeline", "etl");
        KEYWORD_TAGS.put("integration", "integration");
        KEYWORD_TAGS.put("rest api", "integration");
        KEYWORD_TAGS.put("sql", "sql");
        KEYWORD_TAGS.put("mysql", "sql");
        KEYWORD_TAGS.put("java", "java");
        KEYWORD_TAGS.put("maven", "build");
        KEYWORD_TAGS.put("osgi", "build");
        KEYWORD_TAGS.put("backend", "backend");
        KEYWORD_TAGS.put("spring boot", "web");
        KEYWORD_TAGS.put("angular", "web");
        KEYWORD_TAGS.put("python", "python");
        KEYWORD_TAGS.put("scikit", "data-science");
        KEYWORD_TAGS.put("machine learning", "data-science");
        KEYWORD_TAGS.put("row-level security", "security");
        KEYWORD_TAGS.put("rls", "security");
        KEYWORD_TAGS.put("iam", "iam");
        KEYWORD_TAGS.put("identity", "iam");
        KEYWORD_TAGS.put("active directory", "iam");
        KEYWORD_TAGS.put("entra", "iam");
        KEYWORD_TAGS.put("azure ad", "iam");
        KEYWORD_TAGS.put("sso", "iam");
        KEYWORD_TAGS.put("oauth", "iam");
        KEYWORD_TAGS.put("authorization", "authorization");
        KEYWORD_TAGS.put("habilitation", "authorization");
        KEYWORD_TAGS.put("permission", "authorization");
        KEYWORD_TAGS.put("agile", "agile");
        KEYWORD_TAGS.put("scrum", "agile");
        KEYWORD_TAGS.put("gitlab", "cicd");
        KEYWORD_TAGS.put("ci/cd", "cicd");
        KEYWORD_TAGS.put("jenkins", "cicd");
        KEYWORD_TAGS.put("test", "testing");
        KEYWORD_TAGS.put("selenium", "testing");
        KEYWORD_TAGS.put("playwright", "testing");
        KEYWORD_TAGS.put("sonarqube", "quality");
        KEYWORD_TAGS.put("business analys", "business-analysis");
        KEYWORD_TAGS.put("requirement", "business-analysis");
        KEYWORD_TAGS.put("specification", "business-analysis");
        KEYWORD_TAGS.put("stakeholder", "business-analysis");
        KEYWORD_TAGS.put("support", "support");
        KEYWORD_TAGS.put("jira", "support");
        KEYWORD_TAGS.put("banking", "banking");
        KEYWORD_TAGS.put("insurance", "banking");
        KEYWORD_TAGS.put("finance", "banking");
        KEYWORD_TAGS.put("windows server", "infrastructure");
        KEYWORD_TAGS.put("vmware", "infrastructure");
    }

    private static final Pattern ROLE_LINE = Pattern.compile(
            "(?im)^\\s*(?:job title|poste|intitul[ée]|role|position)\\s*[:\\-]\\s*(.+)$");
    private static final Pattern COMPANY_LINE = Pattern.compile(
            "(?im)^\\s*(?:company|entreprise|soci[ée]t[ée]|employer)\\s*[:\\-]\\s*(.+)$");
    private static final Pattern YEARS = Pattern.compile(
            "(?i)(\\d{1,2})\\s*\\+?\\s*(?:years?|ans|années?)");

    public PostingAnalysis analyse(String posting, FactBank factBank) {
        String text = posting == null ? "" : posting;
        String lower = text.toLowerCase(Locale.ROOT);

        List<String> tags = detectTags(lower);
        String family = detectFamily(tags, lower);

        PostingAnalysis analysis = PostingAnalysis.builder()
                .targetRole(firstGroup(ROLE_LINE, text, guessRole(text)))
                .targetCompany(firstGroup(COMPANY_LINE, text, null))
                .jobFamily(family)
                .accentColor(FAMILY_COLORS.getOrDefault(family, FAMILY_COLORS.get("general")))
                .tags(tags)
                .requirements(buildRequirements(text, lower, tags, factBank))
                .build();

        log.debug("Analysed posting: family={} tags={} requirements={}",
                family, tags, analysis.getRequirements().size());
        return analysis;
    }

    private List<String> detectTags(String lower) {
        Map<String, Integer> hits = new LinkedHashMap<>();
        KEYWORD_TAGS.forEach((keyword, tag) -> {
            int count = countOccurrences(lower, keyword);
            if (count > 0) {
                hits.merge(tag, count, Integer::sum);
            }
        });
        return hits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private String detectFamily(List<String> tags, String lower) {
        Set<String> tagSet = new LinkedHashSet<>(tags);
        if (tagSet.contains("iam") || tagSet.contains("authorization")
                || (tagSet.contains("security") && !tagSet.contains("bi"))) {
            return "security";
        }
        if (lower.contains("power bi") || lower.contains("power platform")) {
            return "power-platform";
        }
        if (tagSet.contains("java") || tagSet.contains("backend") || tagSet.contains("build")) {
            return "java";
        }
        if (tagSet.contains("bi") || tagSet.contains("fabric") || tagSet.contains("data-modelling")
                || tagSet.contains("etl") || tagSet.contains("data-science")) {
            return "data";
        }
        return "general";
    }

    /**
     * Every requirement line the posting states, marked against the fact bank.
     * A requirement whose tag we cannot serve is ABSENT and surfaces as a gap.
     */
    private List<PostingAnalysis.Requirement> buildRequirements(
            String text, String lower, List<String> tags, FactBank factBank) {

        Set<String> covered = new LinkedHashSet<>(tags);
        List<PostingAnalysis.Requirement> requirements = new ArrayList<>();

        for (String blocked : factBank.getBlocklist()) {
            if (countOccurrences(lower, blocked.toLowerCase(Locale.ROOT)) > 0) {
                requirements.add(PostingAnalysis.Requirement.builder()
                        .text(blocked)
                        .essential(isEssentialContext(lower, blocked.toLowerCase(Locale.ROOT)))
                        .coverage(PostingAnalysis.Coverage.ABSENT)
                        .interviewAdvice(adviceFor(blocked))
                        .build());
            }
        }

        for (String tag : covered) {
            requirements.add(PostingAnalysis.Requirement.builder()
                    .text(tag)
                    .essential(true)
                    .coverage(PostingAnalysis.Coverage.STRONG)
                    .build());
        }

        Matcher years = YEARS.matcher(text);
        if (years.find()) {
            int required = Integer.parseInt(years.group(1));
            int actual = factBank.getIdentity().getYearsExperience() == null
                    ? 0 : factBank.getIdentity().getYearsExperience();
            requirements.add(PostingAnalysis.Requirement.builder()
                    .text(required + " years of experience required")
                    .essential(true)
                    .coverage(actual >= required
                            ? PostingAnalysis.Coverage.STRONG
                            : PostingAnalysis.Coverage.PARTIAL)
                    .interviewAdvice(actual >= required ? null
                            : "The posting asks for " + required + " years; you have " + actual
                              + ". Lead with the breadth of the Vermeg work rather than the count.")
                    .build());
        }

        requirements.sort(Comparator.comparing(PostingAnalysis.Requirement::getCoverage));
        return requirements;
    }

    private boolean isEssentialContext(String lower, String keyword) {
        int index = lower.indexOf(keyword);
        if (index < 0) {
            return false;
        }
        int from = Math.max(0, index - 200);
        String context = lower.substring(from, index);
        return !(context.contains("nice to have") || context.contains("plus")
                 || context.contains("appréci") || context.contains("bonus"));
    }

    private String adviceFor(String technology) {
        return "The posting mentions " + technology + ", which is not in your verified experience. "
               + "Say so plainly and pivot to the closest thing you have actually done, "
               + "rather than implying exposure you cannot defend.";
    }

    private String guessRole(String text) {
        return Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank() && line.length() < 90)
                .findFirst()
                .orElse("Target role");
    }

    private String firstGroup(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : fallback;
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0;
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            count++;
            index = haystack.indexOf(needle, index + needle.length());
        }
        return count;
    }
}

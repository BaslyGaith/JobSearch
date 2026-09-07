package com.jobfinder.cv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Last line of defence before a CV is stored. A generated draft is rejected -
 * never silently corrected - when it breaks one of the hard rules, because a CV
 * that collapses in a technical interview is worse than one with a visible gap.
 */
@Slf4j
@Component
public class CvGuardrails {

    /** Any digit run that is not part of an allowed figure or a real date. */
    private static final Pattern NUMBERS = Pattern.compile("\\d+(?:[.,]\\d+)?\\s*%?\\+?");

    /** Figures that are always legitimate: years in dates, version numbers, the fact bank's own. */
    private static final Pattern SAFE_NUMBER_CONTEXT = Pattern.compile(
            "(?i)(19|20)\\d{2}|java\\s*8|llama\\s*3|dp-\\d{3}|c\\+\\+");

    public static class ViolationException extends RuntimeException {
        private final transient List<String> violations;

        public ViolationException(List<String> violations) {
            super("Generated CV broke the fact-bank rules: " + String.join("; ", violations));
            this.violations = violations;
        }

        public List<String> getViolations() {
            return violations;
        }
    }

    /**
     * @throws ViolationException when the draft claims something unverified.
     */
    public void verify(CvDraft draft, FactBank factBank) {
        List<String> violations = new ArrayList<>();
        String rendered = flatten(draft);
        String lower = rendered.toLowerCase(Locale.ROOT);

        for (String blocked : factBank.getBlocklist()) {
            if (containsWord(lower, blocked.toLowerCase(Locale.ROOT))) {
                violations.add("claims blocklisted technology: " + blocked);
            }
        }

        for (String forbidden : factBank.getConstraints().getForbiddenTitleWords()) {
            String needle = forbidden.toLowerCase(Locale.ROOT);
            boolean inTitle = draft.getExperiences().stream()
                    .anyMatch(e -> e.getTitle() != null
                                   && e.getTitle().toLowerCase(Locale.ROOT).contains(needle));
            if (inTitle || (draft.getTagline() != null
                            && draft.getTagline().toLowerCase(Locale.ROOT).contains(needle))) {
                violations.add("uses forbidden title word: " + forbidden);
            }
        }

        violations.addAll(checkSpringBootAngularPlacement(draft, factBank));
        violations.addAll(checkFigures(rendered, factBank));

        if (!violations.isEmpty()) {
            log.warn("Rejected generated CV: {}", violations);
            throw new ViolationException(violations);
        }
    }

    /** Spring Boot and Angular belong to the SIMEG project only. */
    private List<String> checkSpringBootAngularPlacement(CvDraft draft, FactBank factBank) {
        String scope = factBank.getConstraints().getSpringBootAngularOnlyIn();
        if (scope == null) {
            return List.of();
        }
        List<String> violations = new ArrayList<>();
        for (CvDraft.ExperienceBlock block : draft.getExperiences()) {
            String text = String.join(" ", block.getBullets() == null ? List.of() : block.getBullets())
                    .toLowerCase(Locale.ROOT);
            if (text.contains("spring boot") || text.contains("angular")) {
                violations.add("Spring Boot / Angular claimed under employment ("
                               + block.getCompany() + ") - allowed only under " + scope);
            }
        }
        return violations;
    }

    /** Only the fact bank's own figures may appear. */
    private List<String> checkFigures(String rendered, FactBank factBank) {
        List<String> allowed = factBank.getConstraints().getAllowedFigures();
        List<String> violations = new ArrayList<>();

        Matcher matcher = NUMBERS.matcher(rendered);
        while (matcher.find()) {
            String figure = matcher.group().trim();
            int start = Math.max(0, matcher.start() - 20);
            int end = Math.min(rendered.length(), matcher.end() + 20);
            String context = rendered.substring(start, end);

            if (SAFE_NUMBER_CONTEXT.matcher(context).find()) {
                continue;
            }
            boolean known = allowed.stream()
                    .anyMatch(a -> a.contains(figure) || figure.contains(a));
            if (!known) {
                violations.add("unverified figure \"" + figure + "\" near: " + context.trim());
            }
        }
        return violations;
    }

    private boolean containsWord(String haystack, String needle) {
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            boolean leftClear = index == 0 || !Character.isLetterOrDigit(haystack.charAt(index - 1));
            int after = index + needle.length();
            boolean rightClear = after >= haystack.length()
                                 || !Character.isLetterOrDigit(haystack.charAt(after));
            if (leftClear && rightClear) {
                return true;
            }
            index = haystack.indexOf(needle, index + 1);
        }
        return false;
    }

    private String flatten(CvDraft draft) {
        StringBuilder sb = new StringBuilder();
        append(sb, draft.getTagline());
        append(sb, draft.getProfile());
        draft.getExperiences().forEach(e -> {
            append(sb, e.getTitle());
            append(sb, e.getCompany());
            if (e.getBullets() != null) {
                e.getBullets().forEach(b -> append(sb, b));
            }
        });
        draft.getWhatIBring().forEach(v -> {
            append(sb, v.getHeading());
            append(sb, v.getBody());
        });
        draft.getCoreSkills().forEach(s -> append(sb, s.getValues()));
        if (draft.getSelectedProject() != null && draft.getSelectedProject().getBullets() != null) {
            draft.getSelectedProject().getBullets().forEach(b -> append(sb, b));
        }
        return sb.toString();
    }

    private void append(StringBuilder sb, String text) {
        if (text != null) {
            sb.append(text).append('\n');
        }
    }
}

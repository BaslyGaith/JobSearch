package com.jobfinder.source;

import com.jobfinder.search.JobSearchPreference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * How well a posting fits the user's stated preferences, as a 0-100 score.
 * Deliberately explainable: every point comes from a preference the user typed,
 * matched against text the source published.
 */
@Component
public class MatchScorer {

    private static final int TITLE_WEIGHT = 40;
    private static final int KEYWORD_WEIGHT = 30;
    private static final int LOCATION_WEIGHT = 20;
    private static final int EMPLOYMENT_WEIGHT = 10;

    public int score(RawJobPosting posting, JobSearchPreference preference) {
        if (preference == null) {
            return 50;
        }
        String title = lower(posting.getTitle());
        String location = lower(posting.getLocation());
        String haystack = title + " " + lower(posting.getDescription());

        int score = 0;
        score += TITLE_WEIGHT * proportionPresent(preference.getJobTitles(), title, true);
        score += KEYWORD_WEIGHT * proportionPresent(preference.getKeywords(), haystack, false);
        score += LOCATION_WEIGHT * proportionPresent(preference.getLocations(), location, true);

        List<String> types = preference.getEmploymentTypes();
        if (types == null || types.isEmpty() || posting.getEmploymentType() == null) {
            score += EMPLOYMENT_WEIGHT / 2;
        } else if (types.stream().anyMatch(t -> t.equalsIgnoreCase(posting.getEmploymentType()))) {
            score += EMPLOYMENT_WEIGHT;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * @param anyCounts when true a single hit scores the whole weight - one
     *                  matching job title is a match, not a third of one.
     */
    private double proportionPresent(List<String> wanted, String haystack, boolean anyCounts) {
        if (wanted == null || wanted.isEmpty()) {
            return 0.5;
        }
        long hits = wanted.stream()
                .filter(w -> !w.isBlank())
                .filter(w -> haystack.contains(w.toLowerCase(Locale.ROOT)))
                .count();
        if (hits == 0) {
            return 0;
        }
        return anyCounts ? 1.0 : Math.min(1.0, (double) hits / Math.min(wanted.size(), 5));
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}

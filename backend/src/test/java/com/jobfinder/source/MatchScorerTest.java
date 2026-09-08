package com.jobfinder.source;

import com.jobfinder.search.JobSearchPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchScorerTest {

    private MatchScorer scorer;
    private JobSearchPreference preference;

    @BeforeEach
    void setUp() {
        scorer = new MatchScorer();
        preference = JobSearchPreference.builder()
                .jobTitles(List.of("Data Analyst", "Power BI Developer"))
                .locations(List.of("Berlin", "Remote"))
                .keywords(List.of("Power BI", "SQL", "DAX"))
                .employmentTypes(List.of("FULL_TIME"))
                .build();
    }

    @Test
    void scoresAPostingThatMatchesEverythingHighly() {
        int score = scorer.score(RawJobPosting.builder()
                .title("Data Analyst")
                .location("Berlin, Germany")
                .employmentType("FULL_TIME")
                .description("You will build Power BI reports, write SQL and DAX measures.")
                .build(), preference);

        assertThat(score).isGreaterThanOrEqualTo(85);
    }

    @Test
    void scoresAnUnrelatedPostingLow() {
        int score = scorer.score(RawJobPosting.builder()
                .title("Warehouse Operative")
                .location("Lyon, France")
                .employmentType("PART_TIME")
                .description("Picking and packing orders in a distribution centre.")
                .build(), preference);

        assertThat(score).isLessThan(20);
    }

    @Test
    void oneMatchingTitleScoresTheWholeTitleWeight() {
        // Titles only, so no keyword or location weight muddies the comparison.
        JobSearchPreference titlesOnly = JobSearchPreference.builder()
                .jobTitles(List.of("Data Analyst", "Power BI Developer"))
                .build();

        int first = scorer.score(posting("Data Analyst"), titlesOnly);
        int second = scorer.score(posting("Power BI Developer"), titlesOnly);

        assertThat(first).isEqualTo(second);
        assertThat(scorer.score(posting("Warehouse Operative"), titlesOnly)).isLessThan(first);
    }

    @Test
    void treatsMissingPreferencesAsNeutral() {
        assertThat(scorer.score(posting("Anything at all"), null)).isEqualTo(50);
    }

    @Test
    void neverLeavesTheZeroToHundredRange() {
        int score = scorer.score(RawJobPosting.builder()
                .title("Data Analyst Power BI Developer")
                .location("Berlin Remote")
                .employmentType("FULL_TIME")
                .description("Power BI SQL DAX Power BI SQL DAX")
                .build(), preference);

        assertThat(score).isBetween(0, 100);
    }

    private RawJobPosting posting(String title) {
        return RawJobPosting.builder().title(title).location("Berlin").description("").build();
    }
}

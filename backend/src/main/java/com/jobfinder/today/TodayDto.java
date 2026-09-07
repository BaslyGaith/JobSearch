package com.jobfinder.today;

import com.jobfinder.job.JobOpportunityDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Everything the Today page needs, in one call. */
@Data
@Builder
public class TodayDto {

    /** Opportunities published today. */
    private long publishedToday;

    /** Unreviewed opportunities, whatever their score. */
    private long awaitingReview;

    /** Unreviewed opportunities scoring 85 or above. */
    private long strongMatches;

    /** Opportunities marked interested but not yet applied to. */
    private long interested;

    private long applied;

    /** CVs already prepared, counting each language version once. */
    private long cvsPrepared;

    private boolean linkedInConnected;

    /** The handful worth looking at first. */
    private List<JobOpportunityDto> worthReviewing;
}

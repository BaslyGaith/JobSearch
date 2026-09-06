package com.jobfinder.job;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobStatsDto {
    private long newOpportunities;
    private long highMatches;
    private long interested;
    private long applied;
}

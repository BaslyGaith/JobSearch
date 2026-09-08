package com.jobfinder.source;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SyncResultDto {

    private int added;
    /** Postings already stored, from this or another source. */
    private int duplicates;
    private List<String> sourcesUsed;
    /** Sources that failed, with the reason, so a silent partial run is visible. */
    private List<String> failures;
}

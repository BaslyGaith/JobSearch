package com.jobfinder.source;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceStatusDto {

    private String name;
    /** False when the source lacks the credentials or configuration it needs. */
    private boolean enabled;
    private long storedOpportunities;
}

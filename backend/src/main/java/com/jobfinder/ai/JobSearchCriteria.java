package com.jobfinder.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobSearchCriteria {
    private List<String> jobTitles;
    private List<String> locations;
    private List<String> employmentTypes;
    private List<String> experienceLevels;
    private String remotePreference;
    private List<String> keywords;
    private int maxResults;
}

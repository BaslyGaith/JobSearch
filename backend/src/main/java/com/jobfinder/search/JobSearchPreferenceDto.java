package com.jobfinder.search;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JobSearchPreferenceDto {
    private UUID id;
    private List<String> jobTitles;
    private List<String> locations;
    private List<String> employmentTypes;
    private List<String> experienceLevels;
    private String remotePreference;
    private List<String> keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

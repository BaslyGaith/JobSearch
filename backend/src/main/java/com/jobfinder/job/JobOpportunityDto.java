package com.jobfinder.job;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JobOpportunityDto {
    private UUID id;
    private String title;
    private String companyName;
    private String location;
    private String employmentType;
    private String description;
    private String jobUrl;
    private String source;
    private LocalDate publicationDate;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterProfileUrl;
    private Integer matchScore;
    private JobStatus status;
    private boolean saved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

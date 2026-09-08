package com.jobfinder.application;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ApplicationDto {

    private UUID id;

    private UUID jobOpportunityId;
    private String jobTitle;
    private String companyName;
    private String jobUrl;
    private Integer matchScore;

    private UUID cvDocumentId;
    private String cvTitle;
    private String cvLanguage;

    private String recipientName;
    private String recipientEmail;
    private String subject;
    private String body;

    private ApplicationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    private List<TimelineEntry> events;

    @Data
    @Builder
    public static class TimelineEntry {
        private String label;
        private String detail;
        private LocalDateTime occurredAt;
    }
}

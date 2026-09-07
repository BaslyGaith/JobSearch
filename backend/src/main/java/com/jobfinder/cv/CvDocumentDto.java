package com.jobfinder.cv;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CvDocumentDto {

    private UUID id;
    private UUID generationId;
    private String title;
    private String language;
    private String targetRole;
    private String targetCompany;
    private String jobFamily;
    private String accentColor;
    private String generatedBy;
    private LocalDateTime createdAt;
    private CvDraft draft;
    private List<PostingAnalysis.Requirement> gaps;
}

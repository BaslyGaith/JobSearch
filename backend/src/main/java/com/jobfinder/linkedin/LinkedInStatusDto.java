package com.jobfinder.linkedin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LinkedInStatusDto {
    private boolean connected;
    private String linkedinEmail;
    private LocalDateTime connectedAt;
    private String mode; // "MOCK" or "LIVE"
}

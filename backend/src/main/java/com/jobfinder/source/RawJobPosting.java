package com.jobfinder.source;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * One posting as a connector found it, before normalisation or scoring.
 * Connectors fill what their source actually provides and leave the rest null -
 * nothing here is inferred or guessed.
 */
@Data
@Builder
public class RawJobPosting {

    private String externalId;
    private String source;
    private String title;
    private String companyName;
    private String location;
    private String employmentType;
    private String description;
    private String jobUrl;
    private LocalDate publicationDate;

    /** Only ever set when the source publishes it as a contact for applications. */
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterProfileUrl;
}

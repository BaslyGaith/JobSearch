package com.jobfinder.cv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class GenerateCvRequest {

    @NotBlank(message = "A job posting is required")
    @Size(max = 20000, message = "Posting is too long")
    private String posting;

    /** Optional: links the generated CVs to an opportunity from the Jobs page. */
    private UUID jobOpportunityId;
}

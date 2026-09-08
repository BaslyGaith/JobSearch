package com.jobfinder.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {

    @NotNull(message = "A status is required")
    private ApplicationStatus status;

    /** Optional note recorded on the timeline. */
    private String note;
}

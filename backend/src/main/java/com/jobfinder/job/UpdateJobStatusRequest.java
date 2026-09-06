package com.jobfinder.job;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJobStatusRequest {
    @NotNull(message = "Status is required")
    private JobStatus status;
}

package com.jobfinder.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateApplicationRequest {

    private String recipientName;

    @Email(message = "That does not look like an email address")
    private String recipientEmail;

    @NotBlank(message = "A subject is required")
    @Size(max = 500, message = "Subject is too long")
    private String subject;

    @NotBlank(message = "The email body cannot be empty")
    private String body;

    /** Swap the attached CV for another of the user's own. */
    private UUID cvDocumentId;
}

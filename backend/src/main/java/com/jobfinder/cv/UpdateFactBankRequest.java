package com.jobfinder.cv;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFactBankRequest {

    @NotBlank(message = "The fact bank cannot be empty")
    private String factBank;
}

package com.jobfinder.cv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameCvRequest {

    @NotBlank(message = "A name is required")
    @Size(max = 300, message = "Name is too long")
    private String title;
}

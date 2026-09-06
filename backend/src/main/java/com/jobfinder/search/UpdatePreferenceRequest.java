package com.jobfinder.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdatePreferenceRequest {
    private List<String> jobTitles = new ArrayList<>();
    private List<String> locations = new ArrayList<>();
    private List<String> employmentTypes = new ArrayList<>();
    private List<String> experienceLevels = new ArrayList<>();
    private String remotePreference = "ANY";
    private List<String> keywords = new ArrayList<>();
}

package com.jobfinder.cv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The verified facts a CV may be built from. Nothing outside this document is
 * allowed to reach a generated CV - see {@link CvGuardrails}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactBank {

    private Identity identity = new Identity();
    private Constraints constraints = new Constraints();
    private List<Experience> experiences = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<String> academicProjects = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private Map<String, List<String>> skills = new LinkedHashMap<>();
    private Map<String, List<String>> skillTags = new LinkedHashMap<>();
    private List<String> blocklist = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identity {
        private String fullName;
        private String email;
        private String alternateEmail;
        private String phone;
        private String linkedin;
        private String location;
        private Integer yearsExperience;
        private List<String> languages = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Constraints {
        private List<String> forbiddenTitleWords = new ArrayList<>();
        private List<String> allowedFigures = new ArrayList<>();
        private String springBootAngularOnlyIn;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Experience {
        private String title;
        private String company;
        private String companyNote;
        private String location;
        private String startDate;
        private String endDate;
        private List<Bullet> bullets = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bullet {
        private String text;
        private List<String> tags = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String name;
        private String organisation;
        private String dates;
        private List<String> bullets = new ArrayList<>();
        private List<String> tags = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Education {
        private String qualification;
        private String institution;
        private String dates;
    }
}

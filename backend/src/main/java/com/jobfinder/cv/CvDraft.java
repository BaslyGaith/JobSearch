package com.jobfinder.cv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * One tailored CV in one language, as structured sections rather than a blob of
 * text, so the same draft can be rendered to HTML, .docx or PDF.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvDraft {

    private String language;
    private String tagline;
    private String profile;
    @Builder.Default
    private List<ExperienceBlock> experiences = new ArrayList<>();
    @Builder.Default
    private List<ValueBlock> whatIBring = new ArrayList<>();
    @Builder.Default
    private List<SkillLine> coreSkills = new ArrayList<>();
    private ProjectBlock selectedProject;
    @Builder.Default
    private List<String> certifications = new ArrayList<>();
    @Builder.Default
    private List<EducationLine> education = new ArrayList<>();
    private String footer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceBlock {
        private String title;
        private String company;
        private String companyNote;
        private String location;
        private String dates;
        private List<String> bullets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueBlock {
        private String heading;
        private String body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillLine {
        private String label;
        private String values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectBlock {
        private String name;
        private String organisation;
        private String dates;
        private List<String> bullets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationLine {
        private String qualification;
        private String institution;
        private String dates;
    }
}

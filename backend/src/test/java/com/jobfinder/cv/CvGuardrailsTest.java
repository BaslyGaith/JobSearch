package com.jobfinder.cv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CvGuardrailsTest {

    private CvGuardrails guardrails;
    private FactBank factBank;

    @BeforeEach
    void setUp() throws Exception {
        guardrails = new CvGuardrails();
        factBank = new ObjectMapper().readValue(
                new ClassPathResource("cv/default-fact-bank.json").getInputStream(), FactBank.class);
    }

    @Test
    void acceptsADraftBuiltOnlyFromTheFactBank() {
        assertThatCode(() -> guardrails.verify(validDraft(), factBank)).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlocklistedTechnology() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setBullets(List.of("Containerised the reporting stack with Docker."));

        assertThatThrownBy(() -> guardrails.verify(draft, factBank))
                .isInstanceOf(CvGuardrails.ViolationException.class)
                .hasMessageContaining("Docker");
    }

    @Test
    void rejectsSeniorInAJobTitle() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setTitle("Senior Analyst Developer");

        assertThatThrownBy(() -> guardrails.verify(draft, factBank))
                .isInstanceOf(CvGuardrails.ViolationException.class)
                .hasMessageContaining("Senior");
    }

    @Test
    void rejectsSpringBootClaimedUnderEmployment() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setBullets(List.of("Built Spring Boot services for the reporting layer."));

        assertThatThrownBy(() -> guardrails.verify(draft, factBank))
                .isInstanceOf(CvGuardrails.ViolationException.class)
                .hasMessageContaining("Spring Boot");
    }

    @Test
    void rejectsInventedMetrics() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setBullets(List.of("Cut report refresh time by 47% across the estate."));

        assertThatThrownBy(() -> guardrails.verify(draft, factBank))
                .isInstanceOf(CvGuardrails.ViolationException.class)
                .hasMessageContaining("47");
    }

    @Test
    void allowsTheFactBanksOwnFigures() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setBullets(
                List.of("Delivered 15+ Power BI solutions across 12 Scrum sprints."));

        assertThatCode(() -> guardrails.verify(draft, factBank)).doesNotThrowAnyException();
    }

    @Test
    void reportsEveryViolationAtOnce() {
        CvDraft draft = validDraft();
        draft.getExperiences().get(0).setTitle("Senior Analyst Developer");
        draft.getExperiences().get(0).setBullets(List.of("Ran the platform on Kubernetes."));

        assertThatThrownBy(() -> guardrails.verify(draft, factBank))
                .isInstanceOfSatisfying(CvGuardrails.ViolationException.class,
                        e -> assertThat(e.getViolations()).hasSize(2));
    }

    private CvDraft validDraft() {
        return CvDraft.builder()
                .language("en")
                .tagline("Business Intelligence Analyst")
                .profile("Analyst Developer at Vermeg, a banking and insurance software vendor.")
                .experiences(new java.util.ArrayList<>(List.of(CvDraft.ExperienceBlock.builder()
                        .title("Analyst Developer")
                        .company("Vermeg")
                        .dates("Sep 2024 - Present")
                        .bullets(new java.util.ArrayList<>(List.of(
                                "Administers Power BI Service: workspace structure, app publishing.")))
                        .build())))
                .whatIBring(new java.util.ArrayList<>(List.of(CvDraft.ValueBlock.builder()
                        .heading("From business need to delivered model")
                        .body("Star schema, DAX measures, Power Query transformations.")
                        .build())))
                .coreSkills(new java.util.ArrayList<>(List.of(CvDraft.SkillLine.builder()
                        .label("BI & Power Platform")
                        .values("Power BI Desktop and Service · DAX")
                        .build())))
                .certifications(new java.util.ArrayList<>(List.of("Microsoft DP-600 Fabric Analytics Engineer")))
                .education(new java.util.ArrayList<>())
                .build();
    }
}

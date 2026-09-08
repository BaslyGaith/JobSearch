package com.jobfinder.application;

import com.jobfinder.cv.CvDocument;
import com.jobfinder.cv.CvProfile;
import com.jobfinder.job.JobOpportunity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailComposerTest {

    private EmailComposer composer;
    private CvProfile profile;

    @BeforeEach
    void setUp() {
        composer = new EmailComposer();
        profile = CvProfile.builder()
                .fullName("Mohamed Gaith Basly")
                .email("mgabasly@gmail.com")
                .phone("+216 56 060 033")
                .yearsExperience(4)
                .build();
    }

    @Test
    void subjectNamesTheRoleAndTheApplicant() {
        String subject = composer.subject(job("Data Analyst", "Example GmbH", null), profile);

        assertThat(subject).isEqualTo("Application - Data Analyst - Mohamed Gaith Basly");
    }

    @Test
    void addressesTheRecruiterWhenTheSourcePublishedAName() {
        String body = composer.body(job("Data Analyst", "Example GmbH", "Sophie Martin"), profile, cv("en"));

        assertThat(body).startsWith("Hello Sophie Martin,");
    }

    @Test
    void fallsBackToANeutralGreetingWithoutAName() {
        String body = composer.body(job("Data Analyst", "Example GmbH", null), profile, cv("en"));

        assertThat(body).startsWith("Hello,");
    }

    @Test
    void writesInFrenchWhenTheFrenchCvIsAttached() {
        String body = composer.body(job("Data Analyst", "ABC France", null), profile, cv("fr"));

        assertThat(body).contains("Je vous écris au sujet du poste");
        assertThat(body).contains("Cordialement,");
    }

    @Test
    void statesOnlyTheYearsHeldOnTheProfile() {
        String body = composer.body(job("Data Analyst", "Example GmbH", null), profile, cv("en"));

        assertThat(body).contains("4 years of experience");
        // No claim about skills the profile has not verified.
        assertThat(body).doesNotContain("expert").doesNotContain("passionate");
    }

    @Test
    void omitsExperienceEntirelyWhenTheProfileHasNoYears() {
        CvProfile withoutYears = CvProfile.builder().fullName("Someone").build();

        String body = composer.body(job("Data Analyst", "Example GmbH", null), withoutYears, cv("en"));

        assertThat(body).doesNotContain("years of experience");
    }

    private JobOpportunity job(String title, String company, String recruiter) {
        return JobOpportunity.builder()
                .title(title)
                .companyName(company)
                .recruiterName(recruiter)
                .build();
    }

    private CvDocument cv(String language) {
        return CvDocument.builder().language(language).title("Data Analyst").build();
    }
}

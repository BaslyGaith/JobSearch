package com.jobfinder.application;

import com.jobfinder.cv.CvDocument;
import com.jobfinder.cv.CvDocumentRepository;
import com.jobfinder.job.JobOpportunity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CvSelectorTest {

    private CvDocumentRepository repository;
    private CvSelector selector;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(CvDocumentRepository.class);
        selector = new CvSelector(repository);
    }

    @Test
    void picksTheCvWhoseTargetRoleMatchesTheOpportunity() {
        stored(cv("Java Developer", "en"), cv("Data Analyst", "en"));

        CvDocument chosen = selector.selectFor(userId, job("Data Analyst", "Berlin, Germany", "")).orElseThrow();

        assertThat(chosen.getTargetRole()).isEqualTo("Data Analyst");
    }

    @Test
    void prefersTheFrenchCvForAFrenchPosting() {
        stored(cv("Data Analyst", "en"), cv("Data Analyst", "fr"));

        CvDocument chosen = selector.selectFor(userId,
                job("Data Analyst", "Paris, France",
                        "Nous recherchons un développeur pour votre équipe. Compétences requises."))
                .orElseThrow();

        assertThat(chosen.getLanguage()).isEqualTo("fr");
    }

    @Test
    void defaultsToEnglishWhenThePostingIsNotFrench() {
        stored(cv("Data Analyst", "fr"), cv("Data Analyst", "en"));

        CvDocument chosen = selector.selectFor(userId,
                job("Data Analyst", "Berlin", "We are looking for an analyst.")).orElseThrow();

        assertThat(chosen.getLanguage()).isEqualTo("en");
    }

    @Test
    void fallsBackToAnyCvWhenTheWantedLanguageIsMissing() {
        stored(cv("Data Analyst", "fr"));

        assertThat(selector.selectFor(userId, job("Data Analyst", "Berlin", "English posting")))
                .isPresent();
    }

    @Test
    void returnsNothingWhenNoCvExistsYet() {
        stored();

        assertThat(selector.selectFor(userId, job("Data Analyst", "Berlin", ""))).isEmpty();
    }

    private void stored(CvDocument... documents) {
        when(repository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(documents));
    }

    private CvDocument cv(String targetRole, String language) {
        return CvDocument.builder()
                .id(UUID.randomUUID())
                .targetRole(targetRole)
                .title(targetRole)
                .language(language)
                .build();
    }

    private JobOpportunity job(String title, String location, String description) {
        return JobOpportunity.builder()
                .title(title)
                .location(location)
                .description(description)
                .build();
    }
}

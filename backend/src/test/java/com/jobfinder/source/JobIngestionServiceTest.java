package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;
import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.job.JobStatus;
import com.jobfinder.search.JobSearchPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JobIngestionServiceTest {

    private JobOpportunityRepository jobRepository;
    private JobSearchPreferenceRepository preferenceRepository;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobOpportunityRepository.class);
        preferenceRepository = mock(JobSearchPreferenceRepository.class);
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(jobRepository.existsByJobUrl(anyString())).thenReturn(false);
        when(jobRepository.existsByTitleIgnoreCaseAndCompanyNameIgnoreCase(anyString(), anyString()))
                .thenReturn(false);
    }

    @Test
    void storesWhatEnabledSourcesReturn() {
        JobIngestionService service = serviceWith(stub("Board", true, posting("Data Analyst", "url-1")));

        SyncResultDto result = service.sync(userId);

        assertThat(result.getAdded()).isEqualTo(1);
        assertThat(result.getSourcesUsed()).containsExactly("Board");

        ArgumentCaptor<JobOpportunity> saved = ArgumentCaptor.forClass(JobOpportunity.class);
        verify(jobRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(JobStatus.NEW);
        assertThat(saved.getValue().getSource()).isEqualTo("Board");
        assertThat(saved.getValue().getMatchScore()).isBetween(0, 100);
    }

    @Test
    void skipsDisabledSources() {
        JobIngestionService service = serviceWith(stub("Off", false, posting("Data Analyst", "url-1")));

        SyncResultDto result = service.sync(userId);

        assertThat(result.getAdded()).isZero();
        assertThat(result.getSourcesUsed()).isEmpty();
        verify(jobRepository, never()).save(any());
    }

    @Test
    void doesNotStoreAPostingAlreadyKnownByUrl() {
        when(jobRepository.existsByJobUrl("url-1")).thenReturn(true);
        JobIngestionService service = serviceWith(stub("Board", true, posting("Data Analyst", "url-1")));

        SyncResultDto result = service.sync(userId);

        assertThat(result.getAdded()).isZero();
        assertThat(result.getDuplicates()).isEqualTo(1);
        verify(jobRepository, never()).save(any());
    }

    @Test
    void treatsTheSameRoleAtTheSameCompanyAsKnown() {
        when(jobRepository.existsByTitleIgnoreCaseAndCompanyNameIgnoreCase("Data Analyst", "Acme"))
                .thenReturn(true);
        JobIngestionService service = serviceWith(stub("Board", true, posting("Data Analyst", "url-9")));

        assertThat(service.sync(userId).getDuplicates()).isEqualTo(1);
    }

    @Test
    void oneFailingSourceDoesNotLoseTheOthers() {
        JobSource failing = new JobSource() {
            public String name() { return "Broken"; }
            public boolean isEnabled() { return true; }
            public List<RawJobPosting> fetch(JobSearchCriteria criteria) {
                throw new IllegalStateException("upstream is down");
            }
        };
        JobIngestionService service = new JobIngestionService(
                List.of(failing, stub("Working", true, posting("Data Analyst", "url-1"))),
                jobRepository, preferenceRepository, new MatchScorer());

        SyncResultDto result = service.sync(userId);

        assertThat(result.getAdded()).isEqualTo(1);
        assertThat(result.getFailures()).hasSize(1);
        assertThat(result.getFailures().get(0)).contains("Broken").contains("upstream is down");
    }

    private JobIngestionService serviceWith(JobSource source) {
        return new JobIngestionService(List.of(source), jobRepository, preferenceRepository, new MatchScorer());
    }

    private JobSource stub(String name, boolean enabled, RawJobPosting... postings) {
        return new JobSource() {
            public String name() { return name; }
            public boolean isEnabled() { return enabled; }
            public List<RawJobPosting> fetch(JobSearchCriteria criteria) { return List.of(postings); }
        };
    }

    private RawJobPosting posting(String title, String url) {
        return RawJobPosting.builder()
                .source("Board")
                .title(title)
                .companyName("Acme")
                .location("Berlin")
                .jobUrl(url)
                .description("A role.")
                .build();
    }
}

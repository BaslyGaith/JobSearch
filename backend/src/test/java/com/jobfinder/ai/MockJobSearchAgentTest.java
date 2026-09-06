package com.jobfinder.ai;

import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockJobSearchAgentTest {

    @Mock
    private JobOpportunityRepository jobRepository;

    @InjectMocks
    private MockJobSearchAgent agent;

    @Test
    void searchJobs_shouldReturnNonEmptyResults() {
        when(jobRepository.findAll()).thenReturn(List.of(
                JobOpportunity.builder().title("Java Developer").build(),
                JobOpportunity.builder().title("Data Engineer").build()
        ));

        JobSearchCriteria criteria = JobSearchCriteria.builder().maxResults(10).build();
        List<JobOpportunity> results = agent.searchJobs(criteria);

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
    }

    @Test
    void searchJobs_shouldRespectMaxResults() {
        List<JobOpportunity> jobs = List.of(
                JobOpportunity.builder().title("Job 1").build(),
                JobOpportunity.builder().title("Job 2").build(),
                JobOpportunity.builder().title("Job 3").build()
        );
        when(jobRepository.findAll()).thenReturn(jobs);

        JobSearchCriteria criteria = JobSearchCriteria.builder().maxResults(2).build();
        List<JobOpportunity> results = agent.searchJobs(criteria);

        assertThat(results).hasSize(2);
    }

    @Test
    void isAvailable_shouldReturnTrue() {
        assertThat(agent.isAvailable()).isTrue();
    }

    @Test
    void getAgentType_shouldReturnMock() {
        assertThat(agent.getAgentType()).isEqualTo("MOCK");
    }
}

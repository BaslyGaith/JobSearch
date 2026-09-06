package com.jobfinder.job;

import com.jobfinder.common.PagedResponse;
import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobOpportunityRepository jobRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobService jobService;

    private UUID userId;
    private JobOpportunity sampleJob;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleJob = JobOpportunity.builder()
                .id(UUID.randomUUID())
                .title("Java Developer")
                .companyName("TechCorp")
                .location("Berlin, Germany")
                .employmentType("FULL_TIME")
                .matchScore(90)
                .status(JobStatus.NEW)
                .publicationDate(LocalDate.now())
                .build();
    }

    @Test
    void getJobs_shouldReturnPagedResponse() {
        Page<JobOpportunity> page = new PageImpl<>(List.of(sampleJob), PageRequest.of(0, 12), 1);
        when(jobRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);
        when(savedJobRepository.findByUserId(userId)).thenReturn(new HashSet<>());

        PagedResponse<JobOpportunityDto> result = jobService.getJobs(
                null, null, null, null, null, null, null, "newest", 0, 12, userId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void getJobById_shouldReturnDto_whenJobExists() {
        UUID jobId = sampleJob.getId();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));
        when(savedJobRepository.existsByUserIdAndJobId(userId, jobId)).thenReturn(false);

        JobOpportunityDto result = jobService.getJobById(jobId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(jobId);
        assertThat(result.isSaved()).isFalse();
    }

    @Test
    void getJobById_shouldThrow_whenJobNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(jobId, userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateJobStatus_shouldUpdateAndReturn() {
        UUID jobId = sampleJob.getId();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));
        when(jobRepository.save(any(JobOpportunity.class))).thenReturn(sampleJob);
        when(savedJobRepository.existsByUserIdAndJobId(userId, jobId)).thenReturn(false);

        JobOpportunityDto result = jobService.updateJobStatus(jobId, JobStatus.INTERESTED, userId);

        assertThat(result.getStatus()).isEqualTo(JobStatus.INTERESTED);
        verify(jobRepository).save(sampleJob);
    }

    @Test
    void saveJob_shouldCreateSavedJob() {
        UUID jobId = sampleJob.getId();
        User user = User.builder().id(userId).email("test@test.com").provider("GOOGLE").providerId("123").build();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));
        when(savedJobRepository.existsByUserIdAndJobId(userId, jobId)).thenReturn(false);
        when(userService.findById(userId)).thenReturn(user);
        when(savedJobRepository.save(any(SavedJob.class))).thenAnswer(inv -> inv.getArgument(0));

        JobOpportunityDto result = jobService.saveJob(jobId, userId);

        assertThat(result).isNotNull();
        assertThat(result.isSaved()).isTrue();
        verify(savedJobRepository).save(any(SavedJob.class));
    }

    @Test
    void getStats_shouldReturnCounts() {
        when(jobRepository.countByStatus(JobStatus.NEW)).thenReturn(10L);
        when(jobRepository.countHighMatches()).thenReturn(5L);
        when(jobRepository.countByStatus(JobStatus.INTERESTED)).thenReturn(3L);
        when(jobRepository.countByStatus(JobStatus.APPLIED)).thenReturn(2L);

        JobStatsDto stats = jobService.getStats();

        assertThat(stats.getNewOpportunities()).isEqualTo(10L);
        assertThat(stats.getHighMatches()).isEqualTo(5L);
        assertThat(stats.getInterested()).isEqualTo(3L);
        assertThat(stats.getApplied()).isEqualTo(2L);
    }
}

package com.jobfinder.job;

import com.jobfinder.common.PagedResponse;
import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobOpportunityRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PagedResponse<JobOpportunityDto> getJobs(
            String search, String location, String company,
            String employmentType, String status, Integer minMatchScore,
            Boolean remote, String sortBy, int page, int size, UUID userId) {

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        JobStatus jobStatus = status != null ? JobStatus.valueOf(status.toUpperCase()) : null;

        Page<JobOpportunity> jobPage = jobRepository.findWithFilters(
                search, location, company, employmentType, jobStatus, minMatchScore, remote, pageable);

        Set<UUID> savedJobIds = savedJobRepository.findByUserId(userId)
                .stream().map(s -> s.getJob().getId()).collect(Collectors.toSet());

        Page<JobOpportunityDto> dtoPage = jobPage.map(job -> toDto(job, savedJobIds.contains(job.getId())));
        return PagedResponse.from(dtoPage);
    }

    /** The unreviewed opportunities worth looking at first, best match leading. */
    @Transactional(readOnly = true)
    public List<JobOpportunityDto> getWorthReviewing(UUID userId, int limit) {
        Set<UUID> savedJobIds = savedJobRepository.findByUserId(userId)
                .stream().map(s -> s.getJob().getId()).collect(Collectors.toSet());

        return jobRepository.findWorthReviewing(PageRequest.of(0, limit)).stream()
                .map(job -> toDto(job, savedJobIds.contains(job.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public JobOpportunityDto getJobById(UUID id, UUID userId) {
        JobOpportunity job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found: " + id));
        boolean saved = savedJobRepository.existsByUserIdAndJobId(userId, id);
        return toDto(job, saved);
    }

    @Transactional
    public JobOpportunityDto updateJobStatus(UUID jobId, JobStatus newStatus, UUID userId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found: " + jobId));
        job.setStatus(newStatus);
        jobRepository.save(job);
        boolean saved = savedJobRepository.existsByUserIdAndJobId(userId, jobId);
        return toDto(job, saved);
    }

    @Transactional
    public JobOpportunityDto saveJob(UUID jobId, UUID userId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found: " + jobId));

        if (!savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            User user = userService.findById(userId);
            SavedJob savedJob = SavedJob.builder().user(user).job(job).build();
            savedJobRepository.save(savedJob);
        }
        return toDto(job, true);
    }

    @Transactional
    public void unsaveJob(UUID jobId, UUID userId) {
        savedJobRepository.findByUserIdAndJobId(userId, jobId)
                .ifPresent(savedJobRepository::delete);
    }

    @Transactional(readOnly = true)
    public JobStatsDto getStats() {
        return JobStatsDto.builder()
                .newOpportunities(jobRepository.countByStatus(JobStatus.NEW))
                .highMatches(jobRepository.countHighMatches())
                .interested(jobRepository.countByStatus(JobStatus.INTERESTED))
                .applied(jobRepository.countByStatus(JobStatus.APPLIED))
                .build();
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "publicationDate");
        return switch (sortBy.toLowerCase()) {
            case "highest_match" -> Sort.by(Sort.Direction.DESC, "matchScore");
            case "company" -> Sort.by(Sort.Direction.ASC, "companyName");
            case "publication_date" -> Sort.by(Sort.Direction.DESC, "publicationDate");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private JobOpportunityDto toDto(JobOpportunity job, boolean saved) {
        return JobOpportunityDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .description(job.getDescription())
                .jobUrl(job.getJobUrl())
                .source(job.getSource())
                .publicationDate(job.getPublicationDate())
                .recruiterName(job.getRecruiterName())
                .recruiterEmail(job.getRecruiterEmail())
                .recruiterProfileUrl(job.getRecruiterProfileUrl())
                .matchScore(job.getMatchScore())
                .status(job.getStatus())
                .saved(saved)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}

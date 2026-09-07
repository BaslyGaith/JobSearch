package com.jobfinder.today;

import com.jobfinder.cv.CvDocumentRepository;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.job.JobService;
import com.jobfinder.job.JobStatus;
import com.jobfinder.linkedin.LinkedInConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodayService {

    private static final int WORTH_REVIEWING = 3;

    private final JobOpportunityRepository jobRepository;
    private final JobService jobService;
    private final CvDocumentRepository cvDocumentRepository;
    private final LinkedInConnectionRepository linkedInRepository;

    @Transactional(readOnly = true)
    public TodayDto forUser(UUID userId) {
        long cvVersions = cvDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(doc -> doc.getGenerationId())
                .distinct()
                .count();

        boolean linkedInConnected = linkedInRepository.findByUserId(userId)
                .map(connection -> connection.isConnected())
                .orElse(false);

        return TodayDto.builder()
                .publishedToday(jobRepository.countByPublicationDateGreaterThanEqual(LocalDate.now()))
                .awaitingReview(jobRepository.countByStatus(JobStatus.NEW))
                .strongMatches(jobRepository.countHighMatches())
                .interested(jobRepository.countByStatus(JobStatus.INTERESTED))
                .applied(jobRepository.countByStatus(JobStatus.APPLIED))
                .cvsPrepared(cvVersions)
                .linkedInConnected(linkedInConnected)
                .worthReviewing(jobService.getWorthReviewing(userId, WORTH_REVIEWING))
                .build();
    }
}

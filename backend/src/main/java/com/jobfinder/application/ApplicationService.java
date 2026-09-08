package com.jobfinder.application;

import com.jobfinder.cv.CvDocument;
import com.jobfinder.cv.CvProfile;
import com.jobfinder.cv.CvService;
import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.job.JobStatus;
import com.jobfinder.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Prepares an application and tracks what happens to it. Nothing here sends
 * anything: the pack is assembled, the user reviews it, and sending stays a
 * deliberate act on their side.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    /** How long a sent application sits without a reply before a nudge is suggested. */
    private static final int FOLLOW_UP_AFTER_DAYS = 7;

    private final ApplicationRepository applicationRepository;
    private final JobOpportunityRepository jobRepository;
    private final CvService cvService;
    private final CvSelector cvSelector;
    private final EmailComposer emailComposer;
    private final UserService userService;

    /**
     * Assembles the pack for an opportunity: best CV, recipient, subject, body.
     * Re-preparing an existing application returns it untouched, so a user's
     * edits are never overwritten by a second click.
     */
    @Transactional
    public ApplicationDto prepare(UUID userId, UUID jobId) {
        return applicationRepository.findByUserIdAndJobOpportunityId(userId, jobId)
                .map(this::toDto)
                .orElseGet(() -> toDto(create(userId, jobId)));
    }

    private Application create(UUID userId, UUID jobId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        CvProfile profile = cvService.getOrCreateProfile(userId);
        CvDocument cv = cvSelector.selectFor(userId, job).orElse(null);

        Application application = Application.builder()
                .user(userService.findById(userId))
                .jobOpportunity(job)
                .cvDocument(cv)
                .recipientName(job.getRecruiterName())
                .recipientEmail(job.getRecruiterEmail())
                .subject(emailComposer.subject(job, profile))
                .body(emailComposer.body(job, profile, cv))
                .status(ApplicationStatus.DRAFT)
                .build();

        application.record("Application prepared",
                cv == null
                        ? "No CV was attached - none has been generated yet."
                        : "CV chosen: " + cv.getTitle() + " (" + cv.getLanguage().toUpperCase() + ")");

        Application saved = applicationRepository.save(application);
        log.info("Prepared application for opportunity {}", jobId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> list(UUID userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDto get(UUID userId, UUID id) {
        return toDto(find(userId, id));
    }

    /** Saves the user's edits to the email before they send it. */
    @Transactional
    public ApplicationDto update(UUID userId, UUID id, UpdateApplicationRequest request) {
        Application application = find(userId, id);

        if (application.getStatus() == ApplicationStatus.SENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This application has already been sent and cannot be edited");
        }
        application.setRecipientName(request.getRecipientName());
        application.setRecipientEmail(request.getRecipientEmail());
        application.setSubject(request.getSubject());
        application.setBody(request.getBody());
        if (request.getCvDocumentId() != null) {
            application.setCvDocument(cvService.findDocument(userId, request.getCvDocumentId()));
        }
        application.setStatus(ApplicationStatus.READY);
        application.record("Reviewed", "Ready to send.");

        return toDto(applicationRepository.save(application));
    }

    /**
     * Records that the user sent it. The send itself happens in the user's own
     * mail client - this only writes down that it did.
     */
    @Transactional
    public ApplicationDto markSent(UUID userId, UUID id) {
        Application application = find(userId, id);
        application.setStatus(ApplicationStatus.SENT);
        application.setSentAt(LocalDateTime.now());
        application.record("Sent", "Sent to " + application.getRecipientEmail());

        JobOpportunity job = application.getJobOpportunity();
        job.setStatus(JobStatus.APPLIED);
        jobRepository.save(job);

        return toDto(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationDto changeStatus(UUID userId, UUID id, ApplicationStatus status, String note) {
        Application application = find(userId, id);
        application.setStatus(status);
        application.record(label(status), note);
        return toDto(applicationRepository.save(application));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        applicationRepository.delete(find(userId, id));
    }

    /** Sent a week ago with nothing back - worth a nudge. */
    @Transactional(readOnly = true)
    public List<ApplicationDto> awaitingFollowUp(UUID userId) {
        return applicationRepository.findByUserIdAndStatusAndSentAtBefore(
                        userId, ApplicationStatus.SENT, LocalDateTime.now().minusDays(FOLLOW_UP_AFTER_DAYS))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Application find(UUID userId, UUID id) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private String label(ApplicationStatus status) {
        return switch (status) {
            case DRAFT -> "Back to draft";
            case READY -> "Ready to send";
            case SENT -> "Sent";
            case FOLLOW_UP -> "Follow-up sent";
            case INTERVIEW -> "Interview";
            case REJECTED -> "Closed - not taken forward";
            case CLOSED -> "Closed";
        };
    }

    private ApplicationDto toDto(Application application) {
        JobOpportunity job = application.getJobOpportunity();
        CvDocument cv = application.getCvDocument();

        return ApplicationDto.builder()
                .id(application.getId())
                .jobOpportunityId(job.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getCompanyName())
                .jobUrl(job.getJobUrl())
                .matchScore(job.getMatchScore())
                .cvDocumentId(cv == null ? null : cv.getId())
                .cvTitle(cv == null ? null : cv.getTitle())
                .cvLanguage(cv == null ? null : cv.getLanguage())
                .recipientName(application.getRecipientName())
                .recipientEmail(application.getRecipientEmail())
                .subject(application.getSubject())
                .body(application.getBody())
                .status(application.getStatus())
                .sentAt(application.getSentAt())
                .createdAt(application.getCreatedAt())
                .events(application.getEvents().stream()
                        .map(event -> ApplicationDto.TimelineEntry.builder()
                                .label(event.getLabel())
                                .detail(event.getDetail())
                                .occurredAt(event.getOccurredAt())
                                .build())
                        .toList())
                .build();
    }
}

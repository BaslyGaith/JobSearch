package com.jobfinder.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Application> findByIdAndUserId(UUID id, UUID userId);

    Optional<Application> findByUserIdAndJobOpportunityId(UUID userId, UUID jobOpportunityId);

    long countByUserIdAndStatus(UUID userId, ApplicationStatus status);

    /** Sent applications with no movement since the cutoff - follow-up candidates. */
    List<Application> findByUserIdAndStatusAndSentAtBefore(
            UUID userId, ApplicationStatus status, LocalDateTime cutoff);
}

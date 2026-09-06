package com.jobfinder.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
    Optional<SavedJob> findByUserIdAndJobId(UUID userId, UUID jobId);
    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);
    Set<SavedJob> findByUserId(UUID userId);
    long countByUserId(UUID userId);
}

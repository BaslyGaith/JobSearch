package com.jobfinder.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobSearchPreferenceRepository extends JpaRepository<JobSearchPreference, UUID> {
    Optional<JobSearchPreference> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}

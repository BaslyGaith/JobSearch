package com.jobfinder.linkedin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkedInConnectionRepository extends JpaRepository<LinkedInConnection, UUID> {
    Optional<LinkedInConnection> findByUserId(UUID userId);
    boolean existsByUserIdAndConnectedTrue(UUID userId);
}

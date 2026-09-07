package com.jobfinder.cv;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CvProfileRepository extends JpaRepository<CvProfile, UUID> {

    Optional<CvProfile> findByUserId(UUID userId);
}

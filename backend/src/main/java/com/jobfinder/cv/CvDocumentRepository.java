package com.jobfinder.cv;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CvDocumentRepository extends JpaRepository<CvDocument, UUID> {

    List<CvDocument> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<CvDocument> findByIdAndUserId(UUID id, UUID userId);

    List<CvDocument> findByUserIdAndJobOpportunityIdOrderByCreatedAtDesc(UUID userId, UUID jobOpportunityId);

    List<CvDocument> findByUserIdAndGenerationId(UUID userId, UUID generationId);
}

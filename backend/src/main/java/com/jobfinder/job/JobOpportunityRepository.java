package com.jobfinder.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobOpportunityRepository extends JpaRepository<JobOpportunity, UUID> {

    @Query("""
            SELECT j FROM JobOpportunity j
            WHERE (:search IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
            AND (:company IS NULL OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :company, '%')))
            AND (:employmentType IS NULL OR j.employmentType = :employmentType)
            AND (:status IS NULL OR j.status = :status)
            AND (:minMatchScore IS NULL OR j.matchScore >= :minMatchScore)
            AND (:remote IS NULL OR (:remote = true AND LOWER(j.location) LIKE '%remote%'))
            """)
    Page<JobOpportunity> findWithFilters(
            @Param("search") String search,
            @Param("location") String location,
            @Param("company") String company,
            @Param("employmentType") String employmentType,
            @Param("status") JobStatus status,
            @Param("minMatchScore") Integer minMatchScore,
            @Param("remote") Boolean remote,
            Pageable pageable
    );

    long countByStatus(JobStatus status);

    @Query("SELECT COUNT(j) FROM JobOpportunity j WHERE j.matchScore >= 85")
    long countHighMatches();

    long countByPublicationDateGreaterThanEqual(LocalDate since);

    long countBySource(String source);

    boolean existsByJobUrl(String jobUrl);

    boolean existsByTitleIgnoreCaseAndCompanyNameIgnoreCase(String title, String companyName);

    /** Unreviewed opportunities worth a look first, best match at the top. */
    @Query("""
            SELECT j FROM JobOpportunity j
            WHERE j.status = com.jobfinder.job.JobStatus.NEW
            ORDER BY j.matchScore DESC NULLS LAST, j.publicationDate DESC
            """)
    List<JobOpportunity> findWorthReviewing(Pageable pageable);
}

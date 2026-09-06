package com.jobfinder.job;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOpportunity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "company_name")
    private String companyName;

    private String location;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "job_url", columnDefinition = "TEXT")
    private String jobUrl;

    private String source;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "recruiter_name")
    private String recruiterName;

    @Column(name = "recruiter_email")
    private String recruiterEmail;

    @Column(name = "recruiter_profile_url", columnDefinition = "TEXT")
    private String recruiterProfileUrl;

    @Column(name = "match_score")
    private Integer matchScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.NEW;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

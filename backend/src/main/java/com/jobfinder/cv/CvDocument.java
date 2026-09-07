package com.jobfinder.cv;

import com.jobfinder.job.JobOpportunity;
import com.jobfinder.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvDocument {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opportunity_id")
    private JobOpportunity jobOpportunity;

    @Column(nullable = false, length = 5)
    private String language;

    @Column(name = "target_role")
    private String targetRole;

    @Column(name = "target_company")
    private String targetCompany;

    @Column(name = "job_family")
    private String jobFamily;

    @Column(name = "accent_color")
    private String accentColor;

    @Column(name = "posting_text", columnDefinition = "TEXT")
    private String postingText;

    /** Serialised {@link CvDraft}. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Serialised gap report - what the posting wants that the fact bank lacks. */
    @Column(columnDefinition = "TEXT")
    private String gaps;

    @Column(name = "generated_by")
    private String generatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

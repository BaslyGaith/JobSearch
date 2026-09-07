package com.jobfinder.cv;

import com.jobfinder.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cv_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvProfile {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String headline;

    private String email;

    private String phone;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedinUrl;

    private String location;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    /** The verified fact bank as JSON. Nothing outside it may reach a CV. */
    @Column(name = "fact_bank", nullable = false, columnDefinition = "TEXT")
    private String factBank;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

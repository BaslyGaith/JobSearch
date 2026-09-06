package com.jobfinder.search;

import com.jobfinder.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_search_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchPreference {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "preference_job_titles",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "job_title")
    @Builder.Default
    private List<String> jobTitles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "preference_locations",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "location")
    @Builder.Default
    private List<String> locations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "preference_employment_types",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "employment_type")
    @Builder.Default
    private List<String> employmentTypes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "preference_experience_levels",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "experience_level")
    @Builder.Default
    private List<String> experienceLevels = new ArrayList<>();

    @Column(name = "remote_preference")
    @Builder.Default
    private String remotePreference = "ANY";

    @ElementCollection
    @CollectionTable(name = "preference_keywords",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

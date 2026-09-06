package com.jobfinder.search;

import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchPreferenceService {

    private final JobSearchPreferenceRepository preferenceRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Optional<JobSearchPreferenceDto> getPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId).map(this::toDto);
    }

    @Transactional
    public JobSearchPreferenceDto savePreferences(UUID userId, UpdatePreferenceRequest request) {
        JobSearchPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.findById(userId);
                    return JobSearchPreference.builder().user(user).build();
                });

        preference.setJobTitles(request.getJobTitles());
        preference.setLocations(request.getLocations());
        preference.setEmploymentTypes(request.getEmploymentTypes());
        preference.setExperienceLevels(request.getExperienceLevels());
        preference.setRemotePreference(request.getRemotePreference());
        preference.setKeywords(request.getKeywords());

        return toDto(preferenceRepository.save(preference));
    }

    @Transactional
    public void deletePreferences(UUID userId) {
        preferenceRepository.deleteByUserId(userId);
    }

    private JobSearchPreferenceDto toDto(JobSearchPreference p) {
        return JobSearchPreferenceDto.builder()
                .id(p.getId())
                .jobTitles(p.getJobTitles())
                .locations(p.getLocations())
                .employmentTypes(p.getEmploymentTypes())
                .experienceLevels(p.getExperienceLevels())
                .remotePreference(p.getRemotePreference())
                .keywords(p.getKeywords())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

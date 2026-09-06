package com.jobfinder.search;

import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSearchPreferenceServiceTest {

    @Mock
    private JobSearchPreferenceRepository preferenceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobSearchPreferenceService preferenceService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).email("test@test.com").provider("GOOGLE").providerId("123").build();
    }

    @Test
    void getPreferences_shouldReturnEmpty_whenNoneExist() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Optional<JobSearchPreferenceDto> result = preferenceService.getPreferences(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getPreferences_shouldReturnDto_whenExists() {
        JobSearchPreference pref = JobSearchPreference.builder()
                .id(UUID.randomUUID())
                .user(user)
                .jobTitles(List.of("Java Developer", "Data Engineer"))
                .locations(List.of("Berlin", "Remote"))
                .remotePreference("REMOTE")
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        Optional<JobSearchPreferenceDto> result = preferenceService.getPreferences(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getJobTitles()).containsExactly("Java Developer", "Data Engineer");
        assertThat(result.get().getRemotePreference()).isEqualTo("REMOTE");
    }

    @Test
    void savePreferences_shouldCreateNew_whenNoneExist() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userService.findById(userId)).thenReturn(user);
        when(preferenceRepository.save(any())).thenAnswer(inv -> {
            JobSearchPreference p = inv.getArgument(0);
            p = JobSearchPreference.builder()
                    .id(UUID.randomUUID())
                    .user(p.getUser())
                    .jobTitles(p.getJobTitles())
                    .locations(p.getLocations())
                    .remotePreference(p.getRemotePreference())
                    .keywords(p.getKeywords())
                    .employmentTypes(p.getEmploymentTypes())
                    .experienceLevels(p.getExperienceLevels())
                    .build();
            return p;
        });

        UpdatePreferenceRequest request = new UpdatePreferenceRequest();
        request.setJobTitles(List.of("Java Developer"));
        request.setLocations(List.of("Germany"));
        request.setRemotePreference("HYBRID");

        JobSearchPreferenceDto result = preferenceService.savePreferences(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.getJobTitles()).containsExactly("Java Developer");
        assertThat(result.getRemotePreference()).isEqualTo("HYBRID");
    }

    @Test
    void deletePreferences_shouldCallRepository() {
        preferenceService.deletePreferences(userId);
        verify(preferenceRepository).deleteByUserId(userId);
    }
}

package com.jobfinder.user;

import com.jobfinder.auth.GoogleOAuth2UserInfo;
import com.jobfinder.auth.MicrosoftOAuth2UserInfo;
import com.jobfinder.auth.OAuth2UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private OAuth2UserInfo googleUserInfo;
    private OAuth2UserInfo microsoftUserInfo;

    @BeforeEach
    void setUp() {
        googleUserInfo = new GoogleOAuth2UserInfo(Map.of(
                "sub", "google-id-123",
                "email", "user@gmail.com",
                "given_name", "John",
                "family_name", "Doe",
                "picture", "https://example.com/photo.jpg"
        ));

        microsoftUserInfo = new MicrosoftOAuth2UserInfo(Map.of(
                "sub", "ms-id-456",
                "email", "user@outlook.com",
                "given_name", "Jane",
                "family_name", "Smith"
        ));
    }

    @Test
    void createOrUpdateUser_shouldCreateNewUser_whenNotExists() {
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-id-123"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(UUID.randomUUID())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .provider(u.getProvider())
                    .providerId(u.getProviderId())
                    .build();
            return u;
        });

        User result = userService.createOrUpdateUser(googleUserInfo);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getProvider()).isEqualTo("GOOGLE");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createOrUpdateUser_shouldUpdateExistingUser_whenExists() {
        User existing = User.builder()
                .id(UUID.randomUUID())
                .email("user@gmail.com")
                .firstName("OldFirst")
                .lastName("OldLast")
                .provider("GOOGLE")
                .providerId("google-id-123")
                .build();

        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-id-123"))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createOrUpdateUser(googleUserInfo);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void createOrUpdateUser_shouldCreateMicrosoftUser() {
        when(userRepository.findByProviderAndProviderId("MICROSOFT", "ms-id-456"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createOrUpdateUser(microsoftUserInfo);

        assertThat(result.getProvider()).isEqualTo("MICROSOFT");
        assertThat(result.getEmail()).isEqualTo("user@outlook.com");
    }

    @Test
    void toDto_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .provider("GOOGLE")
                .build();

        UserDto dto = userService.toDto(user);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getFirstName()).isEqualTo("Test");
        assertThat(dto.getProvider()).isEqualTo("GOOGLE");
    }
}

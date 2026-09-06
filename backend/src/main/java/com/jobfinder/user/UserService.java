package com.jobfinder.user;

import com.jobfinder.auth.OAuth2UserInfo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createOrUpdateUser(OAuth2UserInfo userInfo) {
        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getId())
                .map(existing -> updateUser(existing, userInfo))
                .orElseGet(() -> createUser(userInfo));
    }

    private User createUser(OAuth2UserInfo userInfo) {
        log.info("Creating new user: {} from provider: {}", userInfo.getEmail(), userInfo.getProvider());
        User user = User.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .profilePicture(userInfo.getImageUrl())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getId())
                .build();
        return userRepository.save(user);
    }

    private User updateUser(User user, OAuth2UserInfo userInfo) {
        log.debug("Updating existing user: {}", user.getEmail());
        if (userInfo.getFirstName() != null) user.setFirstName(userInfo.getFirstName());
        if (userInfo.getLastName() != null) user.setLastName(userInfo.getLastName());
        if (userInfo.getImageUrl() != null) user.setProfilePicture(userInfo.getImageUrl());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateUserRequest request) {
        User user = findById(userId);
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getProfilePicture() != null) user.setProfilePicture(request.getProfilePicture());
        return userRepository.save(user);
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

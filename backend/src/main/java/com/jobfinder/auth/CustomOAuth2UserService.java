package com.jobfinder.auth;

import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = resolveUserInfo(registrationId, oAuth2User.getAttributes());

        log.debug("Loading OAuth2 user: {} from provider: {}", userInfo.getEmail(), registrationId);

        User user = userService.createOrUpdateUser(userInfo);
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo resolveUserInfo(String registrationId, java.util.Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "microsoft" -> new MicrosoftOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}

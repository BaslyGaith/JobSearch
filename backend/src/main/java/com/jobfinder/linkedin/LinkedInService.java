package com.jobfinder.linkedin;

import com.jobfinder.config.AppProperties;
import com.jobfinder.user.User;
import com.jobfinder.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInService {

    private final LinkedInConnectionRepository connectionRepository;
    private final UserService userService;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public LinkedInStatusDto getStatus(UUID userId) {
        return connectionRepository.findByUserId(userId)
                .filter(LinkedInConnection::isConnected)
                .map(conn -> LinkedInStatusDto.builder()
                        .connected(true)
                        .linkedinEmail(conn.getLinkedinEmail())
                        .connectedAt(conn.getConnectedAt())
                        .mode(appProperties.getLinkedin().isMockMode() ? "MOCK" : "LIVE")
                        .build())
                .orElse(LinkedInStatusDto.builder()
                        .connected(false)
                        .mode(appProperties.getLinkedin().isMockMode() ? "MOCK" : "LIVE")
                        .build());
    }

    /**
     * Returns the URL to initiate LinkedIn OAuth.
     * In mock mode, returns a local mock URL.
     * In live mode, returns the real LinkedIn authorization URL.
     */
    public String getAuthorizationUrl(UUID userId) {
        if (appProperties.getLinkedin().isMockMode()) {
            log.info("LinkedIn mock mode: generating mock auth URL for user {}", userId);
            return "/api/linkedin/callback?code=mock_code&state=" + userId;
        }
        // Real LinkedIn OAuth URL construction would go here for Sprint 2
        String clientId = appProperties.getLinkedin().getClientId();
        String redirectUri = appProperties.getLinkedin().getRedirectUri();
        String scope = "openid profile email";
        return "https://www.linkedin.com/oauth/v2/authorization"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope.replace(" ", "%20")
                + "&state=" + userId;
    }

    /**
     * Handle the OAuth callback. In mock mode, simulates a successful connection.
     */
    @Transactional
    public LinkedInStatusDto handleCallback(String code, String state) {
        UUID userId = parseUserId(state);
        User user = userService.findById(userId);

        LinkedInConnection connection = connectionRepository.findByUserId(userId)
                .orElseGet(() -> LinkedInConnection.builder().user(user).build());

        if (appProperties.getLinkedin().isMockMode()) {
            // Simulate successful connection with mock data
            connection.setConnected(true);
            connection.setLinkedinEmail("mock.linkedin@example.com");
            connection.setLinkedinId("mock-linkedin-id-" + userId.toString().substring(0, 8));
            connection.setAccessToken("mock-access-token");
            connection.setConnectedAt(LocalDateTime.now());
            connection.setTokenExpiresAt(LocalDateTime.now().plusDays(60));
        } else {
            // Sprint 2: exchange code for real access token and fetch profile
            throw new UnsupportedOperationException("Live LinkedIn integration not implemented in Sprint 1");
        }

        connectionRepository.save(connection);
        return getStatus(userId);
    }

    @Transactional
    public void disconnect(UUID userId) {
        connectionRepository.findByUserId(userId).ifPresent(conn -> {
            conn.setConnected(false);
            conn.setAccessToken(null);
            conn.setRefreshToken(null);
            conn.setLinkedinEmail(null);
            conn.setConnectedAt(null);
            connectionRepository.save(conn);
        });
    }

    private UUID parseUserId(String state) {
        try {
            return UUID.fromString(state);
        } catch (IllegalArgumentException e) {
            // state might be a random UUID set by the mock flow — default to a safe fallback
            throw new IllegalArgumentException("Invalid OAuth state parameter: " + state);
        }
    }
}

package com.jobfinder.linkedin;

import com.jobfinder.auth.CustomOAuth2User;
import com.jobfinder.config.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/linkedin")
@RequiredArgsConstructor
@Tag(name = "LinkedIn", description = "LinkedIn account connection management")
public class LinkedInController {

    private final LinkedInService linkedInService;
    private final AppProperties appProperties;

    @GetMapping("/status")
    @Operation(summary = "Get LinkedIn connection status")
    public ResponseEntity<LinkedInStatusDto> getStatus(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(linkedInService.getStatus(userId));
    }

    @GetMapping("/connect")
    @Operation(summary = "Initiate LinkedIn OAuth connection")
    public void connect(Authentication authentication, HttpServletResponse response) throws IOException {
        UUID userId = getUserId(authentication);
        String authUrl = linkedInService.getAuthorizationUrl(userId);
        log.info("Redirecting user {} to LinkedIn auth: {}", userId, authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    @Operation(summary = "Handle LinkedIn OAuth callback")
    public void callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response) throws IOException {
        try {
            linkedInService.handleCallback(code, state);
            response.sendRedirect(appProperties.getFrontendUrl() + "/linkedin?connected=true");
        } catch (Exception e) {
            log.error("LinkedIn callback failed: {}", e.getMessage());
            response.sendRedirect(appProperties.getFrontendUrl() + "/linkedin?error=true");
        }
    }

    @PostMapping("/disconnect")
    @Operation(summary = "Disconnect LinkedIn account")
    public ResponseEntity<Void> disconnect(Authentication authentication) {
        UUID userId = getUserId(authentication);
        linkedInService.disconnect(userId);
        return ResponseEntity.ok().build();
    }

    private UUID getUserId(Authentication authentication) {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        return principal.getUser().getId();
    }
}

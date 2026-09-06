package com.jobfinder.search;

import com.jobfinder.auth.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Tag(name = "Search Preferences", description = "Job search preference management")
public class JobSearchPreferenceController {

    private final JobSearchPreferenceService preferenceService;

    @GetMapping
    @Operation(summary = "Get current user's job search preferences")
    public ResponseEntity<JobSearchPreferenceDto> getPreferences(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return preferenceService.getPreferences(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    @Operation(summary = "Create job search preferences")
    public ResponseEntity<JobSearchPreferenceDto> createPreferences(
            @RequestBody UpdatePreferenceRequest request, Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(preferenceService.savePreferences(userId, request));
    }

    @PutMapping
    @Operation(summary = "Update job search preferences")
    public ResponseEntity<JobSearchPreferenceDto> updatePreferences(
            @RequestBody UpdatePreferenceRequest request, Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(preferenceService.savePreferences(userId, request));
    }

    @DeleteMapping
    @Operation(summary = "Delete job search preferences")
    public ResponseEntity<Void> deletePreferences(Authentication authentication) {
        UUID userId = getUserId(authentication);
        preferenceService.deletePreferences(userId);
        return ResponseEntity.ok().build();
    }

    private UUID getUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.getUser().getId();
    }
}

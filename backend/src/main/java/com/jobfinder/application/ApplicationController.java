package com.jobfinder.application;

import com.jobfinder.auth.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Preparing and tracking applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "List applications, newest first")
    public ResponseEntity<List<ApplicationDto>> list(Authentication authentication) {
        return ResponseEntity.ok(applicationService.list(userId(authentication)));
    }

    @GetMapping("/follow-up")
    @Operation(summary = "Applications sent a while ago with no reply")
    public ResponseEntity<List<ApplicationDto>> followUp(Authentication authentication) {
        return ResponseEntity.ok(applicationService.awaitingFollowUp(userId(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one application with its timeline")
    public ResponseEntity<ApplicationDto> get(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.get(userId(authentication), id));
    }

    @PostMapping("/prepare/{jobId}")
    @Operation(summary = "Prepare an application for an opportunity")
    public ResponseEntity<ApplicationDto> prepare(Authentication authentication, @PathVariable UUID jobId) {
        return ResponseEntity.ok(applicationService.prepare(userId(authentication), jobId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Save edits to the prepared email")
    public ResponseEntity<ApplicationDto> update(Authentication authentication,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody UpdateApplicationRequest request) {
        return ResponseEntity.ok(applicationService.update(userId(authentication), id, request));
    }

    @PostMapping("/{id}/sent")
    @Operation(summary = "Record that the user sent this application")
    public ResponseEntity<ApplicationDto> markSent(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.markSent(userId(authentication), id));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Move an application along its timeline")
    public ResponseEntity<ApplicationDto> changeStatus(Authentication authentication,
                                                       @PathVariable UUID id,
                                                       @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(applicationService.changeStatus(
                userId(authentication), id, request.getStatus(), request.getNote()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an application")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        applicationService.delete(userId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return ((AppUserPrincipal) authentication.getPrincipal()).getUser().getId();
    }
}

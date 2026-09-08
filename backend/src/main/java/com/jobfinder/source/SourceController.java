package com.jobfinder.source;

import com.jobfinder.auth.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
@Tag(name = "Job sources", description = "Where opportunities are collected from")
public class SourceController {

    private final JobIngestionService ingestionService;

    @GetMapping
    @Operation(summary = "List the configured sources and their state")
    public ResponseEntity<List<SourceStatusDto>> sources() {
        return ResponseEntity.ok(ingestionService.statuses());
    }

    @PostMapping("/sync")
    @Operation(summary = "Collect new opportunities from every enabled source")
    public ResponseEntity<SyncResultDto> sync(Authentication authentication) {
        UUID userId = ((AppUserPrincipal) authentication.getPrincipal()).getUser().getId();
        return ResponseEntity.ok(ingestionService.sync(userId));
    }
}

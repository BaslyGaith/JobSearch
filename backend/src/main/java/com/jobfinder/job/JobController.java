package com.jobfinder.job;

import com.jobfinder.auth.CustomOAuth2User;
import com.jobfinder.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job opportunity management")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "List job opportunities with filters")
    public ResponseEntity<PagedResponse<JobOpportunityDto>> getJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minMatchScore,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        PagedResponse<JobOpportunityDto> response = jobService.getJobs(
                search, location, company, employmentType, status,
                minMatchScore, remote, sortBy, page, size, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get job opportunity statistics")
    public ResponseEntity<JobStatsDto> getStats() {
        return ResponseEntity.ok(jobService.getStats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job opportunity by ID")
    public ResponseEntity<JobOpportunityDto> getJobById(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(jobService.getJobById(id, userId));
    }

    @PostMapping("/{id}/save")
    @Operation(summary = "Save a job opportunity")
    public ResponseEntity<JobOpportunityDto> saveJob(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(jobService.saveJob(id, userId));
    }

    @DeleteMapping("/{id}/save")
    @Operation(summary = "Unsave a job opportunity")
    public ResponseEntity<Void> unsaveJob(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = getUserId(authentication);
        jobService.unsaveJob(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update job opportunity status")
    public ResponseEntity<JobOpportunityDto> updateJobStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobStatusRequest request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(jobService.updateJobStatus(id, request.getStatus(), userId));
    }

    private UUID getUserId(Authentication authentication) {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        return principal.getUser().getId();
    }
}

package com.jobfinder.today;

import com.jobfinder.auth.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/today")
@RequiredArgsConstructor
@Tag(name = "Today", description = "What needs the user's attention right now")
public class TodayController {

    private final TodayService todayService;

    @GetMapping
    @Operation(summary = "Get today's summary")
    public ResponseEntity<TodayDto> today(Authentication authentication) {
        UUID userId = ((AppUserPrincipal) authentication.getPrincipal()).getUser().getId();
        return ResponseEntity.ok(todayService.forUser(userId));
    }
}

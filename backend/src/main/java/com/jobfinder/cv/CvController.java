package com.jobfinder.cv;

import com.jobfinder.auth.AppUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@Tag(name = "CV Agent", description = "Tailored CV generation from a verified fact bank")
public class CvController {

    private final CvService cvService;
    private final CvDocxExporter docxExporter;

    @GetMapping("/profile")
    @Operation(summary = "Get the fact bank the CV agent writes from")
    public ResponseEntity<CvProfile> getProfile(Authentication authentication) {
        return ResponseEntity.ok(cvService.getOrCreateProfile(userId(authentication)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Replace the fact bank")
    public ResponseEntity<CvProfile> updateProfile(Authentication authentication,
                                                   @Valid @RequestBody UpdateFactBankRequest request) {
        return ResponseEntity.ok(cvService.updateFactBank(userId(authentication), request.getFactBank()));
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate the English and French CVs for a posting")
    public ResponseEntity<CvGenerationResponse> generate(Authentication authentication,
                                                         @Valid @RequestBody GenerateCvRequest request) {
        return ResponseEntity.ok(cvService.generate(
                userId(authentication), request.getPosting(), request.getJobOpportunityId()));
    }

    @GetMapping
    @Operation(summary = "List generated CVs, newest first")
    public ResponseEntity<List<CvDocumentDto>> list(Authentication authentication) {
        return ResponseEntity.ok(cvService.list(userId(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one generated CV")
    public ResponseEntity<CvDocumentDto> get(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(cvService.get(userId(authentication), id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a generated CV")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        cvService.delete(userId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a generated CV as .docx")
    public ResponseEntity<ByteArrayResource> download(Authentication authentication,
                                                      @PathVariable UUID id) throws IOException {
        UUID userId = userId(authentication);
        CvDocumentDto document = cvService.get(userId, id);
        CvProfile profile = cvService.getOrCreateProfile(userId);

        byte[] bytes = docxExporter.export(document, profile);
        String filename = fileName(profile.getFullName(), document);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''"
                        + java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"))
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(new ByteArrayResource(bytes));
    }

    /** No underscores in file names - spaces only, per the CV brief. */
    private String fileName(String fullName, CvDocumentDto document) {
        String role = document.getTargetRole() == null ? "CV" : document.getTargetRole();
        String raw = fullName + " - " + role + " - " + document.getLanguage().toUpperCase() + ".docx";
        return raw.replace("_", " ").replaceAll("[\\\\/:*?\"<>|]", " ").replaceAll("\\s+", " ").trim();
    }

    private UUID userId(Authentication authentication) {
        return ((AppUserPrincipal) authentication.getPrincipal()).getUser().getId();
    }
}

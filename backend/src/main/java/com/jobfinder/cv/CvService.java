package com.jobfinder.cv;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.user.User;
import com.jobfinder.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvService {

    private static final List<String> LANGUAGES = List.of("en", "fr");

    private final CvProfileRepository profileRepository;
    private final CvDocumentRepository documentRepository;
    private final JobOpportunityRepository jobRepository;
    private final UserRepository userRepository;
    private final PostingAnalyzer analyzer;
    private final CvGuardrails guardrails;
    private final List<CvGeneratorAgent> agents;
    private final ObjectMapper objectMapper;

    private String defaultFactBankJson;

    @PostConstruct
    void loadDefaultFactBank() throws IOException {
        try (InputStream in = new ClassPathResource("cv/default-fact-bank.json").getInputStream()) {
            defaultFactBankJson = new String(in.readAllBytes());
        }
    }

    @Transactional
    public CvProfile getOrCreateProfile(UUID userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            FactBank seed = parseFactBank(defaultFactBankJson);
            CvProfile profile = CvProfile.builder()
                    .user(user)
                    .fullName(seed.getIdentity().getFullName())
                    .email(seed.getIdentity().getEmail())
                    .phone(seed.getIdentity().getPhone())
                    .linkedinUrl(seed.getIdentity().getLinkedin())
                    .location(seed.getIdentity().getLocation())
                    .yearsExperience(seed.getIdentity().getYearsExperience())
                    .factBank(defaultFactBankJson)
                    .build();
            log.info("Seeding CV fact bank for user {}", userId);
            return profileRepository.save(profile);
        });
    }

    @Transactional
    public CvProfile updateFactBank(UUID userId, String factBankJson) {
        FactBank parsed = parseFactBank(factBankJson);
        CvProfile profile = getOrCreateProfile(userId);
        profile.setFactBank(factBankJson);
        profile.setFullName(parsed.getIdentity().getFullName());
        profile.setEmail(parsed.getIdentity().getEmail());
        profile.setPhone(parsed.getIdentity().getPhone());
        profile.setLinkedinUrl(parsed.getIdentity().getLinkedin());
        profile.setLocation(parsed.getIdentity().getLocation());
        profile.setYearsExperience(parsed.getIdentity().getYearsExperience());
        return profileRepository.save(profile);
    }

    /**
     * Produces both language versions of a CV for one posting, as the brief
     * requires, and stores each with the gap report that goes with it.
     */
    @Transactional
    public CvGenerationResponse generate(UUID userId, String posting, UUID jobOpportunityId) {
        if (posting == null || posting.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A job posting is required");
        }

        CvProfile profile = getOrCreateProfile(userId);
        FactBank factBank = parseFactBank(profile.getFactBank());
        PostingAnalysis analysis = analyzer.analyse(posting, factBank);

        JobOpportunity opportunity = jobOpportunityId == null ? null
                : jobRepository.findById(jobOpportunityId).orElse(null);

        UUID generationId = UUID.randomUUID();
        List<CvDocumentDto> produced = new ArrayList<>();
        for (String language : LANGUAGES) {
            GeneratedDraft generated = generateWithFallback(factBank, analysis, posting, language);
            CvDraft draft = generated.draft();
            CvDocument saved = documentRepository.save(CvDocument.builder()
                    .user(profile.getUser())
                    .jobOpportunity(opportunity)
                    .generationId(generationId)
                    .title(analysis.getTargetRole())
                    .language(language)
                    .targetRole(analysis.getTargetRole())
                    .targetCompany(analysis.getTargetCompany())
                    .jobFamily(analysis.getJobFamily())
                    .accentColor(analysis.getAccentColor())
                    .postingText(posting)
                    .content(write(draft))
                    .gaps(write(analysis.gaps()))
                    .generatedBy(generated.agentName())
                    .build());
            produced.add(toDto(saved, draft, analysis.gaps()));
        }

        return CvGenerationResponse.builder()
                .analysis(analysis)
                .documents(produced)
                .build();
    }

    /** A draft together with the agent that produced it. */
    private record GeneratedDraft(CvDraft draft, String agentName) {
    }

    /**
     * Tries each agent in order and keeps the first draft that survives the
     * guardrails. A model that invents something loses its turn rather than
     * having its output quietly patched.
     */
    private GeneratedDraft generateWithFallback(FactBank factBank, PostingAnalysis analysis,
                                                String posting, String language) {
        List<String> failures = new ArrayList<>();
        for (CvGeneratorAgent agent : agents) {
            if (!agent.isAvailable()) {
                continue;
            }
            try {
                CvDraft draft = agent.generate(factBank, analysis, posting, language);
                guardrails.verify(draft, factBank);
                return new GeneratedDraft(draft, agent.name());
            } catch (CvGuardrails.ViolationException e) {
                log.warn("Agent {} produced an unusable {} CV: {}", agent.name(), language, e.getViolations());
                failures.add(agent.name() + ": " + String.join("; ", e.getViolations()));
            } catch (Exception e) {
                log.warn("Agent {} failed for {}: {}", agent.name(), language, e.getMessage());
                failures.add(agent.name() + ": " + e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No CV agent produced a compliant draft. " + String.join(" | ", failures));
    }

    @Transactional(readOnly = true)
    public List<CvDocumentDto> list(UUID userId) {
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(doc -> toDto(doc, readDraft(doc), readGaps(doc)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CvDocumentDto get(UUID userId, UUID id) {
        CvDocument doc = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
        return toDto(doc, readDraft(doc), readGaps(doc));
    }

    /** Renames every language version of one CV at once. */
    @Transactional
    public void rename(UUID userId, UUID generationId, String title) {
        List<CvDocument> documents = documentRepository.findByUserIdAndGenerationId(userId, generationId);
        if (documents.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found");
        }
        documents.forEach(doc -> doc.setTitle(title));
        documentRepository.saveAll(documents);
    }

    /** Deletes a CV and every language version of it. */
    @Transactional
    public void deleteGeneration(UUID userId, UUID generationId) {
        List<CvDocument> documents = documentRepository.findByUserIdAndGenerationId(userId, generationId);
        if (documents.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found");
        }
        documentRepository.deleteAll(documents);
    }

    /** The stored CV entity, for callers that need to link to it. */
    @Transactional(readOnly = true)
    public CvDocument findDocument(UUID userId, UUID id) {
        return documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        CvDocument doc = documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
        documentRepository.delete(doc);
    }

    public FactBank parseFactBank(String json) {
        try {
            return objectMapper.readValue(json, FactBank.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fact bank: " + e.getMessage());
        }
    }

    private CvDraft readDraft(CvDocument doc) {
        try {
            return objectMapper.readValue(doc.getContent(), CvDraft.class);
        } catch (Exception e) {
            throw new IllegalStateException("Stored CV could not be read: " + e.getMessage(), e);
        }
    }

    private List<PostingAnalysis.Requirement> readGaps(CvDocument doc) {
        if (doc.getGaps() == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(doc.getGaps(), new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialise CV content", e);
        }
    }

    private CvDocumentDto toDto(CvDocument doc, CvDraft draft, List<PostingAnalysis.Requirement> gaps) {
        return CvDocumentDto.builder()
                .id(doc.getId())
                .generationId(doc.getGenerationId())
                .title(doc.getTitle())
                .language(doc.getLanguage())
                .targetRole(doc.getTargetRole())
                .targetCompany(doc.getTargetCompany())
                .jobFamily(doc.getJobFamily())
                .accentColor(doc.getAccentColor())
                .generatedBy(doc.getGeneratedBy())
                .createdAt(doc.getCreatedAt())
                .draft(draft)
                .gaps(gaps)
                .build();
    }
}

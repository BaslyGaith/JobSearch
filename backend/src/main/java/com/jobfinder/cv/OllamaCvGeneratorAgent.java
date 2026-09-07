package com.jobfinder.cv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Runs the CV writer on a local Ollama model. The prompt carries the fact bank
 * and the analysis, and forbids anything outside them; whatever comes back
 * still passes {@link CvGuardrails} before it can be stored.
 */
@Slf4j
@Component
@Order(10)
public class OllamaCvGeneratorAgent implements CvGeneratorAgent {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final boolean enabled;

    public OllamaCvGeneratorAgent(ObjectMapper objectMapper,
                                  @Value("${app.cv.ollama.base-url:http://localhost:11434}") String baseUrl,
                                  @Value("${app.cv.ollama.model:llama3}") String model,
                                  @Value("${app.cv.ollama.enabled:true}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.enabled = enabled;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String name() {
        return "ollama:" + model;
    }

    @Override
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }
        try {
            restClient.get().uri("/api/tags").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("Ollama not reachable, falling back to the template agent: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CvDraft generate(FactBank factBank, PostingAnalysis analysis, String posting, String language) {
        try {
            String prompt = buildPrompt(factBank, analysis, posting, language);
            Map<?, ?> response = restClient.post()
                    .uri("/api/generate")
                    .body(Map.of(
                            "model", model,
                            "prompt", prompt,
                            "stream", false,
                            "format", "json",
                            "options", Map.of("temperature", 0.2)))
                    .retrieve()
                    .body(Map.class);

            String json = response == null ? null : String.valueOf(response.get("response"));
            if (json == null || json.isBlank()) {
                throw new IllegalStateException("Ollama returned an empty response");
            }
            CvDraft draft = objectMapper.readValue(json, CvDraft.class);
            draft.setLanguage(language);
            return draft;
        } catch (Exception e) {
            throw new IllegalStateException("Ollama CV generation failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(FactBank factBank, PostingAnalysis analysis, String posting, String language)
            throws Exception {
        String factBankJson = objectMapper.writeValueAsString(factBank);
        String analysisJson = objectMapper.writeValueAsString(analysis);
        String languageName = "fr".equalsIgnoreCase(language) ? "French" : "English";

        return """
                You are a CV writer. You rewrite verified facts to fit a job posting.

                ABSOLUTE RULE - NEVER INVENT. You may reorganise, reframe and re-emphasise the facts
                below. You may never add a technology, tool, employer, certification, metric or
                achievement that is not in the fact bank. If the posting needs something absent from
                the fact bank, leave it out of the CV entirely.

                Hard constraints:
                - Never use the word "Senior" in any job title or tagline.
                - The only figures allowed anywhere are: %s. Invent no other number, percentage or amount.
                - Spring Boot and Angular may appear ONLY under the SIMEG project, never under a Vermeg role.
                - Never mention any of these: %s
                - Write in %s. Keep established technical terms in English (workspaces, Lakehouse,
                  query folding, DAX) and keep job titles in English even in the French version.
                - Concrete verbs. No filler adjectives, no "passionate about", no "results-driven".
                  Every bullet says what was done and in what context.

                FACT BANK (the only permitted source of truth):
                %s

                POSTING ANALYSIS (what this job actually tests):
                %s

                JOB POSTING:
                %s

                Return ONLY a JSON object with these fields:
                {
                  "tagline": string matching the target job title,
                  "profile": one paragraph of 4 to 6 sentences written for this specific job,
                  "experiences": [{"title","company","companyNote","location","dates","bullets":[string]}],
                  "whatIBring": [{"heading","body"}]  // 3 or 4 blocks mapped onto the posting's stated needs,
                  "coreSkills": [{"label","values"}]  // values separated by " · ",
                  "selectedProject": {"name","organisation","dates","bullets":[string]} or null,
                  "certifications": [string],
                  "education": [{"qualification","institution","dates"}],
                  "footer": string
                }
                Experiences stay in reverse chronological order with the same titles, companies and
                dates as the fact bank. No prose outside the JSON.
                """.formatted(
                String.join(", ", factBank.getConstraints().getAllowedFigures()),
                String.join(", ", factBank.getBlocklist()),
                languageName,
                factBankJson,
                analysisJson,
                posting == null ? "" : posting.substring(0, Math.min(posting.length(), 6000)));
    }
}

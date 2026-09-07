package com.jobfinder.cv;

/**
 * Writes one tailored CV in one language. Implementations may only reorganise,
 * reframe and re-emphasise what the fact bank already contains.
 *
 * @see TemplateCvGeneratorAgent deterministic assembly, always available
 * @see OllamaCvGeneratorAgent local model, used when Ollama is reachable
 */
public interface CvGeneratorAgent {

    /** Identifier stored alongside every generated CV, so output is traceable. */
    String name();

    /** Whether this agent can serve a request right now. */
    boolean isAvailable();

    CvDraft generate(FactBank factBank, PostingAnalysis analysis, String posting, String language);
}

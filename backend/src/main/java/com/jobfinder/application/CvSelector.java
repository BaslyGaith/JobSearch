package com.jobfinder.application;

import com.jobfinder.cv.CvDocument;
import com.jobfinder.cv.CvDocumentRepository;
import com.jobfinder.job.JobOpportunity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Picks which of the user's existing CVs fits an opportunity best. Chooses only
 * among CVs already generated - it never writes one, so nothing unverified can
 * enter through this path.
 */
@Component
@RequiredArgsConstructor
public class CvSelector {

    private final CvDocumentRepository cvDocumentRepository;

    /**
     * @return the closest CV in the language the posting is written in, or empty
     *         when the user has not generated any CV yet.
     */
    public Optional<CvDocument> selectFor(UUID userId, JobOpportunity job) {
        List<CvDocument> candidates = cvDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        String language = guessLanguage(job);

        return candidates.stream()
                .filter(cv -> language.equalsIgnoreCase(cv.getLanguage()))
                .max(Comparator.comparingInt(cv -> similarity(cv, job)))
                .or(() -> candidates.stream().max(Comparator.comparingInt(cv -> similarity(cv, job))));
    }

    /** How closely a CV's target role matches the opportunity's title. */
    private int similarity(CvDocument cv, JobOpportunity job) {
        String cvRole = lower(cv.getTargetRole());
        String jobTitle = lower(job.getTitle());
        if (cvRole.isBlank() || jobTitle.isBlank()) {
            return 0;
        }
        if (cvRole.equals(jobTitle)) {
            return 100;
        }
        if (jobTitle.contains(cvRole) || cvRole.contains(jobTitle)) {
            return 80;
        }
        // Otherwise count the meaningful words the two titles share.
        return (int) List.of(cvRole.split("\\W+")).stream()
                .filter(word -> word.length() > 3)
                .filter(jobTitle::contains)
                .count() * 15;
    }

    /**
     * French postings get the French CV. Deliberately crude: it looks for words
     * that only appear in French job ads, and defaults to English otherwise.
     */
    private String guessLanguage(JobOpportunity job) {
        String text = lower(job.getTitle()) + " " + lower(job.getDescription()) + " " + lower(job.getLocation());
        List<String> frenchMarkers = List.of(
                " nous ", " vous ", " votre ", " recherche ", " poste ", " entreprise ",
                " compétences ", " expérience ", "développeur", "ingénieur", "france", "paris");
        long hits = frenchMarkers.stream().filter(text::contains).count();
        return hits >= 2 ? "fr" : "en";
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}

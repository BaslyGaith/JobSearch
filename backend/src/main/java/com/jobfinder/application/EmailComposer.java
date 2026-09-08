package com.jobfinder.application;

import com.jobfinder.cv.CvDocument;
import com.jobfinder.cv.CvProfile;
import com.jobfinder.job.JobOpportunity;
import org.springframework.stereotype.Component;

/**
 * Writes the application email from facts that already exist: the posting, the
 * user's profile, and the CV chosen for it. It states nothing about the user
 * that is not in their career profile, and never claims a skill.
 */
@Component
public class EmailComposer {

    public String subject(JobOpportunity job, CvProfile profile) {
        return "Application - " + job.getTitle() + " - " + profile.getFullName();
    }

    public String body(JobOpportunity job, CvProfile profile, CvDocument cv) {
        boolean french = cv != null && "fr".equalsIgnoreCase(cv.getLanguage());
        return french ? frenchBody(job, profile) : englishBody(job, profile);
    }

    private String englishBody(JobOpportunity job, CvProfile profile) {
        String company = job.getCompanyName() == null ? "your team" : job.getCompanyName();
        String years = profile.getYearsExperience() == null ? "" :
                " I have " + profile.getYearsExperience() + " years of experience in this field.";

        return """
                %s

                I am writing about the %s position at %s.%s My CV is attached.

                %s

                I would be glad to talk it through at your convenience.

                Kind regards,
                %s
                %s
                %s""".formatted(
                greeting(job, false),
                job.getTitle(),
                company,
                years,
                relevanceLine(profile, false),
                profile.getFullName(),
                nullToEmpty(profile.getEmail()),
                nullToEmpty(profile.getPhone())).trim();
    }

    private String frenchBody(JobOpportunity job, CvProfile profile) {
        String company = job.getCompanyName() == null ? "votre équipe" : job.getCompanyName();
        String years = profile.getYearsExperience() == null ? "" :
                " Je dispose de " + profile.getYearsExperience() + " ans d'expérience dans ce domaine.";

        return """
                %s

                Je vous écris au sujet du poste de %s chez %s.%s Vous trouverez mon CV en pièce jointe.

                %s

                Je reste à votre disposition pour en échanger.

                Cordialement,
                %s
                %s
                %s""".formatted(
                greeting(job, true),
                job.getTitle(),
                company,
                years,
                relevanceLine(profile, true),
                profile.getFullName(),
                nullToEmpty(profile.getEmail()),
                nullToEmpty(profile.getPhone())).trim();
    }

    private String greeting(JobOpportunity job, boolean french) {
        String name = job.getRecruiterName();
        if (name == null || name.isBlank()) {
            return french ? "Bonjour," : "Hello,";
        }
        return french ? "Bonjour " + name + "," : "Hello " + name + ",";
    }

    /** Points at the attached CV rather than asserting anything new about the user. */
    private String relevanceLine(CvProfile profile, boolean french) {
        if (french) {
            return "Le CV joint détaille mon parcours et les compétences correspondant à ce poste.";
        }
        return "The attached CV sets out my background and how it lines up with what the role asks for.";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

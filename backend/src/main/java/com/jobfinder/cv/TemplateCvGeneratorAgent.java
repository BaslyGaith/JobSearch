package com.jobfinder.cv;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Assembles a CV from the fact bank without a model: bullets are selected and
 * ordered by how well their tags match the posting, and the prose sections come
 * from templates. Always available, so a CV can be produced with Ollama down.
 */
@Component
@Order(100)
@RequiredArgsConstructor
public class TemplateCvGeneratorAgent implements CvGeneratorAgent {

    private static final DateTimeFormatter EN_MONTH = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter FR_MONTH = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

    private static final int MAX_BULLETS_RECENT = 6;
    private static final int MAX_BULLETS_OLDER = 5;

    @Override
    public String name() {
        return "template";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public CvDraft generate(FactBank factBank, PostingAnalysis analysis, String posting, String language) {
        boolean french = "fr".equalsIgnoreCase(language);

        List<CvDraft.ExperienceBlock> experiences = new ArrayList<>();
        for (int i = 0; i < factBank.getExperiences().size(); i++) {
            FactBank.Experience source = factBank.getExperiences().get(i);
            experiences.add(CvDraft.ExperienceBlock.builder()
                    .title(source.getTitle())
                    .company(source.getCompany())
                    .companyNote(source.getCompanyNote())
                    .location(source.getLocation())
                    .dates(formatRange(source.getStartDate(), source.getEndDate(), french))
                    .bullets(selectBullets(source, analysis.getTags(),
                            i == 0 ? MAX_BULLETS_RECENT : MAX_BULLETS_OLDER))
                    .build());
        }

        return CvDraft.builder()
                .language(french ? "fr" : "en")
                .tagline(analysis.getTargetRole())
                .profile(profile(factBank, analysis, french))
                .experiences(experiences)
                .whatIBring(whatIBring(analysis, french))
                .coreSkills(coreSkills(factBank, analysis))
                .selectedProject(selectedProject(factBank, analysis, french))
                .certifications(factBank.getCertifications())
                .education(education(factBank))
                .footer(french
                        ? "Disponible sur Tunis et en remote. Références disponibles sur demande."
                        : "Available in Tunis and remotely. References available on request.")
                .build();
    }

    /**
     * Bullets whose tags overlap the posting first, the rest in original order,
     * so a CV never drops to a thin list when the posting is unusual.
     */
    private List<String> selectBullets(FactBank.Experience experience, List<String> tags, int limit) {
        List<FactBank.Bullet> bullets = new ArrayList<>(experience.getBullets());
        bullets.sort((a, b) -> Integer.compare(score(b, tags), score(a, tags)));
        return bullets.stream()
                .limit(limit)
                .map(FactBank.Bullet::getText)
                .toList();
    }

    private int score(FactBank.Bullet bullet, List<String> tags) {
        int score = 0;
        for (int i = 0; i < tags.size(); i++) {
            if (bullet.getTags().contains(tags.get(i))) {
                score += Math.max(1, tags.size() - i);
            }
        }
        return score;
    }

    private String profile(FactBank factBank, PostingAnalysis analysis, boolean french) {
        String years = String.valueOf(factBank.getIdentity().getYearsExperience());
        String role = analysis.getTargetRole();
        if (french) {
            return "Analyst Developer chez Vermeg, éditeur de logiciels bancaires et assurantiels, avec "
                   + years + " ans d'expérience entre analyse métier, modélisation de données et "
                   + "conception des habilitations. Je recueille les besoins avec les équipes produit et "
                   + "opérations, puis je construis la solution correspondante : modèle de données, couche "
                   + "sémantique, rapports, rôles de sécurité. J'administre Power BI Service et la couche "
                   + "données sur Microsoft Fabric, et je définis quels périmètres sont accessibles à quelle "
                   + "population. Certifié DP-600 et DP-700. Ce profil est orienté vers le poste de "
                   + role + ".";
        }
        return "Analyst Developer at Vermeg, a banking and insurance software vendor, with " + years
               + " years spanning business analysis, data modelling and authorization design. I gather "
               + "requirements with product and operations teams, then build the solution behind them: "
               + "data model, semantic layer, reports, security roles. I administer Power BI Service and "
               + "the data layer on Microsoft Fabric, and I own which population reaches which data "
               + "perimeter. Microsoft DP-600 and DP-700 certified. This CV is written for the "
               + role + " role.";
    }

    private List<CvDraft.ValueBlock> whatIBring(PostingAnalysis analysis, boolean french) {
        Set<String> tags = new LinkedHashSet<>(analysis.getTags());
        List<CvDraft.ValueBlock> blocks = new ArrayList<>();

        if (tags.contains("bi") || tags.contains("fabric") || tags.contains("data-modelling")) {
            blocks.add(block(french ? "Du besoin métier au modèle livré" : "From business need to delivered model",
                    french
                            ? "Je pars de l'atelier de cadrage et je vais jusqu'au rapport publié : modèle en étoile, "
                              + "mesures DAX, transformations Power Query, puis publication dans des workspaces "
                              + "gouvernés. Le réglage des performances passe par le query folding, les tables "
                              + "d'agrégation et le rafraîchissement incrémental."
                            : "I start at the framing workshop and finish at the published report: star schema, DAX "
                              + "measures, Power Query transformations, then publication into governed workspaces. "
                              + "Performance work goes through query folding, aggregation tables and incremental refresh."));
        }
        if (tags.contains("iam") || tags.contains("security") || tags.contains("authorization")) {
            blocks.add(block(french ? "Habilitations pensées avant la livraison" : "Authorization designed before release",
                    french
                            ? "Je définis quelle population accède à quel périmètre, je l'implémente en rôles de "
                              + "sécurité - RLS statique et dynamique, sécurité au niveau objet - et je valide avant "
                              + "mise en production. La ségrégation entre périmètres clients est une exigence "
                              + "contractuelle dans la banque et l'assurance."
                            : "I define which population reaches which perimeter, implement it as security roles - "
                              + "static and dynamic row-level security, object-level security - and validate before "
                              + "release. Segregating client perimeters is a contractual requirement in banking and insurance."));
        }
        if (tags.contains("java") || tags.contains("backend") || tags.contains("build")) {
            blocks.add(block(french ? "Backend Java en contexte progiciel" : "Java backend in a product context",
                    french
                            ? "Développement Java 8+ sur le framework Palmyra : définition de modèles PML, génération "
                              + "xmlToJava, automatisation de plugins Maven. Côté build : Maven et Tycho, bundles OSGi, "
                              + "packaging de plugins Eclipse, dépôts p2, diagnostic des pipelines GitLab CI."
                            : "Java 8+ development on the Palmyra framework: PML model definition, xmlToJava generation, "
                              + "Maven plugin automation. On the build side: Maven and Tycho, OSGi bundles, Eclipse plugin "
                              + "packaging, p2 repositories, GitLab CI pipeline diagnostics."));
        }
        if (tags.contains("etl") || tags.contains("integration")) {
            blocks.add(block(french ? "Intégration et qualité des données" : "Integration and data quality",
                    french
                            ? "Conception et maintenance de pipelines ETL, intégration de sources ERP et CRM via API REST "
                              + "dont HubSpot, contrôles de qualité qui réduisent les incohérences récurrentes."
                            : "ETL pipeline design and maintenance, ERP and CRM integration over REST APIs including "
                              + "HubSpot, and data quality controls that cut recurring inconsistencies."));
        }
        if (blocks.size() < 3) {
            blocks.add(block(french ? "Interlocuteur du métier" : "A counterpart the business can use",
                    french
                            ? "Ateliers de recueil, spécifications fonctionnelles, définition des KPI, recette "
                              + "utilisateur, puis support de niveau 1 via Jira jusqu'à la clôture. Je travaille en "
                              + "français, anglais et arabe."
                            : "Requirements workshops, functional specifications, KPI definitions and user acceptance "
                              + "testing, then first-level support through Jira to closure. I work in French, English "
                              + "and Arabic."));
        }
        return blocks.subList(0, Math.min(4, blocks.size()));
    }

    private CvDraft.ValueBlock block(String heading, String body) {
        return CvDraft.ValueBlock.builder().heading(heading).body(body).build();
    }

    /** Skill groups whose tags match the posting, most relevant first. */
    private List<CvDraft.SkillLine> coreSkills(FactBank factBank, PostingAnalysis analysis) {
        List<String> tags = analysis.getTags();
        List<Map.Entry<String, List<String>>> groups = new ArrayList<>(factBank.getSkills().entrySet());

        groups.sort((a, b) -> Integer.compare(
                groupScore(factBank, b.getKey(), tags), groupScore(factBank, a.getKey(), tags)));

        List<CvDraft.SkillLine> lines = new ArrayList<>();
        for (Map.Entry<String, List<String>> group : groups) {
            if (groupScore(factBank, group.getKey(), tags) == 0 && lines.size() >= 6) {
                continue;
            }
            lines.add(CvDraft.SkillLine.builder()
                    .label(group.getKey())
                    .values(String.join(" · ", group.getValue()))
                    .build());
            if (lines.size() >= 8) {
                break;
            }
        }
        return lines;
    }

    private int groupScore(FactBank factBank, String group, List<String> tags) {
        List<String> groupTags = factBank.getSkillTags().getOrDefault(group, List.of());
        int score = 0;
        for (int i = 0; i < tags.size(); i++) {
            if (groupTags.contains(tags.get(i))) {
                score += Math.max(1, tags.size() - i);
            }
        }
        return score;
    }

    /** SIMEG appears only when the posting is close enough to justify it. */
    private CvDraft.ProjectBlock selectedProject(FactBank factBank, PostingAnalysis analysis, boolean french) {
        if (factBank.getProjects().isEmpty()) {
            return null;
        }
        FactBank.Project project = factBank.getProjects().get(0);
        boolean relevant = project.getTags().stream().anyMatch(analysis.getTags()::contains);
        if (!relevant) {
            return null;
        }
        return CvDraft.ProjectBlock.builder()
                .name(project.getName())
                .organisation(project.getOrganisation())
                .dates(project.getDates())
                .bullets(project.getBullets())
                .build();
    }

    private List<CvDraft.EducationLine> education(FactBank factBank) {
        return factBank.getEducation().stream()
                .map(e -> CvDraft.EducationLine.builder()
                        .qualification(e.getQualification())
                        .institution(e.getInstitution())
                        .dates(e.getDates())
                        .build())
                .toList();
    }

    private String formatRange(String start, String end, boolean french) {
        DateTimeFormatter formatter = french ? FR_MONTH : EN_MONTH;
        String from = start == null ? "" : YearMonth.parse(start).format(formatter);
        String to = end == null
                ? (french ? "Aujourd'hui" : "Present")
                : YearMonth.parse(end).format(formatter);
        return from + " - " + to;
    }
}

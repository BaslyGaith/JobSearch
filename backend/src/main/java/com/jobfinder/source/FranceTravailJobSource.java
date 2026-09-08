package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * France Travail (ex Pôle emploi) "Offres d'emploi v2", their official public
 * API. Needs a client id and secret from the France Travail developer portal;
 * without them the source reports itself disabled and is skipped.
 *
 * @see <a href="https://francetravail.io/produits-partages/catalogue">francetravail.io</a>
 */
@Slf4j
@Component
public class FranceTravailJobSource implements JobSource {

    private static final String AUTH_URL = "https://entreprise.francetravail.fr";
    private static final String API_URL = "https://api.francetravail.io";
    private static final String SCOPE = "api_offresdemploiv2 o2dsoffre";

    private final RestClient authClient;
    private final RestClient apiClient;
    private final String clientId;
    private final String clientSecret;

    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public FranceTravailJobSource(@Value("${app.sources.france-travail.client-id:}") String clientId,
                                  @Value("${app.sources.france-travail.client-secret:}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authClient = RestClient.builder().baseUrl(AUTH_URL).build();
        this.apiClient = RestClient.builder().baseUrl(API_URL).build();
    }

    @Override
    public String name() {
        return "France Travail";
    }

    @Override
    public boolean isEnabled() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RawJobPosting> fetch(JobSearchCriteria criteria) {
        String keywords = String.join(" ", criteria.getJobTitles() == null ? List.of() : criteria.getJobTitles());
        int range = Math.max(0, Math.min(criteria.getMaxResults(), 150)) - 1;

        Map<String, Object> response = apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/partenaire/offresdemploi/v2/offres/search")
                        .queryParam("motsCles", keywords.isBlank() ? null : keywords)
                        .queryParam("range", "0-" + Math.max(range, 0))
                        .build())
                .header("Authorization", "Bearer " + token())
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> offers = response == null ? List.of()
                : (List<Map<String, Object>>) response.getOrDefault("resultats", List.of());

        return offers.stream().map(this::toPosting).toList();
    }

    @SuppressWarnings("unchecked")
    private RawJobPosting toPosting(Map<String, Object> offer) {
        Map<String, Object> company = (Map<String, Object>) offer.get("entreprise");
        Map<String, Object> place = (Map<String, Object>) offer.get("lieuTravail");
        Map<String, Object> contact = (Map<String, Object>) offer.get("contact");

        return RawJobPosting.builder()
                .externalId(asString(offer.get("id")))
                .source(name())
                .title(asString(offer.get("intitule")))
                .companyName(company == null ? null : asString(company.get("nom")))
                .location(place == null ? null : asString(place.get("libelle")))
                .employmentType(normaliseContract(asString(offer.get("typeContrat"))))
                .description(truncate(asString(offer.get("description"))))
                .jobUrl(asString(offer.get("origineOffre") instanceof Map<?, ?> origin
                        ? ((Map<String, Object>) origin).get("urlOrigine") : null))
                .publicationDate(parseDate(offer.get("dateCreation")))
                // Contact details only when the employer published them for applications.
                .recruiterName(contact == null ? null : asString(contact.get("nom")))
                .recruiterEmail(contact == null ? null : asString(contact.get("courriel")))
                .build();
    }

    /** Client-credentials token, reused until shortly before it expires. */
    private String token() {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", SCOPE);

        Map<?, ?> response = authClient.post()
                .uri("/connexion/oauth2/access_token?realm=%2Fpartenaire")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("France Travail did not return an access token");
        }
        accessToken = String.valueOf(response.get("access_token"));
        long expiresIn = response.get("expires_in") instanceof Number n ? n.longValue() : 1200L;
        tokenExpiry = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
        return accessToken;
    }

    private String normaliseContract(String type) {
        if (type == null) {
            return null;
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "CDI" -> "FULL_TIME";
            case "CDD", "MIS" -> "CONTRACT";
            case "SAI" -> "PART_TIME";
            default -> null;
        };
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(String.valueOf(value)).toLocalDate();
        } catch (Exception e) {
            try {
                return Instant.parse(String.valueOf(value)).atZone(ZoneOffset.UTC).toLocalDate();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 8000 ? text.substring(0, 8000) : text;
    }
}

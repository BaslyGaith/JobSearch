package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lever-hosted career pages, through their documented public postings feed.
 *
 * @see <a href="https://github.com/lever/postings-api">Lever postings API</a>
 */
@Slf4j
@Component
public class LeverJobSource implements JobSource {

    private static final String BASE_URL = "https://api.lever.co";

    private final RestClient restClient;
    private final List<String> companies;

    public LeverJobSource(@Value("${app.sources.lever.companies:}") List<String> companies) {
        this.companies = companies == null ? List.of() : companies.stream().filter(c -> !c.isBlank()).toList();
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public String name() {
        return "Lever";
    }

    @Override
    public boolean isEnabled() {
        return !companies.isEmpty();
    }

    @Override
    public List<RawJobPosting> fetch(JobSearchCriteria criteria) {
        List<RawJobPosting> postings = new ArrayList<>();
        for (String company : companies) {
            try {
                postings.addAll(fetchCompany(company, criteria));
            } catch (Exception e) {
                log.warn("Lever feed for {} could not be read: {}", company, e.getMessage());
            }
        }
        return postings;
    }

    @SuppressWarnings("unchecked")
    private List<RawJobPosting> fetchCompany(String company, JobSearchCriteria criteria) {
        List<Map<String, Object>> postings = restClient.get()
                .uri("/v0/postings/{company}?mode=json", company)
                .retrieve()
                .body(List.class);

        if (postings == null) {
            return List.of();
        }
        return postings.stream()
                .filter(posting -> matchesTitles(String.valueOf(posting.get("text")), criteria))
                .map(posting -> toPosting(company, posting))
                .limit(criteria.getMaxResults() > 0 ? criteria.getMaxResults() : Long.MAX_VALUE)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private RawJobPosting toPosting(String company, Map<String, Object> posting) {
        Map<String, Object> categories = (Map<String, Object>) posting.get("categories");
        Object createdAt = posting.get("createdAt");

        return RawJobPosting.builder()
                .externalId(String.valueOf(posting.get("id")))
                .source(name())
                .title(String.valueOf(posting.get("text")))
                .companyName(company)
                .location(categories == null ? null : asString(categories.get("location")))
                .employmentType(categories == null ? null : normaliseCommitment(asString(categories.get("commitment"))))
                .description(truncate(asString(posting.get("descriptionPlain"))))
                .jobUrl(asString(posting.get("hostedUrl")))
                .publicationDate(createdAt instanceof Number millis
                        ? Instant.ofEpochMilli(millis.longValue()).atZone(ZoneOffset.UTC).toLocalDate()
                        : null)
                .build();
    }

    private boolean matchesTitles(String title, JobSearchCriteria criteria) {
        List<String> wanted = criteria.getJobTitles();
        if (wanted == null || wanted.isEmpty()) {
            return true;
        }
        String lower = title.toLowerCase(Locale.ROOT);
        return wanted.stream().anyMatch(w -> lower.contains(w.toLowerCase(Locale.ROOT)));
    }

    private String normaliseCommitment(String commitment) {
        if (commitment == null) {
            return null;
        }
        return switch (commitment.toLowerCase(Locale.ROOT)) {
            case "full-time", "full time" -> "FULL_TIME";
            case "part-time", "part time" -> "PART_TIME";
            case "contract", "contractor", "freelance" -> "CONTRACT";
            case "internship", "intern" -> "INTERNSHIP";
            default -> null;
        };
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

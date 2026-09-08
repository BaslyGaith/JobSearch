package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Greenhouse-hosted career pages. Boards expose a documented public JSON
 * endpoint - no key, no scraping - so each company a user follows is one
 * board token in configuration.
 *
 * @see <a href="https://developers.greenhouse.io/job-board.html">Job Board API</a>
 */
@Slf4j
@Component
public class GreenhouseJobSource implements JobSource {

    private static final String BASE_URL = "https://boards-api.greenhouse.io";

    private final RestClient restClient;
    private final List<String> boards;

    public GreenhouseJobSource(@Value("${app.sources.greenhouse.boards:}") List<String> boards) {
        this.boards = boards == null ? List.of() : boards.stream().filter(b -> !b.isBlank()).toList();
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public String name() {
        return "Greenhouse";
    }

    @Override
    public boolean isEnabled() {
        return !boards.isEmpty();
    }

    @Override
    public List<RawJobPosting> fetch(JobSearchCriteria criteria) {
        List<RawJobPosting> postings = new ArrayList<>();
        for (String board : boards) {
            try {
                postings.addAll(fetchBoard(board, criteria));
            } catch (Exception e) {
                // One unreachable board must not lose the others.
                log.warn("Greenhouse board {} could not be read: {}", board, e.getMessage());
            }
        }
        return postings;
    }

    @SuppressWarnings("unchecked")
    private List<RawJobPosting> fetchBoard(String board, JobSearchCriteria criteria) {
        Map<String, Object> response = restClient.get()
                .uri("/v1/boards/{board}/jobs?content=true", board)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> jobs = response == null ? List.of()
                : (List<Map<String, Object>>) response.getOrDefault("jobs", List.of());

        return jobs.stream()
                .filter(job -> matchesTitles(String.valueOf(job.get("title")), criteria))
                .map(job -> toPosting(board, job))
                .limit(criteria.getMaxResults() > 0 ? criteria.getMaxResults() : Long.MAX_VALUE)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private RawJobPosting toPosting(String board, Map<String, Object> job) {
        Map<String, Object> location = (Map<String, Object>) job.get("location");
        String content = job.get("content") == null ? null : stripHtml(String.valueOf(job.get("content")));

        return RawJobPosting.builder()
                .externalId(String.valueOf(job.get("id")))
                .source(name())
                .title(String.valueOf(job.get("title")))
                .companyName(board)
                .location(location == null ? null : String.valueOf(location.get("name")))
                .description(content)
                .jobUrl(String.valueOf(job.get("absolute_url")))
                .publicationDate(parseDate(job.get("updated_at")))
                .build();
    }

    /** Keeps a posting only when it looks like something the user searches for. */
    private boolean matchesTitles(String title, JobSearchCriteria criteria) {
        List<String> wanted = criteria.getJobTitles();
        if (wanted == null || wanted.isEmpty()) {
            return true;
        }
        String lower = title.toLowerCase(Locale.ROOT);
        return wanted.stream().anyMatch(w -> lower.contains(w.toLowerCase(Locale.ROOT)))
               || wanted.stream()
                       .flatMap(w -> List.of(w.toLowerCase(Locale.ROOT).split("\\s+")).stream())
                       .filter(word -> word.length() > 3)
                       .anyMatch(lower::contains);
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(String.valueOf(value)).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    /** Greenhouse returns HTML-escaped markup; the CV agent only needs the prose. */
    private String stripHtml(String html) {
        String text = html
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&nbsp;", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return text.length() > 8000 ? text.substring(0, 8000) : text;
    }
}

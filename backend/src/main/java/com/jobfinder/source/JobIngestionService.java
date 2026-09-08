package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;
import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.job.JobStatus;
import com.jobfinder.search.JobSearchPreference;
import com.jobfinder.search.JobSearchPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Pulls from every enabled source, drops what is already known, scores the rest
 * against the user's preferences and stores it. A source that fails is reported
 * and skipped - one bad connector never loses the whole run.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobIngestionService {

    private static final int PER_SOURCE_LIMIT = 50;

    private final List<JobSource> sources;
    private final JobOpportunityRepository jobRepository;
    private final JobSearchPreferenceRepository preferenceRepository;
    private final MatchScorer matchScorer;

    @Transactional(readOnly = true)
    public List<SourceStatusDto> statuses() {
        return sources.stream()
                .map(source -> SourceStatusDto.builder()
                        .name(source.name())
                        .enabled(source.isEnabled())
                        .storedOpportunities(jobRepository.countBySource(source.name()))
                        .build())
                .toList();
    }

    @Transactional
    public SyncResultDto sync(UUID userId) {
        JobSearchPreference preference = preferenceRepository.findByUserId(userId).orElse(null);
        JobSearchCriteria criteria = toCriteria(preference);

        int added = 0;
        int duplicates = 0;
        List<String> used = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (JobSource source : sources) {
            if (!source.isEnabled()) {
                continue;
            }
            used.add(source.name());
            try {
                for (RawJobPosting posting : source.fetch(criteria)) {
                    if (isDuplicate(posting)) {
                        duplicates++;
                        continue;
                    }
                    jobRepository.save(toOpportunity(posting, preference));
                    added++;
                }
            } catch (Exception e) {
                log.warn("Source {} failed during sync: {}", source.name(), e.getMessage());
                failed.add(source.name() + ": " + e.getMessage());
            }
        }

        log.info("Sync finished: {} added, {} already known, sources {}", added, duplicates, used);
        return SyncResultDto.builder()
                .added(added)
                .duplicates(duplicates)
                .sourcesUsed(used)
                .failures(failed)
                .build();
    }

    /**
     * The same role often appears on several boards. A posting is already known
     * when its URL matches, or when the same title sits at the same company.
     */
    private boolean isDuplicate(RawJobPosting posting) {
        if (posting.getJobUrl() != null && jobRepository.existsByJobUrl(posting.getJobUrl())) {
            return true;
        }
        return posting.getTitle() != null && posting.getCompanyName() != null
               && jobRepository.existsByTitleIgnoreCaseAndCompanyNameIgnoreCase(
                       posting.getTitle(), posting.getCompanyName());
    }

    private JobOpportunity toOpportunity(RawJobPosting posting, JobSearchPreference preference) {
        return JobOpportunity.builder()
                .title(posting.getTitle())
                .companyName(posting.getCompanyName())
                .location(posting.getLocation())
                .employmentType(posting.getEmploymentType())
                .description(posting.getDescription())
                .jobUrl(posting.getJobUrl())
                .source(posting.getSource())
                .publicationDate(posting.getPublicationDate())
                .recruiterName(posting.getRecruiterName())
                .recruiterEmail(posting.getRecruiterEmail())
                .recruiterProfileUrl(posting.getRecruiterProfileUrl())
                .matchScore(matchScorer.score(posting, preference))
                .status(JobStatus.NEW)
                .build();
    }

    private JobSearchCriteria toCriteria(JobSearchPreference preference) {
        if (preference == null) {
            return JobSearchCriteria.builder().maxResults(PER_SOURCE_LIMIT).build();
        }
        return JobSearchCriteria.builder()
                .jobTitles(preference.getJobTitles())
                .locations(preference.getLocations())
                .employmentTypes(preference.getEmploymentTypes())
                .experienceLevels(preference.getExperienceLevels())
                .remotePreference(preference.getRemotePreference() == null
                        ? null : String.valueOf(preference.getRemotePreference()).toLowerCase(Locale.ROOT))
                .keywords(preference.getKeywords())
                .maxResults(PER_SOURCE_LIMIT)
                .build();
    }
}

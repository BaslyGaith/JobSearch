package com.jobfinder.ai;

import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import com.jobfinder.search.JobSearchPreference;
import com.jobfinder.search.JobSearchPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final JobSearchAgent jobSearchAgent;
    private final JobSearchPreferenceRepository preferenceRepository;
    private final JobOpportunityRepository jobOpportunityRepository;

    /**
     * Trigger a job search for the given user using their saved preferences.
     * Sprint 1: delegates to MockJobSearchAgent.
     * Sprint 2: will call a real AI agent, deduplicate, score, and persist.
     */
    @Transactional
    public List<JobOpportunity> triggerSearch(UUID userId) {
        log.info("Triggering job search for user: {}", userId);

        JobSearchCriteria criteria = preferenceRepository.findByUserId(userId)
                .map(this::toCriteria)
                .orElseGet(() -> JobSearchCriteria.builder().maxResults(15).build());

        if (!jobSearchAgent.isAvailable()) {
            log.warn("Job search agent is not available (type: {})", jobSearchAgent.getAgentType());
            return List.of();
        }

        List<JobOpportunity> results = jobSearchAgent.searchJobs(criteria);
        log.info("Agent returned {} job opportunities", results.size());
        return results;
    }

    private JobSearchCriteria toCriteria(JobSearchPreference pref) {
        return JobSearchCriteria.builder()
                .jobTitles(pref.getJobTitles())
                .locations(pref.getLocations())
                .employmentTypes(pref.getEmploymentTypes())
                .experienceLevels(pref.getExperienceLevels())
                .remotePreference(pref.getRemotePreference())
                .keywords(pref.getKeywords())
                .maxResults(50)
                .build();
    }
}

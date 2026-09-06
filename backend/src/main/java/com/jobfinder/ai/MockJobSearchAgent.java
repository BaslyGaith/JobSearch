package com.jobfinder.ai;

import com.jobfinder.job.JobOpportunity;
import com.jobfinder.job.JobOpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sprint 1 mock implementation of JobSearchAgent.
 * Returns jobs that are already seeded in the database.
 * In Sprint 2, this will be replaced by a real AI-powered agent
 * that searches LinkedIn and other sources via authorized APIs.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class MockJobSearchAgent implements JobSearchAgent {

    private final JobOpportunityRepository jobRepository;

    @Override
    public List<JobOpportunity> searchJobs(JobSearchCriteria criteria) {
        log.info("MockJobSearchAgent.searchJobs() — returning seeded mock opportunities");
        // In Sprint 1, jobs come from the V5 seed migration.
        // The mock agent simply returns all existing opportunities.
        // Sprint 2 will call real APIs and persist new results here.
        int maxResults = criteria.getMaxResults() > 0 ? criteria.getMaxResults() : 15;
        return jobRepository.findAll().stream().limit(maxResults).toList();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getAgentType() {
        return "MOCK";
    }
}

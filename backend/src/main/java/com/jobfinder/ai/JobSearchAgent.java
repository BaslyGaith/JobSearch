package com.jobfinder.ai;

import com.jobfinder.job.JobOpportunity;

import java.util.List;

/**
 * Interface for the AI-powered job search agent.
 * Sprint 1: MockJobSearchAgent provides sample data.
 * Sprint 2: RealJobSearchAgent will implement actual LinkedIn/API integration.
 */
public interface JobSearchAgent {

    /**
     * Search for job opportunities based on the given criteria.
     *
     * @param criteria the search parameters configured by the user
     * @return list of discovered job opportunities
     */
    List<JobOpportunity> searchJobs(JobSearchCriteria criteria);

    /**
     * Check whether the agent is currently operational.
     */
    boolean isAvailable();

    /**
     * Return the name/type of this agent implementation.
     */
    String getAgentType();
}

package com.jobfinder.source;

import com.jobfinder.ai.JobSearchCriteria;

import java.util.List;

/**
 * A place opportunities come from. Every source is an official, permitted API:
 * no scraping, no credential replay, no bypassing a site's protections.
 * Implementations must respect their source's terms and rate limits.
 */
public interface JobSource {

    /** Stored on every opportunity so its origin is always traceable. */
    String name();

    /** False when the source lacks the credentials or configuration it needs. */
    boolean isEnabled();

    /**
     * @return what the source returns for these criteria, already mapped onto
     *         {@link RawJobPosting}. Failures are the caller's to handle.
     */
    List<RawJobPosting> fetch(JobSearchCriteria criteria);
}

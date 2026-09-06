CREATE TABLE job_opportunities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    location VARCHAR(255),
    employment_type VARCHAR(100),
    description TEXT,
    job_url TEXT,
    source VARCHAR(100) DEFAULT 'MOCK',
    publication_date DATE,
    recruiter_name VARCHAR(255),
    recruiter_email VARCHAR(255),
    recruiter_profile_url TEXT,
    match_score INTEGER CHECK (match_score BETWEEN 0 AND 100),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE saved_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    job_id UUID NOT NULL,
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT saved_jobs_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT saved_jobs_job_fk FOREIGN KEY (job_id) REFERENCES job_opportunities (id) ON DELETE CASCADE,
    CONSTRAINT saved_jobs_user_job_unique UNIQUE (user_id, job_id)
);

CREATE INDEX idx_jobs_status ON job_opportunities (status);
CREATE INDEX idx_jobs_match_score ON job_opportunities (match_score DESC);
CREATE INDEX idx_jobs_publication_date ON job_opportunities (publication_date DESC);
CREATE INDEX idx_jobs_company ON job_opportunities (company_name);
CREATE INDEX idx_saved_jobs_user ON saved_jobs (user_id);

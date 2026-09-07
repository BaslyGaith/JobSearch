-- CV agent: a per-user fact bank plus every tailored CV generated from it.

CREATE TABLE cv_profiles (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    full_name   VARCHAR(200) NOT NULL,
    headline    VARCHAR(300),
    email       VARCHAR(255),
    phone       VARCHAR(50),
    linkedin_url TEXT,
    location    VARCHAR(200),
    years_experience INTEGER,
    -- The verified fact bank. Nothing outside this document may reach a CV.
    fact_bank   TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cv_documents (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_opportunity_id  UUID REFERENCES job_opportunities (id) ON DELETE SET NULL,
    language            VARCHAR(5) NOT NULL,
    target_role         VARCHAR(300),
    target_company      VARCHAR(300),
    job_family          VARCHAR(50),
    accent_color        VARCHAR(20),
    posting_text        TEXT,
    -- Rendered CV (structured sections) and the honesty report beside it.
    content             TEXT NOT NULL,
    gaps                TEXT,
    generated_by        VARCHAR(50),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cv_documents_user ON cv_documents (user_id, created_at DESC);
CREATE INDEX idx_cv_documents_opportunity ON cv_documents (job_opportunity_id);

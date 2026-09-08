-- Applications: one row per role the user applies for, plus a timeline of what
-- happened to it. Nothing here sends anything - the user always presses send.

CREATE TABLE applications (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_opportunity_id  UUID NOT NULL REFERENCES job_opportunities (id) ON DELETE CASCADE,
    cv_document_id      UUID REFERENCES cv_documents (id) ON DELETE SET NULL,

    recipient_name      VARCHAR(200),
    recipient_email     VARCHAR(255),
    subject             VARCHAR(500),
    body                TEXT,

    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    -- Set once the draft reaches Gmail; null while it lives only here.
    gmail_draft_id      VARCHAR(200),
    sent_at             TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- One application per opportunity per user.
    CONSTRAINT uq_application_user_job UNIQUE (user_id, job_opportunity_id)
);

CREATE INDEX idx_applications_user ON applications (user_id, created_at DESC);
CREATE INDEX idx_applications_status ON applications (user_id, status);

CREATE TABLE application_events (
    id              UUID PRIMARY KEY,
    application_id  UUID NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    label           VARCHAR(200) NOT NULL,
    detail          TEXT,
    occurred_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_application_events_application
    ON application_events (application_id, occurred_at);

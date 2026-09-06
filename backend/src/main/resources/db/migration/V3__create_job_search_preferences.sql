CREATE TABLE job_search_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    remote_preference VARCHAR(50) DEFAULT 'ANY',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT preferences_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT preferences_user_unique UNIQUE (user_id)
);

CREATE TABLE preference_job_titles (
    preference_id UUID NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    CONSTRAINT pref_job_titles_fk FOREIGN KEY (preference_id)
        REFERENCES job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE preference_locations (
    preference_id UUID NOT NULL,
    location VARCHAR(255) NOT NULL,
    CONSTRAINT pref_locations_fk FOREIGN KEY (preference_id)
        REFERENCES job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE preference_employment_types (
    preference_id UUID NOT NULL,
    employment_type VARCHAR(100) NOT NULL,
    CONSTRAINT pref_employment_types_fk FOREIGN KEY (preference_id)
        REFERENCES job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE preference_experience_levels (
    preference_id UUID NOT NULL,
    experience_level VARCHAR(100) NOT NULL,
    CONSTRAINT pref_experience_levels_fk FOREIGN KEY (preference_id)
        REFERENCES job_search_preferences (id) ON DELETE CASCADE
);

CREATE TABLE preference_keywords (
    preference_id UUID NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    CONSTRAINT pref_keywords_fk FOREIGN KEY (preference_id)
        REFERENCES job_search_preferences (id) ON DELETE CASCADE
);

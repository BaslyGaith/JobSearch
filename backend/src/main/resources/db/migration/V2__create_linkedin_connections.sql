CREATE TABLE linkedin_connections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    linkedin_email VARCHAR(255),
    linkedin_id VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    connected BOOLEAN NOT NULL DEFAULT FALSE,
    connected_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT linkedin_connections_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT linkedin_connections_user_unique UNIQUE (user_id)
);

CREATE INDEX idx_linkedin_user_id ON linkedin_connections (user_id);

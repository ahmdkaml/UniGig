CREATE TABLE freelancer_profiles (
    user_id        BIGINT         PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    title          VARCHAR(120)   NOT NULL,
    bio            TEXT,
    skills         TEXT ARRAY     NOT NULL DEFAULT '{}',
    portfolio_url  VARCHAR(255),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE gigs (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
    client_id   BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT         NOT NULL REFERENCES categories(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gigs_status ON gigs(status);
CREATE INDEX idx_gigs_category_id ON gigs(category_id);
CREATE INDEX idx_gigs_client_id ON gigs(client_id);

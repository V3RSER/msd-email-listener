CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE user_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL UNIQUE,
    access_token VARCHAR(2048) NOT NULL,
    refresh_token VARCHAR(2048) NOT NULL,
    token_expiration TIMESTAMP WITH TIME ZONE NOT NULL
);

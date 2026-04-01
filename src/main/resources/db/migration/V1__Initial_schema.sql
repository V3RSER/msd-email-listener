CREATE TABLE user_connections (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    access_token VARCHAR(2048) NOT NULL,
    refresh_token VARCHAR(2048) NOT NULL,
    token_expiration TIMESTAMP WITH TIME ZONE NOT NULL
);

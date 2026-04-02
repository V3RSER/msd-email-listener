ALTER TABLE user_connection ADD COLUMN access_token_issued_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_connection ADD COLUMN access_token_expires_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_connection DROP COLUMN token_expiration;

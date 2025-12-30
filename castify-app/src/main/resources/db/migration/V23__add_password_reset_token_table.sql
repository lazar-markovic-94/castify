-- Password reset token table for handling password reset requests
CREATE TABLE password_reset_token (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(user_id)
        ON DELETE CASCADE
);

-- Index for quick token lookup
CREATE INDEX idx_password_reset_token_token ON password_reset_token(token);

-- Index for user lookup
CREATE INDEX idx_password_reset_token_user_id ON password_reset_token(user_id);

-- Index for cleanup of expired tokens
CREATE INDEX idx_password_reset_token_expires_at ON password_reset_token(expires_at);

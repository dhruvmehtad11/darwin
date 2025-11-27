-- Migration: Create default admin user
-- Date: 2025-11-19
-- Description: Inserts a default admin user if it doesn't already exist
-- Note: The default token should be changed in production environments

-- Insert admin user only if it doesn't exist
INSERT INTO users (username, token, created_at, updated_at)
SELECT 'admin', 'admin-token-default-change-in-production', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);


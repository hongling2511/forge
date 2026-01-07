-- =====================================================
-- Schema Version: V1
-- Description: Initial schema for authentication system
-- Compatible: MySQL 8.0+, PostgreSQL 12+, H2
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- Description: User account information
-- -----------------------------------------------------
CREATE TABLE users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,          -- UUID primary key
    username VARCHAR(50) NOT NULL UNIQUE,         -- Login username
    email VARCHAR(100) NOT NULL UNIQUE,           -- Email address
    password_hash VARCHAR(255) NOT NULL,          -- BCrypt hashed password
    first_name VARCHAR(50),                       -- User's first name
    last_name VARCHAR(50),                        -- User's last name
    enabled BOOLEAN NOT NULL DEFAULT TRUE,        -- Account status
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table: refresh_tokens
-- Description: JWT refresh tokens for session management
-- -----------------------------------------------------
CREATE TABLE refresh_tokens (
    id VARCHAR(36) NOT NULL PRIMARY KEY,          -- UUID primary key
    user_id VARCHAR(36) NOT NULL,                 -- Reference to users.id
    token_hash VARCHAR(255) NOT NULL UNIQUE,      -- SHA-256 hashed token
    expires_at TIMESTAMP NOT NULL,                -- Token expiration time
    revoked BOOLEAN NOT NULL DEFAULT FALSE,       -- Revocation status
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table: user_roles
-- Description: User role assignments (USER, ADMIN)
-- -----------------------------------------------------
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,                 -- Reference to users.id
    role VARCHAR(20) NOT NULL,                    -- Role name: USER, ADMIN
    PRIMARY KEY (user_id, role)
);

-- -----------------------------------------------------
-- Indexes: Performance optimization
-- -----------------------------------------------------
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

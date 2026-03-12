-- =====================================================
-- V1__create_users_table.sql
-- Banking API — User entity schema
-- =====================================================

CREATE TABLE users (
    id              VARCHAR(36)     PRIMARY KEY DEFAULT gen_random_uuid()::text,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    phone_number    VARCHAR(15)     UNIQUE,
    role            VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

-- Indexes for frequent lookups
CREATE INDEX idx_users_email       ON users (email);
CREATE INDEX idx_users_phone       ON users (phone_number);
CREATE INDEX idx_users_role        ON users (role);
CREATE INDEX idx_users_enabled     ON users (enabled);

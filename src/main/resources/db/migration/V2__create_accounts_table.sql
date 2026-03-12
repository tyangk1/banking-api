-- =====================================================
-- V2__create_accounts_table.sql
-- Banking API — Account entity schema
-- =====================================================

CREATE TABLE accounts (
    id              VARCHAR(36)     PRIMARY KEY DEFAULT gen_random_uuid()::text,
    account_number  VARCHAR(20)     NOT NULL UNIQUE,
    account_name    VARCHAR(100)    NOT NULL,
    account_type    VARCHAR(20)     NOT NULL,
    balance         DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'VND',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    user_id         VARCHAR(36)     NOT NULL,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),

    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_accounts_user_id        ON accounts (user_id);
CREATE INDEX idx_accounts_number         ON accounts (account_number);
CREATE INDEX idx_accounts_status         ON accounts (status);
CREATE INDEX idx_accounts_type           ON accounts (account_type);
CREATE INDEX idx_accounts_user_status    ON accounts (user_id, status);

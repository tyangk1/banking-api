-- =====================================================
-- V3__create_transactions_table.sql
-- Banking API — Transaction entity schema
-- =====================================================

CREATE TABLE transactions (
    id                          VARCHAR(36)     PRIMARY KEY DEFAULT gen_random_uuid()::text,
    reference_number            VARCHAR(50)     NOT NULL UNIQUE,
    type                        VARCHAR(20)     NOT NULL,
    amount                      DECIMAL(19,4)   NOT NULL,
    fee                         DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    currency                    VARCHAR(3)      NOT NULL DEFAULT 'VND',
    description                 VARCHAR(500),
    status                      VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    balance_after_transaction   DECIMAL(19,4),
    source_account_id           VARCHAR(36),
    destination_account_id      VARCHAR(36),
    version                     BIGINT          NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_by                  VARCHAR(255),

    CONSTRAINT fk_tx_source      FOREIGN KEY (source_account_id)      REFERENCES accounts(id),
    CONSTRAINT fk_tx_destination FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
);

-- Indexes for transaction queries
CREATE INDEX idx_tx_reference        ON transactions (reference_number);
CREATE INDEX idx_tx_source_account   ON transactions (source_account_id);
CREATE INDEX idx_tx_dest_account     ON transactions (destination_account_id);
CREATE INDEX idx_tx_type             ON transactions (type);
CREATE INDEX idx_tx_status           ON transactions (status);
CREATE INDEX idx_tx_created_at       ON transactions (created_at);
CREATE INDEX idx_tx_source_status    ON transactions (source_account_id, status);

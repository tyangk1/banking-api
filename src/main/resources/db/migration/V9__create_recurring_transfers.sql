-- Recurring (Scheduled) Transfers
CREATE TABLE recurring_transfers (
    id              VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id         VARCHAR(36) NOT NULL REFERENCES users(id),
    source_account_id VARCHAR(36) NOT NULL REFERENCES accounts(id),
    destination_account_number VARCHAR(50) NOT NULL,
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'VND',
    description     VARCHAR(500),

    -- Schedule config
    frequency       VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY
    day_of_week     INT,                  -- 1-7 for WEEKLY (1=Monday)
    day_of_month    INT,                  -- 1-28 for MONTHLY
    start_date      DATE NOT NULL,
    end_date        DATE,                 -- NULL = no end

    -- Runtime state
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, COMPLETED, CANCELLED
    next_execution  TIMESTAMP NOT NULL,
    last_executed   TIMESTAMP,
    execution_count INT NOT NULL DEFAULT 0,
    max_executions  INT,                  -- NULL = unlimited
    last_error      VARCHAR(500),

    -- Audit
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_recurring_active ON recurring_transfers(status, next_execution)
    WHERE status = 'ACTIVE';
CREATE INDEX idx_recurring_user ON recurring_transfers(user_id);

-- =====================================================
-- V8__create_approval_requests.sql
-- Approval workflow for transactions exceeding limits
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_requests (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    requester_user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    approver_user_id VARCHAR(36) REFERENCES users(id),
    source_account_id VARCHAR(36) NOT NULL REFERENCES accounts(id),
    destination_account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    limit_type VARCHAR(30) NOT NULL,
    limit_value DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_approval_requester ON approval_requests(requester_user_id, status);
CREATE INDEX idx_approval_approver ON approval_requests(approver_user_id, status);
CREATE INDEX idx_approval_status ON approval_requests(status);

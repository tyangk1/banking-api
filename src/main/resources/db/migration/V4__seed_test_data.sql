-- =====================================================
-- V4__seed_test_data.sql
-- Banking API — Test/Demo data for development
-- =====================================================

-- ===== Test Users =====
-- Password is 'Password123' encoded with BCrypt
INSERT INTO users (id, first_name, last_name, email, password, phone_number, role, enabled)
VALUES
    ('user-001', 'Nguyen', 'Van A', 'nguyenvana@email.com',
     '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO',
     '0901234567', 'ROLE_USER', true),
    ('user-002', 'Tran', 'Thi B', 'tranthib@email.com',
     '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO',
     '0907654321', 'ROLE_USER', true),
    ('admin-001', 'Admin', 'Banking', 'admin@banking.com',
     '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO',
     '0909999999', 'ROLE_ADMIN', true),
    ('manager-001', 'Manager', 'Banking', 'manager@banking.com',
     '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO',
     '0908888888', 'ROLE_MANAGER', true);

-- ===== Test Accounts =====
INSERT INTO accounts (id, account_number, account_name, account_type, balance, currency, status, user_id)
VALUES
    ('acc-001', '1000000001', 'Tài khoản tiết kiệm', 'SAVINGS', 45000000.0000, 'VND', 'ACTIVE', 'user-001'),
    ('acc-002', '1000000002', 'Tài khoản thanh toán', 'CHECKING', 15000000.0000, 'VND', 'ACTIVE', 'user-001'),
    ('acc-003', '1000000003', 'Tài khoản chính', 'CHECKING', 25000000.0000, 'VND', 'ACTIVE', 'user-002'),
    ('acc-004', '1000000004', 'Tài khoản dự phòng', 'SAVINGS', 10000000.0000, 'VND', 'ACTIVE', 'user-002');

-- ===== Test Transactions =====
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id)
VALUES
    ('tx-001', 'TXN-20240101-DEMO0001', 'DEPOSIT', 50000000.0000, 0.0000, 'VND', 'Lương tháng 1', 'COMPLETED', 50000000.0000, NULL, 'acc-001'),
    ('tx-002', 'TXN-20240115-DEMO0002', 'TRANSFER', 5000000.0000, 0.0000, 'VND', 'Chuyển tiền sinh hoạt', 'COMPLETED', 45000000.0000, 'acc-001', 'acc-002'),
    ('tx-003', 'TXN-20240201-DEMO0003', 'DEPOSIT', 15000000.0000, 0.0000, 'VND', 'Lương tháng 2', 'COMPLETED', 15000000.0000, NULL, 'acc-002'),
    ('tx-004', 'TXN-20240210-DEMO0004', 'TRANSFER', 2000000.0000, 0.0000, 'VND', 'Thanh toán tiền thuê', 'COMPLETED', 43000000.0000, 'acc-001', 'acc-003'),
    ('tx-005', 'TXN-20240220-DEMO0005', 'DEPOSIT', 30000000.0000, 0.0000, 'VND', 'Thu nhập freelance', 'COMPLETED', 25000000.0000, NULL, 'acc-003');

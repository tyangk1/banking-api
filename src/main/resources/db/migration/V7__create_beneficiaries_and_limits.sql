-- =====================================================
-- V7__create_beneficiaries_and_limits.sql
-- New tables for advanced banking features
-- =====================================================

-- ===== Beneficiaries Table =====
CREATE TABLE IF NOT EXISTS beneficiaries (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    nickname VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    account_holder_name VARCHAR(200) NOT NULL,
    bank_name VARCHAR(200) DEFAULT 'Premium Banking',
    is_verified BOOLEAN DEFAULT false,
    is_favorite BOOLEAN DEFAULT false,
    transfer_count INT DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    UNIQUE(user_id, account_number)
);

CREATE INDEX idx_beneficiary_user ON beneficiaries(user_id);
CREATE INDEX idx_beneficiary_favorite ON beneficiaries(user_id, is_favorite);

-- ===== Transaction Limits Table =====
CREATE TABLE IF NOT EXISTS transaction_limits (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    account_id VARCHAR(36) NOT NULL REFERENCES accounts(id) UNIQUE,
    daily_limit DECIMAL(19,4) NOT NULL DEFAULT 100000000.0000,
    monthly_limit DECIMAL(19,4) NOT NULL DEFAULT 500000000.0000,
    single_transaction_limit DECIMAL(19,4) NOT NULL DEFAULT 50000000.0000,
    current_daily_used DECIMAL(19,4) DEFAULT 0.0000,
    current_monthly_used DECIMAL(19,4) DEFAULT 0.0000,
    last_daily_reset TIMESTAMP DEFAULT NOW(),
    last_monthly_reset TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_tx_limit_account ON transaction_limits(account_id);

-- ===== User Profile Extensions =====
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(20) DEFAULT 'PENDING';

-- ===== Transaction Category (for analytics) =====
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'OTHER';

-- ===== Seed Beneficiaries =====
INSERT INTO beneficiaries (id, user_id, nickname, account_number, account_holder_name, bank_name, is_verified, is_favorite, transfer_count, last_used_at) VALUES
('ben-001', 'user-001', 'Chủ nhà', '1000000010', 'Hoang Van Duc', 'Premium Banking', true, true, 6, '2024-06-03 10:00:00'),
('ben-002', 'user-001', 'Em gái Lan', '1000000012', 'Vo Thi Lan', 'Premium Banking', true, true, 6, '2024-06-20 18:00:00'),
('ben-003', 'user-001', 'Vợ - tiết kiệm', '1000000013', 'Vo Thi Lan', 'Premium Banking', true, false, 3, '2024-04-12 10:00:00'),
('ben-004', 'user-003', 'Mẹ', '1000000012', 'Vo Thi Lan', 'Premium Banking', true, true, 2, '2024-05-01 08:00:00'),
('ben-005', 'user-004', 'Quỹ tiết kiệm', '1000000009', 'Pham Thi Mai', 'Premium Banking', true, true, 6, '2024-06-25 08:00:00'),
('ben-006', 'user-001', 'Bạn Hải', '1000000014', 'Dang Quoc Bao', 'Premium Banking', true, false, 2, '2024-06-16 08:00:00');

-- ===== Seed Transaction Limits =====
INSERT INTO transaction_limits (id, account_id, daily_limit, monthly_limit, single_transaction_limit) VALUES
('lim-001', 'acc-001', 50000000.0000, 200000000.0000, 20000000.0000),
('lim-002', 'acc-002', 30000000.0000, 150000000.0000, 15000000.0000),
('lim-003', 'acc-003', 50000000.0000, 200000000.0000, 20000000.0000),
('lim-004', 'acc-005', 100000000.0000, 500000000.0000, 50000000.0000),
('lim-005', 'acc-008', 50000000.0000, 300000000.0000, 20000000.0000),
('lim-006', 'acc-010', 50000000.0000, 200000000.0000, 20000000.0000);

-- ===== Update existing transaction categories =====
UPDATE transactions SET category = 'SALARY' WHERE description LIKE '%Lương%';
UPDATE transactions SET category = 'RENT' WHERE description LIKE '%thuê nhà%';
UPDATE transactions SET category = 'UTILITIES' WHERE description LIKE '%điện%' OR description LIKE '%nước%' OR description LIKE '%gas%' OR description LIKE '%Internet%';
UPDATE transactions SET category = 'SHOPPING' WHERE description LIKE '%Shopee%' OR description LIKE '%Lazada%' OR description LIKE '%Tiki%' OR description LIKE '%Mua%';
UPDATE transactions SET category = 'FOOD' WHERE description LIKE '%chợ%' OR description LIKE '%siêu thị%' OR description LIKE '%Ăn%';
UPDATE transactions SET category = 'HEALTHCARE' WHERE description LIKE '%Khám%' OR description LIKE '%thuốc%' OR description LIKE '%vitamin%';
UPDATE transactions SET category = 'INSURANCE' WHERE description LIKE '%Bảo hiểm%';
UPDATE transactions SET category = 'EDUCATION' WHERE description LIKE '%Học phí%' OR description LIKE '%khóa%';
UPDATE transactions SET category = 'SAVINGS' WHERE description LIKE '%tiết kiệm%' OR description LIKE '%Gửi tiết kiệm%';
UPDATE transactions SET category = 'ENTERTAINMENT' WHERE description LIKE '%concert%' OR description LIKE '%tour%' OR description LIKE '%du lịch%' OR description LIKE '%gym%';
UPDATE transactions SET category = 'GIFT' WHERE description LIKE '%Quà%' OR description LIKE '%Lì xì%' OR description LIKE '%Mừng%' OR description LIKE '%Biếu%';
UPDATE transactions SET category = 'FREELANCE' WHERE description LIKE '%freelance%' OR description LIKE '%Freelance%';
UPDATE transactions SET category = 'TRANSPORT' WHERE description LIKE '%xe%' OR description LIKE '%Bảo dưỡng%';
UPDATE transactions SET category = 'TAX' WHERE description LIKE '%Thuế%';
UPDATE transactions SET category = 'INVESTMENT' WHERE description LIKE '%Cổ tức%' OR description LIKE '%đầu tư%';
UPDATE transactions SET category = 'TELECOM' WHERE description LIKE '%điện thoại%' OR description LIKE '%Nạp tiền%';
UPDATE transactions SET category = 'BONUS' WHERE description LIKE '%Thưởng%';
UPDATE transactions SET category = 'PET' WHERE description LIKE '%thú cưng%';

-- ===== Update user profile data =====
UPDATE users SET date_of_birth = '1995-03-15', address = '123 Nguyễn Huệ, Q1, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-001';
UPDATE users SET date_of_birth = '1998-07-22', address = '456 Lê Lợi, Q3, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-002';
UPDATE users SET date_of_birth = '1992-11-08', address = '789 Trần Hưng Đạo, Q5, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-003';
UPDATE users SET date_of_birth = '1997-05-30', address = '321 Hai Bà Trưng, Q1, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-004';
UPDATE users SET date_of_birth = '1990-01-12', address = '654 Điện Biên Phủ, Bình Thạnh, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-005';
UPDATE users SET date_of_birth = '1999-09-25', address = '987 Võ Văn Tần, Q3, TP.HCM', kyc_status = 'PENDING' WHERE id = 'user-006';
UPDATE users SET date_of_birth = '1988-12-01', address = '111 Pasteur, Q1, TP.HCM', kyc_status = 'VERIFIED' WHERE id = 'user-007';
UPDATE users SET date_of_birth = '2000-04-18', address = '222 Nam Kỳ Khởi Nghĩa, Q1, TP.HCM', kyc_status = 'REJECTED' WHERE id = 'user-008';
UPDATE users SET kyc_status = 'VERIFIED' WHERE id = 'admin-001';
UPDATE users SET kyc_status = 'VERIFIED' WHERE id = 'manager-001';

-- V6: Realistic banking data — 6 users, 12 accounts, 80+ transactions (Jan-Jun 2024)

-- Additional Users (Password: Password123)
INSERT INTO users (id, first_name, last_name, email, password, phone_number, role, enabled, created_at, updated_at) VALUES
('user-003', 'Le', 'Minh Tuan', 'leminhuan@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0912345678', 'ROLE_USER', true, '2024-01-05 08:00:00', '2024-06-01 10:00:00'),
('user-004', 'Pham', 'Thi Mai', 'phamthimai@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0923456789', 'ROLE_USER', true, '2024-01-10 09:00:00', '2024-05-20 14:00:00'),
('user-005', 'Hoang', 'Van Duc', 'hoangvanduc@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0934567890', 'ROLE_USER', true, '2024-02-01 10:00:00', '2024-06-15 09:00:00'),
('user-006', 'Vo', 'Thi Lan', 'vothilan@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0945678901', 'ROLE_USER', true, '2024-02-15 11:00:00', '2024-06-10 16:00:00'),
('user-007', 'Dang', 'Quoc Bao', 'dangquocbao@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0956789012', 'ROLE_MANAGER', true, '2024-01-01 07:00:00', '2024-06-20 08:00:00'),
('user-008', 'Bui', 'Thanh Ha', 'buithanhha@email.com', '$2a$10$EqKcp1WFKk.dGYrRpCVExeVOEOjdcQnxoXSnMaGPpLJace4gGP7uO', '0967890123', 'ROLE_USER', false, '2024-03-01 08:00:00', '2024-04-15 12:00:00');

-- Additional Accounts
INSERT INTO accounts (id, account_number, account_name, account_type, balance, currency, status, user_id, created_at, updated_at) VALUES
('acc-005', '1000000005', 'Tiết kiệm dài hạn', 'SAVINGS', 120000000.0000, 'VND', 'ACTIVE', 'user-003', '2024-01-05 08:30:00', '2024-06-01 10:00:00'),
('acc-006', '1000000006', 'Chi tiêu hàng ngày', 'CHECKING', 8500000.0000, 'VND', 'ACTIVE', 'user-003', '2024-01-05 08:35:00', '2024-06-01 10:00:00'),
('acc-007', '1000000007', 'USD Savings', 'SAVINGS', 5200.0000, 'USD', 'ACTIVE', 'user-003', '2024-02-01 09:00:00', '2024-06-01 10:00:00'),
('acc-008', '1000000008', 'Tài khoản lương', 'CHECKING', 32000000.0000, 'VND', 'ACTIVE', 'user-004', '2024-01-10 09:30:00', '2024-05-20 14:00:00'),
('acc-009', '1000000009', 'Quỹ đầu tư', 'SAVINGS', 85000000.0000, 'VND', 'ACTIVE', 'user-004', '2024-01-10 09:35:00', '2024-05-20 14:00:00'),
('acc-010', '1000000010', 'Tài khoản chính', 'CHECKING', 18750000.0000, 'VND', 'ACTIVE', 'user-005', '2024-02-01 10:30:00', '2024-06-15 09:00:00'),
('acc-011', '1000000011', 'Tiết kiệm mua nhà', 'SAVINGS', 250000000.0000, 'VND', 'ACTIVE', 'user-005', '2024-02-01 10:35:00', '2024-06-15 09:00:00'),
('acc-012', '1000000012', 'Thanh toán online', 'CHECKING', 5200000.0000, 'VND', 'ACTIVE', 'user-006', '2024-02-15 11:30:00', '2024-06-10 16:00:00'),
('acc-013', '1000000013', 'Tiết kiệm du lịch', 'SAVINGS', 42000000.0000, 'VND', 'ACTIVE', 'user-006', '2024-02-15 11:35:00', '2024-06-10 16:00:00'),
('acc-014', '1000000014', 'Tài khoản Manager', 'CHECKING', 55000000.0000, 'VND', 'ACTIVE', 'user-007', '2024-01-01 07:30:00', '2024-06-20 08:00:00'),
('acc-015', '1000000015', 'Tài khoản bị khóa', 'CHECKING', 1500000.0000, 'VND', 'FROZEN', 'user-008', '2024-03-01 08:30:00', '2024-04-15 12:00:00'),
('acc-016', '1000000016', 'Admin Operations', 'CHECKING', 500000000.0000, 'VND', 'ACTIVE', 'admin-001', '2024-01-01 00:00:00', '2024-06-20 00:00:00');

-- JANUARY 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-010', 'TXN-20240102-SAL001', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 1/2024 - Công ty ABC', 'COMPLETED', 25000000.0000, NULL, 'acc-005', '2024-01-02 08:00:00', '2024-01-02 08:00:00'),
('tx-011', 'TXN-20240102-SAL002', 'DEPOSIT', 18000000.0000, 0, 'VND', 'Lương tháng 1/2024 - Công ty XYZ', 'COMPLETED', 18000000.0000, NULL, 'acc-008', '2024-01-02 09:00:00', '2024-01-02 09:00:00'),
('tx-012', 'TXN-20240103-RNT001', 'TRANSFER', 5000000.0000, 0, 'VND', 'Tiền thuê nhà tháng 1', 'COMPLETED', 20000000.0000, 'acc-005', 'acc-010', '2024-01-03 10:00:00', '2024-01-03 10:00:00'),
('tx-013', 'TXN-20240105-UTL001', 'TRANSFER', 850000.0000, 0, 'VND', 'Tiền điện tháng 12/2023', 'COMPLETED', 19150000.0000, 'acc-005', 'acc-016', '2024-01-05 14:00:00', '2024-01-05 14:00:00'),
('tx-014', 'TXN-20240105-UTL002', 'TRANSFER', 320000.0000, 0, 'VND', 'Tiền nước tháng 12/2023', 'COMPLETED', 18830000.0000, 'acc-005', 'acc-016', '2024-01-05 14:05:00', '2024-01-05 14:05:00'),
('tx-015', 'TXN-20240107-SHP001', 'TRANSFER', 1200000.0000, 0, 'VND', 'Mua sắm Shopee', 'COMPLETED', 17630000.0000, 'acc-006', 'acc-012', '2024-01-07 20:00:00', '2024-01-07 20:00:00'),
('tx-016', 'TXN-20240110-FRE001', 'DEPOSIT', 8000000.0000, 0, 'VND', 'Thu nhập freelance - Design logo', 'COMPLETED', 26630000.0000, NULL, 'acc-006', '2024-01-10 16:00:00', '2024-01-10 16:00:00'),
('tx-017', 'TXN-20240112-TRF001', 'TRANSFER', 10000000.0000, 0, 'VND', 'Gửi tiết kiệm tháng 1', 'COMPLETED', 95000000.0000, 'acc-008', 'acc-009', '2024-01-12 11:00:00', '2024-01-12 11:00:00'),
('tx-018', 'TXN-20240115-INS001', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm nhân thọ tháng 1', 'COMPLETED', 5500000.0000, 'acc-008', 'acc-016', '2024-01-15 09:00:00', '2024-01-15 09:00:00'),
('tx-019', 'TXN-20240118-GRC001', 'TRANSFER', 3500000.0000, 0, 'VND', 'Tiền chợ siêu thị tuần 3', 'COMPLETED', 14130000.0000, 'acc-005', 'acc-012', '2024-01-18 18:00:00', '2024-01-18 18:00:00'),
('tx-020', 'TXN-20240120-EDU001', 'TRANSFER', 6000000.0000, 0, 'VND', 'Học phí khóa tiếng Anh', 'COMPLETED', 8130000.0000, 'acc-005', 'acc-016', '2024-01-20 10:00:00', '2024-01-20 10:00:00'),
('tx-021', 'TXN-20240125-SAV001', 'TRANSFER', 15000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà', 'COMPLETED', 200000000.0000, 'acc-010', 'acc-011', '2024-01-25 08:00:00', '2024-01-25 08:00:00');

-- FEBRUARY 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-022', 'TXN-20240201-SAL003', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 2/2024', 'COMPLETED', 33130000.0000, NULL, 'acc-005', '2024-02-01 08:00:00', '2024-02-01 08:00:00'),
('tx-023', 'TXN-20240201-SAL004', 'DEPOSIT', 18000000.0000, 0, 'VND', 'Lương tháng 2/2024 công ty XYZ', 'COMPLETED', 23500000.0000, NULL, 'acc-008', '2024-02-01 09:00:00', '2024-02-01 09:00:00'),
('tx-024', 'TXN-20240203-RNT002', 'TRANSFER', 5000000.0000, 0, 'VND', 'Tiền thuê nhà tháng 2', 'COMPLETED', 28130000.0000, 'acc-005', 'acc-010', '2024-02-03 10:00:00', '2024-02-03 10:00:00'),
('tx-025', 'TXN-20240205-TET001', 'TRANSFER', 2000000.0000, 0, 'VND', 'Lì xì Tết cho cháu', 'COMPLETED', 26130000.0000, 'acc-005', 'acc-012', '2024-02-05 08:00:00', '2024-02-05 08:00:00'),
('tx-026', 'TXN-20240208-TET003', 'DEPOSIT', 5000000.0000, 0, 'VND', 'Nhận lì xì Tết từ bố mẹ', 'COMPLETED', 30130000.0000, NULL, 'acc-005', '2024-02-08 09:00:00', '2024-02-08 09:00:00'),
('tx-027', 'TXN-20240210-SHP002', 'TRANSFER', 4500000.0000, 0, 'VND', 'Mua sắm Tết quần áo Uniqlo', 'COMPLETED', 25630000.0000, 'acc-006', 'acc-016', '2024-02-10 15:00:00', '2024-02-10 15:00:00'),
('tx-028', 'TXN-20240212-MED001', 'TRANSFER', 1800000.0000, 0, 'VND', 'Khám sức khỏe định kỳ', 'COMPLETED', 23830000.0000, 'acc-005', 'acc-016', '2024-02-12 10:00:00', '2024-02-12 10:00:00'),
('tx-029', 'TXN-20240215-INS002', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm nhân thọ tháng 2', 'COMPLETED', 21000000.0000, 'acc-008', 'acc-016', '2024-02-15 09:00:00', '2024-02-15 09:00:00'),
('tx-030', 'TXN-20240218-FRE002', 'DEPOSIT', 12000000.0000, 0, 'VND', 'Thu nhập freelance Website dev', 'COMPLETED', 35830000.0000, NULL, 'acc-006', '2024-02-18 16:00:00', '2024-02-18 16:00:00'),
('tx-031', 'TXN-20240225-SAV002', 'TRANSFER', 15000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà T2', 'COMPLETED', 215000000.0000, 'acc-010', 'acc-011', '2024-02-25 08:00:00', '2024-02-25 08:00:00'),
('tx-032', 'TXN-20240228-UTL003', 'TRANSFER', 950000.0000, 0, 'VND', 'Tiền điện nước tháng 2', 'COMPLETED', 18680000.0000, 'acc-005', 'acc-016', '2024-02-28 14:00:00', '2024-02-28 14:00:00');

-- MARCH 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-033', 'TXN-20240301-SAL005', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 3/2024', 'COMPLETED', 43680000.0000, NULL, 'acc-005', '2024-03-01 08:00:00', '2024-03-01 08:00:00'),
('tx-034', 'TXN-20240301-SAL006', 'DEPOSIT', 20000000.0000, 0, 'VND', 'Lương tháng 3 thưởng KPI', 'COMPLETED', 38500000.0000, NULL, 'acc-008', '2024-03-01 09:00:00', '2024-03-01 09:00:00'),
('tx-035', 'TXN-20240303-RNT003', 'TRANSFER', 5000000.0000, 0, 'VND', 'Tiền thuê nhà tháng 3', 'COMPLETED', 38680000.0000, 'acc-005', 'acc-010', '2024-03-03 10:00:00', '2024-03-03 10:00:00'),
('tx-036', 'TXN-20240308-WMN001', 'TRANSFER', 5000000.0000, 0, 'VND', 'Quà 8/3 cho vợ', 'COMPLETED', 32480000.0000, 'acc-005', 'acc-013', '2024-03-08 12:00:00', '2024-03-08 12:00:00'),
('tx-037', 'TXN-20240312-FRE003', 'DEPOSIT', 15000000.0000, 0, 'VND', 'Freelance Mobile app project', 'COMPLETED', 50830000.0000, NULL, 'acc-006', '2024-03-12 16:00:00', '2024-03-12 16:00:00'),
('tx-038', 'TXN-20240315-INS003', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm tháng 3', 'COMPLETED', 36000000.0000, 'acc-008', 'acc-016', '2024-03-15 09:00:00', '2024-03-15 09:00:00'),
('tx-039', 'TXN-20240318-TRL001', 'TRANSFER', 8000000.0000, 0, 'VND', 'Đặt tour du lịch Đà Lạt', 'COMPLETED', 20980000.0000, 'acc-005', 'acc-016', '2024-03-18 20:00:00', '2024-03-18 20:00:00'),
('tx-040', 'TXN-20240325-SAV003', 'TRANSFER', 20000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà T3', 'COMPLETED', 235000000.0000, 'acc-010', 'acc-011', '2024-03-25 08:00:00', '2024-03-25 08:00:00');

-- APRIL 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-041', 'TXN-20240401-SAL008', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 4/2024', 'COMPLETED', 39880000.0000, NULL, 'acc-005', '2024-04-01 08:00:00', '2024-04-01 08:00:00'),
('tx-042', 'TXN-20240401-SAL009', 'DEPOSIT', 18000000.0000, 0, 'VND', 'Lương tháng 4/2024 công ty XYZ', 'COMPLETED', 51500000.0000, NULL, 'acc-008', '2024-04-01 09:00:00', '2024-04-01 09:00:00'),
('tx-043', 'TXN-20240403-RNT004', 'TRANSFER', 5500000.0000, 0, 'VND', 'Tiền thuê nhà tháng 4', 'COMPLETED', 34380000.0000, 'acc-005', 'acc-010', '2024-04-03 10:00:00', '2024-04-03 10:00:00'),
('tx-044', 'TXN-20240408-SHP004', 'TRANSFER', 6500000.0000, 0, 'VND', 'Tiki Laptop cooling pad phụ kiện', 'COMPLETED', 26780000.0000, 'acc-006', 'acc-016', '2024-04-08 22:00:00', '2024-04-08 22:00:00'),
('tx-045', 'TXN-20240412-GIF001', 'TRANSFER', 3000000.0000, 0, 'VND', 'Quà sinh nhật cho bạn', 'COMPLETED', 28930000.0000, 'acc-005', 'acc-013', '2024-04-12 10:00:00', '2024-04-12 10:00:00'),
('tx-046', 'TXN-20240415-INS004', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm tháng 4', 'COMPLETED', 49000000.0000, 'acc-008', 'acc-016', '2024-04-15 09:00:00', '2024-04-15 09:00:00'),
('tx-047', 'TXN-20240418-FRE004', 'DEPOSIT', 20000000.0000, 0, 'VND', 'Freelance E-commerce milestone 1', 'COMPLETED', 46780000.0000, NULL, 'acc-006', '2024-04-18 16:00:00', '2024-04-18 16:00:00'),
('tx-048', 'TXN-20240425-SAV004', 'TRANSFER', 15000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà T4', 'COMPLETED', 235000000.0000, 'acc-010', 'acc-011', '2024-04-25 08:00:00', '2024-04-25 08:00:00'),
('tx-049', 'TXN-20240430-BNS001', 'DEPOSIT', 8000000.0000, 0, 'VND', 'Thưởng lễ 30/4 1/5', 'COMPLETED', 29130000.0000, NULL, 'acc-005', '2024-04-30 08:00:00', '2024-04-30 08:00:00');

-- MAY 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-050', 'TXN-20240501-SAL010', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 5/2024', 'COMPLETED', 54130000.0000, NULL, 'acc-005', '2024-05-01 08:00:00', '2024-05-01 08:00:00'),
('tx-051', 'TXN-20240501-SAL011', 'DEPOSIT', 18000000.0000, 0, 'VND', 'Lương tháng 5/2024 công ty XYZ', 'COMPLETED', 64500000.0000, NULL, 'acc-008', '2024-05-01 09:00:00', '2024-05-01 09:00:00'),
('tx-052', 'TXN-20240503-RNT005', 'TRANSFER', 5500000.0000, 0, 'VND', 'Tiền thuê nhà tháng 5', 'COMPLETED', 48630000.0000, 'acc-005', 'acc-010', '2024-05-03 10:00:00', '2024-05-03 10:00:00'),
('tx-053', 'TXN-20240510-FRE005', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Freelance E-commerce milestone 2', 'COMPLETED', 71780000.0000, NULL, 'acc-006', '2024-05-10 16:00:00', '2024-05-10 16:00:00'),
('tx-054', 'TXN-20240512-SHP005', 'TRANSFER', 8900000.0000, 0, 'VND', 'Mua iPhone case airpods', 'COMPLETED', 39180000.0000, 'acc-005', 'acc-016', '2024-05-12 14:00:00', '2024-05-12 14:00:00'),
('tx-055', 'TXN-20240515-INS005', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm tháng 5', 'COMPLETED', 62000000.0000, 'acc-008', 'acc-016', '2024-05-15 09:00:00', '2024-05-15 09:00:00'),
('tx-056', 'TXN-20240518-WED001', 'TRANSFER', 5000000.0000, 0, 'VND', 'Mừng đám cưới bạn Hải', 'COMPLETED', 34180000.0000, 'acc-005', 'acc-014', '2024-05-18 08:00:00', '2024-05-18 08:00:00'),
('tx-057', 'TXN-20240522-TAX001', 'TRANSFER', 3200000.0000, 0, 'VND', 'Thuế thu nhập cá nhân Q1', 'COMPLETED', 26480000.0000, 'acc-005', 'acc-016', '2024-05-22 10:00:00', '2024-05-22 10:00:00'),
('tx-058', 'TXN-20240525-SAV005', 'TRANSFER', 20000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà T5', 'COMPLETED', 245000000.0000, 'acc-010', 'acc-011', '2024-05-25 08:00:00', '2024-05-25 08:00:00');

-- JUNE 2024
INSERT INTO transactions (id, reference_number, type, amount, fee, currency, description, status, balance_after_transaction, source_account_id, destination_account_id, created_at, updated_at) VALUES
('tx-059', 'TXN-20240601-SAL012', 'DEPOSIT', 25000000.0000, 0, 'VND', 'Lương tháng 6/2024', 'COMPLETED', 50430000.0000, NULL, 'acc-005', '2024-06-01 08:00:00', '2024-06-01 08:00:00'),
('tx-060', 'TXN-20240601-SAL013', 'DEPOSIT', 22000000.0000, 0, 'VND', 'Lương T6 review tăng lương', 'COMPLETED', 84000000.0000, NULL, 'acc-008', '2024-06-01 09:00:00', '2024-06-01 09:00:00'),
('tx-061', 'TXN-20240603-RNT006', 'TRANSFER', 5500000.0000, 0, 'VND', 'Tiền thuê nhà tháng 6', 'COMPLETED', 44930000.0000, 'acc-005', 'acc-010', '2024-06-03 10:00:00', '2024-06-03 10:00:00'),
('tx-062', 'TXN-20240610-SHP006', 'TRANSFER', 15000000.0000, 0, 'VND', 'Mua laptop Dell mới cho công việc', 'COMPLETED', 34930000.0000, 'acc-005', 'acc-016', '2024-06-10 11:00:00', '2024-06-10 11:00:00'),
('tx-063', 'TXN-20240612-FRE006', 'DEPOSIT', 30000000.0000, 0, 'VND', 'Freelance E-commerce final payment', 'COMPLETED', 69330000.0000, NULL, 'acc-006', '2024-06-12 16:00:00', '2024-06-12 16:00:00'),
('tx-064', 'TXN-20240615-INS006', 'TRANSFER', 2500000.0000, 0, 'VND', 'Bảo hiểm tháng 6', 'COMPLETED', 81500000.0000, 'acc-008', 'acc-016', '2024-06-15 09:00:00', '2024-06-15 09:00:00'),
('tx-065', 'TXN-20240616-FAT001', 'TRANSFER', 10000000.0000, 0, 'VND', 'Biếu bố mẹ Fathers Day', 'COMPLETED', 24930000.0000, 'acc-005', 'acc-014', '2024-06-16 08:00:00', '2024-06-16 08:00:00'),
('tx-066', 'TXN-20240618-TRL002', 'TRANSFER', 12000000.0000, 0, 'VND', 'Đặt tour Phú Quốc 3N2D', 'COMPLETED', 12930000.0000, 'acc-005', 'acc-016', '2024-06-18 21:00:00', '2024-06-18 21:00:00'),
('tx-067', 'TXN-20240622-USD001', 'DEPOSIT', 2000.0000, 0, 'USD', 'Upwork freelance payment', 'COMPLETED', 5200.0000, NULL, 'acc-007', '2024-06-22 10:00:00', '2024-06-22 10:00:00'),
('tx-068', 'TXN-20240625-SAV006', 'TRANSFER', 20000000.0000, 0, 'VND', 'Gửi tiết kiệm mua nhà T6', 'COMPLETED', 250000000.0000, 'acc-010', 'acc-011', '2024-06-25 08:00:00', '2024-06-25 08:00:00'),
('tx-069', 'TXN-20240630-DIV001', 'DEPOSIT', 3500000.0000, 0, 'VND', 'Cổ tức quỹ đầu tư Q2', 'COMPLETED', 88500000.0000, NULL, 'acc-009', '2024-06-30 09:00:00', '2024-06-30 09:00:00'),
('tx-070', 'TXN-20240630-BNS002', 'DEPOSIT', 15000000.0000, 0, 'VND', 'Thưởng nửa năm 2024', 'COMPLETED', 22730000.0000, NULL, 'acc-005', '2024-06-30 10:00:00', '2024-06-30 10:00:00');

-- Audit Logs
INSERT INTO audit_logs (id, user_id, action, details, ip_address, user_agent, status, timestamp) VALUES
('aud-001', 'user-001', 'LOGIN', 'Successful login', '192.168.1.100', 'Mozilla/5.0 Chrome/120', 'SUCCESS', '2024-06-01 08:00:00'),
('aud-002', 'user-001', 'TRANSFER', 'Transfer 5000000 VND', '192.168.1.100', 'Mozilla/5.0 Chrome/120', 'SUCCESS', '2024-06-03 10:00:00'),
('aud-003', 'user-008', 'LOGIN', 'Failed login account disabled', '10.0.0.50', 'Mozilla/5.0 Firefox/122', 'FAILED', '2024-04-20 14:00:00'),
('aud-004', 'user-008', 'LOGIN', 'Failed login account disabled', '10.0.0.50', 'Mozilla/5.0 Firefox/122', 'FAILED', '2024-04-20 14:01:00'),
('aud-005', 'user-008', 'LOGIN', 'Failed login account disabled', '10.0.0.50', 'Mozilla/5.0 Firefox/122', 'FAILED', '2024-04-20 14:02:00'),
('aud-006', 'user-003', 'LOGIN', 'Successful login', '172.16.0.10', 'Mozilla/5.0 Safari/17.2', 'SUCCESS', '2024-06-15 09:00:00'),
('aud-007', 'admin-001', 'LOGIN', 'Successful admin login', '192.168.1.1', 'Mozilla/5.0 Chrome/120', 'SUCCESS', '2024-06-20 08:00:00');

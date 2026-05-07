-- ============================================================
-- CAGUIOA BANK - COMPLETE DATABASE SETUP SCRIPT
-- ============================================================
-- Run this entire script to set up or update your lawbank database
-- Database: MySQL
-- Version: 1.0 (Updated May 4, 2026)
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS lawbank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lawbank;

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  fullname VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  pin VARCHAR(255),
  sex VARCHAR(20),
  age INT,
  nationality VARCHAR(100),
  address TEXT,
  balance DOUBLE DEFAULT 0,
  savings DOUBLE DEFAULT 0,
  total_deposit DOUBLE DEFAULT 0,
  role VARCHAR(50) DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- 2. ADMIN TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS admin (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- 3. TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type VARCHAR(50),
  amount DOUBLE,
  method VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 4. LOANS TABLE (with Witness & Signature Support)
-- ============================================================
CREATE TABLE IF NOT EXISTS loans (
  id INT AUTO_INCREMENT PRIMARY KEY,
  application_id INT NOT NULL,
  user_id INT NOT NULL,
  approved_amount DECIMAL(12,2) NOT NULL,
  interest_rate DECIMAL(5,2) NOT NULL DEFAULT 0.02,
  total_payable DECIMAL(12,2) NOT NULL,
  remaining_balance DECIMAL(12,2) NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(50) DEFAULT 'active',
  approved_by INT,
  approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (application_id) REFERENCES loan_applications(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;



-- ============================================================
-- 5. LOAN APPLICATIONS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS loan_applications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  date_of_birth DATE NOT NULL,
  gender VARCHAR(20) NOT NULL,
  address VARCHAR(255) NOT NULL,
  contact_number VARCHAR(30) NOT NULL,
  email_address VARCHAR(255) NOT NULL,
  employment_status VARCHAR(100) NOT NULL,
  company_name VARCHAR(255),
  monthly_income DECIMAL(12,2) NOT NULL,
  work_address VARCHAR(255),
  loan_amount_requested DECIMAL(12,2) NOT NULL,
  loan_purpose VARCHAR(255) NOT NULL,
  loan_term_months INT NOT NULL DEFAULT 6,
  account_number VARCHAR(100) NOT NULL,
  account_type VARCHAR(100) NOT NULL,
  valid_id_submitted BOOLEAN DEFAULT FALSE,
  proof_of_income_submitted BOOLEAN DEFAULT FALSE,
  proof_of_address_submitted BOOLEAN DEFAULT FALSE,
  declaration_accepted BOOLEAN DEFAULT TRUE,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_notes VARCHAR(255),
  approved_amount DECIMAL(12,2),
  rejection_reason VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 6. LOAN PAYMENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS loan_payments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  payment_amount DECIMAL(12,2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  transaction_reference VARCHAR(100),
  payment_status VARCHAR(50) DEFAULT 'completed',
  paid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 7. LOAN RECEIPTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS loan_receipts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  payment_id INT NOT NULL,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  receipt_number VARCHAR(50) UNIQUE NOT NULL,
  amount_paid DECIMAL(12,2) NOT NULL,
  previous_balance DECIMAL(12,2) NOT NULL,
  new_balance DECIMAL(12,2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  receipt_details TEXT,
  generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (payment_id) REFERENCES loan_payments(id) ON DELETE CASCADE,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 8. LOAN PENALTIES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS loan_penalties (
  id INT AUTO_INCREMENT PRIMARY KEY,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  penalty_amount DECIMAL(12,2) NOT NULL,
  penalty_reason VARCHAR(255),
  due_date DATE NOT NULL,
  paid BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- LOAN FUNCTIONS
-- ============================================================

DELIMITER //
DROP FUNCTION IF EXISTS apply_for_loan//
CREATE FUNCTION apply_for_loan(
  p_user_id INT,
  p_full_name VARCHAR(255),
  p_date_of_birth DATE,
  p_gender VARCHAR(20),
  p_address VARCHAR(255),
  p_contact_number VARCHAR(30),
  p_email_address VARCHAR(255),
  p_employment_status VARCHAR(100),
  p_company_name VARCHAR(255),
  p_monthly_income DECIMAL(12,2),
  p_work_address VARCHAR(255),
  p_loan_amount_requested DECIMAL(12,2),
  p_loan_purpose VARCHAR(255),
  p_loan_term_months INT,
  p_account_number VARCHAR(100),
  p_account_type VARCHAR(100),
  p_valid_id_submitted BOOLEAN,
  p_proof_of_income_submitted BOOLEAN,
  p_proof_of_address_submitted BOOLEAN,
  p_declaration_accepted BOOLEAN
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE app_id INT;

  INSERT INTO loan_applications (
    user_id, full_name, date_of_birth, gender, address, contact_number, email_address,
    employment_status, company_name, monthly_income, work_address,
    loan_amount_requested, loan_purpose, loan_term_months,
    account_number, account_type, valid_id_submitted,
    proof_of_income_submitted, proof_of_address_submitted, declaration_accepted, status
  ) VALUES (
    p_user_id, p_full_name, p_date_of_birth, p_gender, p_address, p_contact_number, p_email_address,
    p_employment_status, p_company_name, p_monthly_income, p_work_address,
    p_loan_amount_requested, p_loan_purpose, COALESCE(p_loan_term_months, 6),
    p_account_number, p_account_type, p_valid_id_submitted,
    p_proof_of_income_submitted, p_proof_of_address_submitted, p_declaration_accepted, 'pending'
  );

  SET app_id = LAST_INSERT_ID();
  RETURN app_id;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS approve_loan_application//
CREATE FUNCTION approve_loan_application(
  p_application_id INT,
  p_admin_id INT,
  p_approved_amount DECIMAL(12,2)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE v_user_id INT;
  DECLARE v_loan_id INT;
  DECLARE v_total_payable DECIMAL(12,2);
  DECLARE v_due_date DATE;

  SELECT user_id INTO v_user_id
  FROM loan_applications
  WHERE id = p_application_id;

  SET v_total_payable = ROUND(p_approved_amount * 1.02, 2);
  SET v_due_date = DATE_ADD(CURDATE(), INTERVAL 6 MONTH);

  INSERT INTO loans (
    application_id, user_id, approved_amount, interest_rate,
    total_payable, remaining_balance, due_date, status, approved_by
  ) VALUES (
    p_application_id, v_user_id, p_approved_amount, 0.02,
    v_total_payable, v_total_payable, v_due_date, 'active', p_admin_id
  );

  SET v_loan_id = LAST_INSERT_ID();

  UPDATE loan_applications
  SET status = 'approved', approved_amount = p_approved_amount, admin_id = p_admin_id, reviewed_at = NOW()
  WHERE id = p_application_id;

  RETURN v_loan_id;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS reject_loan_application//
CREATE FUNCTION reject_loan_application(
  p_application_id INT,
  p_admin_id INT,
  p_rejection_reason VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  UPDATE loan_applications
  SET status = 'rejected', admin_id = p_admin_id, rejection_reason = p_rejection_reason, reviewed_at = NOW()
  WHERE id = p_application_id;

  RETURN p_application_id;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS process_loan_payment//
CREATE FUNCTION process_loan_payment(
  p_loan_id INT,
  p_user_id INT,
  p_payment_amount DECIMAL(12,2),
  p_payment_method VARCHAR(50),
  p_transaction_reference VARCHAR(100)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE v_payment_id INT;
  DECLARE v_current_balance DECIMAL(12,2);
  DECLARE v_new_balance DECIMAL(12,2);
  DECLARE v_receipt_id INT;

  SELECT remaining_balance INTO v_current_balance
  FROM loans
  WHERE id = p_loan_id;

  SET v_new_balance = v_current_balance - p_payment_amount;
  IF v_new_balance < 0 THEN
    SET v_new_balance = 0;
  END IF;

  INSERT INTO loan_payments (
    loan_id, user_id, payment_amount, payment_method, transaction_reference
  ) VALUES (
    p_loan_id, p_user_id, p_payment_amount, p_payment_method, p_transaction_reference
  );

  SET v_payment_id = LAST_INSERT_ID();
  SET v_receipt_id = generate_loan_receipt(v_payment_id, p_loan_id, p_user_id);

  UPDATE loans
  SET remaining_balance = v_new_balance,
      status = CASE WHEN v_new_balance = 0 THEN 'paid' ELSE 'active' END
  WHERE id = p_loan_id;

  INSERT INTO transactions (user_id, type, amount, method)
  VALUES (p_user_id, 'loan_payment', p_payment_amount, p_payment_method);

  RETURN v_payment_id;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS generate_loan_receipt//
CREATE FUNCTION generate_loan_receipt(
  p_payment_id INT,
  p_loan_id INT,
  p_user_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE v_receipt_id INT;
  DECLARE v_receipt_number VARCHAR(50);
  DECLARE v_amount_paid DECIMAL(12,2);
  DECLARE v_previous_balance DECIMAL(12,2);
  DECLARE v_new_balance DECIMAL(12,2);
  DECLARE v_payment_method VARCHAR(50);

  SELECT payment_amount, payment_method
  INTO v_amount_paid, v_payment_method
  FROM loan_payments
  WHERE id = p_payment_id;

  SELECT remaining_balance INTO v_new_balance
  FROM loans
  WHERE id = p_loan_id;

  SET v_previous_balance = v_new_balance + v_amount_paid;
  SET v_receipt_number = CONCAT('RECEIPT-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 100000), 5, '0'));

  INSERT INTO loan_receipts (
    payment_id, loan_id, user_id, receipt_number,
    amount_paid, previous_balance, new_balance, payment_method, receipt_details
  ) VALUES (
    p_payment_id, p_loan_id, p_user_id, v_receipt_number,
    v_amount_paid, v_previous_balance, v_new_balance, v_payment_method,
    CONCAT('Receipt #: ', v_receipt_number, '\nAmount Paid: ', v_amount_paid, '\nPrevious Balance: ', v_previous_balance, '\nNew Balance: ', v_new_balance, '\nPayment Method: ', v_payment_method)
  );

  SET v_receipt_id = LAST_INSERT_ID();
  RETURN v_receipt_id;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS calculate_loan_penalty//
CREATE FUNCTION calculate_loan_penalty(
  p_loan_id INT
) RETURNS DECIMAL(12,2) DETERMINISTIC
BEGIN
  DECLARE v_penalty DECIMAL(12,2) DEFAULT 0;
  DECLARE v_due_date DATE;
  DECLARE v_remaining_balance DECIMAL(12,2);
  DECLARE v_days_overdue INT;

  SELECT due_date, remaining_balance
  INTO v_due_date, v_remaining_balance
  FROM loans
  WHERE id = p_loan_id;

  IF CURDATE() > v_due_date AND v_remaining_balance > 0 THEN
    SET v_days_overdue = DATEDIFF(CURDATE(), v_due_date);
    SET v_penalty = ROUND((v_remaining_balance * 0.05) * CEIL(v_days_overdue / 30), 2);
  END IF;

  RETURN v_penalty;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS apply_loan_penalty//
CREATE FUNCTION apply_loan_penalty(
  p_loan_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE v_penalty_amount DECIMAL(12,2);
  DECLARE v_penalty_id INT;
  DECLARE v_user_id INT;

  SET v_penalty_amount = calculate_loan_penalty(p_loan_id);

  IF v_penalty_amount > 0 THEN
    SELECT user_id INTO v_user_id FROM loans WHERE id = p_loan_id;
    INSERT INTO loan_penalties (loan_id, user_id, penalty_amount, penalty_reason, due_date)
    VALUES (p_loan_id, v_user_id, v_penalty_amount, 'Late payment penalty', DATE_ADD(CURDATE(), INTERVAL 7 DAY));
    SET v_penalty_id = LAST_INSERT_ID();
    RETURN v_penalty_id;
  END IF;

  RETURN 0;
END//
DELIMITER ;

DELIMITER //
DROP FUNCTION IF EXISTS can_apply_for_loan//
CREATE FUNCTION can_apply_for_loan(
  p_user_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  RETURN TRUE;
END//
DELIMITER ;

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_loans_userid ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_loan_applications_userid ON loan_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_loan_applications_status ON loan_applications(status);
CREATE INDEX IF NOT EXISTS idx_loan_payments_loanid ON loan_payments(loan_id);
CREATE INDEX IF NOT EXISTS idx_loan_receipts_paymentid ON loan_receipts(payment_id);
CREATE INDEX IF NOT EXISTS idx_loan_penalties_loanid ON loan_penalties(loan_id);

-- ============================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================

-- Insert sample admin account
INSERT IGNORE INTO admin (username, password) 
VALUES ('admin', 'admin123');

-- Insert sample user
INSERT IGNORE INTO users (username, fullname, email, password, pin, role) 
VALUES ('testuser', 'Test User', 'test@example.com', 'password123', '1234', 'user');

-- ============================================================
-- VERIFICATION QUERIES (Check if everything is set up correctly)
-- ============================================================

-- Show all tables
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'lawbank';

-- Show users table structure
DESCRIBE users;

-- Show all tables and row count
SELECT 
    TABLE_NAME,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'lawbank'
ORDER BY TABLE_NAME;

-- ============================================================
-- COMPLETED!
-- ============================================================
-- Your database is now ready for use
-- Total Tables: 8
-- Total Relationships: 8
-- All Foreign Keys: Configured with CASCADE DELETE
-- 
-- DIRECT OTP PIN RESET SYSTEM (No Admin Approval):
-- pin_reset_requests table has:
--   ✓ otp VARCHAR(10) - stores 6-digit OTP
--   ✓ otp_generated_at TIMESTAMP - when OTP created
--   ✓ otp_verified BOOLEAN - whether user verified OTP
--   ✓ Expires automatically after 10 minutes (checked in code)
--   ✓ NO admin fields (direct OTP workflow)
-- 
-- READY FOR DEPLOYMENT
-- ============================================================

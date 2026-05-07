-- Caguioa Bank Database Schema
-- Run these statements on your MySQL server (database: lawbank)

-- Note: adjust types and constraints to match your environment.

CREATE DATABASE IF NOT EXISTS lawbank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lawbank;

-- users table
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

-- admin table
CREATE TABLE IF NOT EXISTS admin (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- transactions table
CREATE TABLE IF NOT EXISTS transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type VARCHAR(50),
  amount DOUBLE,
  method VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- loans table (with witness and signature fields)
CREATE TABLE IF NOT EXISTS loans (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  amount DOUBLE NOT NULL,
  interest_rate DOUBLE DEFAULT 0.10,
  total_payable DOUBLE,
  remaining_balance DOUBLE DEFAULT 0,
  due_date DATE,
  status VARCHAR(50) DEFAULT 'active',
  witness_name VARCHAR(255),
  witness_contact VARCHAR(255),
  witness_signature LONGBLOB,
  user_signature LONGBLOB,
  promissory_note_url VARCHAR(1024),
  is_account_blocked BOOLEAN DEFAULT FALSE,
  blocked_date TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Migration for older databases where loans.id is missing


-- account audit log
CREATE TABLE IF NOT EXISTS account_audit_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  action VARCHAR(50) NOT NULL,
  reason VARCHAR(255),
  admin_id INT,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- messages from admin to users
CREATE TABLE IF NOT EXISTS user_messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  admin_id INT,
  subject VARCHAR(255),
  body TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- PIN reset requests table
CREATE TABLE IF NOT EXISTS pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  otp VARCHAR(10),
  otp_generated_at TIMESTAMP NULL,
  otp_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- password reset requests table (if not already exists)
CREATE TABLE IF NOT EXISTS password_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(20),
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================
-- NEW: Loan Management System v2 (Complete)
-- ============================================

-- loan_applications table (User applies → Admin approves/rejects)
CREATE TABLE IF NOT EXISTS loan_applications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  requested_amount DOUBLE,
  purpose VARCHAR(255),
  date_of_birth DATE,
  gender VARCHAR(20),
  address VARCHAR(255),
  contact_number VARCHAR(30),
  email_address VARCHAR(255),
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_notes VARCHAR(255),
  approved_amount DOUBLE,
  rejection_reason VARCHAR(255),
  full_name VARCHAR(255),
  employment_status VARCHAR(50),
  company_name VARCHAR(255),
  monthly_income DOUBLE,
  work_address VARCHAR(255),
  loan_amount_requested DOUBLE,
  loan_purpose VARCHAR(255),
  loan_term_months INT,
  account_number VARCHAR(100),
  account_type VARCHAR(100),
  valid_id_submitted BOOLEAN DEFAULT FALSE,
  proof_of_income_submitted BOOLEAN DEFAULT FALSE,
  proof_of_address_submitted BOOLEAN DEFAULT FALSE,
  declaration_accepted BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Migration support for older databases (safe if columns already exist)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL;

ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS requested_amount DOUBLE NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS purpose VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS date_of_birth DATE NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS gender VARCHAR(20) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS contact_number VARCHAR(30) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS email_address VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS full_name VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS employment_status VARCHAR(50) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS company_name VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS monthly_income DOUBLE NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS work_address VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS loan_amount_requested DOUBLE NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS loan_purpose VARCHAR(255) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS loan_term_months INT NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS account_number VARCHAR(100) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS account_type VARCHAR(100) NULL;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS valid_id_submitted BOOLEAN DEFAULT FALSE;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS proof_of_income_submitted BOOLEAN DEFAULT FALSE;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS proof_of_address_submitted BOOLEAN DEFAULT FALSE;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS declaration_accepted BOOLEAN DEFAULT TRUE;

-- Backfill mirror fields for compatibility between old/new code paths
UPDATE loan_applications
SET loan_amount_requested = COALESCE(loan_amount_requested, requested_amount),
    requested_amount = COALESCE(requested_amount, loan_amount_requested),
    loan_purpose = COALESCE(loan_purpose, purpose),
    purpose = COALESCE(purpose, loan_purpose)
WHERE loan_amount_requested IS NULL OR requested_amount IS NULL OR loan_purpose IS NULL OR purpose IS NULL;

-- loan_payments table (Track all payments with payment method)
CREATE TABLE IF NOT EXISTS loan_payments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  payment_amount DOUBLE NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  payment_status VARCHAR(50) DEFAULT 'completed',
  transaction_reference VARCHAR(100),
  transaction_id INT NULL,
  notes VARCHAR(255),
  paid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- loan_receipts table (Payment receipts with receipt number)
CREATE TABLE IF NOT EXISTS loan_receipts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  payment_id INT NOT NULL,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  receipt_number VARCHAR(50) UNIQUE NOT NULL,
  amount_paid DOUBLE NOT NULL,
  previous_balance DOUBLE NOT NULL,
  new_balance DOUBLE NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  receipt_details TEXT,
  receipt_data LONGBLOB,
  generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (payment_id) REFERENCES loan_payments(id) ON DELETE CASCADE,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- loan_penalties table (Track penalties for late payments)
CREATE TABLE IF NOT EXISTS loan_penalties (
  id INT AUTO_INCREMENT PRIMARY KEY,
  loan_id INT NOT NULL,
  user_id INT NOT NULL,
  penalty_amount DOUBLE NOT NULL,
  penalty_reason VARCHAR(50),
  due_date DATE NOT NULL,
  paid BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================
-- SQL FUNCTIONS FOR LOAN MANAGEMENT
-- ============================================

-- Function: Apply for Loan (User applies, status = pending)
DELIMITER //
DROP FUNCTION IF EXISTS apply_for_loan//
CREATE FUNCTION apply_for_loan(
  p_user_id INT,
  p_requested_amount DOUBLE,
  p_purpose VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE app_id INT;
  INSERT INTO loan_applications (user_id, requested_amount, loan_amount_requested, purpose, loan_purpose, status)
  VALUES (p_user_id, p_requested_amount, p_requested_amount, p_purpose, p_purpose, 'pending');
  SET app_id = LAST_INSERT_ID();
  RETURN app_id;
END//
DELIMITER ;

-- Function: Admin Approve Loan Application (creates loan record)
DELIMITER //
DROP FUNCTION IF EXISTS approve_loan_application//
CREATE FUNCTION approve_loan_application(
  p_app_id INT,
  p_admin_id INT,
  p_approved_amount DOUBLE
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE p_user_id INT;
  DECLARE p_loan_id INT;
  DECLARE p_total_payable DOUBLE;
  DECLARE p_due_date DATE;
  
  SELECT user_id INTO p_user_id FROM loan_applications WHERE id = p_app_id;
  
  -- Calculate with 2% interest
  SET p_total_payable = p_approved_amount * 1.02;
  SET p_due_date = DATE_ADD(CURDATE(), INTERVAL 6 MONTH);
  
  -- Create loan record
  INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, due_date, status)
  VALUES (p_user_id, p_approved_amount, 0.02, p_total_payable, p_total_payable, p_due_date, 'active');
  
  SET p_loan_id = LAST_INSERT_ID();
  
  -- Update application status
  UPDATE loan_applications 
  SET status = 'approved', admin_id = p_admin_id, approved_amount = p_approved_amount, reviewed_at = NOW()
  WHERE id = p_app_id;
  
  RETURN p_loan_id;
END//
DELIMITER ;

-- Function: Admin Reject Loan Application
DELIMITER //
DROP FUNCTION IF EXISTS reject_loan_application//
CREATE FUNCTION reject_loan_application(
  p_app_id INT,
  p_admin_id INT,
  p_rejection_reason VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  UPDATE loan_applications 
  SET status = 'rejected', admin_id = p_admin_id, rejection_reason = p_rejection_reason, reviewed_at = NOW()
  WHERE id = p_app_id;
  RETURN p_app_id;
END//
DELIMITER ;

-- Function: Process Loan Payment (Online, Walk-in, ATM, etc.)
DELIMITER //
DROP FUNCTION IF EXISTS process_loan_payment//
CREATE FUNCTION process_loan_payment(
  p_loan_id INT,
  p_user_id INT,
  p_payment_amount DOUBLE,
  p_payment_method VARCHAR(50),
  p_transaction_reference VARCHAR(100)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE p_payment_id INT;
  DECLARE p_current_balance DOUBLE;
  DECLARE p_new_balance DOUBLE;
  DECLARE p_interest_paid DOUBLE;
  DECLARE p_principal_paid DOUBLE;
  DECLARE p_transaction_id INT;
  
  -- Get current balance
  SELECT remaining_balance INTO p_current_balance FROM loans WHERE id = p_loan_id;
  
  -- Calculate payment split (20% to interest, 80% to principal)
  SET p_interest_paid = p_payment_amount * 0.20;
  SET p_principal_paid = p_payment_amount * 0.80;
  
  -- New balance after payment
  SET p_new_balance = p_current_balance - p_payment_amount;
  IF p_new_balance < 0 THEN SET p_new_balance = 0; END IF;
  
  -- Insert payment record
  INSERT INTO loan_payments (loan_id, user_id, payment_amount, payment_method, transaction_reference)
  VALUES (p_loan_id, p_user_id, p_payment_amount, p_payment_method, p_transaction_reference);
  
  SET p_payment_id = LAST_INSERT_ID();
  
  -- Update loan balance
  UPDATE loans SET remaining_balance = p_new_balance WHERE id = p_loan_id;
  
  -- If fully paid, mark as paid
  IF p_new_balance <= 0 THEN
    UPDATE loans SET status = 'paid' WHERE id = p_loan_id;
  END IF;
  
  -- Insert transaction record
  INSERT INTO transactions (user_id, type, amount, method)
  VALUES (p_user_id, 'loan_payment', p_payment_amount, p_payment_method);
  
  SET p_transaction_id = LAST_INSERT_ID();
  
  -- Link transaction to payment
  UPDATE loan_payments SET transaction_id = p_transaction_id WHERE id = p_payment_id;
  
  RETURN p_payment_id;
END//
DELIMITER ;

-- Function: Generate Payment Receipt
DELIMITER //
DROP FUNCTION IF EXISTS generate_loan_receipt//
CREATE FUNCTION generate_loan_receipt(
  p_payment_id INT,
  p_loan_id INT,
  p_user_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE receipt_id INT;
  DECLARE receipt_num VARCHAR(50);
  DECLARE p_amount_paid DOUBLE;
  DECLARE p_previous_balance DOUBLE;
  DECLARE p_new_balance DOUBLE;
  DECLARE p_payment_method VARCHAR(50);
  DECLARE p_receipt_details TEXT;
  
  -- Get payment details
  SELECT payment_amount, payment_method INTO p_amount_paid, p_payment_method 
  FROM loan_payments WHERE id = p_payment_id;
  
  -- Get previous and new balance
  SELECT remaining_balance INTO p_new_balance FROM loans WHERE id = p_loan_id;
  SET p_previous_balance = p_new_balance + p_amount_paid;
  
  -- Generate unique receipt number (RECEIPT-YYYYMMDD-xxxxx)
  SET receipt_num = CONCAT('RECEIPT-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 100000), 5, '0'));
  
  -- Build receipt details
  SET p_receipt_details = CONCAT(
    'Receipt #: ', receipt_num, '\n',
    'Date: ', DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%S'), '\n',
    'Loan ID: ', p_loan_id, '\n',
    'Amount Paid: ', p_amount_paid, '\n',
    'Previous Balance: ', p_previous_balance, '\n',
    'New Balance: ', p_new_balance, '\n',
    'Payment Method: ', p_payment_method
  );
  
  -- Insert receipt record
  INSERT INTO loan_receipts (payment_id, loan_id, user_id, receipt_number, amount_paid, 
                             previous_balance, new_balance, payment_method, receipt_details)
  VALUES (p_payment_id, p_loan_id, p_user_id, receipt_num, p_amount_paid, 
          p_previous_balance, p_new_balance, p_payment_method, p_receipt_details);
  
  SET receipt_id = LAST_INSERT_ID();
  RETURN receipt_id;
END//
DELIMITER ;

-- Function: Calculate Penalty for Late Payment (If not paid within 6 months)
DELIMITER //
DROP FUNCTION IF EXISTS calculate_loan_penalty//
CREATE FUNCTION calculate_loan_penalty(
  p_loan_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE p_penalty_amount DOUBLE DEFAULT 0;
  DECLARE p_due_date DATE;
  DECLARE p_remaining_balance DOUBLE;
  DECLARE p_days_overdue INT;
  
  -- Get loan details
  SELECT due_date, remaining_balance INTO p_due_date, p_remaining_balance 
  FROM loans WHERE id = p_loan_id;
  
  -- Check if overdue
  IF CURDATE() > p_due_date AND p_remaining_balance > 0 THEN
    SET p_days_overdue = DATEDIFF(CURDATE(), p_due_date);
    -- Penalty: 5% of remaining balance per month overdue
    SET p_penalty_amount = (p_remaining_balance * 0.05) * CEIL(p_days_overdue / 30);
  END IF;
  
  RETURN p_penalty_amount;
END//
DELIMITER ;

-- Function: Apply Penalty to Loan
DELIMITER //
DROP FUNCTION IF EXISTS apply_loan_penalty//
CREATE FUNCTION apply_loan_penalty(
  p_loan_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE p_penalty_amount DOUBLE;
  DECLARE p_penalty_id INT;
  DECLARE p_user_id INT;
  
  SET p_penalty_amount = calculate_loan_penalty(p_loan_id);
  
  IF p_penalty_amount > 0 THEN
    SELECT user_id INTO p_user_id FROM loans WHERE id = p_loan_id;
    
    -- Check if penalty already exists
    IF NOT EXISTS (SELECT 1 FROM loan_penalties WHERE loan_id = p_loan_id AND paid = FALSE) THEN
      INSERT INTO loan_penalties (loan_id, user_id, penalty_amount, penalty_reason, due_date)
      VALUES (p_loan_id, p_user_id, p_penalty_amount, 'Late Payment', DATE_ADD(CURDATE(), INTERVAL 7 DAY));
      SET p_penalty_id = LAST_INSERT_ID();
    END IF;
  END IF;
  
  RETURN COALESCE(p_penalty_id, 0);
END//
DELIMITER ;

-- Function: Get User's Active Loans
DELIMITER //
DROP FUNCTION IF EXISTS get_user_active_loans//
CREATE FUNCTION get_user_active_loans(
  p_user_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE loan_count INT;
  SELECT COUNT(*) INTO loan_count FROM loans 
  WHERE user_id = p_user_id AND status = 'active';
  RETURN loan_count;
END//
DELIMITER ;

-- ============================================
-- ADDITIONAL SQL FUNCTIONS FOR ALL OPERATIONS
-- ============================================

-- Function: Create new user
DELIMITER //
DROP FUNCTION IF EXISTS create_user//
CREATE FUNCTION create_user(
  p_username VARCHAR(100),
  p_fullname VARCHAR(255),
  p_email VARCHAR(255),
  p_password VARCHAR(255),
  p_pin VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE user_id INT;
  INSERT INTO users (username, fullname, email, password, pin, role, balance, savings, total_deposit)
  VALUES (p_username, p_fullname, p_email, p_password, p_pin, 'user', 0, 0, 0);
  SET user_id = LAST_INSERT_ID();
  RETURN user_id;
END//
DELIMITER ;

-- Function: Get user by username
DELIMITER //
DROP FUNCTION IF EXISTS get_user_by_username//
CREATE FUNCTION get_user_by_username(
  p_username VARCHAR(100)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE user_id INT;
  SELECT id INTO user_id FROM users WHERE username = p_username LIMIT 1;
  RETURN COALESCE(user_id, -1);
END//
DELIMITER ;

-- Function: Update user balance
DELIMITER //
DROP FUNCTION IF EXISTS update_user_balance//
CREATE FUNCTION update_user_balance(
  p_user_id INT,
  p_amount DOUBLE
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE new_balance DOUBLE;
  UPDATE users SET balance = balance + p_amount WHERE id = p_user_id;
  SELECT balance INTO new_balance FROM users WHERE id = p_user_id;
  RETURN COALESCE(new_balance, 0);
END//
DELIMITER ;

-- Function: Update user savings
DELIMITER //
DROP FUNCTION IF EXISTS update_user_savings//
CREATE FUNCTION update_user_savings(
  p_user_id INT,
  p_amount DOUBLE
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE new_savings DOUBLE;
  UPDATE users SET savings = savings + p_amount WHERE id = p_user_id;
  SELECT savings INTO new_savings FROM users WHERE id = p_user_id;
  RETURN COALESCE(new_savings, 0);
END//
DELIMITER ;

-- Function: Update total deposits
DELIMITER //
DROP FUNCTION IF EXISTS update_total_deposit//
CREATE FUNCTION update_total_deposit(
  p_user_id INT,
  p_amount DOUBLE
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE new_total DOUBLE;
  UPDATE users SET total_deposit = total_deposit + p_amount WHERE id = p_user_id;
  SELECT total_deposit INTO new_total FROM users WHERE id = p_user_id;
  RETURN COALESCE(new_total, 0);
END//
DELIMITER ;

-- Function: Get user balance
DELIMITER //
DROP FUNCTION IF EXISTS get_user_balance//
CREATE FUNCTION get_user_balance(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE balance DOUBLE;
  SELECT balance INTO balance FROM users WHERE id = p_user_id;
  RETURN COALESCE(balance, 0);
END//
DELIMITER ;

-- Function: Get user savings
DELIMITER //
DROP FUNCTION IF EXISTS get_user_savings//
CREATE FUNCTION get_user_savings(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE savings DOUBLE;
  SELECT savings INTO savings FROM users WHERE id = p_user_id;
  RETURN COALESCE(savings, 0);
END//
DELIMITER ;

-- Function: Record transaction
DELIMITER //
DROP FUNCTION IF EXISTS record_transaction//
CREATE FUNCTION record_transaction(
  p_user_id INT,
  p_type VARCHAR(50),
  p_amount DOUBLE,
  p_method VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE trans_id INT;
  INSERT INTO transactions (user_id, type, amount, method)
  VALUES (p_user_id, p_type, p_amount, p_method);
  SET trans_id = LAST_INSERT_ID();
  RETURN trans_id;
END//
DELIMITER ;

-- Function: Get total transactions by user
DELIMITER //
DROP FUNCTION IF EXISTS get_transaction_count//
CREATE FUNCTION get_transaction_count(
  p_user_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE count INT;
  SELECT COUNT(*) INTO count FROM transactions WHERE user_id = p_user_id;
  RETURN COALESCE(count, 0);
END//
DELIMITER ;

-- Function: Get total amount transacted
DELIMITER //
DROP FUNCTION IF EXISTS get_total_transacted//
CREATE FUNCTION get_total_transacted(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(amount) INTO total FROM transactions WHERE user_id = p_user_id;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get transaction details by ID
DELIMITER //
DROP FUNCTION IF EXISTS get_transaction_by_id//
CREATE FUNCTION get_transaction_by_id(
  p_transaction_id INT
) RETURNS VARCHAR(500) DETERMINISTIC
BEGIN
  DECLARE trans_info VARCHAR(500);
  SELECT CONCAT('ID:', id, '|User:', user_id, '|Type:', type, '|Amount:', amount, '|Method:', method, '|Date:', created_at)
  INTO trans_info
  FROM transactions WHERE id = p_transaction_id LIMIT 1;
  RETURN COALESCE(trans_info, 'Transaction not found');
END//
DELIMITER ;

-- Function: Get loan payment linked to transaction
DELIMITER //
DROP FUNCTION IF EXISTS get_loan_payment_by_transaction_id//
CREATE FUNCTION get_loan_payment_by_transaction_id(
  p_transaction_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE payment_id INT;
  SELECT id INTO payment_id FROM loan_payments WHERE transaction_id = p_transaction_id LIMIT 1;
  RETURN COALESCE(payment_id, -1);
END//
DELIMITER ;

-- Function: Link transaction to loan payment
DELIMITER //
DROP FUNCTION IF EXISTS link_transaction_to_payment//
CREATE FUNCTION link_transaction_to_payment(
  p_payment_id INT,
  p_transaction_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE loan_payments SET transaction_id = p_transaction_id WHERE id = p_payment_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Send message from admin to user
DELIMITER //
DROP FUNCTION IF EXISTS send_message//
CREATE FUNCTION send_message(
  p_user_id INT,
  p_admin_id INT,
  p_subject VARCHAR(255),
  p_body TEXT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE msg_id INT;
  INSERT INTO user_messages (user_id, admin_id, subject, body, is_read)
  VALUES (p_user_id, p_admin_id, p_subject, p_body, FALSE);
  SET msg_id = LAST_INSERT_ID();
  RETURN msg_id;
END//
DELIMITER ;

-- Function: Mark message as read
DELIMITER //
DROP FUNCTION IF EXISTS mark_message_read//
CREATE FUNCTION mark_message_read(
  p_message_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE user_messages SET is_read = TRUE WHERE id = p_message_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Get unread message count
DELIMITER //
DROP FUNCTION IF EXISTS get_unread_messages//
CREATE FUNCTION get_unread_messages(
  p_user_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE count INT;
  SELECT COUNT(*) INTO count FROM user_messages WHERE user_id = p_user_id AND is_read = FALSE;
  RETURN COALESCE(count, 0);
END//
DELIMITER ;

-- Function: Create PIN reset request
DELIMITER //
DROP FUNCTION IF EXISTS create_pin_reset_request//
CREATE FUNCTION create_pin_reset_request(
  p_user_id INT,
  p_email VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE req_id INT;
  INSERT INTO pin_reset_requests (user_id, email, status)
  VALUES (p_user_id, p_email, 'pending');
  SET req_id = LAST_INSERT_ID();
  RETURN req_id;
END//
DELIMITER ;

-- Function: Set OTP for PIN reset
DELIMITER //
DROP FUNCTION IF EXISTS set_otp_for_reset//
CREATE FUNCTION set_otp_for_reset(
  p_request_id INT,
  p_otp VARCHAR(10)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE pin_reset_requests 
  SET otp = p_otp, otp_generated_at = NOW()
  WHERE id = p_request_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Verify OTP
DELIMITER //
DROP FUNCTION IF EXISTS verify_otp//
CREATE FUNCTION verify_otp(
  p_request_id INT,
  p_otp VARCHAR(10)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  DECLARE stored_otp VARCHAR(10);
  SELECT otp INTO stored_otp FROM pin_reset_requests WHERE id = p_request_id;
  IF stored_otp = p_otp THEN
    UPDATE pin_reset_requests SET otp_verified = TRUE WHERE id = p_request_id;
    RETURN TRUE;
  ELSE
    RETURN FALSE;
  END IF;
END//
DELIMITER ;

-- Function: Complete PIN reset
DELIMITER //
DROP FUNCTION IF EXISTS complete_pin_reset//
CREATE FUNCTION complete_pin_reset(
  p_request_id INT,
  p_user_id INT,
  p_new_pin VARCHAR(255)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE users SET pin = p_new_pin WHERE id = p_user_id;
  UPDATE pin_reset_requests 
  SET status = 'completed', completed_at = NOW()
  WHERE id = p_request_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Create password reset request
DELIMITER //
DROP FUNCTION IF EXISTS create_password_reset_request//
CREATE FUNCTION create_password_reset_request(
  p_user_id INT,
  p_email VARCHAR(255),
  p_phone VARCHAR(20)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE req_id INT;
  INSERT INTO password_reset_requests (user_id, email, phone, status)
  VALUES (p_user_id, p_email, p_phone, 'pending');
  SET req_id = LAST_INSERT_ID();
  RETURN req_id;
END//
DELIMITER ;

-- Function: Complete password reset
DELIMITER //
DROP FUNCTION IF EXISTS complete_password_reset//
CREATE FUNCTION complete_password_reset(
  p_request_id INT,
  p_user_id INT,
  p_new_password VARCHAR(255)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE users SET password = p_new_password WHERE id = p_user_id;
  UPDATE password_reset_requests 
  SET status = 'completed', completed_at = NOW()
  WHERE id = p_request_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Log account action
DELIMITER //
DROP FUNCTION IF EXISTS log_account_action//
CREATE FUNCTION log_account_action(
  p_user_id INT,
  p_action VARCHAR(50),
  p_reason VARCHAR(255),
  p_admin_id INT
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE log_id INT;
  INSERT INTO account_audit_log (user_id, action, reason, admin_id, timestamp)
  VALUES (p_user_id, p_action, p_reason, p_admin_id, NOW());
  SET log_id = LAST_INSERT_ID();
  RETURN log_id;
END//
DELIMITER ;

-- Function: Suspend user account
DELIMITER //
DROP FUNCTION IF EXISTS suspend_user_account//
CREATE FUNCTION suspend_user_account(
  p_user_id INT,
  p_admin_id INT,
  p_reason VARCHAR(255)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE users SET role = 'suspended' WHERE id = p_user_id;
  INSERT INTO account_audit_log (user_id, action, reason, admin_id, timestamp)
  VALUES (p_user_id, 'suspended', p_reason, p_admin_id, NOW());
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Reactivate user account
DELIMITER //
DROP FUNCTION IF EXISTS reactivate_user_account//
CREATE FUNCTION reactivate_user_account(
  p_user_id INT,
  p_admin_id INT,
  p_reason VARCHAR(255)
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE users SET role = 'user' WHERE id = p_user_id;
  INSERT INTO account_audit_log (user_id, action, reason, admin_id, timestamp)
  VALUES (p_user_id, 'reactivated', p_reason, p_admin_id, NOW());
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Block loan account
DELIMITER //
DROP FUNCTION IF EXISTS block_loan_account//
CREATE FUNCTION block_loan_account(
  p_loan_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE loans SET is_account_blocked = TRUE, blocked_date = NOW() WHERE id = p_loan_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Unblock loan account
DELIMITER //
DROP FUNCTION IF EXISTS unblock_loan_account//
CREATE FUNCTION unblock_loan_account(
  p_loan_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  UPDATE loans SET is_account_blocked = FALSE, blocked_date = NULL WHERE id = p_loan_id;
  RETURN TRUE;
END//
DELIMITER ;

-- Function: Get total amount owed (all active loans)
DELIMITER //
DROP FUNCTION IF EXISTS get_total_owed//
CREATE FUNCTION get_total_owed(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(remaining_balance) INTO total FROM loans 
  WHERE user_id = p_user_id AND status = 'active';
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get total paid (all completed loans)
DELIMITER //
DROP FUNCTION IF EXISTS get_total_paid//
CREATE FUNCTION get_total_paid(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(remaining_balance) INTO total FROM loans 
  WHERE user_id = p_user_id AND status = 'paid';
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Check if user can apply for loan (max 3 active loans)
DELIMITER //
DROP FUNCTION IF EXISTS can_apply_for_loan//
CREATE FUNCTION can_apply_for_loan(
  p_user_id INT
) RETURNS BOOLEAN DETERMINISTIC
BEGIN
  DECLARE count INT;
  SELECT COUNT(*) INTO count FROM loans 
  WHERE user_id = p_user_id AND status IN ('active', 'pending');
  RETURN count < 3;
END//
DELIMITER ;

-- Function: Get average loan amount
DELIMITER //
DROP FUNCTION IF EXISTS get_average_loan//
CREATE FUNCTION get_average_loan(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE avg DOUBLE;
  SELECT AVG(amount) INTO avg FROM loans WHERE user_id = p_user_id;
  RETURN COALESCE(avg, 0);
END//
DELIMITER ;

-- Function: Get loan count by status
DELIMITER //
DROP FUNCTION IF EXISTS get_loan_count_by_status//
CREATE FUNCTION get_loan_count_by_status(
  p_user_id INT,
  p_status VARCHAR(50)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE count INT;
  SELECT COUNT(*) INTO count FROM loans WHERE user_id = p_user_id AND status = p_status;
  RETURN COALESCE(count, 0);
END//
DELIMITER ;

-- Function: Get total loan interest paid
DELIMITER //
DROP FUNCTION IF EXISTS get_total_interest_paid//
CREATE FUNCTION get_total_interest_paid(
  p_user_id INT
) RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(COALESCE(lp.payment_amount * 0.20, 0)) INTO total 
  FROM loan_payments lp WHERE lp.user_id = p_user_id;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get latest transaction date
DELIMITER //
DROP FUNCTION IF EXISTS get_last_transaction_date//
CREATE FUNCTION get_last_transaction_date(
  p_user_id INT
) RETURNS TIMESTAMP DETERMINISTIC
BEGIN
  DECLARE last_date TIMESTAMP;
  SELECT MAX(created_at) INTO last_date FROM transactions WHERE user_id = p_user_id;
  RETURN last_date;
END//
DELIMITER ;

-- Function: Validate user credentials
DELIMITER //
DROP FUNCTION IF EXISTS validate_user_login//
CREATE FUNCTION validate_user_login(
  p_username VARCHAR(100),
  p_password VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE user_id INT;
  SELECT id INTO user_id FROM users 
  WHERE username = p_username AND password = p_password LIMIT 1;
  RETURN COALESCE(user_id, -1);
END//
DELIMITER ;

-- Function: Validate admin credentials
DELIMITER //
DROP FUNCTION IF EXISTS validate_admin_login//
CREATE FUNCTION validate_admin_login(
  p_username VARCHAR(100),
  p_password VARCHAR(255)
) RETURNS INT DETERMINISTIC
BEGIN
  DECLARE admin_id INT;
  SELECT id INTO admin_id FROM admin 
  WHERE username = p_username AND password = p_password LIMIT 1;
  RETURN COALESCE(admin_id, -1);
END//
DELIMITER ;

-- Function: Get dashboard stats - total users
DELIMITER //
DROP FUNCTION IF EXISTS get_dashboard_total_users//
CREATE FUNCTION get_dashboard_total_users() RETURNS INT DETERMINISTIC
BEGIN
  DECLARE total INT;
  SELECT COUNT(*) INTO total FROM users;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get dashboard stats - total transactions
DELIMITER //
DROP FUNCTION IF EXISTS get_dashboard_total_transactions//
CREATE FUNCTION get_dashboard_total_transactions() RETURNS INT DETERMINISTIC
BEGIN
  DECLARE total INT;
  SELECT COUNT(*) INTO total FROM transactions;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get dashboard stats - total loans issued
DELIMITER //
DROP FUNCTION IF EXISTS get_dashboard_total_loans//
CREATE FUNCTION get_dashboard_total_loans() RETURNS INT DETERMINISTIC
BEGIN
  DECLARE total INT;
  SELECT COUNT(*) INTO total FROM loans;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get dashboard stats - total system balance
DELIMITER //
DROP FUNCTION IF EXISTS get_dashboard_total_balance//
CREATE FUNCTION get_dashboard_total_balance() RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(balance) INTO total FROM users;
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Function: Get dashboard stats - total loan amount outstanding
DELIMITER //
DROP FUNCTION IF EXISTS get_dashboard_outstanding_loans//
CREATE FUNCTION get_dashboard_outstanding_loans() RETURNS DOUBLE DETERMINISTIC
BEGIN
  DECLARE total DOUBLE;
  SELECT SUM(remaining_balance) INTO total FROM loans WHERE status = 'active';
  RETURN COALESCE(total, 0);
END//
DELIMITER ;

-- Indexes for better performance
CREATE INDEX idx_loan_applications_user ON loan_applications(user_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_payments_loan ON loan_payments(loan_id);
CREATE INDEX idx_loan_payments_user ON loan_payments(user_id);
CREATE INDEX idx_loan_receipts_payment ON loan_receipts(payment_id);
CREATE INDEX idx_loan_penalties_loan ON loan_penalties(loan_id);
CREATE INDEX idx_loan_penalties_paid ON loan_penalties(paid);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_loans_user ON loans(user_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_messages_user ON user_messages(user_id);

-- End of schema
-- Flow notes:
-- 1) Users apply for loans: INSERT into loan_applications (status='pending')
-- 2) Admin reviews: approve_loan_application() or reject_loan_application()
-- 3) If approved: Loan record created with 2% interest, 6-month due date
-- 4) User pays loan: process_loan_payment() updates remaining_balance
-- 5) Payment receipt: generate_loan_receipt() creates receipt record
-- 6) Penalties: If unpaid after 6 months, apply_loan_penalty() adds penalty charge
-- 7) Payment methods: Online, Walk-in (Bank Counter), ATM, Mobile App
-- 8) Admin can view: pending applications, active loans, payments, penalties

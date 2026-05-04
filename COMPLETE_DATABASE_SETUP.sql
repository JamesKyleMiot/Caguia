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
  nationality VARCHAR(100),
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

-- ============================================================
-- 5. ACCOUNT AUDIT LOG TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS account_audit_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  action VARCHAR(50) NOT NULL,
  reason VARCHAR(255),
  admin_id INT,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 6. USER MESSAGES TABLE
-- ============================================================
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

-- ============================================================
-- 7. PASSWORD RESET REQUESTS TABLE
-- ============================================================
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

-- ============================================================
-- 8. PIN RESET REQUESTS TABLE (NEW - May 4, 2026)
-- ============================================================
CREATE TABLE IF NOT EXISTS pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  otp VARCHAR(10),                   -- 6-digit OTP code
  otp_generated_at TIMESTAMP NULL,   -- When OTP was generated
  otp_verified BOOLEAN DEFAULT FALSE, -- Is OTP verified by user?
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_loans_userid ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_userid ON user_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_userid ON password_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_status ON password_reset_requests(status);
CREATE INDEX IF NOT EXISTS idx_pin_reset_userid ON pin_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_pin_reset_status ON pin_reset_requests(status);

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
-- ============================================================

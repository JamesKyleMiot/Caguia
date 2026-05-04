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

-- End of schema
-- Flow notes:
-- 1) Users request loans: row inserted to `loans` with amount, interest and due_date.
-- 2) `LoanManager` tracks remaining_balance and due_date; when overdue, admin sees it in Loan Management.
-- 2a) `UserDashboard` loads all user transactions and all user loans so the user can see loan history.
-- 3) Admin actions (send reminder, suspend, reactivate) create rows in `user_messages` and insert into `account_audit_log`.
-- 4) Email notifications are attempted via `EmailNotifier`; messages are stored in DB for in-app display in `UserDashboard`.
-- 5) Payments create entries in `transactions` and update `loans.remaining_balance`; when remaining_balance <= 0, `loans.status` becomes 'paid' and account reactivation occurs.
-- 6) `DBInit` / schema updates should keep the table structure aligned with the app screens.

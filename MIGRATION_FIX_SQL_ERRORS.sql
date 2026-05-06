-- ========================================
-- DATABASE MIGRATION FIX - SQL ERROR RESOLUTION
-- Run this script to fix "Unknown column" errors
-- May 7, 2026
-- ========================================

-- Add missing columns to loan_applications table
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS full_name VARCHAR(255) NULL AFTER rejection_reason;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS employment_status VARCHAR(50) NULL AFTER full_name;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS monthly_income DOUBLE NULL AFTER employment_status;
ALTER TABLE loan_applications ADD COLUMN IF NOT EXISTS loan_term_months INT NULL AFTER monthly_income;

-- NOTE: Transactions table already has id INT AUTO_INCREMENT PRIMARY KEY defined in database_schema.sql
-- No ALTER statement needed for transactions table id column

-- Add missing column to loan_payments table
ALTER TABLE loan_payments ADD COLUMN IF NOT EXISTS transaction_id INT NULL AFTER transaction_reference;

-- Add foreign key constraint if it doesn't exist
ALTER TABLE loan_payments ADD CONSTRAINT fk_loanpayments_transaction 
FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL;

-- Verify all tables have correct structure
-- You can run these SELECT statements to verify:
SELECT 'Checking loan_applications columns:' AS check_result;
DESCRIBE loan_applications;

SELECT 'Checking transactions columns:' AS check_result;
DESCRIBE transactions;

SELECT 'Checking loan_payments columns:' AS check_result;
DESCRIBE loan_payments;

-- Commit the changes
COMMIT;

-- ========================================
-- SUMMARY OF FIXES
-- ========================================
-- 1. Added full_name column to loan_applications
-- 2. Added employment_status column to loan_applications
-- 3. Added monthly_income column to loan_applications
-- 4. Added loan_term_months column to loan_applications
-- 5. Ensured transactions table has id column
-- 6. Added transaction_id column to loan_payments
-- 7. Added foreign key constraint linking payments to transactions
-- ========================================

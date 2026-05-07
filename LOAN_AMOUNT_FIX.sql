-- ============================================================
-- LOAN AMOUNT DEFAULT VALUE FIX
-- ============================================================
-- This script fixes the "doesn't have a default value" error
-- Run this on your existing lawbank database
-- ============================================================

USE lawbank;

-- Modify loan_applications table columns to have defaults
ALTER TABLE loan_applications MODIFY COLUMN requested_amount DECIMAL(12,2) DEFAULT 0;
ALTER TABLE loan_applications MODIFY COLUMN loan_amount DECIMAL(12,2) DEFAULT 0;
ALTER TABLE loan_applications MODIFY COLUMN purpose VARCHAR(255) DEFAULT '';
ALTER TABLE loan_applications MODIFY COLUMN company_name VARCHAR(255) DEFAULT '';
ALTER TABLE loan_applications MODIFY COLUMN work_address VARCHAR(255) DEFAULT '';
ALTER TABLE loan_applications MODIFY COLUMN admin_id INT DEFAULT NULL;
ALTER TABLE loan_applications MODIFY COLUMN admin_notes VARCHAR(255) DEFAULT '';
ALTER TABLE loan_applications MODIFY COLUMN approved_amount DECIMAL(12,2) DEFAULT 0;
ALTER TABLE loan_applications MODIFY COLUMN rejection_reason VARCHAR(255) DEFAULT '';

-- Backfill NULL values with defaults
UPDATE loan_applications
SET 
  requested_amount = COALESCE(requested_amount, loan_amount_requested, 0),
  loan_amount = COALESCE(loan_amount, loan_amount_requested, requested_amount, 0),
  purpose = COALESCE(purpose, loan_purpose, ''),
  company_name = COALESCE(company_name, ''),
  work_address = COALESCE(work_address, ''),
  admin_notes = COALESCE(admin_notes, ''),
  approved_amount = COALESCE(approved_amount, 0),
  rejection_reason = COALESCE(rejection_reason, '')
WHERE requested_amount IS NULL 
   OR loan_amount IS NULL 
   OR purpose IS NULL 
   OR company_name IS NULL 
   OR work_address IS NULL;

-- Verify the fix
SELECT COUNT(*) as total_applications,
       COUNT(CASE WHEN loan_amount = 0 OR loan_amount IS NULL THEN 1 END) as zero_amount,
       COUNT(CASE WHEN loan_amount_requested > 0 THEN 1 END) as valid_applications
FROM loan_applications;

-- ============================================================
-- COMPLETED - Database is now fixed!
-- ============================================================

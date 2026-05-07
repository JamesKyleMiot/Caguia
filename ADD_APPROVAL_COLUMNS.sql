-- Add missing columns to loan_applications table for approval/rejection workflow

ALTER TABLE loan_applications ADD COLUMN admin_id INT NULL AFTER status;
ALTER TABLE loan_applications ADD COLUMN admin_comments VARCHAR(500) NULL AFTER admin_id;
ALTER TABLE loan_applications ADD COLUMN approved_at TIMESTAMP NULL AFTER admin_comments;
ALTER TABLE loan_applications ADD COLUMN rejected_at TIMESTAMP NULL AFTER approved_at;
ALTER TABLE loan_applications ADD COLUMN rejection_reason VARCHAR(500) NULL AFTER rejected_at;
ALTER TABLE loan_applications ADD COLUMN approved_amount DECIMAL(15,2) DEFAULT 0 AFTER rejection_reason;

-- Verify columns were added
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loan_applications' 
AND COLUMN_NAME IN ('admin_id', 'admin_comments', 'approved_at', 'rejected_at', 'rejection_reason', 'approved_amount')
ORDER BY ORDINAL_POSITION;

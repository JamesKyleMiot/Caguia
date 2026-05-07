-- Fix missing ID column in loan_applications table
-- This column is required as the primary key

ALTER TABLE loan_applications ADD COLUMN id INT AUTO_INCREMENT UNIQUE FIRST;

-- Verify the column was added
SELECT 'loan_applications table fixed' as status;
SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loan_applications' 
ORDER BY ORDINAL_POSITION LIMIT 5;

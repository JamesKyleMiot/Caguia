-- SQL TEST QUERIES for Loan Management System
-- Use these to test the system without GUI

-- ============================================================
-- 1. VIEW ALL OVERDUE LOANS
-- ============================================================
SELECT 
    l.id AS 'Loan ID',
    l.user_id AS 'User ID',
    u.fullname AS 'Name',
    u.username AS 'Username',
    u.email AS 'Email',
    l.amount AS 'Amount',
    l.due_date AS 'Due Date',
    l.remaining_balance AS 'Remaining',
    DATEDIFF(CURDATE(), l.due_date) AS 'Days Overdue',
    l.is_account_blocked AS 'Blocked',
    l.status AS 'Status'
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.due_date < CURDATE() AND l.status = 'active'
ORDER BY l.due_date ASC;

-- ============================================================
-- 2. VIEW ALL LOANS WITH WITNESS INFORMATION
-- ============================================================
SELECT 
    l.id AS 'Loan ID',
    u.username AS 'Borrower',
    l.amount AS 'Amount',
    l.witness_name AS 'Witness Name',
    l.witness_contact AS 'Witness Contact',
    CASE WHEN l.user_signature IS NOT NULL THEN 'YES' ELSE 'NO' END AS 'User Signed',
    CASE WHEN l.witness_signature IS NOT NULL THEN 'YES' ELSE 'NO' END AS 'Witness Signed',
    l.due_date AS 'Due Date'
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.witness_name IS NOT NULL
ORDER BY l.created_at DESC;

-- ============================================================
-- 3. VIEW ALL SUSPENDED ACCOUNTS
-- ============================================================
SELECT 
    id,
    fullname,
    username,
    role,
    balance,
    created_at
FROM users
WHERE role = 'suspended'
ORDER BY id;

-- ============================================================
-- 4. VIEW ACCOUNT AUDIT LOG (All account actions)
-- ============================================================
SELECT 
    aal.id,
    aal.user_id,
    u.username AS 'User',
    aal.action,
    aal.reason,
    aal.timestamp,
    a.username AS 'Admin'
FROM account_audit_log aal
LEFT JOIN users u ON aal.user_id = u.id
LEFT JOIN admin a ON aal.admin_id = a.id
ORDER BY aal.timestamp DESC
LIMIT 50;

-- ============================================================
-- 5. CHECK SPECIFIC USER'S LOANS
-- ============================================================
-- Replace 'user1' with actual username
SELECT 
    l.id,
    l.amount,
    l.total_payable,
    l.remaining_balance,
    l.due_date,
    l.status,
    l.witness_name,
    l.is_account_blocked
FROM loans l
WHERE l.user_id = (SELECT id FROM users WHERE username = 'user1')
ORDER BY l.created_at DESC;

-- ============================================================
-- 6. GET ACCOUNT STATUS FOR USER
-- ============================================================
-- Replace 'user1' with actual username
SELECT 
    id,
    username,
    fullname,
    role AS 'Account Status',
    balance,
    CASE 
        WHEN role = 'suspended' THEN 'Account is BLOCKED'
        WHEN role = 'user' THEN 'Account is ACTIVE'
        WHEN role = 'admin' THEN 'Administrator'
        ELSE 'Unknown'
    END AS 'Status Description'
FROM users
WHERE username = 'user1';

-- ============================================================
-- 7. SIMULATE LOAN DEACTIVATION (Manual)
-- ============================================================
-- Replace values with actual IDs
-- UPDATE loans SET is_account_blocked = TRUE, blocked_date = CURRENT_TIMESTAMP WHERE id = 1;
-- UPDATE users SET role = 'suspended' WHERE id = 1;

-- ============================================================
-- 8. SIMULATE PAYMENT PROCESSING (Manual)
-- ============================================================
-- Check current balance:
-- SELECT remaining_balance FROM loans WHERE id = 1;
-- 
-- Process payment of 5000:
-- UPDATE loans 
-- SET remaining_balance = remaining_balance - 5000,
--     status = CASE WHEN (remaining_balance - 5000) <= 0 THEN 'paid' ELSE 'active' END
-- WHERE id = 1;
-- 
-- Reactivate account if paid:
-- UPDATE users SET role = 'user' WHERE id = (SELECT user_id FROM loans WHERE id = 1);

-- ============================================================
-- 9. SUMMARY STATISTICS
-- ============================================================
SELECT 
    COUNT(DISTINCT l.id) AS 'Total Loans',
    COUNT(DISTINCT CASE WHEN l.status = 'active' THEN l.id END) AS 'Active Loans',
    COUNT(DISTINCT CASE WHEN l.status = 'paid' THEN l.id END) AS 'Paid Loans',
    COUNT(DISTINCT CASE WHEN l.due_date < CURDATE() AND l.status = 'active' THEN l.id END) AS 'Overdue Loans',
    COUNT(DISTINCT CASE WHEN l.is_account_blocked = TRUE THEN l.id END) AS 'Blocked Accounts',
    SUM(CASE WHEN l.status = 'active' THEN l.remaining_balance ELSE 0 END) AS 'Total Outstanding'
FROM loans l;

-- ============================================================
-- 10. LIST OF USERS WITH BLOCKED ACCOUNTS
-- ============================================================
SELECT 
    u.id,
    u.fullname,
    u.username,
    u.role,
    COUNT(l.id) AS 'Blocked Loans',
    SUM(l.remaining_balance) AS 'Total Owed'
FROM users u
LEFT JOIN loans l ON u.id = l.user_id AND l.is_account_blocked = TRUE
WHERE u.role = 'suspended'
GROUP BY u.id, u.fullname, u.username, u.role;

-- ============================================================
-- 11. LOANS APPROACHING DUE DATE (Next 7 days)
-- ============================================================
SELECT 
    l.id,
    u.username,
    l.amount,
    l.due_date,
    DATEDIFF(l.due_date, CURDATE()) AS 'Days Until Due',
    l.witness_name
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND l.status = 'active'
ORDER BY l.due_date ASC;

-- ============================================================
-- 12. DETAILED LOAN REPORT FOR SPECIFIC LOAN
-- ============================================================
-- Replace 1 with actual loan ID
SELECT 
    l.id AS 'Loan ID',
    l.created_at AS 'Created',
    u.fullname AS 'Borrower',
    u.username AS 'Username',
    u.email AS 'Email',
    l.amount AS 'Principal',
    l.interest_rate AS 'Interest %',
    l.total_payable AS 'Total Payable',
    l.remaining_balance AS 'Remaining',
    l.due_date AS 'Due Date',
    DATEDIFF(CURDATE(), l.due_date) AS 'Days Overdue',
    l.status AS 'Status',
    l.witness_name AS 'Witness',
    l.witness_contact AS 'Contact',
    l.is_account_blocked AS 'Account Blocked',
    l.blocked_date AS 'Block Date'
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.id = 1;

-- ============================================================
-- TEST DATA CREATION (Optional)
-- ============================================================
-- Create test loan with future due date:
-- INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, status, due_date, witness_name, witness_contact)
-- VALUES (1, 10000.00, 10, 11000.00, 11000.00, 'active', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'Test Witness', '09123456789');

-- Create test loan with past due date:
-- INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, status, due_date, witness_name, witness_contact)
-- VALUES (1, 5000.00, 5, 5250.00, 5250.00, 'active', DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Test Witness 2', '09987654321');

-- ============================================================
-- CLEANUP QUERIES (Use with caution!)
-- ============================================================
-- Remove test loans:
-- DELETE FROM loans WHERE witness_name LIKE 'Test%';

-- Reset user role from suspended to user:
-- UPDATE users SET role = 'user' WHERE role = 'suspended';

-- Clear audit log:
-- DELETE FROM account_audit_log;

-- ============================================================
-- EXPORT RESULTS TO FILE
-- ============================================================
-- SELECT * FROM loans INTO OUTFILE 'C:/loans_backup.csv' FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n';
-- SELECT * FROM users WHERE role = 'suspended' INTO OUTFILE 'C:/suspended_accounts.csv' FIELDS TERMINATED BY ',';

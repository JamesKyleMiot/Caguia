# DATABASE QUICK REFERENCE GUIDE

## 📊 All Tables at a Glance

### 🔷 USERS TABLE (70 rows avg)
```
Columns: id, username, fullname, email, password, pin, nationality, 
         balance, savings, total_deposit, role, created_at
Purpose: User account storage
Keys: PK(id), UK(username), UK(email)
```

### 🔶 ADMIN TABLE (5 rows avg)
```
Columns: id, username, password, created_at
Purpose: Administrator accounts
Keys: PK(id), UK(username)
```

### 🔷 TRANSACTIONS TABLE (500+ rows)
```
Columns: id, user_id, type, amount, method, created_at
Purpose: Transaction history
Keys: PK(id), FK(user_id→users)
```

### 🔷 LOANS TABLE (100+ rows)
```
Columns: id, user_id, amount, interest_rate, total_payable, 
         remaining_balance, due_date, status, witness_name, 
         witness_contact, witness_signature, user_signature,
         promissory_note_url, is_account_blocked, blocked_date, created_at
Purpose: Loan management with witness support
Keys: PK(id), FK(user_id→users)
Index: idx_loans_userid
```

### 🔷 ACCOUNT_AUDIT_LOG TABLE (200+ rows)
```
Columns: id, user_id, action, reason, admin_id, timestamp
Purpose: Track all admin actions
Keys: PK(id), FK(user_id→users)
```

### 🔷 USER_MESSAGES TABLE (100+ rows)
```
Columns: id, user_id, admin_id, subject, body, is_read, created_at
Purpose: Admin-to-user messaging
Keys: PK(id), FK(user_id→users)
Index: idx_messages_userid
```

### 🟢 PASSWORD_RESET_REQUESTS TABLE (20+ rows)
```
Columns: id, user_id, email, phone, status, admin_id, admin_response,
         created_at, reviewed_at, expires_at, completed_at
Purpose: Password reset workflow with admin approval
Keys: PK(id), FK(user_id→users)
Index: idx_reset_userid, idx_reset_status
Status Values: pending, approved, denied, completed
```

### 🟣 PIN_RESET_REQUESTS TABLE (10+ rows) - NEW
```
Columns: id, user_id, email, status, admin_id, admin_response,
         created_at, reviewed_at, expires_at, completed_at
Purpose: PIN reset workflow with admin approval
Keys: PK(id), FK(user_id→users)
Index: idx_pin_reset_userid, idx_pin_reset_status
Status Values: pending, approved, denied, completed
```

---

## 🔗 RELATIONSHIPS

```
┌─ USERS ─────────┐
│ (Central Hub)   │
├─────────────────┤
│ id (PK)         │
│ username (UK)   │
│ email (UK)      │
│ password        │
│ pin (NEW)       │
└────────┬────────┘
         │
    ┌────┴──────────┬─────────────────┬──────────────────┬─────────────────┐
    │               │                 │                  │                 │
 (1:N)          (1:N)             (1:N)            (1:N)              (1:N)
    │               │                 │                  │                 │
TRANSACTIONS   LOANS             ACCOUNT_        USER_             PASSWORD/PIN
               AUDIT_LOG         MESSAGES        RESET_REQUESTS

    │               │                 │                  │                 │
    └────┬──────────┴─────────────────┴──────────────────┴─────────────────┘
         │
    ADMIN (reviews requests, blocks accounts, sends messages)
```

---

## 🔄 Data Flow Examples

### Example 1: User Takes a Loan
```
1. User fills loan form → loans table
2. Admin reviews → may add witness info
3. User signs & witness signs → stored as LONGBLOB
4. Loan status = "active"
5. Payments create transactions
6. remaining_balance decreases
7. When paid: status = "paid" ✓
```

### Example 2: User Forgets PIN
```
1. User clicks "Forgot PIN?" on login
2. Submits request with email → pin_reset_requests (status='pending')
3. Admin reviews PIN_Reset_Requests dialog
4. Admin approves → status='approved', expires_at set to NOW()+1hour
5. User can now set new PIN
6. New PIN stored in users.pin (hashed)
7. Request marked complete
```

### Example 3: Admin Action
```
1. Admin blocks user for overdue loan
2. INSERT account_audit_log (action='block', reason='overdue')
3. UPDATE users SET role='suspended'
4. INSERT user_messages (notify user)
5. Audit trail created ✓
```

---

## 📈 Field Sizes Reference

| Field | Type | Size | Notes |
|-------|------|------|-------|
| username | VARCHAR | 100 | Unique, indexed |
| fullname | VARCHAR | 255 | Text |
| email | VARCHAR | 255 | Unique, indexed |
| password | VARCHAR | 255 | Hashed |
| pin | VARCHAR | 255 | Hashed, NEW |
| balance | DOUBLE | 8 bytes | Decimal numbers |
| amount | DOUBLE | 8 bytes | Decimal numbers |
| witness_signature | LONGBLOB | 4GB | Binary image data |
| status | VARCHAR | 50 | Enum values |
| created_at | TIMESTAMP | 4 bytes | Auto-set |

---

## 🔐 Security Features

✅ **Unique Constraints:**
- username (can't have duplicates)
- email (can't have duplicates)

✅ **Foreign Keys:**
- All user-related tables reference users(id)
- CASCADE DELETE enabled

✅ **Hashing:**
- Password field (hashed)
- PIN field (hashed) - NEW

✅ **Audit Trail:**
- account_audit_log tracks all admin actions
- Timestamp on all actions
- Admin ID recorded

✅ **Approval Workflow:**
- password_reset_requests require admin approval
- pin_reset_requests require admin approval
- Request expiration (1 hour for PIN, variable for password)

---

## 🗂️ Files You Need

**SQL Files:**
```
database_schema.sql ..................... Main schema definition
COMPLETE_DATABASE_SETUP.sql ............ All-in-one setup (run this)
password_reset_migration.sql ........... Password reset table
```

**Documentation:**
```
DATABASE_UPDATES_COMPLETE.md ........... Detailed field reference
DATABASE_MIGRATION_HISTORY.md ......... Complete migration history
DATABASE_QUICK_REFERENCE.md ........... This file
PIN_RESET_SYSTEM.md ................... PIN reset implementation
```

---

## ⚡ Quick Commands

### Setup Database
```bash
mysql -u root -p < COMPLETE_DATABASE_SETUP.sql
```

### Check All Tables
```sql
SHOW TABLES;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='lawbank';
```

### Check Table Structure
```sql
DESCRIBE users;
DESCRIBE loans;
DESCRIBE pin_reset_requests;
```

### Get Pending Requests
```sql
SELECT * FROM pin_reset_requests WHERE status='pending';
SELECT * FROM password_reset_requests WHERE status='pending';
```

### Get User Loans
```sql
SELECT * FROM loans WHERE user_id=1;
```

### Get Transaction History
```sql
SELECT * FROM transactions WHERE user_id=1 ORDER BY created_at DESC;
```

### Check Admin Audit Log
```sql
SELECT * FROM account_audit_log ORDER BY timestamp DESC LIMIT 50;
```

---

## 📊 Statistics

| Metric | Count |
|--------|-------|
| Total Tables | 8 |
| Total Columns | 70+ |
| Total Indexes | 6+ |
| Total Foreign Keys | 8 |
| Unique Constraints | 3 |
| CASCADE DELETEs | 8 |
| Status Values | 4 (pending, approved, denied, completed) |
| User Roles | 3+ (user, admin, suspended) |

---

## 🎯 Purpose of Each Table

| Table | Purpose | Example |
|-------|---------|---------|
| users | User accounts | john_doe (username), balance |
| admin | Admin accounts | admin1 (super user) |
| transactions | Record all money moves | Deposit ₱5000 |
| loans | Manage loans | Borrow ₱50000 at 10% |
| account_audit_log | Track admin actions | Blocked user123 for overdue |
| user_messages | Admin notifications | "Loan overdue, please pay" |
| password_reset_requests | Request new password | User forgot password, admin approves |
| pin_reset_requests | Request new PIN | User forgot PIN, admin approves |

---

## 🚀 Performance Tips

1. **Indexes are on:**
   - Foreign keys (automatic)
   - user_id in loans table
   - user_id in messages table
   - Status fields in request tables

2. **Use these when querying:**
   - WHERE user_id = ? (indexed, fast)
   - WHERE status = 'pending' (indexed, fast)
   - WHERE created_at > DATE (not indexed, slower)

3. **Optimize queries:**
   - Use SELECT with specific columns (not SELECT *)
   - Use WHERE to filter early
   - ORDER BY on indexed columns when possible

---

## ✅ Verification Checklist

Run these to verify database setup:

```sql
-- Check all tables exist
SHOW TABLES; 
-- Should show 8 tables

-- Check users has pin field
DESCRIBE users;
-- Should show 'pin' column

-- Check pin_reset_requests exists
DESCRIBE pin_reset_requests;
-- Should show all PIN reset fields

-- Check indexes
SHOW INDEX FROM users;
SHOW INDEX FROM loans;
SHOW INDEX FROM password_reset_requests;
SHOW INDEX FROM pin_reset_requests;

-- Count records
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'loans', COUNT(*) FROM loans
UNION ALL
SELECT 'transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'pin_reset_requests', COUNT(*) FROM pin_reset_requests;
```

---

## 🔄 Common Operations

### Create New User
```sql
INSERT INTO users (username, fullname, email, password, pin) 
VALUES ('john', 'John Doe', 'john@example.com', 'hashed_pwd', 'hashed_pin');
```

### Create New Loan
```sql
INSERT INTO loans (user_id, amount, total_payable, due_date, status)
VALUES (1, 50000, 55000, DATE_ADD(NOW(), INTERVAL 30 DAY), 'active');
```

### Submit PIN Reset Request
```sql
INSERT INTO pin_reset_requests (user_id, email, status)
VALUES (1, 'john@example.com', 'pending');
```

### Approve PIN Reset Request
```sql
UPDATE pin_reset_requests 
SET status='approved', admin_id=1, expires_at=DATE_ADD(NOW(), INTERVAL 1 HOUR)
WHERE id=1;
```

### Block User Account (Overdue Loan)
```sql
UPDATE users SET role='suspended' WHERE id=1;
INSERT INTO account_audit_log (user_id, action, reason, admin_id)
VALUES (1, 'block', 'Loan overdue', 1);
```

---

## 📞 Support

**Database Version:** 1.0 (May 4, 2026)
**Engine:** MySQL 5.7+
**Character Set:** UTF-8 (utf8mb4)

All tables created and ready for use! 🎉

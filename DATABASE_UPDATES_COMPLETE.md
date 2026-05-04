# CAGUIOA BANK - COMPLETE DATABASE UPDATES

## Database Information
- **Database Name:** `lawbank`
- **Character Set:** UTF-8 (utf8mb4)
- **Collation:** utf8mb4_unicode_ci
- **Engine:** InnoDB

---

## TABLE STRUCTURE

### 1. USERS TABLE
**Purpose:** Store user account information  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique user identifier |
| username | VARCHAR(100) | UNIQUE, NOT NULL | - | Login username |
| fullname | VARCHAR(255) | NOT NULL | - | User's full name |
| email | VARCHAR(255) | UNIQUE | NULL | User's email address |
| password | VARCHAR(255) | - | NULL | Hashed password |
| pin | VARCHAR(255) | - | NULL | **NEW:** Hashed PIN for authentication |
| balance | DOUBLE | - | 0 | User's account balance |
| savings | DOUBLE | - | 0 | User's savings account |
| total_deposit | DOUBLE | - | 0 | Total deposits made |
| role | VARCHAR(50) | - | 'user' | User role (user, admin, suspended) |
| nationality | VARCHAR(100) | - | NULL | User's nationality (added via ALTER) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Account creation timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE (username)
- UNIQUE (email)

**Foreign Keys:** None (references only)

---

### 2. ADMIN TABLE
**Purpose:** Store administrator accounts  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique admin identifier |
| username | VARCHAR(100) | UNIQUE, NOT NULL | - | Admin login username |
| password | VARCHAR(255) | NOT NULL | - | Hashed admin password |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Admin creation timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE (username)

**Foreign Keys:** None

---

### 3. TRANSACTIONS TABLE
**Purpose:** Track all user transactions (deposits, withdrawals, payments)  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique transaction ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| type | VARCHAR(50) | - | NULL | Transaction type (deposit, withdrawal, payment, etc.) |
| amount | DOUBLE | - | NULL | Transaction amount |
| method | VARCHAR(255) | - | NULL | Payment method (cash, card, transfer, etc.) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Transaction timestamp |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)

**Constraints:**
- ON DELETE CASCADE (if user deleted, transactions deleted)

---

### 4. LOANS TABLE
**Purpose:** Manage loan applications with witness and signature support  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique loan ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| amount | DOUBLE | NOT NULL | - | Loan amount |
| interest_rate | DOUBLE | - | 0.10 | Interest rate (10% default) |
| total_payable | DOUBLE | - | NULL | Total amount with interest |
| remaining_balance | DOUBLE | - | 0 | Remaining loan balance |
| due_date | DATE | - | NULL | Loan due date |
| status | VARCHAR(50) | - | 'active' | Loan status (active, paid, overdue, suspended) |
| witness_name | VARCHAR(255) | - | NULL | Witness name for loan |
| witness_contact | VARCHAR(255) | - | NULL | Witness contact information |
| witness_signature | LONGBLOB | - | NULL | Witness signature (binary image) |
| user_signature | LONGBLOB | - | NULL | User signature (binary image) |
| promissory_note_url | VARCHAR(1024) | - | NULL | URL to promissory note document |
| is_account_blocked | BOOLEAN | - | FALSE | Whether user account is blocked due to overdue |
| blocked_date | TIMESTAMP | - | NULL | Date account was blocked |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Loan creation timestamp |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)
- idx_loans_userid

**Constraints:**
- ON DELETE CASCADE (if user deleted, loans deleted)

---

### 5. ACCOUNT_AUDIT_LOG TABLE
**Purpose:** Track all admin actions on user accounts  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique log entry ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| action | VARCHAR(50) | NOT NULL | - | Action performed (block, unblock, suspend, etc.) |
| reason | VARCHAR(255) | - | NULL | Reason for action |
| admin_id | INT | - | NULL | Admin who performed action |
| timestamp | TIMESTAMP | - | CURRENT_TIMESTAMP | When action was performed |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)

**Constraints:**
- ON DELETE CASCADE (if user deleted, audit logs deleted)

---

### 6. USER_MESSAGES TABLE
**Purpose:** Store admin-to-user notifications and messages  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique message ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| admin_id | INT | - | NULL | Admin who sent message |
| subject | VARCHAR(255) | - | NULL | Message subject |
| body | TEXT | - | NULL | Message content |
| is_read | BOOLEAN | - | FALSE | Whether user has read message |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Message creation timestamp |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)
- idx_messages_userid

**Constraints:**
- ON DELETE CASCADE (if user deleted, messages deleted)

---

### 7. PASSWORD_RESET_REQUESTS TABLE
**Purpose:** Manage password reset requests (admin approval required)  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT, PRIMARY KEY | - | Unique request ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| email | VARCHAR(255) | - | NULL | User's email for reset |
| phone | VARCHAR(20) | - | NULL | User's phone for contact |
| status | VARCHAR(50) | - | 'pending' | Status: pending, approved, denied, completed |
| admin_id | INT | - | NULL | Admin who reviewed request |
| admin_response | VARCHAR(255) | - | NULL | Admin's reason for denial (if denied) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Request creation timestamp |
| reviewed_at | TIMESTAMP | - | NULL | When admin reviewed request |
| expires_at | TIMESTAMP | - | NULL | When approved request expires |
| completed_at | TIMESTAMP | - | NULL | When password was reset |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)
- idx_reset_userid
- idx_reset_status

**Constraints:**
- ON DELETE CASCADE (if user deleted, password reset requests deleted)

---

### 8. PIN_RESET_REQUESTS TABLE (NEW - JUST ADDED)
**Purpose:** Manage PIN reset requests (admin approval required)  
**Primary Key:** `id`

| Column Name | Data Type | Constraints | Default | Notes |
|---|---|---|---|---|
| id | INT | AUTO_INCREMENT PRIMARY KEY | - | Unique request ID |
| user_id | INT | FOREIGN KEY, NOT NULL | - | References users(id) |
| email | VARCHAR(255) | NOT NULL | - | User's email for verification |
| status | VARCHAR(50) | - | 'pending' | Status: pending, approved, denied, completed |
| admin_id | INT | - | NULL | Admin who reviewed request |
| admin_response | VARCHAR(255) | - | NULL | Admin's reason for denial (if denied) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | Request creation timestamp |
| reviewed_at | TIMESTAMP | - | NULL | When admin reviewed request |
| expires_at | TIMESTAMP | - | NULL | When approved request expires (1 hour) |
| completed_at | TIMESTAMP | - | NULL | When PIN was reset |

**Indexes:**
- PRIMARY KEY (id)
- FOREIGN KEY: user_id → users(id)

**Constraints:**
- ON DELETE CASCADE (if user deleted, PIN reset requests deleted)

---

## DATABASE RELATIONSHIPS DIAGRAM

```
users (1) ─── (Many) transactions
users (1) ─── (Many) loans
users (1) ─── (Many) account_audit_log
users (1) ─── (Many) user_messages
users (1) ─── (Many) password_reset_requests
users (1) ─── (Many) pin_reset_requests

admin (1) ─── (Many) account_audit_log
admin (1) ─── (Many) user_messages
admin (1) ─── (Many) password_reset_requests
admin (1) ─── (Many) pin_reset_requests
```

---

## RECENT UPDATES (Latest Changes)

### Update 1: Added PIN Field to Users Table
**Date:** May 4, 2026  
**Change:** Added PIN authentication support
```sql
ALTER TABLE users ADD COLUMN pin VARCHAR(255);
```

### Update 2: Created PIN Reset Requests Table
**Date:** May 4, 2026  
**Purpose:** Allow users to request PIN reset with admin approval
```sql
CREATE TABLE IF NOT EXISTS pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### Update 3: Created Password Reset Requests Table
**Date:** Earlier  
**Purpose:** Allow users to request password reset with admin approval
```sql
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
```

---

## DATA INTEGRITY FEATURES

### Foreign Key Constraints
- **Cascade Delete:** Deleting a user automatically deletes all related:
  - Transactions
  - Loans
  - Audit logs
  - Messages
  - Password reset requests
  - PIN reset requests

### Unique Constraints
- `users.username` - Each user has unique username
- `users.email` - Each email is unique
- `admin.username` - Each admin has unique username

### Default Values
- `users.role` = 'user'
- `users.balance` = 0
- `loans.status` = 'active'
- `loans.interest_rate` = 0.10 (10%)
- `transactions` timestamps auto-set to current time

---

## SQL INITIALIZATION

To initialize the database, run:
1. **Create Database:** `lawbank`
2. **Execute:** `database_schema.sql`
3. **Or:** Call `DBInit.ensureAllTables()` from Java code

**Total Tables:** 8  
**Total Fields:** 70+  
**Total Indexes:** 5+  
**Foreign Key Relationships:** 8

---

## COMMON QUERIES

### Get User with All Loans
```sql
SELECT u.*, COUNT(l.id) as loan_count, SUM(l.remaining_balance) as total_owed
FROM users u
LEFT JOIN loans l ON u.id = l.user_id
WHERE u.id = ?
GROUP BY u.id;
```

### Get All Overdue Loans
```sql
SELECT l.*, u.fullname, u.email
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.due_date < CURDATE() AND l.status = 'active';
```

### Get Pending PIN Reset Requests
```sql
SELECT p.*, u.fullname, u.email
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE p.status = 'pending'
ORDER BY p.created_at DESC;
```

### Get User Transaction History
```sql
SELECT * FROM transactions
WHERE user_id = ?
ORDER BY created_at DESC
LIMIT 50;
```

### Get Account Audit Log
```sql
SELECT aal.*, u.username, a.username as admin_name
FROM account_audit_log aal
LEFT JOIN users u ON aal.user_id = u.id
LEFT JOIN admin a ON aal.admin_id = a.id
ORDER BY aal.timestamp DESC;
```

---

## MIGRATION STEPS (If Updating Existing Database)

If you're updating an existing database, run these steps:

```sql
-- Step 1: Add PIN field if not exists
ALTER TABLE users ADD COLUMN pin VARCHAR(255);

-- Step 2: Add nationality field if not exists
ALTER TABLE users ADD COLUMN nationality VARCHAR(100);

-- Step 3: Create password reset requests table
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

-- Step 4: Create PIN reset requests table
CREATE TABLE IF NOT EXISTS pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Step 5: Create indexes if not exists
CREATE INDEX IF NOT EXISTS idx_loans_userid ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_userid ON user_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_userid ON password_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_status ON password_reset_requests(status);
```

---

## SUMMARY

**Total Tables:** 8
- users
- admin
- transactions
- loans
- account_audit_log
- user_messages
- password_reset_requests
- pin_reset_requests

**Key Features:**
✓ User authentication (username + pin/password)
✓ Admin management system
✓ Transaction tracking
✓ Loan management with witness support
✓ Account audit logging
✓ User messaging system
✓ Password reset workflow
✓ PIN reset workflow (NEW)
✓ Referential integrity with foreign keys
✓ Automatic timestamps
✓ Cascade delete for data consistency

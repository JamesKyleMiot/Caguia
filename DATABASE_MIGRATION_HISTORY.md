# CAGUIOA BANK - DATABASE MIGRATION HISTORY

## Overview
This document tracks all database changes made to the lawbank database throughout the project lifecycle.

---

## PHASE 1: INITIAL SETUP (Foundation)

### Tables Created:
1. **users** - User account storage
2. **admin** - Administrator accounts
3. **transactions** - Transaction history
4. **loans** - Loan management

### Initial Schema
```sql
-- users
id, username, fullname, email, password, balance, savings, total_deposit, role, created_at

-- admin
id, username, password, created_at

-- transactions
id, user_id, type, amount, method, created_at

-- loans
id, user_id, amount, due_date, status, created_at
```

---

## PHASE 2: LOAN MANAGEMENT ENHANCEMENTS

### Date: Early Development
### Change Type: Column Additions to Loans Table

#### Added via LoanManager.ensureColumn():
```sql
ALTER TABLE loans ADD COLUMN interest_rate DOUBLE DEFAULT 0.10;
ALTER TABLE loans ADD COLUMN total_payable DOUBLE;
ALTER TABLE loans ADD COLUMN remaining_balance DOUBLE DEFAULT 0;
ALTER TABLE loans ADD COLUMN due_date DATE;
ALTER TABLE loans ADD COLUMN status VARCHAR(50) DEFAULT 'active';
```

### New Loan Features:
- Interest rate calculation (10% default)
- Total payable amount tracking
- Remaining balance tracking
- Loan status management (active, paid, overdue)

---

## PHASE 3: WITNESS & SIGNATURE SUPPORT

### Date: Mid-Development
### Change Type: Column Additions to Loans Table

#### Added for Legal Compliance:
```sql
ALTER TABLE loans ADD COLUMN witness_name VARCHAR(255);
ALTER TABLE loans ADD COLUMN witness_contact VARCHAR(255);
ALTER TABLE loans ADD COLUMN witness_signature LONGBLOB;
ALTER TABLE loans ADD COLUMN user_signature LONGBLOB;
ALTER TABLE loans ADD COLUMN promissory_note_url VARCHAR(1024);
ALTER TABLE loans ADD COLUMN is_account_blocked BOOLEAN DEFAULT FALSE;
ALTER TABLE loans ADD COLUMN blocked_date TIMESTAMP NULL;
ALTER TABLE loans ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

### Features Added:
- Witness name and contact tracking
- Digital signature storage (witness and user)
- Promissory note URL storage
- Account blocking for overdue loans
- Blocked date tracking

---

## PHASE 4: ADMIN & AUDIT INFRASTRUCTURE

### Date: Mid-Development
### Change Type: New Tables

#### New Tables Created:
```sql
-- account_audit_log
CREATE TABLE account_audit_log (
  id, user_id, action, reason, admin_id, timestamp
)

-- user_messages
CREATE TABLE user_messages (
  id, user_id, admin_id, subject, body, is_read, created_at
)
```

### Features Added:
- Complete audit trail of admin actions
- Admin-to-user messaging system
- Read/unread status tracking
- Action history for compliance

---

## PHASE 5: PASSWORD RESET WORKFLOW

### Date: Feature Implementation
### Change Type: New Table

#### Created:
```sql
CREATE TABLE password_reset_requests (
  id, user_id, email, phone, status, admin_id, admin_response,
  created_at, reviewed_at, expires_at, completed_at
)
```

### Features Added:
- User password reset requests
- Admin approval workflow
- Request expiration (time-limited)
- Multiple contact methods (email + phone)
- Admin response tracking
- Request completion tracking

### Indexes Created:
```sql
CREATE INDEX idx_reset_userid ON password_reset_requests(user_id);
CREATE INDEX idx_reset_status ON password_reset_requests(status);
```

---

## PHASE 6: USER PROFILE ENHANCEMENTS

### Date: User Registration Improvements
### Change Type: Column Addition

#### Added to Users Table:
```sql
ALTER TABLE users ADD COLUMN nationality VARCHAR(100);
```

### Features Added:
- User nationality tracking
- Enhanced user profile information
- Better KYC (Know Your Customer) compliance

---

## PHASE 7: PIN AUTHENTICATION & RESET (LATEST - May 4, 2026)

### Date: May 4, 2026
### Change Type: Column Addition + New Table

#### Added to Users Table:
```sql
ALTER TABLE users ADD COLUMN pin VARCHAR(255);
```

#### New Table Created:
```sql
CREATE TABLE pin_reset_requests (
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

### Features Added:
- PIN-based authentication (separate from password)
- User PIN reset requests
- Admin approval workflow for PIN resets
- Request expiration (1 hour validity)
- Denial reason tracking
- Completion tracking

### New Java Classes:
- `PINResetManager.java` - PIN reset operations
- `ForgotPIN.java` - User PIN reset dialog
- `PINResetRequestDialog.java` - Admin management interface

### UI Updates:
- Added "Forgot PIN?" button to login screen
- Added "🔐 PIN Reset Requests" button to admin dashboard

---

## COMPLETE DATABASE EVOLUTION

```
Phase 1: Basic Tables
├─ users
├─ admin
├─ transactions
└─ loans (basic)

Phase 2: Loan Enhancements
├─ loans (enhanced with calculations)
└─ (No new tables)

Phase 3: Legal Compliance
├─ loans (with signatures & witness)
└─ (No new tables)

Phase 4: Admin Infrastructure
├─ account_audit_log
├─ user_messages
└─ (audit tracking)

Phase 5: Password Reset
├─ password_reset_requests
└─ (approval workflow)

Phase 6: User Profiles
├─ users (added nationality)
└─ (KYC compliance)

Phase 7: PIN Authentication (LATEST)
├─ users (added pin field)
├─ pin_reset_requests
└─ (dual auth support)
```

---

## SUMMARY OF ALL CHANGES

### Total Tables: 8
| Table | Created | Purpose |
|---|---|---|
| users | Phase 1 | User accounts (with pin added Phase 7) |
| admin | Phase 1 | Admin accounts |
| transactions | Phase 1 | Transaction history |
| loans | Phase 1 | Loan management (enhanced Phases 2-3) |
| account_audit_log | Phase 4 | Admin action tracking |
| user_messages | Phase 4 | Admin-to-user messaging |
| password_reset_requests | Phase 5 | Password reset workflow |
| pin_reset_requests | Phase 7 | PIN reset workflow (NEW) |

### Total Fields Added: 40+
### Total Relationships: 8
### Total Indexes: 6

---

## KEY MILESTONES

1. ✅ **Initial Setup** - Core tables created
2. ✅ **Loan Management** - Calculation and tracking fields added
3. ✅ **Legal Compliance** - Witness and signature support
4. ✅ **Admin Infrastructure** - Audit trail and messaging
5. ✅ **Password Reset** - User-requested password changes with admin approval
6. ✅ **User Profiles** - Enhanced user information (KYC)
7. ✅ **PIN Authentication** - Dual authentication (password + PIN) with admin-controlled resets

---

## WHAT'S NEXT?

Potential future enhancements:
- [ ] Two-factor authentication (2FA)
- [ ] Email verification system
- [ ] Mobile app token authentication
- [ ] Biometric support
- [ ] Account recovery questions
- [ ] Transaction alerts
- [ ] Loan payment schedule tracking
- [ ] Automated late payment reminders
- [ ] Document management system
- [ ] Advanced reporting and analytics

---

## MIGRATION CHECKLIST

When updating from older version to current:

- [ ] Backup existing database
- [ ] Run PHASE 1 tables (if new install)
- [ ] Run PHASE 2 ALTER statements
- [ ] Run PHASE 3 ALTER statements
- [ ] Run PHASE 4 table creation
- [ ] Run PHASE 5 table creation
- [ ] Run PHASE 6 ALTER statements
- [ ] Run PHASE 7 table creation & pin column addition
- [ ] Verify all tables created: `DESCRIBE [table_name];`
- [ ] Test password reset functionality
- [ ] Test PIN reset functionality
- [ ] Run sample data inserts
- [ ] Verify all indexes created

---

## ROLLBACK PROCEDURES

If you need to rollback PIN changes (Phase 7):
```sql
-- CAUTION: This will delete all PIN reset requests!
DROP TABLE IF EXISTS pin_reset_requests;
ALTER TABLE users DROP COLUMN pin;
```

To rollback Password Reset (Phase 5):
```sql
DROP TABLE IF EXISTS password_reset_requests;
```

---

## FILE REFERENCES

**Schema Files:**
- `database_schema.sql` - Complete current schema
- `COMPLETE_DATABASE_SETUP.sql` - All-in-one setup script
- `password_reset_migration.sql` - Password reset table creation
- `DATABASE_UPDATES_COMPLETE.md` - Detailed field documentation

**Java Classes:**
- `DBInit.java` - Programmatic table creation
- `CreateLawBank.java` - Initial setup class
- `CreateAdminTable.java` - Admin table setup
- `AccountManager.java` - Account audit log management
- `MessageManager.java` - User messaging
- `PasswordResetManager.java` - Password reset handling
- `PINResetManager.java` - PIN reset handling (NEW)

---

## VERSION HISTORY

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | May 4, 2026 | PIN authentication and reset workflow added |
| 0.9 | Earlier | Password reset workflow added |
| 0.8 | Earlier | User profile enhancements |
| 0.7 | Earlier | Admin infrastructure completed |
| 0.6 | Earlier | Witness & signature support |
| 0.5 | Earlier | Loan management enhancements |
| 0.1 | Earlier | Initial setup |

---

## TOTAL STATS

- **Database:** lawbank
- **Tables:** 8
- **Columns:** 70+
- **Indexes:** 6+
- **Foreign Keys:** 8
- **Constraints:** Cascade Delete enabled
- **Character Set:** UTF-8 (utf8mb4)
- **Engine:** InnoDB

**Status:** ✅ Production Ready

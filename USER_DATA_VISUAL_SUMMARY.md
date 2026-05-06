# User Data Flow - Visual Summary

## 1. Database Schema - User & Related Tables

```
┌─────────────────────────────────────────────────────────────┐
│                      USERS TABLE                             │
├─────────────────────────────────────────────────────────────┤
│  id (PK, INT)                  [Auto-increment]              │
│  fullname (VARCHAR 100)         [Required]                   │
│  username (VARCHAR 50, UNIQUE)  [Required]                   │
│  sex (VARCHAR 10)               [Optional]                   │
│  nationality (VARCHAR 100)      [Optional]                   │
│  address (VARCHAR 255)          [Optional]                   │
│  age (INT)                      [Optional]                   │
│  pin (VARCHAR 255)              [Required, Hashed]           │
│  password (VARCHAR 255)         [Optional]                   │
│  email (VARCHAR 255, UNIQUE)    [In some schemas]            │
│  role (VARCHAR 20)              [Default: 'user']            │
│  balance (DECIMAL 15,2)         [Default: 0]                │
│  savings (DECIMAL 15,2)         [Default: 0]                │
│  total_deposit (DECIMAL 15,2)   [Default: 0]                │
│  created_at (TIMESTAMP)         [Current timestamp]          │
└─────────────────────────────────────────────────────────────┘
         ↓                    ↓                    ↓
    ┌────────────┐    ┌──────────────┐    ┌──────────────┐
    │ LOANS      │    │ TRANSACTIONS │    │ LOAN_APPS    │
    ├────────────┤    ├──────────────┤    ├──────────────┤
    │ id         │    │ id           │    │ id           │
    │ user_id(FK)│    │ user_id(FK)  │    │ user_id(FK)  │
    │ amount     │    │ amount       │    │ requested_amt│
    │ status     │    │ type         │    │ status       │
    │ due_date   │    │ method       │    │ admin_id(FK) │
    │ witness    │    │ created_at   │    │ approved_amt │
    └────────────┘    └──────────────┘    └──────────────┘
```

## 2. Session Management

```
LOGIN PROCESS
─────────────────────────────────────────────────────────
User Input (username, PIN)
          ↓
    [SignInUsers validates]
          ↓
  SELECT * FROM users 
  WHERE username=? AND pin=?
          ↓
    ✓ If match found:
    ├─ Session.userId = user.id
    ├─ Session.fullname = user.fullname
    ├─ Session.role = user.role
    ├─ Session.isAdmin = false
    └─ UserDashboard opens
          ↓
    ✗ If no match:
    └─ Login error message
```

## 3. User Information Categories

```
┌──────────────────────────────────────────────────────────┐
│              USER INFORMATION CATEGORIES                  │
├──────────────────────────────────────────────────────────┤
│                                                            │
│  🔐 AUTHENTICATION (Security-Critical)                   │
│  ├─ username      [Unique identifier for login]          │
│  ├─ pin           [4-6 digit code, hashed]               │
│  ├─ password      [Optional, hashed]                     │
│  └─ role          ['user', 'suspended', 'admin']         │
│                                                            │
│  👤 PERSONAL INFORMATION (KYC Data)                      │
│  ├─ fullname      [User's name]                          │
│  ├─ age           [User's age]                           │
│  ├─ sex           [Male/Female]                          │
│  ├─ nationality   [Country]                              │
│  ├─ address       [Physical address]                     │
│  └─ email         [Contact email]                        │
│                                                            │
│  💰 FINANCIAL DATA (Account Summary)                     │
│  ├─ balance       [Current account balance]              │
│  ├─ savings       [Savings account balance]              │
│  └─ total_deposit [Total amount deposited]               │
│                                                            │
│  📅 SYSTEM FIELDS (Administrative)                       │
│  └─ created_at    [Account creation timestamp]           │
│                                                            │
└──────────────────────────────────────────────────────────┘
```

## 4. User Profile View Flow

```
USER DASHBOARD
         │
         ├─ Balance/Savings Display ◄── SELECT balance, savings FROM users
         │
         ├─ Transactions ◄── SELECT * FROM transactions WHERE user_id=?
         │
         ├─ Loans ◄── SELECT * FROM loans WHERE user_id=?
         │
         └─ [Transaction Button] → showAllRecordsDialog()
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ↓                   ↓                   ↓
            PROFILE TAB          TRANSACTIONS TAB      LOANS TAB
            (READ-ONLY)          (VIEW ONLY)           (VIEW ONLY)
            ─────────────        ────────────          ─────────
            • fullname           • All user txs        • All user
            • username           • Amounts             • loans
            • age                • Methods             • status
            • sex                • Dates               • amounts
            • nationality        • Types              • dates
            • address                                  
            • email                                    
            • balance                                  
            • savings                                  
            • total_deposit                            
            • created_at                               
```

## 5. Admin Dashboard - User Management

```
ADMIN DASHBOARD
        │
        ├─ Users Table ◄── Complex JOIN query
        │   ├─ id
        │   ├─ username
        │   ├─ balance
        │   ├─ savings
        │   ├─ total_deposit
        │   ├─ loans_total
        │   ├─ active_loans_total
        │   └─ loans_count
        │
        │   ⚠️ NOTE: Personal info NOT visible
        │   ✗ No address
        │   ✗ No email
        │   ✗ No age/nationality
        │
        ├─ Loans Management
        ├─ Loan Applications Review
        ├─ Transaction History
        ├─ PIN Reset Requests
        ├─ Messaging
        └─ Account Suspension/Reactivation
```

## 6. Data Update Patterns - What CAN Be Changed

```
UPDATE OPERATIONS AVAILABLE
───────────────────────────────────────────────────────────

✅ Financial Updates (UserDashboard)
   └─ UPDATE users SET balance=?, savings=?, total_deposit=?
      WHERE id=?

✅ PIN Management (PINResetManager)
   └─ UPDATE users SET pin=? WHERE id=?

✅ Password Management (PasswordResetManager)
   └─ UPDATE users SET password=? WHERE id=?

✅ Account Status (AccountManager)
   └─ UPDATE users SET role='suspended'|'user' WHERE id=?

❌ PROFILE INFORMATION - CANNOT BE UPDATED
   └─ fullname       ✗ No update mechanism
   └─ address        ✗ No update mechanism
   └─ age            ✗ No update mechanism
   └─ sex            ✗ No update mechanism
   └─ nationality    ✗ No update mechanism
   └─ email          ✗ No update mechanism
```

## 7. Current Implementation - Summary Table

```
┌──────────────────────────────────────────────────────────────┐
│  FEATURE / DATA FIELD         │ User Can │ Admin Can │ Status │
├────────────────────────────────────────────────────────────────┤
│ VIEW Profile                  │    ✓     │     ✓     │ ✅     │
│ EDIT Profile (personal info)  │    ✗     │     ✗     │ ❌     │
│ Change PIN                    │    ✓     │     ✓     │ ✅     │
│ Reset Password                │    ✓     │     ✓     │ ✅     │
│ View Balance/Savings          │    ✓     │     ✓     │ ✅     │
│ Update Balance                │    ✓     │     -     │ ✅     │
│ View Transactions             │    ✓     │     ✓     │ ✅     │
│ View Loans                    │    ✓     │     ✓     │ ✅     │
│ Apply for Loan                │    ✓     │     -     │ ✅     │
│ Manage Loan (Admin)           │    -     │     ✓     │ ✅     │
│ View Full User Info           │    ✓     │     ✗     │ ⚠️      │
│ Edit User Info (Name/Address) │    ✗     │     ✗     │ ❌     │
│ Send Message to User          │    -     │     ✓     │ ✅     │
│ Suspend Account               │    -     │     ✓     │ ✅     │
└────────────────────────────────────────────────────────────────┘
Legend: ✅ = Fully Implemented
        ⚠️  = Partially Implemented
        ❌ = Not Implemented
        - = Not Applicable
```

## 8. Schema Inconsistency Matrix

```
Field          │ CreateLaw  │ COMPLETE_  │ DBInit.   │ Used In
               │ Bank.java  │ DATABASE   │ java      │ Code
───────────────┼────────────┼────────────┼───────────┼──────────
fullname       │     ✓      │     ✓      │     ✓     │ ✅ Yes
username       │     ✓      │     ✓      │     ✓     │ ✅ Yes
email          │     ✗      │     ✓      │     ✗     │ ⚠️ Mixed
sex            │     ✓      │     ✗      │     ✗     │ ✅ Used
nationality    │     ✓      │     ✓      │     ✗     │ ✅ Used
address        │     ✓      │     ✗      │     ✗     │ ✅ Used
age            │     ✓      │     ✗      │     ✗     │ ✅ Used
pin            │     ✓      │     ✓      │     ✓     │ ✅ Yes
password       │     ✓      │     ✓      │     ✓     │ ✅ Yes
balance        │     ✓      │     ✓      │     ✓     │ ✅ Yes
savings        │     ✓      │     ✓      │     ✓     │ ✅ Yes
total_deposit  │     ✓      │     ✓      │     ✓     │ ✅ Yes
role           │     ✓      │     ✓      │     ✓     │ ✅ Yes
created_at     │     ✓      │     ✓      │     ✓     │ ✅ Yes

PRIMARY: CreateLawBank.java (Most Complete)
```

## 9. Code Location Quick Reference

```
USER PROFILE VIEWING
  ├─ UserDashboard.java
  │  ├─ showAllRecordsDialog() [Line 1266]
  │  └─ buildProfileTable() [Line 1483]
  │
  └─ View Trigger: TransactionBtn click event

USER REGISTRATION
  ├─ RegesterUser.java
  │  └─ Form with 9 fields
  │
  └─ INSERT: fullname, username, sex, nationality, address, age, pin

SESSION MANAGEMENT
  ├─ Session.java [5 static fields]
  └─ Populated on login via SignInUsers.java

ADMIN DASHBOARD
  ├─ AdminDashboard.java [800+ lines]
  │  ├─ buildTableModel() [Line 469]
  │  └─ User query [Line 363]
  │
  └─ Display: id, username, balance, savings, total_deposit, 
             loans_total, active_loans_total, loans_count

PIN RESET
  ├─ PINResetManager.java
  │  └─ updateUserPIN(int userId, String newPIN)
  │
  └─ Query: UPDATE users SET pin=? WHERE id=?

DATABASE SCHEMA
  ├─ CreateLawBank.java [Primary] ✅
  ├─ COMPLETE_DATABASE_SETUP.sql
  ├─ DBInit.java
  └─ database_schema.sql
```

---

**This visual summary complements the detailed USER_PROFILE_EXPLORATION_REPORT.md**

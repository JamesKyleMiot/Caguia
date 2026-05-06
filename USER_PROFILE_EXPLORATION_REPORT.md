# Caguioa Bank - User Profile & Data Exploration Report

**Date**: May 7, 2026  
**Scope**: Complete analysis of user data model, storage, display, and management

---

## EXECUTIVE SUMMARY

The Caguioa Bank system collects comprehensive user information during registration but **lacks user profile editing capabilities**. Users can view their profile but cannot modify personal information (name, address, age, nationality, sex). The session management is minimal, storing only essential identifiers.

---

## 1. USER TABLE STRUCTURE

### 1.1 Complete User Table Schema

**Source**: `CreateLawBank.java` (line 37-51) - Primary implementation

```sql
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  fullname VARCHAR(100) NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  sex VARCHAR(10),
  nationality VARCHAR(100),
  address VARCHAR(255),
  age INT,
  pin VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'user',
  balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  savings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  total_deposit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
```

### 1.2 Schema Inconsistencies

⚠️ **Database schemas are inconsistent across files:**

| File | Schema | Missing Fields |
|------|--------|-----------------|
| `CreateLawBank.java` | Complete | None |
| `COMPLETE_DATABASE_SETUP.sql` | Basic | sex, age, address |
| `DBInit.java` | Basic | sex, age, address, email |
| `database_schema.sql` | Basic | sex, age, address |

**Recommendation**: Standardize on CreateLawBank.java schema (most complete)

---

## 2. USER INFORMATION STORAGE

### 2.1 User Fields Collected During Registration

**Source**: `RegesterUser.java` (line 302)

```java
INSERT INTO users(fullname, username, sex, nationality, address, age, pin)
```

**Registration Form Fields:**
- ✅ Full Name (VARCHAR 100)
- ✅ Username (VARCHAR 50, UNIQUE)
- ✅ Sex (Male/Female)
- ✅ Nationality (Dropdown - VARCHAR 100)
- ✅ Address (VARCHAR 255)
- ✅ Age (INT)
- ✅ PIN (VARCHAR 255, hashed)
- ✅ Password (separate field, label shows "jPasswordField1")
- ✅ Email (form has label, but implementation unclear)

### 2.2 User Data Categories

#### Authentication Fields
- `username` - Unique login identifier
- `pin` - Hashed PIN code (4-6 digits)
- `password` - Optional in some schemas
- `role` - 'user', 'suspended', or custom roles

#### Personal Information Fields
- `fullname` - User's full name
- `sex` - Male/Female
- `age` - User's age
- `nationality` - Country of residence
- `address` - Physical address
- `email` - Email address (optional in main schema, present in some)

#### Financial Fields
- `balance` - Current account balance (DECIMAL 15,2)
- `savings` - Savings account balance (DECIMAL 15,2)
- `total_deposit` - Total amount deposited (DECIMAL 15,2)

#### System Fields
- `id` - Auto-increment primary key
- `created_at` - Account creation timestamp

---

## 3. SESSION MANAGEMENT

### 3.1 Session.java - User Session Storage

**Source**: `Session.java` (line 1-10)

```java
public class Session {
    public static int userId;           // Current logged-in user ID
    public static int adminId;          // Admin ID (if admin logged in)
    public static String role;          // User role ('user', 'admin', etc.)
    public static String fullname;      // User's full name
    public static boolean isAdmin;      // Flag for admin status
}
```

**Stored in Memory**:
- Only 5 pieces of information stored
- No email, address, or other personal details
- All static (application-wide, not per-session)

### 3.2 Data Flow
1. User logs in → Database query fetches user record
2. Session variables populated → User navigates application
3. Session destroyed → Logout

**Limitation**: Minimal session data requires frequent database queries for user information.

---

## 4. USER PROFILE DISPLAY

### 4.1 UserDashboard.java - Profile Viewing

#### Profile Dialog (Line 1266-1357)

**Method**: `showAllRecordsDialog()`

**Features**:
- Modal dialog with tabbed interface
- Three tabs: Profile, Transactions, Loans
- Overview cards showing financial summary
- Read-only display (no editing)

**Displayed in Profile Tab**:
```java
JTable profileTable = buildProfileTable(con);
// Executes: SELECT * FROM users WHERE id=?
// Displays all user fields in Field/Value format
```

**Overview Cards**:
- Current Balance
- Savings Amount
- Total Deposit
- Transaction Count
- Loan Count
- Account Health Status (based on total_deposit >= 50000)

#### Profile Table Implementation (Line 1483-1500)

```java
private JTable buildProfileTable(Connection con) throws Exception {
    PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
    pst.setInt(1, Session.userId);
    ResultSet rs = pst.executeQuery();
    
    DefaultTableModel model = new DefaultTableModel(new Object[]{"Field", "Value"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;  // ❌ NOT EDITABLE
        }
    };
    
    // Iterates through all columns and displays them
    if (rs.next()) {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            model.addRow(new Object[]{metaData.getColumnLabel(i), rs.getObject(i)});
        }
    }
    return table;
}
```

**Triggered By**: `TransactionBtnActionPerformed()` event

---

## 5. ADMIN DASHBOARD - USER MANAGEMENT

### 5.1 AllUsersTable Display (Line 363-369)

**SQL Query**:
```sql
SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, 
       COALESCE(SUM(l.amount),0) AS loans_total, 
       COALESCE(SUM(CASE WHEN l.status='active' THEN l.amount ELSE 0 END),0) AS active_loans_total, 
       COUNT(l.id) AS loans_count 
FROM users u LEFT JOIN loans l ON l.user_id = u.id 
GROUP BY u.id ORDER BY u.id
```

**Columns Shown to Admin**:
1. id - User ID
2. username - Username
3. balance - Current balance
4. savings - Savings amount
5. total_deposit - Total deposits
6. loans_total - Sum of all loans
7. active_loans_total - Sum of active loans
8. loans_count - Count of loans

**Limitations**:
- ❌ No personal information visible (address, email, age, sex)
- ❌ Table is **non-editable** (isCellEditable returns false)
- ❌ No "Edit User" button or functionality
- ✅ Admin can manage loans and PIN resets

### 5.2 Admin Capabilities

**Available Actions**:
- 📊 View user list with financial summary
- 🔒 Loan Management
- 📋 Loan Applications
- 📧 Message users
- 🔑 PIN Reset management
- 🚫 Suspend/Unsuspend accounts

**NOT Available**:
- ❌ Edit user personal information
- ❌ Update email or address
- ❌ Modify user profile details

---

## 6. EXISTING USER-RELATED CODE PATTERNS

### 6.1 Database Queries

#### Load User Data
```java
// Get all user fields
PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
pst.setInt(1, Session.userId);

// Get specific fields
PreparedStatement pst = con.prepareStatement(
    "SELECT balance, savings, total_deposit FROM users WHERE id=?"
);
```

#### Update Operations (Currently Implemented)

| Operation | SQL | Class | Purpose |
|-----------|-----|-------|---------|
| Update Balance | `UPDATE users SET balance=balance+? WHERE id=?` | UserDashboard | Deposit/Withdraw |
| Update Savings | `UPDATE users SET savings=savings-? WHERE id=?` | UserDashboard | Transfer to balance |
| Update PIN | `UPDATE users SET pin=? WHERE id=?` | PINResetManager | PIN change/reset |
| Update Password | `UPDATE users SET password=? WHERE id=?` | PasswordResetManager | Password recovery |
| Suspend Account | `UPDATE users SET role='suspended' WHERE id=?` | AccountManager | Account suspension |
| Unsuspend Account | `UPDATE users SET role='user' WHERE id=?` | AccountManager | Account reactivation |

**NO UPDATES FOR**:
- ❌ fullname
- ❌ address
- ❌ email
- ❌ age
- ❌ sex
- ❌ nationality

### 6.2 User-Related Classes

**Core Classes**:
- `Session.java` - Session management
- `UserDashboard.java` - User interface & profile view
- `RegesterUser.java` - User registration
- `DB.java` - Database connection utility

**Management Classes**:
- `AccountManager.java` - Suspend/unsuspend accounts
- `PINResetManager.java` - PIN reset functionality
- `PasswordResetManager.java` - Password recovery
- `LoanManager.java` - Loan-related user updates
- `MessageManager.java` - User messaging

**Dialog/UI Classes**:
- `PINResetRequestDialog.java` - PIN reset request
- `PasswordResetRequestDialog.java` - Password reset request
- `PINResetOTPDialog.java` - OTP verification for PIN reset
- `LoanApplicationDialog.java` - Loan application
- `LoanPaymentDialog.java` - Loan payment

---

## 7. RELATED DATA STRUCTURES

### 7.1 Loan Information
```sql
CREATE TABLE loans (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  interest_rate DECIMAL(5,2) NOT NULL,
  total_payable DECIMAL(15,2) NOT NULL,
  remaining_balance DECIMAL(15,2) NOT NULL,
  status VARCHAR(50) DEFAULT 'active',
  due_date DATE NOT NULL,
  witness_name VARCHAR(100),
  witness_contact VARCHAR(100),
  witness_signature LONGBLOB,
  user_signature LONGBLOB,
  promissory_note_url VARCHAR(255),
  is_account_blocked BOOLEAN DEFAULT FALSE,
  blocked_date TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### 7.2 Loan Applications
- Requested amount, purpose, employment status, monthly income
- Admin approval/rejection workflow
- Status tracking (pending, approved, rejected)

### 7.3 Transactions
- Type (deposit, withdrawal, transfer)
- Amount, method, timestamp
- Transaction history for users

### 7.4 PIN/Password Reset Requests
- OTP generation and verification
- Email-based recovery workflow
- Admin review and approval

---

## 8. DATA FLOW DIAGRAMS

### 8.1 User Login Flow
```
User Input (username, PIN)
    ↓
SignInUsers.java validates
    ↓
DB query: SELECT * FROM users WHERE username=? AND pin=?
    ↓
Session.userId = returned_user_id
Session.fullname = returned_fullname
Session.role = returned_role
    ↓
UserDashboard opens
```

### 8.2 Profile View Flow
```
User clicks "Transaction" button
    ↓
showAllRecordsDialog() triggered
    ↓
DB query: SELECT * FROM users WHERE id=?
    ↓
buildProfileTable() creates non-editable JTable
    ↓
Display in modal dialog with tabs
```

### 8.3 PIN Reset Flow
```
User requests PIN reset
    ↓
PINResetRequestDialog captures request
    ↓
INSERT into pin_reset_requests
    ↓
OTP generated by OTPGenerator
    ↓
Email sent to user
    ↓
Admin reviews in AdminDashboard
    ↓
Admin approves → OTP verified
    ↓
User enters new PIN
    ↓
UPDATE users SET pin=? WHERE id=?
```

---

## 9. KEY FINDINGS

### ✅ STRENGTHS
1. **Rich User Data**: Comprehensive personal information collected during registration
2. **Structured Database**: Clear separation of concerns (users, loans, transactions)
3. **Financial Tracking**: Robust balance, savings, and deposit management
4. **Security Features**: Hashed PIN storage, OTP-based recovery
5. **Admin Tools**: Loan management, account suspension, messaging

### ❌ GAPS & LIMITATIONS
1. **No Profile Editing**: Users cannot update personal information after registration
2. **Minimal Session Data**: Only userId, fullname, role stored in memory
3. **No Email Updates**: Users cannot change email address
4. **No Address Updates**: Users cannot update address after registration
5. **Admin Cannot Edit**: Admins have no interface to edit user details
6. **Schema Inconsistency**: Multiple conflicting database schemas across files
7. **Limited Profile View**: Profile shows all fields but no context or editable form

### 🔄 DATA CONSISTENCY ISSUES
1. Email field present in COMPLETE_DATABASE_SETUP.sql but missing in CreateLawBank.java
2. RegesterUser.java collects email but updates only partial fields
3. Session doesn't load email, address, or other personal data
4. AdminDashboard doesn't display personal information

---

## 10. RECOMMENDED IMPROVEMENTS

### 10.1 Immediate Priorities
1. **Create UserProfileEditDialog.java** - Allow users to edit personal information
2. **Standardize User Table** - Use CreateLawBank.java schema consistently
3. **Add Email Field** - Include email in Session management
4. **Create AdminUserEditDialog.java** - Allow admins to edit user details

### 10.2 Medium-term Enhancements
1. **User Profile Validation** - Add phone number and email validation
2. **Profile History** - Track changes to user information
3. **Admin User Search** - Better user lookup in admin dashboard
4. **User Settings Page** - Centralized profile management interface

### 10.3 Long-term Considerations
1. **KYC Verification** - Document verification system
2. **Address Verification** - Proof of address requirement
3. **Profile Completion Status** - Track which users have complete profiles
4. **Data Privacy** - GDPR compliance for personal data

---

## 11. FILES REFERENCED IN THIS ANALYSIS

### Core User Management
- `Session.java` - Session storage
- `UserDashboard.java` - User interface (1,600+ lines)
- `RegesterUser.java` - Registration form
- `AdminDashboard.java` - Admin interface (800+ lines)

### Database Schemas
- `CreateLawBank.java` - Primary schema definition ✅
- `database_schema.sql` - SQL schema file
- `COMPLETE_DATABASE_SETUP.sql` - Complete setup script
- `DBInit.java` - Alternative schema initialization

### Related Management
- `AccountManager.java` - Account suspension
- `PINResetManager.java` - PIN reset
- `PasswordResetManager.java` - Password reset
- `LoanManager.java` - Loan operations

### UI Components
- `UserDashboard.form` - GUI form definition (NetBeans)
- `AdminDashboard.java` - Admin GUI
- `LoanApplicationDialog.java` - Loan UI
- `LoanPaymentDialog.java` - Payment UI

---

## 12. CONCLUSION

The Caguioa Bank system has a **solid foundation for user management** with comprehensive data collection and secure storage. However, it **lacks user profile editing capabilities**, preventing users from maintaining current information after registration. The system would benefit from:

1. A dedicated user profile editing interface
2. Standardized database schema across all initialization files
3. Enhanced session management with more user details
4. Admin tools for user profile management

This analysis provides the foundation for implementing these enhancements.

---

**End of Report**

# 📋 Complete System Documentation
## Caguioa Bank - Loan Management & Account Deactivation System

---

## 📑 Table of Contents
1. [System Overview](#system-overview)
2. [Features Implemented](#features-implemented)
3. [Architecture](#architecture)
4. [Database Schema](#database-schema)
5. [Classes & Methods](#classes--methods)
6. [Admin Interface Guide](#admin-interface-guide)
7. [Email Configuration](#email-configuration)
8. [Installation & Setup](#installation--setup)
9. [Usage Examples](#usage-examples)
10. [Troubleshooting](#troubleshooting)

---

## System Overview

The Caguioa Bank Loan Management System is a comprehensive solution for:
- **Managing overdue loans** with automatic account suspension
- **Witness/guarantor system** for loan security
- **Email notifications** at each stage (reminder, suspension, reactivation)
- **Payment processing** with balance tracking
- **Account deactivation** for unpaid loans
- **Audit logging** of all administrative actions

**Target Users**: Bank Administrators managing customer loans

---

## Features Implemented

### ✅ Core Features
1. **Account Deactivation**
   - Automatic suspension when loan becomes overdue
   - User role changed to 'suspended' = cannot login
   - User receives suspension warning email
   - Reactivates automatically when payment made

2. **Witness/Guarantor System**
   - Witness name and contact info stored with loan
   - Digital signature storage (user & witness)
   - Promissory note generation
   - Witness contact available for payment collection

3. **Email Notifications**
   - Loan due date reminders
   - Account suspension warnings
   - Payment confirmation emails
   - Loan creation notices with witness details

4. **Payment Processing**
   - Admin records payment in system
   - Remaining balance auto-calculated
   - Account auto-reactivated when fully paid
   - Payment history tracked

5. **Loan Management Dashboard**
   - View all overdue loans in one place
   - Days overdue calculation
   - Real-time status updates
   - Quick action buttons

6. **Audit Trail**
   - All account actions logged
   - Timestamp and admin recorded
   - Reason for action stored
   - Complete history available for review

### 🎯 Admin Actions Available
| Action | Effect | Email Sent |
|--------|--------|-----------|
| Send Reminder | Email user payment notice | ✓ Yes |
| Deactivate Account | Suspend user, block loan | ✓ Yes (warning) |
| Process Payment | Update balance, mark paid | ✓ Yes (confirmation) |
| Reactivate Account | Restore access | ✓ Yes |

---

## Architecture

### Class Diagram
```
AdminDashboard (Main Interface)
    ↓
    └─→ LoanManagementDialog (New UI Component)
            ├─→ LoanManager (Business Logic)
            │   ├─→ DB (Database Connection)
            │   └─→ EmailNotifier (Notifications)
            ├─→ AccountManager (Account Control)
            └─→ WitnessManager (Witness Handling)
```

### Data Flow for Deactivation
```
System Detects Overdue Loan
    ↓
Admin Opens Loan Management
    ↓
Admin Clicks "Deactivate Account"
    ↓
LoanManager.deactivateAccountForUnpaidLoan()
    ├─→ Updates loans.is_account_blocked = TRUE
    ├─→ Updates users.role = 'suspended'
    └─→ AccountManager.logAccountAction()
    ↓
EmailNotifier.sendAccountSuspensionWarning()
    ├─→ Sends email to user
    └─→ Includes payment details
    ↓
User Account Now Suspended
    └─→ Cannot login
    └─→ Cannot perform transactions
```

---

## Database Schema

### Original Tables (Unchanged)
- `users` - User accounts
- `transactions` - User transactions
- `admin` - Administrator accounts

### Enhanced `loans` Table

#### Original Fields
```sql
id INT - Primary Key
user_id INT - Foreign Key to users
amount DECIMAL - Loan principal
interest_rate DECIMAL - Interest percentage
total_payable DECIMAL - Total with interest
status VARCHAR - 'active' or 'paid'
created_at TIMESTAMP - Creation date
```

#### NEW FIELDS ADDED ⭐
```sql
remaining_balance DECIMAL(15,2)      -- Unpaid amount
due_date DATE NOT NULL               -- Payment deadline
witness_name VARCHAR(100)            -- Guarantor name
witness_contact VARCHAR(100)         -- Guarantor contact
witness_signature LONGBLOB           -- Guarantor signature image
user_signature LONGBLOB              -- Borrower signature image
promissory_note_url VARCHAR(255)     -- Agreement link
is_account_blocked BOOLEAN           -- Suspension flag
blocked_date TIMESTAMP               -- When suspended
```

### New Tables Auto-Created

#### `account_audit_log` (Auto-created by AccountManager)
```sql
id INT - Primary Key
user_id INT - User being acted upon
action VARCHAR(50) - SUSPENDED, REACTIVATED, etc.
reason VARCHAR(255) - Why action was taken
admin_id INT - Which admin did it
timestamp TIMESTAMP - When it happened
```

---

## Classes & Methods

### 1️⃣ LoanManager.java
**Purpose**: Core loan operations

```java
// Get all overdue loans
public static List<Map<String, Object>> getOverdueLoans()

// Get complete loan info with witness
public static Map<String, Object> getLoanDetails(int loanId)

// Suspend account + block loan
public static boolean deactivateAccountForUnpaidLoan(int loanId, int userId)

// Restore account + unblock loan
public static boolean reactivateAccountAfterPayment(int loanId, int userId)

// Add witness information
public static boolean updateWitnessInfo(int loanId, String witnessName, String witnessContact)

// Store signatures
public static boolean saveSignatures(int loanId, byte[] userSignature, byte[] witnessSignature)

// Record payment
public static boolean processLoanPayment(int loanId, double paymentAmount)

// Get user's all loans
public static List<Map<String, Object>> getUserLoans(int userId)
```

### 2️⃣ EmailNotifier.java
**Purpose**: Send email notifications

```java
// Remind about payment due
public static boolean sendLoanDueReminder(String email, String name, String amount, String dueDate)

// Warn about suspension
public static boolean sendAccountSuspensionWarning(String email, String name, String amount, String daysOverdue)

// Confirm payment received
public static boolean sendAccountReactivationEmail(String email, String name, String paidAmount)

// Notify of new loan
public static boolean sendLoanCreationConfirmation(String email, String name, ...)
```

**Configuration Required**:
```java
private static final String SENDER_EMAIL = "your_email@gmail.com";
private static final String SENDER_PASSWORD = "your_app_password";
```

### 3️⃣ AccountManager.java
**Purpose**: User account status management

```java
// Suspend user account
public static boolean suspendAccount(int userId, String reason)

// Restore user account
public static boolean reactivateAccount(int userId, String reason)

// Check if suspended
public static boolean isAccountSuspended(int userId)

// Get account status
public static String getAccountStatus(int userId)
```

### 4️⃣ WitnessManager.java
**Purpose**: Witness/guarantor operations

```java
// Set witness info
public static boolean setWitnessInfo(int loanId, String name, String contact)

// Get witness info
public static Map<String, String> getWitnessInfo(int loanId)

// Store signatures
public static boolean storeUserSignature(int loanId, byte[] signature)
public static boolean storeWitnessSignature(int loanId, byte[] signature)

// Retrieve signatures
public static byte[] getUserSignature(int loanId)
public static byte[] getWitnessSignature(int loanId)

// Check if both signed
public static boolean bothPartiesSigned(int loanId)

// List loans with witness
public static List<Map<String, Object>> getLoansWithWitness()

// Generate agreement
public static String generatePromissoryNote(...)
```

### 5️⃣ LoanManagementDialog.java
**Purpose**: Admin interface for loan control

**UI Components**:
- JTable: Displays overdue loans
- JButton: Send Reminder (blue)
- JButton: Deactivate Account (red)
- JButton: Process Payment (green)
- JButton: Reactivate Account (dark green)

**User Selection**:
- Click row to select loan
- Status label shows selected loan details
- Buttons enabled only when loan selected

---

## Admin Interface Guide

### Opening Loan Management

**Location**: AdminDashboard Header
**Button**: 🔒 Loan Management (Red button)

```
Admin Dashboard
    ↓
Header Actions (Top Right)
    ↓
Click "🔒 Loan Management"
    ↓
LoanManagementDialog Opens
```

### Loan Management Dialog

#### Table Columns
| Column | Content | Purpose |
|--------|---------|---------|
| Loan ID | Unique loan identifier | Reference |
| User ID | Customer ID | Reference |
| Full Name | Customer name | Identification |
| Username | Login username | Contact |
| Email | Email address | Notifications |
| Amount | Loan principal | Payment amount |
| Due Date | Payment deadline | Status indicator |
| Remaining | Unpaid balance | Key metric |
| Days Overdue | # of days late | Priority |
| Status | 'OVERDUE' | Status indicator |

#### Actions & Effects

**📧 Send Reminder**
- Requires: Loan selected
- Action: Sends email to user
- Email includes: Loan details, amount due, due date
- Effect: User notified of obligation

**🔒 Deactivate Account**
- Requires: Loan selected
- Action: Suspends user account
- Changes: 
  - User role = 'suspended'
  - Loan.is_account_blocked = TRUE
  - blocked_date = current timestamp
- Emails: Suspension warning sent
- Effect: User CANNOT login

**💳 Process Payment**
- Requires: Loan selected
- Action: Records payment amount
- Prompt: Enter payment amount
- Calculation: remaining_balance -= payment amount
- Auto: If balance ≤ 0, account reactivated
- Emails: Confirmation sent if paid off
- Effect: Balance updated, account may restore

**✅ Reactivate Account**
- Requires: Loan selected + Paid
- Action: Restores user access
- Changes:
  - User role = 'user'
  - Loan.is_account_blocked = FALSE
- Emails: Optional confirmation
- Effect: User CAN login again

---

## Email Configuration

### Gmail Setup (Required for email features)

#### Step 1: Enable 2-Factor Authentication
1. Go to: https://myaccount.google.com/security
2. Click "2-Step Verification"
3. Follow prompts to enable
4. Once enabled, proceed to step 2

#### Step 2: Create App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select "Mail" and "Windows Computer"
3. Google generates 16-character password
4. Copy the generated password (without spaces)
5. Keep this password safe

#### Step 3: Update EmailNotifier
```java
// File: src/caguioa/bank/EmailNotifier.java
// Line ~13-14

private static final String SENDER_EMAIL = "your_email@gmail.com";
private static final String SENDER_PASSWORD = "xxxx xxxx xxxx xxxx"; // 16-char app password
private static final String SMTP_HOST = "smtp.gmail.com";
private static final int SMTP_PORT = 587;
```

#### Step 4: Rebuild & Test
1. Save EmailNotifier.java
2. Clean and Build project
3. Run AdminDashboard
4. Send test reminder email
5. Check your inbox

### Email Templates Provided

**1. Loan Due Reminder**
```
Subject: ⏰ Loan Payment Reminder - Due Date Approaching
Content: 
  - Greeting with customer name
  - Loan amount and due date
  - Payment instructions
  - Contact info for questions
```

**2. Account Suspension Warning**
```
Subject: ⚠️ URGENT: Account Suspension Notice - Unpaid Loan
Content:
  - WARNING in red
  - Loan amount and days overdue
  - Explanation of suspension
  - Urgency to contact admin
```

**3. Account Reactivation Confirmation**
```
Subject: ✅ Account Reactivation - Payment Received
Content:
  - Confirmation of payment
  - Payment amount and date
  - Account now active
  - Thank you message
```

**4. Loan Creation Confirmation**
```
Subject: 📄 Loan Agreement Confirmation
Content:
  - Loan terms (amount, interest, total payable, due date)
  - Witness information
  - Important notes about payment
  - Account suspension warning
```

---

## Installation & Setup

### Prerequisites
- NetBeans IDE (or similar Java IDE)
- MySQL Server running
- Java JDK 8 or higher
- Gmail account (optional but recommended)

### Step 1: Copy New Files
Copy to `src/caguioa/bank/`:
- ✅ LoanManager.java
- ✅ EmailNotifier.java
- ✅ AccountManager.java
- ✅ WitnessManager.java
- ✅ LoanManagementDialog.java

### Step 2: Update Database
```
1. Open NetBeans
2. Right-click CreateLawBank.java
3. Select "Run File"
4. This updates the loans table schema
```

### Step 3: Configure Email (Optional)
```
1. Edit: EmailNotifier.java
2. Update SENDER_EMAIL and SENDER_PASSWORD
3. Use Gmail App Password (not regular password)
4. Save file
```

### Step 4: Rebuild Project
```
1. In NetBeans: Clean and Build
2. Wait for build to complete
3. Fix any compilation errors
4. All 5 new classes should compile successfully
```

### Step 5: Run Application
```
1. Run AdminDashboard.java
2. Login with credentials (admin / admin123)
3. Verify "🔒 Loan Management" button appears
4. Click to test dialog opening
```

### Step 6: Test Features
```
1. Create test loan with witness
2. Set due date in past
3. Open Loan Management
4. Test each action:
   - Send Reminder
   - Deactivate Account
   - Process Payment
   - Reactivate Account
```

---

## Usage Examples

### Example 1: Send Reminder to Customer

**Scenario**: Due date approaching, want to remind customer

**Steps**:
1. Open AdminDashboard
2. Click "🔒 Loan Management"
3. View "Overdue Loans" table
4. Find loan with "Days Overdue" = negative (future due)
5. Click the loan row to select it
6. Click "📧 Send Reminder" button
7. Confirm message shows email sent

**Result**: Customer receives reminder email

---

### Example 2: Deactivate Account for Unpaid Loan

**Scenario**: Loan is 15 days overdue, customer hasn't paid

**Steps**:
1. Open AdminDashboard
2. Click "🔒 Loan Management"
3. Find loan with "Days Overdue" = 15 or more
4. Click loan row to select it
5. Click "🔒 Deactivate Account" button
6. Confirm dialog appears ("Are you sure?")
7. Click "Yes" to confirm
8. Wait for "Success" message

**Results**:
- Customer's role changed to 'suspended'
- Customer CANNOT login
- Customer receives suspension warning email
- Loan marked as blocked in database
- Action logged in audit trail

**To Verify**:
```sql
-- Check user status
SELECT username, role FROM users WHERE id = ?;
-- Should show: role = 'suspended'

-- Check loan status
SELECT is_account_blocked, blocked_date FROM loans WHERE id = ?;
-- Should show: is_account_blocked = TRUE, blocked_date = timestamp
```

---

### Example 3: Process Payment & Reactivate

**Scenario**: Customer makes payment after account deactivation

**Steps**:
1. Customer contacts admin and makes payment
2. Admin opens AdminDashboard
3. Click "🔒 Loan Management"
4. Select the previously blocked loan
5. Click "💳 Process Payment" button
6. Prompt: "Enter payment amount"
7. Type payment amount (e.g., "5250.00")
8. Press Enter or click OK
9. Wait for "Success" message

**Automatic Results**:
- Remaining balance updated
- If balance ≤ 0:
  - Loan status set to 'paid'
  - User account automatically reactivated
  - User role changed back to 'user'
  - Confirmation email sent to customer
- Customer can now login again

**To Verify**:
```sql
-- Check remaining balance
SELECT remaining_balance, status FROM loans WHERE id = ?;
-- Should show: remaining_balance = 0, status = 'paid'

-- Check user access restored
SELECT username, role FROM users WHERE id = ?;
-- Should show: role = 'user'
```

---

### Example 4: View All Witness Information

**Database Query**:
```sql
SELECT 
    l.id,
    u.username,
    l.amount,
    l.witness_name,
    l.witness_contact,
    CASE WHEN l.user_signature IS NOT NULL THEN 'YES' ELSE 'NO' END AS 'User Signed',
    CASE WHEN l.witness_signature IS NOT NULL THEN 'YES' ELSE 'NO' END AS 'Witness Signed'
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.witness_name IS NOT NULL;
```

---

## Troubleshooting

### Issue: "LoanManagementDialog class not found"

**Cause**: File not in correct location or not compiled

**Solutions**:
- ✅ Check file is in: `src/caguioa/bank/LoanManagementDialog.java`
- ✅ Right-click project → Clean and Build
- ✅ Restart NetBeans
- ✅ Check for compilation errors in Output window

---

### Issue: Emails not sending

**Cause**: Email configuration incorrect

**Solutions**:
1. ✅ Verify SENDER_EMAIL in EmailNotifier.java
2. ✅ Verify SENDER_PASSWORD is App Password (not regular password)
3. ✅ Enable 2FA on Gmail account first
4. ✅ Check SMTP settings (host: smtp.gmail.com, port: 587)
5. ✅ Check firewall allows port 587

**Test Email Code**:
```java
String testEmail = "recipient@example.com";
boolean sent = EmailNotifier.sendLoanDueReminder(
    testEmail, 
    "Test User", 
    "5000.00", 
    "2026-05-30"
);
System.out.println("Email sent: " + sent);
```

---

### Issue: Account not deactivating

**Cause**: Database error or permission issue

**Solutions**:
- ✅ Verify MySQL server is running
- ✅ Test database connection: `DB.connect()`
- ✅ Check if user exists in database
- ✅ Check if loan exists in database
- ✅ Review console output for SQL errors
- ✅ Verify database has write permissions

**Debug Queries**:
```sql
-- Check user exists
SELECT * FROM users WHERE id = ?;

-- Check loan exists
SELECT * FROM loans WHERE id = ?;

-- Check suspended accounts
SELECT * FROM users WHERE role = 'suspended';
```

---

### Issue: Payment not processing

**Cause**: Invalid amount or database issue

**Solutions**:
- ✅ Ensure amount entered is numeric
- ✅ Ensure amount is positive (> 0)
- ✅ Ensure loan still exists in database
- ✅ Check remaining_balance exists in loans table
- ✅ Review console for SQL errors

**Test Payment Code**:
```java
// Get loan details first
Map<String, Object> loan = LoanManager.getLoanDetails(1);
double remaining = (Double) loan.get("remaining_balance");
System.out.println("Remaining balance: " + remaining);

// Process payment
boolean success = LoanManager.processLoanPayment(1, 1000.00);
System.out.println("Payment processed: " + success);
```

---

### Issue: Loan Management dialog won't open

**Cause**: Button not added or dialog class missing

**Solutions**:
- ✅ Verify button code in AdminDashboard.java (look for "Loan Management")
- ✅ Verify `openLoanManagementDialog()` method exists
- ✅ Check LoanManagementDialog.java is in project
- ✅ Rebuild project
- ✅ Check console for NullPointerException

---

### Issue: "Cannot connect to database"

**Cause**: MySQL not running or credentials wrong

**Solutions**:
- ✅ Start MySQL server
- ✅ Verify credentials in DB.java
- ✅ Check MySQL is on localhost:3306
- ✅ Check database "lawbank" exists
- ✅ Test connection with MySQL client:
  ```
  mysql -u root -p
  SHOW DATABASES;
  USE lawbank;
  ```

---

## Performance Notes

- **Overdue loans query**: Indexes on `due_date` and `user_id` recommended
- **Account audit log**: Can grow large; archive old entries periodically
- **Email notifications**: May take 1-2 seconds per email
- **Signature storage**: BLOB fields can be large; monitor database size

---

## Security Checklist

- [ ] EmailNotifier configured with App Password (not regular password)
- [ ] AdminDashboard restricted to admin users only
- [ ] Loan Management only accessible to admins
- [ ] Account audit log enabled for tracking
- [ ] Database backups scheduled
- [ ] Sensitive data (passwords) not logged
- [ ] Email credentials stored securely

---

## Version Information

- **System Version**: 1.0
- **Release Date**: 2026
- **Compatible With**: Java 8+, NetBeans 8.0+, MySQL 5.7+
- **Last Updated**: 2026

---

## Support & Contact

For issues or questions:
1. Check QUICK_SETUP.md for quick reference
2. Review SQL_TEST_QUERIES.sql for testing
3. Check class docstrings and comments
4. Review AdminDashboard integration

---

**End of Documentation**

🎉 System ready for deployment!

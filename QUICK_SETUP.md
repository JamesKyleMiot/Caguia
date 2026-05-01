# Quick Setup Guide - Loan Management System

## 🚀 Getting Started (5 Steps)

### Step 1: Update Database Schema
```
1. Open NetBeans
2. Run: CreateLawBank.java (right-click → Run)
3. This updates the loans table with new fields
```

### Step 2: Configure Email (Optional but Recommended)
```
1. Edit: src/caguioa/bank/EmailNotifier.java
2. Find these lines (around line 13-14):
   
   private static final String SENDER_EMAIL = "your_email@gmail.com";
   private static final String SENDER_PASSWORD = "your_app_password";

3. Replace with your Gmail details:
   - Generate App Password in Google Account settings
   - NEVER use your regular Gmail password
   
4. Save the file
```

### Step 3: Rebuild Project
```
1. In NetBeans: Clean and Build Project
2. Fix any compilation errors
3. Ensure all new classes compile successfully
```

### Step 4: Run Application
```
1. Run AdminDashboard.java
2. Login with admin credentials (username: admin, password: admin123)
3. You should see new "🔒 Loan Management" button in header
```

### Step 5: Test the System
```
1. Create a test loan with witness info
2. Set due date in the past
3. Open Loan Management dialog
4. Test: Send Reminder, Deactivate Account, Process Payment
```

---

## 📋 What Each Class Does

| Class | Purpose | Key Methods |
|-------|---------|------------|
| **LoanManager** | Core loan logic | getOverdueLoans(), processLoanPayment() |
| **EmailNotifier** | Send emails | sendLoanDueReminder(), sendAccountSuspensionWarning() |
| **AccountManager** | Suspend/activate accounts | suspendAccount(), reactivateAccount() |
| **WitnessManager** | Manage witness info | storeUserSignature(), getWitnessInfo() |
| **LoanManagementDialog** | Admin UI | Opens when clicking "Loan Management" button |

---

## 🎯 Common Tasks

### Task 1: Send Payment Reminder to User
```
Admin Dashboard → 🔒 Loan Management
→ Select loan from table
→ Click "📧 Send Reminder"
→ User receives email
```

### Task 2: Deactivate Account (Unpaid Loan)
```
Admin Dashboard → 🔒 Loan Management
→ Select loan from table
→ Click "🔒 Deactivate Account"
→ Confirm action
→ User account suspended + email sent
→ User cannot login
```

### Task 3: Record Payment & Reactivate
```
Admin Dashboard → 🔒 Loan Management
→ Select loan from table
→ Click "💳 Process Payment"
→ Enter payment amount
→ If fully paid: account auto-reactivated
→ Confirmation email sent to user
```

### Task 4: Check Loan Witness Information
```
Open: src/caguioa/bank/WitnessManager.java
Use: getLoansWithWitness() → Lists all loans with witness
     getWitnessInfo(loanId) → Get witness for specific loan
```

---

## ⚙️ Configuration Options

### Email Settings (EmailNotifier.java)
```java
private static final String SENDER_EMAIL = "your_email@gmail.com";
private static final String SENDER_PASSWORD = "your_app_password"; // NOT regular password
private static final String SMTP_HOST = "smtp.gmail.com";
private static final int SMTP_PORT = 587;
```

### Database Connection (DB.java)
```java
return DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/lawbank",
    "root",          // Change if needed
    ""               // Password (empty by default)
);
```

---

## 🐛 Troubleshooting

### Problem: Emails not sending
**Solution**:
- ✅ Enable Gmail 2-factor authentication
- ✅ Create App Password (not regular password)
- ✅ Update EmailNotifier.java with App Password
- ✅ Check firewall allows port 587

### Problem: Account not deactivating
**Solution**:
- ✅ Verify MySQL server is running
- ✅ Check database connection in DB.java
- ✅ Ensure loan and user exist
- ✅ Check console for error messages

### Problem: "LoanManagementDialog class not found"
**Solution**:
- ✅ Ensure LoanManagementDialog.java is in src/caguioa/bank/ folder
- ✅ Clean and rebuild project
- ✅ Restart NetBeans if needed

### Problem: Database schema not updated
**Solution**:
- ✅ Run CreateLawBank.java explicitly
- ✅ Check MySQL server is running
- ✅ Verify database "lawbank" exists
- ✅ Check for console error messages

---

## 📧 Gmail Setup (2-3 minutes)

1. Go to: https://myaccount.google.com/apppasswords
2. Sign in with your Gmail account
3. Select "Mail" and "Windows Computer" (or your device)
4. Google generates 16-character App Password
5. Copy this password → paste in EmailNotifier.java
6. Save and rebuild project

---

## 📊 Database Schema Changes

```sql
-- New fields added to LOANS table:
remaining_balance DECIMAL(15,2)      -- Unpaid amount
due_date DATE NOT NULL               -- Payment deadline
witness_name VARCHAR(100)            -- Guarantor name
witness_contact VARCHAR(100)         -- Guarantor phone/email
witness_signature LONGBLOB           -- Guarantor signature image
user_signature LONGBLOB              -- Borrower signature image
promissory_note_url VARCHAR(255)     -- Link to agreement
is_account_blocked BOOLEAN DEFAULT 0 -- Account suspended flag
blocked_date TIMESTAMP NULL          -- When account was blocked

-- Auto-created table:
account_audit_log                    -- Tracks all account actions
```

---

## ✅ Verification Checklist

After implementation, verify:
- [ ] CreateLawBank.java runs without errors
- [ ] New classes appear in code with no syntax errors
- [ ] AdminDashboard shows "🔒 Loan Management" button
- [ ] Loan Management dialog opens when clicked
- [ ] Table displays overdue loans
- [ ] Can select loan and perform actions
- [ ] Email notifications send (if configured)
- [ ] Account deactivation works (user role = 'suspended')
- [ ] Payment processing updates balance
- [ ] Account reactivation works

---

## 📞 Quick Reference Commands

### MySQL Check
```
mysql -u root
SHOW DATABASES;
USE lawbank;
SHOW TABLES;
DESC loans;
```

### View Overdue Loans
```sql
SELECT l.id, u.username, l.amount, l.due_date, l.remaining_balance
FROM loans l
JOIN users u ON l.user_id = u.id
WHERE l.status = 'active' AND l.due_date < CURDATE();
```

### Check Suspended Accounts
```sql
SELECT id, username, role FROM users WHERE role = 'suspended';
```

### View Account Audit Log
```sql
SELECT * FROM account_audit_log ORDER BY timestamp DESC LIMIT 20;
```

---

**Ready to Deploy! 🎉**

Questions? Check:
- LOAN_MANAGEMENT_README.md (detailed documentation)
- IMPLEMENTATION_SUMMARY.md (complete overview)
- Each class file (contains detailed comments)

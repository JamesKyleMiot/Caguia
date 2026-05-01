# Caguioa Bank - Loan Management & Account Deactivation System

## Overview
This document outlines the comprehensive loan management system with account deactivation, witness requirements, and email notifications for unpaid loans.

## New Features Implemented

### 1. **Enhanced Loan Database Schema**
The loans table now includes:
- **Witness Information**: `witness_name`, `witness_contact`
- **Signatures**: `user_signature`, `witness_signature` (stored as BLOB)
- **Loan Tracking**: `remaining_balance`, `due_date`
- **Account Control**: `is_account_blocked`, `blocked_date`
- **Promissory Note**: `promissory_note_url` (reference to agreement document)

### 2. **LoanManager Utility Class**
Core loan operations handler:

#### Methods:
- `getOverdueLoans()` - Retrieves all overdue loans that aren't blocked
- `deactivateAccountForUnpaidLoan()` - Suspends user account and marks loan as blocked
- `reactivateAccountAfterPayment()` - Restores account after loan payment
- `getLoanDetails()` - Fetches complete loan information with witness details
- `updateWitnessInfo()` - Updates witness name and contact information
- `saveSignatures()` - Stores digital signatures of user and witness
- `processLoanPayment()` - Processes payment and updates remaining balance
- `getUserLoans()` - Gets all loans for a specific user

### 3. **EmailNotifier System**
Automated email notifications for loan management:

#### Email Types:
- **Loan Due Reminder** - Sent to users approaching due date
- **Account Suspension Warning** - Sent when account is deactivated for unpaid loan
- **Account Reactivation Confirmation** - Sent when account is restored after payment
- **Loan Creation Confirmation** - Sent with witness details when loan is created

#### Configuration:
To use email notifications, update these settings in `EmailNotifier.java`:
```java
private static final String SENDER_EMAIL = "your_email@gmail.com";
private static final String SENDER_PASSWORD = "your_app_password";
```
**Note**: For Gmail, use an App Password (not your regular password)

### 4. **AccountManager Class**
Account status management:

#### Methods:
- `suspendAccount()` - Deactivate user account (set role to 'suspended')
- `reactivateAccount()` - Activate user account (set role back to 'user')
- `isAccountSuspended()` - Check if account is suspended
- `getAccountStatus()` - Get current account role/status

### 5. **Loan Management Dialog (UI)**
New admin interface in AdminDashboard with:

#### Features:
- **Overdue Loans Table** showing:
  - Loan ID, User ID, Full Name, Username, Email
  - Loan Amount, Due Date, Remaining Balance
  - Days Overdue, Status indicator

#### Admin Actions:
- **📧 Send Reminder** - Send email reminder to user
- **🔒 Deactivate Account** - Suspend account for unpaid loan
- **💳 Process Payment** - Record loan payment and update balance
- **✅ Reactivate Account** - Restore access after payment

## Workflow for Unpaid Loans

### Step 1: Loan Creation
1. Admin creates loan with witness information
2. Email confirmation sent to user with:
   - Loan terms
   - Witness details
   - Due date
   - Promissory note requirement

### Step 2: Payment Reminder (Before Due Date)
1. Admin can send reminder email
2. User has notice to prepare payment

### Step 3: Loan Overdue (After Due Date)
1. System identifies overdue loans
2. Admin can deactivate account:
   - User status changed to 'suspended'
   - Loan marked as blocked
   - Suspension warning email sent
   - User cannot access account

### Step 4: Payment Processing
1. Admin receives payment from user
2. Admin enters payment amount in system
3. System updates remaining balance
4. If fully paid:
   - Account automatically reactivated
   - Confirmation email sent
   - Loan status set to 'paid'

## Witness Role & Responsibility

The witness (guarantor):
- Signs the promissory note alongside the borrower
- Acts as backup payment guarantee
- Can be required to pay loan if user cannot
- Is notified of loan status and payment deadlines
- Contact information stored for follow-up communication

## Database Changes

Run `CreateLawBank.java` to apply schema updates:

```sql
-- Updated LOANS table with new fields:
ALTER TABLE loans ADD COLUMN remaining_balance DECIMAL(15,2);
ALTER TABLE loans ADD COLUMN due_date DATE;
ALTER TABLE loans ADD COLUMN witness_name VARCHAR(100);
ALTER TABLE loans ADD COLUMN witness_contact VARCHAR(100);
ALTER TABLE loans ADD COLUMN witness_signature LONGBLOB;
ALTER TABLE loans ADD COLUMN user_signature LONGBLOB;
ALTER TABLE loans ADD COLUMN promissory_note_url VARCHAR(255);
ALTER TABLE loans ADD COLUMN is_account_blocked BOOLEAN DEFAULT FALSE;
ALTER TABLE loans ADD COLUMN blocked_date TIMESTAMP NULL;
```

## New Classes Created

1. **LoanManager.java** - Core loan operations
2. **EmailNotifier.java** - Email notification system
3. **AccountManager.java** - Account status management
4. **LoanManagementDialog.java** - Admin UI for loan control

## Access from AdminDashboard

New button in Admin Dashboard: **🔒 Loan Management**
- Click to open loan management dialog
- View all overdue loans
- Perform account deactivation/reactivation
- Process payments
- Send notifications

## Security Considerations

1. **Account Suspension**: User cannot login when role = 'suspended'
2. **Audit Logging**: All account actions logged in `account_audit_log` table
3. **Email Verification**: Update EmailNotifier with your SMTP credentials
4. **Signature Storage**: Digital signatures stored securely as BLOB data
5. **Access Control**: Only admins can access loan management

## Configuration Required

### Email Setup (Important!)
1. Enable 2-factor authentication on Gmail (if using Gmail)
2. Create an App Password in Google Account settings
3. Update `EmailNotifier.java` with:
   - Your email address
   - Your App Password (not regular password)

### Database Connection
- Ensure MySQL is running on localhost:3306
- Database name: `lawbank`
- Default credentials: root / (no password)

## Testing Checklist

- [ ] Run CreateLawBank.java to update schema
- [ ] Create a test loan with witness info
- [ ] Send reminder email
- [ ] Deactivate account for unpaid loan
- [ ] Verify user cannot login (role is 'suspended')
- [ ] Process payment
- [ ] Verify account reactivation
- [ ] Check email notifications received

## Support & Troubleshooting

**Email not sending:**
- Check SMTP credentials in EmailNotifier
- Verify Gmail App Password is set correctly
- Check firewall/network settings
- Ensure 2FA is enabled on Gmail account

**Account not deactivating:**
- Verify loan ID is correct
- Check database connection
- Review console output for errors

**Missing witness information:**
- When creating loans, ensure witness details are captured
- Update witness info before blocking account

---
**System Version**: 1.0
**Last Updated**: 2026
**Caguioa Bank Admin System**

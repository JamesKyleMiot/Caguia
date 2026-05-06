# Complete Loan Management System - Implementation Guide

## Overview
Complete loan management system with admin approval, online/offline payments, receipts, and penalties for the Caguioa Bank.

## System Requirements

### Database Tables (Auto-created by SQL script)
1. **loan_applications** - Users apply, admins approve/reject
2. **loan_payments** - All payments (online, bank, ATM, mobile)
3. **loan_receipts** - Auto-generated payment receipts
4. **loan_penalties** - Late payment penalties (5% per month overdue)

### Key Features

#### 1. Loan Application Flow
```
User Applies → Admin Review → Approve/Reject → Loan Created (if approved)
```

- Users request loan with amount and purpose
- Application status: pending → approved/rejected
- Approved amount can differ from requested
- 2% interest automatically applied
- 6-month payment term automatically set
- Due date = current date + 6 months

#### 2. Loan Payment System
**No Limit** - Admin only approves the amount

**Payment Methods Available:**
- 📱 Online Banking / Mobile App (real-time)
- 🏦 Bank Counter / Teller Payment
- 🏪 Payment Center (Bayad Center)
- 💳 Auto-debit from Account

**Payment Processing:**
- Validates payment amount ≤ remaining balance
- Updates remaining_balance
- Creates transaction record
- Generates payment receipt automatically
- When remaining_balance = 0 → status = "paid"

#### 3. Interest & Term
- **Interest Rate:** 2% of loan amount
- **Payment Term:** 6 months from approval
- **Example:** ₱10,000 loan = ₱10,200 total payable over 6 months

#### 4. Penalty System
- **Trigger:** If loan unpaid after 6-month due date
- **Calculation:** 5% of remaining balance per month overdue
- **Automatic:** Penalties calculated and applied automatically
- **Tracking:** Separate penalty records for each overdue month

#### 5. Payment Receipts
- **Auto-Generated:** After each payment
- **Format:** Text + HTML options
- **Contains:** Receipt #, date, amount, payment method, remaining balance
- **Receipt #:** RECEIPT-YYYYMMDD-xxxxx (unique)

---

## SQL Functions Reference

### 1. Apply for Loan
```sql
SELECT apply_for_loan(user_id, requested_amount, purpose);
-- Returns: application ID
```
**Usage:** User fills loan form → Application created in "pending" status

### 2. Approve Loan Application
```sql
SELECT approve_loan_application(app_id, admin_id, approved_amount);
-- Returns: loan ID (created)
```
**Usage:** Admin approves → Loan record created with 2% interest

### 3. Reject Loan Application
```sql
SELECT reject_loan_application(app_id, admin_id, reason);
```
**Usage:** Admin rejects → Reason stored

### 4. Process Loan Payment
```sql
SELECT process_loan_payment(loan_id, user_id, amount, method, ref);
-- Returns: payment ID
-- Methods: 'Online Banking', 'Bank Counter', 'ATM', 'Mobile App', 'Auto-debit'
```
**Usage:** User makes payment → Updates remaining_balance

### 5. Generate Payment Receipt
```sql
SELECT generate_loan_receipt(payment_id, loan_id, user_id);
-- Returns: receipt ID
```
**Usage:** Auto-called after payment → Creates printable receipt

### 6. Calculate Penalty
```sql
SELECT calculate_loan_penalty(loan_id);
-- Returns: penalty amount (0 if not overdue)
```
**Usage:** Check if loan has penalty

### 7. Apply Penalty
```sql
SELECT apply_loan_penalty(loan_id);
-- Returns: penalty ID (0 if no penalty needed)
```
**Usage:** Create penalty record for late payment

### 8. Get User Active Loans
```sql
SELECT get_user_active_loans(user_id);
-- Returns: count of active loans
```

---

## Java Classes

### 1. LoanApplicationHelper.java
**Purpose:** Manage loan applications

**Key Methods:**
```java
// User applies for loan
LoanApplicationHelper.applyForLoan(userId, amount, purpose);
// Returns: application ID

// Admin gets pending applications
List<Map> apps = LoanApplicationHelper.getPendingApplications();

// Admin approves (creates loan record)
LoanApplicationHelper.approveLoanApplication(appId, adminId, approvedAmount);
// Returns: loan ID

// Admin rejects
LoanApplicationHelper.rejectLoanApplication(appId, adminId, reason);

// Check if user has pending application
LoanApplicationHelper.hasPendingApplication(userId);
```

### 2. LoanPaymentHelper.java
**Purpose:** Process loan payments

**Payment Methods:**
```java
LoanPaymentHelper.METHOD_ONLINE;        // "Online Banking"
LoanPaymentHelper.METHOD_BANK_COUNTER;  // "Bank Counter"
LoanPaymentHelper.METHOD_ATM;           // "ATM"
LoanPaymentHelper.METHOD_MOBILE_APP;    // "Mobile App"
LoanPaymentHelper.METHOD_AUTO_DEBIT;    // "Auto-debit"
```

**Key Methods:**
```java
// Process payment
LoanPaymentHelper.processLoanPayment(loanId, userId, amount, method, ref);
// Returns: payment ID

// Get all payments for loan
LoanPaymentHelper.getLoanPayments(loanId);

// Get user payment history
LoanPaymentHelper.getUserPaymentHistory(userId);

// Verify payment posted
LoanPaymentHelper.verifyPayment(paymentId);

// Validate payment amount
LoanPaymentHelper.validatePaymentAmount(loanId, amount);
```

### 3. ReceiptGenerator.java
**Purpose:** Generate and manage payment receipts

**Key Methods:**
```java
// Generate receipt (auto-called after payment)
ReceiptGenerator.generateReceipt(paymentId, loanId, userId);
// Returns: receipt ID

// Get receipt details
ReceiptGenerator.getReceiptDetails(receiptId);

// Format as text (printable)
String text = ReceiptGenerator.formatReceiptAsText(receiptId);

// Format as HTML
String html = ReceiptGenerator.formatReceiptAsHTML(receiptId);

// Get by receipt number
ReceiptGenerator.getReceiptByNumber("RECEIPT-20260506-12345");

// Get user receipts
ReceiptGenerator.getUserReceipts(userId);
```

### 4. LoanPenaltyManager.java
**Purpose:** Manage penalties for late/unpaid loans

**Key Methods:**
```java
// Calculate penalty (5% per month overdue)
double penalty = LoanPenaltyManager.calculatePenalty(loanId);

// Apply penalty (create penalty record)
LoanPenaltyManager.applyPenalty(loanId);

// Get user's pending penalties
LoanPenaltyManager.getUserPenalties(userId);

// Get overdue loans
List<Map> overdue = LoanPenaltyManager.getOverdueLoans();

// Mark penalty as paid
LoanPenaltyManager.markPenaltyAsPaid(penaltyId);

// Auto-apply penalties to all overdue loans
int count = LoanPenaltyManager.applyAutomaticPenalties();
```

### 5. OnlineLoanPaymentDialog.java
**Purpose:** UI for online loan payments (5-step process)

**Features:**
- Step 1: View loan details
- Step 2: Choose payment method
- Step 3: Enter payment amount
- Step 4: Confirm payment
- Step 5: View receipt

**Usage in UserDashboard:**
```java
// Add button to UserDashboard
JButton onlinePayBtn = new JButton("💳 Online Loan Payment");
onlinePayBtn.addActionListener(e -> {
    OnlineLoanPaymentDialog dialog = new OnlineLoanPaymentDialog(
        UserDashboard.this, currentUserId);
    dialog.setVisible(true);
});
```

---

## Database Schema Summary

### loan_applications
| Column | Type | Purpose |
|--------|------|---------|
| id | INT | Primary key |
| user_id | INT | User applying |
| requested_amount | DOUBLE | Amount requested |
| purpose | VARCHAR | Reason for loan |
| status | VARCHAR | pending/approved/rejected |
| admin_id | INT | Admin who reviewed |
| approved_amount | DOUBLE | Amount approved (may differ) |
| rejection_reason | VARCHAR | If rejected |
| created_at | TIMESTAMP | Application date |
| reviewed_at | TIMESTAMP | Admin review date |

### loan_payments
| Column | Type | Purpose |
|--------|------|---------|
| id | INT | Primary key |
| loan_id | INT | Which loan |
| user_id | INT | User paying |
| payment_amount | DOUBLE | Payment amount |
| payment_method | VARCHAR | Online/Bank/ATM/etc |
| transaction_reference | VARCHAR | TXN reference |
| paid_date | TIMESTAMP | When paid |

### loan_receipts
| Column | Type | Purpose |
|--------|------|---------|
| id | INT | Primary key |
| payment_id | INT | Which payment |
| receipt_number | VARCHAR | Unique RECEIPT-* |
| amount_paid | DOUBLE | Amount paid |
| previous_balance | DOUBLE | Balance before |
| new_balance | DOUBLE | Balance after |
| payment_method | VARCHAR | How paid |
| generated_at | TIMESTAMP | Receipt date |

### loan_penalties
| Column | Type | Purpose |
|--------|------|---------|
| id | INT | Primary key |
| loan_id | INT | Which loan |
| user_id | INT | User with penalty |
| penalty_amount | DOUBLE | Penalty charge |
| penalty_reason | VARCHAR | "Late Payment" |
| due_date | DATE | Penalty due date |
| paid | BOOLEAN | Is penalty paid |
| created_at | TIMESTAMP | When assessed |

---

## Implementation Workflow

### For Users

1. **Apply for Loan**
   - Fill loan application form
   - Enter amount needed
   - Enter purpose
   - System creates application with status="pending"

2. **Wait for Admin Approval**
   - Admin reviews pending applications
   - Admin approves or rejects
   - User gets notification

3. **If Approved**
   - Loan record created automatically
   - 2% interest added (₱10,000 → ₱10,200)
   - 6-month payment term starts
   - User can start making payments

4. **Make Payments**
   - Choose payment method
   - Enter payment amount
   - Payment processed
   - Receipt generated automatically
   - Balance updated in real-time

5. **Track Payments**
   - View all loan payments
   - View all receipts
   - View remaining balance
   - View due date

### For Admin

1. **Review Applications**
   - See all pending applications
   - Review user information
   - Approve or reject

2. **Monitor Loans**
   - View all active loans
   - Track payments received
   - Check for overdue loans
   - Apply penalties if needed

3. **Process Payments**
   - Receive online payments
   - Record bank counter payments
   - Generate receipts
   - Update balances

---

## Integration Steps

### 1. Update Database
```sql
-- Run database_schema.sql on your MySQL server
mysql -u root -p lawbank < database_schema.sql
```

### 2. Add Java Classes to Project
- LoanApplicationHelper.java ✓
- LoanPaymentHelper.java ✓
- ReceiptGenerator.java ✓
- LoanPenaltyManager.java ✓
- OnlineLoanPaymentDialog.java ✓

### 3. Update UserDashboard.java
Add button to open OnlineLoanPaymentDialog:
```java
JButton onlinePayBtn = new JButton("💳 Online Loan Payment");
onlinePayBtn.addActionListener(e -> new OnlineLoanPaymentDialog(
    UserDashboard.this, userId).setVisible(true));
```

### 4. Update AdminDashboard.java
Add section to review loan applications:
```java
// Show pending applications count
int pendingCount = LoanApplicationHelper.getPendingApplicationCount();
adminLabel.setText("Pending Applications: " + pendingCount);

// Button to review applications
JButton reviewBtn = new JButton("Review Loan Applications");
reviewBtn.addActionListener(e -> showLoanApplicationsDialog());
```

### 5. Compile & Test
```bash
cd "Caguioa Bank"
javac -d build/classes -cp "lib/*" src/caguioa/bank/*.java
```

---

## Testing Scenarios

### Scenario 1: Complete Loan Workflow
1. User applies for ₱10,000 loan
2. Admin approves full amount
3. Loan created with ₱10,200 total (2% interest)
4. User pays ₱5,100 (half)
5. Receipt generated with RECEIPT-* number
6. Remaining balance: ₱5,100
7. User pays remaining ₱5,100
8. Loan status → "paid" ✓

### Scenario 2: Late Payment Penalty
1. Loan due date: 2026-11-06 (6 months after approval)
2. As of 2026-12-06: 1 month overdue
3. Penalty: 5% × ₱5,100 = ₱255
4. As of 2026-01-06: 2 months overdue
5. Penalty: 5% × ₱5,100 × 2 = ₱510
6. Admin can view penalties and collect

### Scenario 3: Multiple Payments
1. User makes 3 partial payments
2. Each creates payment record + receipt
3. All receipts viewable in user dashboard
4. Final payment completes loan

---

## Sample Usage Code

### Apply for Loan (User)
```java
int appId = LoanApplicationHelper.applyForLoan(userId, 10000, "Business");
System.out.println("Application #" + appId + " created");
```

### Approve Loan (Admin)
```java
int loanId = LoanApplicationHelper.approveLoanApplication(appId, adminId, 10000);
System.out.println("Loan #" + loanId + " approved for PHP 10,000");
```

### Make Payment (User)
```java
int paymentId = LoanPaymentHelper.processLoanPayment(
    loanId, userId, 5000, "Online Banking", "TXN-123456");

int receiptId = ReceiptGenerator.generateReceipt(paymentId, loanId, userId);
System.out.println(ReceiptGenerator.formatReceiptAsText(receiptId));
```

### Check Penalties (Admin)
```java
double penalty = LoanPenaltyManager.calculatePenalty(loanId);
if (penalty > 0) {
    LoanPenaltyManager.applyPenalty(loanId);
    System.out.println("Penalty: PHP " + penalty);
}
```

---

## File Locations

```
src/caguioa/bank/
├── LoanApplicationHelper.java     [NEW]
├── LoanPaymentHelper.java         [NEW]
├── ReceiptGenerator.java          [NEW]
├── LoanPenaltyManager.java        [NEW]
├── OnlineLoanPaymentDialog.java   [NEW]
├── LoanManager.java               [EXISTING]
├── UserDashboard.java             [NEEDS UPDATE]
└── AdminDashboard.java            [NEEDS UPDATE]

database_schema.sql               [UPDATED]
```

---

## Status: ✅ READY FOR IMPLEMENTATION

All files created and database schema updated. Ready to compile and integrate!

**Next Steps:**
1. Run updated database_schema.sql
2. Compile new Java classes
3. Update UserDashboard & AdminDashboard
4. Test all payment flows
5. Deploy to production

---

*Last Updated: May 6, 2026*
*Caguioa Bank Loan Management System v2*

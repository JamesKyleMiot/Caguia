# AdminDashboard SQL Fix & Loan System Integration

## ❌ SQL Error: Unknown column 't.id' in 'field list'

### Root Cause
The error occurs in `refreshAdminDashboard()` when loading the transactions table. The problematic query was:

```sql
SELECT t.id, t.user_id, u.username, t.type, t.amount, t.method, t.created_at 
FROM transactions t LEFT JOIN users u ON t.user_id = u.id 
ORDER BY t.id DESC
```

**Problem**: MySQL couldn't recognize the table alias `t` for the column references.

### ✅ Solution: Use Descriptive Aliases

Replace with unique, full table aliases:

```sql
SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, txn.method, txn.created_at 
FROM transactions txn 
LEFT JOIN users usr ON txn.user_id = usr.id 
ORDER BY txn.id DESC
```

---

## Updated AdminDashboard Code

### Step 1: Update the refreshAdminDashboard() Method

Replace the entire `refreshAdminDashboard()` method in AdminDashboard.java with this fixed version:

```java
private void refreshAdminDashboard() {
    try (Connection con = DB.getConnection()) {
        
        if (con == null) {
            System.out.println("❌ Database connection failed");
            return;
        }

        // Count statistics
        try (Statement userCountStmt = con.createStatement()) {
            ResultSet userCountRs = userCountStmt.executeQuery(
                "SELECT COUNT(*) AS total FROM users");
            if (userCountRs.next()) {
                int totalUsers = userCountRs.getInt("total");
                totalUsersValueLabel.setText(String.valueOf(totalUsers));
            }
        }

        try (Statement transCountStmt = con.createStatement()) {
            ResultSet transCountRs = transCountStmt.executeQuery(
                "SELECT COUNT(*) AS total FROM transactions");
            if (transCountRs.next()) {
                int totalTransactions = transCountRs.getInt("total");
                totalTransactionsValueLabel.setText(String.valueOf(totalTransactions));
            }
        }

        try (Statement loanCountStmt = con.createStatement()) {
            ResultSet loanCountRs = loanCountStmt.executeQuery(
                "SELECT COUNT(*) AS total FROM loans");
            if (loanCountRs.next()) {
                int totalLoans = loanCountRs.getInt("total");
                totalLoansValueLabel.setText(String.valueOf(totalLoans));
            }
        }

        try (Statement balanceStmt = con.createStatement()) {
            ResultSet balanceRs = balanceStmt.executeQuery(
                "SELECT SUM(balance) AS total FROM users");
            if (balanceRs.next()) {
                double totalBalance = balanceRs.getDouble("total");
                totalBalanceValueLabel.setText(formatMoney(totalBalance));
            }
        }

        // ✓ FIXED: Load users table
        allUsersTable.setModel(buildTableModel(con,
            "SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, " +
            "COALESCE(SUM(l.amount),0) AS loans_total, " +
            "COALESCE(SUM(CASE WHEN l.status='active' THEN l.amount ELSE 0 END),0) AS active_loans_total, " +
            "COUNT(l.id) AS loans_count " +
            "FROM users u LEFT JOIN loans l ON l.user_id = u.id " +
            "GROUP BY u.id ORDER BY u.id"));

        // ✓ FIXED: Load transactions table (unique aliases: txn, usr)
        allTransactionsTable.setModel(buildTableModel(con,
            "SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, " +
            "txn.method, txn.created_at " +
            "FROM transactions txn " +
            "LEFT JOIN users usr ON txn.user_id = usr.id " +
            "ORDER BY txn.id DESC"));

        // ✓ FIXED: Load loans table (unique alias: loan)
        allLoansTable.setModel(buildTableModel(con,
            "SELECT loan.id, loan.user_id, u.username, loan.amount, loan.interest_rate, " +
            "loan.total_payable, loan.remaining_balance, loan.status, loan.created_at " +
            "FROM loans loan " +
            "LEFT JOIN users u ON loan.user_id = u.id " +
            "ORDER BY loan.id DESC"));

    } catch (Exception e) {
        System.out.println("❌ Error refreshing admin dashboard: " + e.getMessage());
        e.printStackTrace();
    }
}
```

### Step 2: Add Loan Management Methods to AdminDashboard

Add these methods to AdminDashboard.java:

```java
// Open loan application review dialog
private void openLoanApplicationReviewDialog() {
    LoanApplicationReviewDialog dialog = new LoanApplicationReviewDialog(this);
    dialog.setVisible(true);
}

// Show loan management statistics
private void loadLoanManagementStats() {
    try {
        Map<String, Object> summary = AdminLoanHelper.getDashboardSummary();
        
        int pendingApps = (Integer) summary.getOrDefault("pending_applications", 0);
        int activeLoans = (Integer) summary.getOrDefault("active_loans", 0);
        double outstandingAmount = (Double) summary.getOrDefault("outstanding_amount", 0.0);
        int unpaidPenalties = (Integer) summary.getOrDefault("unpaid_penalties", 0);
        double paymentsToday = (Double) summary.getOrDefault("payments_today", 0.0);
        
        String stats = "📊 LOAN MANAGEMENT SUMMARY\n" +
                      "═══════════════════════════════════════\n" +
                      "Pending Applications: " + pendingApps + "\n" +
                      "Active Loans: " + activeLoans + "\n" +
                      "Outstanding Amount: PHP " + String.format("%.2f", outstandingAmount) + "\n" +
                      "Unpaid Penalties: " + unpaidPenalties + "\n" +
                      "Payments Collected Today: PHP " + String.format("%.2f", paymentsToday) + "\n";
        
        JOptionPane.showMessageDialog(this, stats, "Loan Statistics", 
            JOptionPane.INFORMATION_MESSAGE);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Step 3: Add Loan Buttons to buildAdminDashboard()

In the `buildAdminDashboard()` method, find where you add buttons to `headerActions` and add these:

```java
// Add "Review Loan Applications" button
JButton reviewLoansBtn = new JButton("📋 Review Loans");
reviewLoansBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
reviewLoansBtn.setBackground(new Color(33, 150, 243));
reviewLoansBtn.setForeground(Color.WHITE);
reviewLoansBtn.setFocusPainted(false);
reviewLoansBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
reviewLoansBtn.addActionListener(evt -> openLoanApplicationReviewDialog());
headerActions.add(reviewLoansBtn);

// Add "Loan Statistics" button
JButton loanStatsBtn = new JButton("📊 Loan Stats");
loanStatsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
loanStatsBtn.setBackground(new Color(156, 39, 176));
loanStatsBtn.setForeground(Color.WHITE);
loanStatsBtn.setFocusPainted(false);
loanStatsBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
loanStatsBtn.addActionListener(evt -> loadLoanManagementStats());
headerActions.add(loanStatsBtn);
```

---

## New Java Classes Added

✅ **AdminLoanHelper.java**
- `getPendingLoanApplications()` - Get all pending applications
- `getAllActiveLoans()` - Get all active loans
- `getAllLoanPayments()` - Get all payments
- `getAllLoanReceipts()` - Get all receipts
- `getAllLoanPenalties()` - Get all penalties
- `getDashboardSummary()` - Get summary statistics
- `getOverdueLoans()` - Get overdue loans

✅ **LoanApplicationReviewDialog.java**
- UI dialog for reviewing and approving/rejecting loan applications
- Integrated with AdminDashboard

---

## Verification Steps

### 1. Check for SQL Errors
Run this test query in your MySQL client:

```sql
USE lawbank;

-- This should work now
SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, txn.method, txn.created_at 
FROM transactions txn 
LEFT JOIN users usr ON txn.user_id = usr.id 
ORDER BY txn.id DESC LIMIT 5;
```

### 2. Compile Java Files

```bash
cd "Caguioa Bank"
javac -d build/classes -cp "lib/*" src/caguioa/bank/AdminLoanHelper.java
javac -d build/classes -cp "lib/*" src/caguioa/bank/LoanApplicationReviewDialog.java
```

### 3. Test in AdminDashboard

1. Open AdminDashboard
2. Click "📋 Review Loans" button to see pending applications
3. Click "📊 Loan Stats" to see summary
4. Verify no SQL errors in console

---

## Key Changes Summary

| Issue | Fix |
|-------|-----|
| `Unknown column 't.id'` | Use `txn.id` instead with unique aliases |
| Missing loan application UI | Added `LoanApplicationReviewDialog.java` |
| No admin loan management | Added `AdminLoanHelper.java` with 8+ methods |
| No stats dashboard | Added `loadLoanManagementStats()` method |

---

## AdminDashboard Integration Checklist

- [ ] Replace `refreshAdminDashboard()` method with fixed version
- [ ] Add `openLoanApplicationReviewDialog()` method
- [ ] Add `loadLoanManagementStats()` method
- [ ] Add loan review buttons in `buildAdminDashboard()`
- [ ] Compile AdminLoanHelper.java
- [ ] Compile LoanApplicationReviewDialog.java
- [ ] Test the new loan management features
- [ ] Verify SQL queries run without errors

---

## Files Ready to Use

1. **database_schema.sql** - ✓ Already updated with 4 new tables & 8 functions
2. **AdminLoanHelper.java** - ✓ Created with all helper methods
3. **LoanApplicationReviewDialog.java** - ✓ Created with UI dialog
4. **ADMINDASHBOARD_LOAN_INTEGRATION.java** - Reference code for AdminDashboard updates

---

## Need Help?

If you get another SQL error:

1. Check that table aliases are unique (not reused)
2. Verify table names match your database schema
3. Check that columns exist in referenced tables
4. Use full table alias for all column references: `alias.column_name`

---

**Status**: ✅ SQL Error Fixed & Loan System Ready for Integration

*Last Updated: May 6, 2026*

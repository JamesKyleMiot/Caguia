# Quick Patch for AdminDashboard.java

## What to Change

### CHANGE #1: Line ~365-366 (Load Transactions Table)

**OLD CODE:**
```java
// Load all transactions table (exclude details column)
allTransactionsTable.setModel(buildTableModel(con,
    "SELECT t.id, t.user_id, u.username, t.type, t.amount, t.method, t.created_at "
    + "FROM transactions t LEFT JOIN users u ON t.user_id = u.id ORDER BY t.id DESC"));
```

**NEW CODE:**
```java
// Load all transactions table (fixed with unique aliases)
allTransactionsTable.setModel(buildTableModel(con,
    "SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, txn.method, txn.created_at "
    + "FROM transactions txn LEFT JOIN users usr ON txn.user_id = usr.id ORDER BY txn.id DESC"));
```

---

### CHANGE #2: Line ~370-371 (Load Loans Table)

**OLD CODE:**
```java
// Load all loans table (full loan attributes with username)
allLoansTable.setModel(buildTableModel(con,
    "SELECT l.id, l.user_id, u.username, l.amount, l.interest_rate, l.total_payable, l.status, l.created_at "
    + "FROM loans l LEFT JOIN users u ON l.user_id = u.id ORDER BY l.id DESC"));
```

**NEW CODE:**
```java
// Load all loans table (fixed with unique aliases)
allLoansTable.setModel(buildTableModel(con,
    "SELECT loan.id, loan.user_id, u.username, loan.amount, loan.interest_rate, "
    + "loan.total_payable, loan.remaining_balance, loan.status, loan.created_at "
    + "FROM loans loan LEFT JOIN users u ON loan.user_id = u.id ORDER BY loan.id DESC"));
```

---

### CHANGE #3: Add After Header Setup (~Line 100)

Add these imports at the top of AdminDashboard.java:
```java
import java.util.Map;
```

---

### CHANGE #4: Add These New Methods to AdminDashboard Class

Add at the end of the class before the closing brace:

```java
    /**
     * Open loan application review dialog
     */
    private void openLoanApplicationReviewDialog() {
        LoanApplicationReviewDialog dialog = new LoanApplicationReviewDialog(this);
        dialog.setVisible(true);
    }

    /**
     * Display loan management statistics
     */
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
            
            JOptionPane.showMessageDialog(this, stats, "📊 Loan Statistics", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

---

### CHANGE #5: Add Buttons to buildAdminDashboard() Method

Find this section in `buildAdminDashboard()`:
```java
headerActions.add(loanMgmtBtn);
```

Add after it:
```java
        // Loan applications button
        JButton reviewLoansBtn = new JButton("📋 Review Loans");
        reviewLoansBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        reviewLoansBtn.setBackground(new Color(33, 150, 243));
        reviewLoansBtn.setForeground(Color.WHITE);
        reviewLoansBtn.setFocusPainted(false);
        reviewLoansBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        reviewLoansBtn.addActionListener(evt -> openLoanApplicationReviewDialog());
        headerActions.add(reviewLoansBtn);

        // Loan statistics button
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

## Summary of Changes

| Item | Change Type | Location |
|------|-------------|----------|
| Transaction query | REPLACE | Line ~365 |
| Loan query | REPLACE | Line ~370 |
| Import statement | ADD | Top of file |
| Two new methods | ADD | End of class |
| Two new buttons | ADD | buildAdminDashboard() method |

---

## After Making Changes

1. **Compile**:
```bash
javac -d build/classes -cp "lib/*" src/caguioa/bank/AdminDashboard.java
javac -d build/classes -cp "lib/*" src/caguioa/bank/AdminLoanHelper.java
javac -d build/classes -cp "lib/*" src/caguioa/bank/LoanApplicationReviewDialog.java
```

2. **Test**: Run AdminDashboard and check that:
   - No SQL errors appear
   - "📋 Review Loans" button works
   - "📊 Loan Stats" button works
   - Tables load without errors

3. **Deploy**: Use the updated files in your production build

---

**Total Changes: 5 simple replacements/additions**  
**Files Affected: AdminDashboard.java (only)**  
**Time to Update: ~5 minutes**

✅ Ready to fix!

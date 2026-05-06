/**
 * INTEGRATION CODE FOR AdminDashboard.java
 * Add this method to your AdminDashboard class to enable loan management
 */

// Add this method to AdminDashboard.java to open the loan application review dialog
private void openLoanApplicationReviewDialog() {
    LoanApplicationReviewDialog dialog = new LoanApplicationReviewDialog(this);
    dialog.setVisible(true);
}

// Add this method to AdminDashboard.java to show dashboard summary with loan stats
private void loadLoanManagementStats() {
    try {
        Map<String, Object> summary = AdminLoanHelper.getDashboardSummary();
        
        // Get values from summary
        int pendingApps = (Integer) summary.getOrDefault("pending_applications", 0);
        int activeLoans = (Integer) summary.getOrDefault("active_loans", 0);
        double outstandingAmount = (Double) summary.getOrDefault("outstanding_amount", 0.0);
        int unpaidPenalties = (Integer) summary.getOrDefault("unpaid_penalties", 0);
        double paymentsToday = (Double) summary.getOrDefault("payments_today", 0.0);
        
        // Display in labels or dialog
        String stats = "📊 LOAN MANAGEMENT SUMMARY\n" +
                      "═══════════════════════════════════════\n" +
                      "Pending Applications: " + pendingApps + "\n" +
                      "Active Loans: " + activeLoans + "\n" +
                      "Outstanding Amount: PHP " + String.format("%.2f", outstandingAmount) + "\n" +
                      "Unpaid Penalties: " + unpaidPenalties + "\n" +
                      "Payments Collected Today: PHP " + String.format("%.2f", paymentsToday) + "\n";
        
        System.out.println(stats);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Replace the broken transactions query with this fixed version:
// Instead of:
//   "SELECT t.id, t.user_id, u.username, t.type, t.amount, t.method, t.created_at "+ 
//   "FROM transactions t LEFT JOIN users u ON t.user_id = u.id ORDER BY t.id DESC"
//
// Use this fixed version:
private void loadTransactionsTable(Connection conn) throws SQLException {
    String query = "SELECT trans.id, trans.user_id, usr.username, trans.type, " +
                   "trans.amount, trans.method, trans.created_at " +
                   "FROM transactions trans " +
                   "LEFT JOIN users usr ON trans.user_id = usr.id " +
                   "ORDER BY trans.id DESC";
    
    allTransactionsTable.setModel(buildTableModel(conn, query));
}

// Add button to AdminDashboard header to review loan applications
// In buildAdminDashboard() method, add this after the existing buttons:
JButton reviewLoansBtn = new JButton("📋 Review Loan Applications");
reviewLoansBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
reviewLoansBtn.setBackground(new Color(33, 150, 243));
reviewLoansBtn.setForeground(Color.WHITE);
reviewLoansBtn.setFocusPainted(false);
reviewLoansBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
reviewLoansBtn.addActionListener(evt -> openLoanApplicationReviewDialog());
headerActions.add(reviewLoansBtn);

// Add this to show loan summary stats
JButton loanStatsBtn = new JButton("📊 Loan Stats");
loanStatsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
loanStatsBtn.setBackground(new Color(156, 39, 176));
loanStatsBtn.setForeground(Color.WHITE);
loanStatsBtn.setFocusPainted(false);
loanStatsBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
loanStatsBtn.addActionListener(evt -> loadLoanManagementStats());
headerActions.add(loanStatsBtn);


/**
 * COMPLETE REPLACEMENT METHOD FOR refreshAdminDashboard()
 * Use this to replace the existing method in AdminDashboard
 */
private void refreshAdminDashboard() {
    try (Connection con = DB.getConnection()) {
        
        if (con == null) {
            System.out.println("❌ Database connection failed");
            return;
        }

        // Count statistics
        try (Statement userCountStmt = con.createStatement()) {
            ResultSet userCountRs = userCountStmt.executeQuery("SELECT COUNT(*) AS total FROM users");
            if (userCountRs.next()) {
                int totalUsers = userCountRs.getInt("total");
                totalUsersValueLabel.setText(String.valueOf(totalUsers));
            }
        }

        try (Statement transCountStmt = con.createStatement()) {
            ResultSet transCountRs = transCountStmt.executeQuery("SELECT COUNT(*) AS total FROM transactions");
            if (transCountRs.next()) {
                int totalTransactions = transCountRs.getInt("total");
                totalTransactionsValueLabel.setText(String.valueOf(totalTransactions));
            }
        }

        try (Statement loanCountStmt = con.createStatement()) {
            ResultSet loanCountRs = loanCountStmt.executeQuery("SELECT COUNT(*) AS total FROM loans");
            if (loanCountRs.next()) {
                int totalLoans = loanCountRs.getInt("total");
                totalLoansValueLabel.setText(String.valueOf(totalLoans));
            }
        }

        try (Statement balanceStmt = con.createStatement()) {
            ResultSet balanceRs = balanceStmt.executeQuery("SELECT SUM(balance) AS total FROM users");
            if (balanceRs.next()) {
                double totalBalance = balanceRs.getDouble("total");
                totalBalanceValueLabel.setText(formatMoney(totalBalance));
            }
        }

        // ✓ FIXED: Load users table with proper alias
        allUsersTable.setModel(buildTableModel(con,
            "SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, " +
            "COALESCE(SUM(l.amount),0) AS loans_total, " +
            "COALESCE(SUM(CASE WHEN l.status='active' THEN l.amount ELSE 0 END),0) AS active_loans_total, " +
            "COUNT(l.id) AS loans_count " +
            "FROM users u LEFT JOIN loans l ON l.user_id = u.id " +
            "GROUP BY u.id ORDER BY u.id"));

        // ✓ FIXED: Load transactions table with unique table aliases
        allTransactionsTable.setModel(buildTableModel(con,
            "SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, txn.method, txn.created_at " +
            "FROM transactions txn " +
            "LEFT JOIN users usr ON txn.user_id = usr.id " +
            "ORDER BY txn.id DESC"));

        // ✓ FIXED: Load loans table with proper alias
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

package caguioa.bank;

import java.util.*;

/**
 * Utility to view all transactions for a user and verify they are recorded
 */
public class ViewUserTransactions {

    public static void main(String[] args) {
        // Show transactions for user 11 (miot@gmail.com)
        int userId = 11;
        viewAllTransactions(userId);
    }

    public static void viewAllTransactions(int userId) {
        System.out.println("\n========== USER TRANSACTION HISTORY ==========");
        System.out.println("User ID: " + userId);
        System.out.println("==============================================\n");

        // Get user info
        Map<String, Object> account = UserBankAccountManager.getUserBankAccount(userId);
        if (account != null) {
            System.out.println("💳 Bank Account Information:");
            System.out.println("   Account Number: " + account.get("account_number"));
            System.out.println("   Account Holder: " + account.get("account_holder_name"));
            System.out.println("   Bank: " + account.get("bank_name"));
            System.out.println("   Type: " + account.get("account_type"));
            System.out.println();
        }

        // Get all transactions
        List<Map<String, Object>> transactions = TransactionManager.getUserTransactions(userId);
        if (transactions.isEmpty()) {
            System.out.println("ℹ No transactions found for this user");
        } else {
            System.out.println("📊 All Transactions (" + transactions.size() + " total):\n");
            System.out.println("┌─────┬──────────────────────────┬─────────────┬───────────────────────────────┐");
            System.out.println("│ ID  │ Date/Time                │ Type        │ Amount (₱)  | Method          │");
            System.out.println("├─────┼──────────────────────────┼─────────────┼─────────────┼─────────────────┤");

            int count = 0;
            double totalAmount = 0;
            for (Map<String, Object> txn : transactions) {
                count++;
                java.sql.Timestamp ts = (java.sql.Timestamp) txn.get("created_at");
                String type = (String) txn.get("type");
                double amount = (Double) txn.get("amount");
                String method = (String) txn.get("method");

                totalAmount += amount;

                System.out.printf("│ %3d │ %24s │ %-11s │ %11.2f │ %-15s │%n",
                    (Integer) txn.get("id"),
                    ts != null ? ts.toString() : "N/A",
                    type != null ? type : "N/A",
                    amount,
                    method != null ? method : "N/A");
            }
            System.out.println("└─────┴──────────────────────────┴─────────────┴─────────────┴─────────────────┘");
            System.out.println();
            System.out.printf("Total Amount Transacted: ₱%.2f\n", totalAmount);
        }

        // Get transaction summary
        System.out.println("\n📈 Transaction Summary:");
        Map<String, Object> summary = TransactionManager.getUserTransactionSummary(userId);
        if (summary != null) {
            System.out.println("   Total Transactions: " + summary.get("total_transactions"));
            System.out.printf("   Total Amount: ₱%.2f\n", (Double) summary.get("total_amount"));
            System.out.printf("   Total Deposits: ₱%.2f\n", (Double) summary.get("total_deposits"));
            System.out.printf("   Total Loan Payments: ₱%.2f\n", (Double) summary.get("total_loan_payments"));
            if (summary.get("last_transaction_date") != null) {
                System.out.println("   Last Transaction: " + summary.get("last_transaction_date"));
            }
        }

        // Get transaction stats by type
        System.out.println("\n📋 Transaction Statistics by Type:");
        Map<String, Object> stats = TransactionManager.getTransactionStatsByType(userId);
        if (stats.isEmpty()) {
            System.out.println("   No transactions found");
        } else {
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) entry.getValue();
                System.out.printf("   %-25s: %3d transactions, Total: ₱%.2f\n",
                    entry.getKey(),
                    (Integer) data.get("count"),
                    (Double) data.get("total"));
            }
        }

        // Get transactions by type breakdown
        System.out.println("\n🔍 Detailed Transaction Breakdown:\n");

        String[] types = {
            TransactionManager.TYPE_DEPOSIT,
            TransactionManager.TYPE_WITHDRAWAL,
            TransactionManager.TYPE_LOAN_DISBURSEMENT,
            TransactionManager.TYPE_LOAN_PAYMENT,
            TransactionManager.TYPE_LOAN_APPLICATION,
            TransactionManager.TYPE_TRANSFER,
            TransactionManager.TYPE_SAVINGS_TRANSFER,
            TransactionManager.TYPE_BILL_PAYMENT
        };

        for (String type : types) {
            List<Map<String, Object>> typeTransactions = TransactionManager.getUserTransactionsByType(userId, type);
            if (!typeTransactions.isEmpty()) {
                System.out.println("   " + type + " (" + typeTransactions.size() + "):");
                double typeTotal = 0;
                for (Map<String, Object> txn : typeTransactions) {
                    double amount = (Double) txn.get("amount");
                    typeTotal += amount;
                }
                System.out.printf("      Total: ₱%.2f\n", typeTotal);
            }
        }

        System.out.println("\n==============================================");
        System.out.println("Transaction view complete");
        System.out.println("==============================================\n");
    }
}

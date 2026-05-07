package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Quick database debugging utility to inspect loan data for a specific user
 */
public class DatabaseDebugger {

    public static void main(String[] args) {
        String email = "miot@gmail.com";
        debugUserLoans(email);
    }

    public static void debugUserLoans(String email) {
        System.out.println("\n========== DATABASE DEBUG ==========");
        System.out.println("Looking for user with email: " + email);
        System.out.println("====================================\n");

        try (Connection conn = DB.connect()) {
            if (conn == null) {
                System.out.println("❌ Database connection failed!");
                return;
            }

            // Step 1: Find user by email
            System.out.println("STEP 1: Finding user...");
            int userId = -1;
            String userQuery = "SELECT id, username, fullname, email FROM users WHERE email = ? OR username = ?";
            try (PreparedStatement pst = conn.prepareStatement(userQuery)) {
                pst.setString(1, email);
                pst.setString(2, email);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                        System.out.println("✓ Found user:");
                        System.out.println("  - ID: " + userId);
                        System.out.println("  - Username: " + rs.getString("username"));
                        System.out.println("  - Full Name: " + rs.getString("fullname"));
                        System.out.println("  - Email: " + rs.getString("email"));
                    } else {
                        System.out.println("❌ No user found with email: " + email);
                        return;
                    }
                }
            }

            // Step 2: Check all loans for this user
            System.out.println("\nSTEP 2: Checking loans for user ID " + userId + "...");
            String loansQuery = "SELECT id, amount, total_payable, remaining_balance, status, created_at FROM loans WHERE user_id = ? ORDER BY id DESC";
            try (PreparedStatement pst = conn.prepareStatement(loansQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ No loans found for this user!");
                    } else {
                        rs.beforeFirst();
                        int loanCount = 0;
                        while (rs.next()) {
                            loanCount++;
                            System.out.println("\nLoan #" + loanCount + ":");
                            System.out.println("  - ID: " + rs.getInt("id"));
                            System.out.println("  - Amount: ₱" + rs.getDouble("amount"));
                            System.out.println("  - Total Payable: ₱" + rs.getDouble("total_payable"));
                            System.out.println("  - Remaining Balance: ₱" + rs.getDouble("remaining_balance"));
                            System.out.println("  - Status: " + rs.getString("status"));
                            System.out.println("  - Created: " + rs.getTimestamp("created_at"));
                        }
                        System.out.println("\n✓ Total loans: " + loanCount);
                    }
                }
            }

            // Step 3: Check loan payments
            System.out.println("\nSTEP 3: Checking loan payments...");
            String paymentsQuery = "SELECT id, loan_id, payment_amount, payment_method, payment_status, paid_date FROM loan_payments WHERE user_id = ? ORDER BY paid_date DESC LIMIT 10";
            try (PreparedStatement pst = conn.prepareStatement(paymentsQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("ℹ No payment records found");
                    } else {
                        rs.beforeFirst();
                        int paymentCount = 0;
                        while (rs.next()) {
                            paymentCount++;
                            System.out.println("\nPayment #" + paymentCount + ":");
                            System.out.println("  - Payment ID: " + rs.getInt("id"));
                            System.out.println("  - Loan ID: " + rs.getInt("loan_id"));
                            System.out.println("  - Amount: ₱" + rs.getDouble("payment_amount"));
                            System.out.println("  - Method: " + rs.getString("payment_method"));
                            System.out.println("  - Status: " + rs.getString("payment_status"));
                            System.out.println("  - Date: " + rs.getTimestamp("paid_date"));
                        }
                        System.out.println("\n✓ Total payments: " + paymentCount);
                    }
                }
            }

            // Step 4: Check transactions
            System.out.println("\nSTEP 4: Checking transactions...");
            String txnQuery = "SELECT id, type, amount, method, created_at FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";
            try (PreparedStatement pst = conn.prepareStatement(txnQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("ℹ No transaction records found");
                    } else {
                        rs.beforeFirst();
                        int txnCount = 0;
                        while (rs.next()) {
                            txnCount++;
                            System.out.println("\nTransaction #" + txnCount + ":");
                            System.out.println("  - ID: " + rs.getInt("id"));
                            System.out.println("  - Type: " + rs.getString("type"));
                            System.out.println("  - Amount: ₱" + rs.getDouble("amount"));
                            System.out.println("  - Method: " + rs.getString("method"));
                            System.out.println("  - Date: " + rs.getTimestamp("created_at"));
                        }
                        System.out.println("\n✓ Total transactions: " + txnCount);
                    }
                }
            }

            System.out.println("\n====================================");
            System.out.println("Debug complete!");
            System.out.println("====================================\n");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

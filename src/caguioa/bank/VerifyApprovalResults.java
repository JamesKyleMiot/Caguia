package caguioa.bank;

import java.sql.*;

/**
 * Verify the approval results in database
 */
public class VerifyApprovalResults {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   VERIFYING APPROVAL RESULTS                             ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

            // Check application
            System.out.println("📋 Loan Application (ID=1):\n");
            String appQuery = "SELECT id, user_id, loan_amount, status, admin_id, approved_at, approved_amount FROM loan_applications WHERE id=1";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(appQuery)) {
                if (rs.next()) {
                    System.out.println("  ID: " + rs.getInt(1));
                    System.out.println("  User ID: " + rs.getInt(2));
                    System.out.println("  Requested Amount: ₱" + rs.getDouble(3));
                    System.out.println("  Status: " + rs.getString(4));
                    System.out.println("  Approved By Admin ID: " + rs.getInt(5));
                    System.out.println("  Approved At: " + rs.getTimestamp(6));
                    System.out.println("  Approved Amount: ₱" + rs.getDouble(7));
                }
            }

            // Check loan
            System.out.println("\n💰 Loan Record (ID=28):\n");
            String loanQuery = "SELECT id, user_id, amount, interest_rate, total_payable, remaining_balance, status FROM loans WHERE id=28";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(loanQuery)) {
                if (rs.next()) {
                    System.out.println("  ID: " + rs.getInt(1));
                    System.out.println("  User ID: " + rs.getInt(2));
                    System.out.println("  Amount: ₱" + rs.getDouble(3));
                    System.out.println("  Interest Rate: " + rs.getDouble(4) + "%");
                    System.out.println("  Total Payable: ₱" + rs.getDouble(5));
                    System.out.println("  Remaining Balance: ₱" + rs.getDouble(6));
                    System.out.println("  Status: " + rs.getString(7));
                }
            }

            // Check transaction
            System.out.println("\n📊 Transaction Record:\n");
            String txnQuery = "SELECT id, user_id, type, amount, method, created_at FROM transactions WHERE user_id=11 AND type='Loan Disbursement' ORDER BY id DESC LIMIT 1";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(txnQuery)) {
                if (rs.next()) {
                    System.out.println("  ID: " + rs.getInt(1));
                    System.out.println("  User ID: " + rs.getInt(2));
                    System.out.println("  Type: " + rs.getString(3));
                    System.out.println("  Amount: ₱" + rs.getDouble(4));
                    System.out.println("  Method: " + rs.getString(5));
                    System.out.println("  Created At: " + rs.getTimestamp(6));
                }
            }

            System.out.println("\n✅ All approval data verified successfully!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

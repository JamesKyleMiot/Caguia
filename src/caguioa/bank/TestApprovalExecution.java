package caguioa.bank;

import java.sql.*;

/**
 * Test utility to actually execute approve/reject operations
 */
public class TestApprovalExecution {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   EXECUTING ACTUAL LOAN APPROVAL TEST                   ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        try {
            Connection con = DB.connect();
            if (con == null) {
                System.out.println("❌ Failed to connect to database");
                return;
            }

            // Find a pending application to test
            System.out.println("📋 Finding pending application...\n");
            String getAppQuery = "SELECT id, user_id, full_name, requested_amount FROM loan_applications WHERE status='pending' LIMIT 1";
            int appId = 0;
            int userId = 0;
            String fullName = "";
            double amount = 0;
            
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(getAppQuery)) {
                if (rs.next()) {
                    appId = rs.getInt("id");
                    userId = rs.getInt("user_id");
                    fullName = rs.getString("full_name");
                    amount = rs.getDouble("requested_amount");
                    
                    System.out.println("✓ Found pending application:");
                    System.out.println("  ID: " + appId);
                    System.out.println("  User: " + fullName + " (ID: " + userId + ")");
                    System.out.println("  Amount: ₱" + String.format("%.2f", amount));
                    System.out.println();
                }
            }

            if (appId == 0) {
                System.out.println("⚠️  No pending applications found");
                return;
            }

            // Test approve
            System.out.println("🔍 Testing APPROVE operation...\n");
            
            try {
                // Update application
                String updateQuery = "UPDATE loan_applications SET status='approved', admin_id=?, approved_at=NOW(), approved_amount=? WHERE id=?";
                try (PreparedStatement stmt = con.prepareStatement(updateQuery)) {
                    stmt.setInt(1, 1); // Admin ID = 1
                    stmt.setDouble(2, amount);
                    stmt.setInt(3, appId);
                    int updated = stmt.executeUpdate();
                    System.out.println("✓ Application updated: " + updated + " row(s)");
                }

                // Create loan with required fields
                double interestRate = 5.0; // Default 5% interest
                double totalPayable = amount + (amount * interestRate / 100);
                
                String loanQuery = "INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, status, created_at) VALUES (?, ?, ?, ?, ?, 'active', NOW())";
                int loanId = 0;
                try (PreparedStatement stmt = con.prepareStatement(loanQuery, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, amount);
                    stmt.setDouble(3, interestRate);
                    stmt.setDouble(4, totalPayable);
                    stmt.setDouble(5, totalPayable);
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            loanId = rs.getInt(1);
                            System.out.println("✓ Loan created: ID=" + loanId);
                        }
                    }
                }

                // Record transaction
                String txnQuery = "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, 'Loan Disbursement', ?, 'Loan Approval')";
                try (PreparedStatement stmt = con.prepareStatement(txnQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, amount);
                    stmt.executeUpdate();
                    System.out.println("✓ Transaction recorded");
                }

                System.out.println("\n✅ APPROVAL SUCCESSFUL");
                System.out.println("   Loan ID: " + loanId);
                System.out.println("   Amount: ₱" + String.format("%.2f", amount));
                System.out.println("   User: " + fullName + "\n");

            } catch (SQLException e) {
                System.out.println("❌ APPROVAL FAILED: " + e.getMessage());
                System.out.println("Error Code: " + e.getErrorCode());
                System.out.println("SQL State: " + e.getSQLState());
                e.printStackTrace();
            }

            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║   ✅ TEST COMPLETE                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

            con.close();

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

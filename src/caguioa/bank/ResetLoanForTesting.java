package caguioa.bank;

import java.sql.*;

/**
 * Utility to reset a loan to active status for testing payment flow
 */
public class ResetLoanForTesting {

    public static void main(String[] args) {
        resetLoanToActive(27, 50000); // Reset Loan 27 to active with ₱50,000 balance
    }

    public static void resetLoanToActive(int loanId, double remainingBalance) {
        System.out.println("\n========== RESET LOAN FOR TESTING ==========");
        System.out.println("Resetting Loan ID " + loanId + " to active status with ₱" + remainingBalance + " balance");
        System.out.println("===========================================\n");

        try (Connection conn = DB.connect()) {
            if (conn == null) {
                System.out.println("❌ Database connection failed!");
                return;
            }

            // Update loan status and balance
            String updateQuery = "UPDATE loans SET status = 'active', remaining_balance = ?, is_account_blocked = FALSE WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(updateQuery)) {
                pst.setDouble(1, remainingBalance);
                pst.setInt(2, loanId);
                int result = pst.executeUpdate();
                
                if (result > 0) {
                    System.out.println("✓ Loan " + loanId + " successfully reset to active");
                    System.out.println("  - Status: active");
                    System.out.println("  - Remaining Balance: ₱" + remainingBalance);
                } else {
                    System.out.println("❌ Loan " + loanId + " not found or not updated");
                }
            }

            System.out.println("\n===========================================");
            System.out.println("You can now test the payment flow!");
            System.out.println("===========================================\n");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

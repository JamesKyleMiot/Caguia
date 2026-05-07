package caguioa.bank;

import java.sql.*;

/**
 * Reset loan application to pending status for testing
 */
public class ResetApplicationToPending {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            // Reset application ID 1 back to pending
            String query = "UPDATE loan_applications SET status='pending', admin_id=NULL, admin_comments=NULL, approved_at=NULL, approved_amount=0 WHERE id=1";
            try (Statement stmt = con.createStatement()) {
                int rows = stmt.executeUpdate(query);
                System.out.println("✓ Reset " + rows + " application(s) back to pending");
            }
            
            // Delete any loans created from this application
            String deleteLoanQuery = "DELETE FROM loans WHERE user_id=11";
            try (Statement stmt = con.createStatement()) {
                int rows = stmt.executeUpdate(deleteLoanQuery);
                System.out.println("✓ Deleted " + rows + " loan record(s)");
            }
            
            // Delete any transactions created
            String deleteTxnQuery = "DELETE FROM transactions WHERE user_id=11 AND type='Loan Disbursement' AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
            try (Statement stmt = con.createStatement()) {
                int rows = stmt.executeUpdate(deleteTxnQuery);
                System.out.println("✓ Deleted " + rows + " transaction record(s)");
            }
            
            System.out.println("\n✅ Application ready for testing");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

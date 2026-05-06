package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Helper class for loan applications (user applies → admin approves/rejects)
 */
public class LoanApplicationHelper {

    /**
     * User applies for a loan
     * @param userId User ID
     * @param requestedAmount Loan amount requested
     * @param purpose Purpose of loan (optional)
     * @return Application ID, or -1 if failed
     */
    public static int applyForLoan(int userId, double requestedAmount, String purpose) {
        String query = "SELECT apply_for_loan(?, ?, ?)";
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call apply_for_loan(?, ?, ?)}")) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, userId);
            stmt.setDouble(3, requestedAmount);
            stmt.setString(4, purpose);
            stmt.execute();
            
            int appId = stmt.getInt(1);
            System.out.println("✓ Loan application created: ID=" + appId);
            return appId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get all pending loan applications for admin
     * @return List of pending applications
     */
    public static List<Map<String, Object>> getPendingApplications() {
        List<Map<String, Object>> applications = new ArrayList<>();
        String query = "SELECT la.id, la.user_id, u.username, u.fullname, la.requested_amount, " +
                       "la.purpose, la.created_at FROM loan_applications la " +
                       "JOIN users u ON la.user_id = u.id WHERE la.status = 'pending'";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> app = new HashMap<>();
                app.put("id", rs.getInt("id"));
                app.put("user_id", rs.getInt("user_id"));
                app.put("username", rs.getString("username"));
                app.put("fullname", rs.getString("fullname"));
                app.put("requested_amount", rs.getDouble("requested_amount"));
                app.put("purpose", rs.getString("purpose"));
                app.put("created_at", rs.getTimestamp("created_at"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    /**
     * Admin approves a loan application
     * @param appId Application ID
     * @param adminId Admin ID
     * @param approvedAmount Approved amount (can be different from requested)
     * @return Loan ID, or -1 if failed
     */
    public static int approveLoanApplication(int appId, int adminId, double approvedAmount) {
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call approve_loan_application(?, ?, ?)}")) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, appId);
            stmt.setInt(3, adminId);
            stmt.setDouble(4, approvedAmount);
            stmt.execute();
            
            int loanId = stmt.getInt(1);
            System.out.println("✓ Loan approved: ID=" + loanId + ", Amount=" + approvedAmount);
            return loanId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Admin rejects a loan application
     * @param appId Application ID
     * @param adminId Admin ID
     * @param reason Rejection reason
     * @return true if successful
     */
    public static boolean rejectLoanApplication(int appId, int adminId, String reason) {
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call reject_loan_application(?, ?, ?)}")) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, appId);
            stmt.setInt(3, adminId);
            stmt.setString(4, reason);
            stmt.execute();
            
            System.out.println("✓ Loan application rejected: " + reason);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user's loan application history
     * @param userId User ID
     * @return List of applications
     */
    public static List<Map<String, Object>> getUserApplications(int userId) {
        List<Map<String, Object>> applications = new ArrayList<>();
        String query = "SELECT id, requested_amount, purpose, status, approved_amount, " +
                       "rejection_reason, created_at, reviewed_at FROM loan_applications " +
                       "WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> app = new HashMap<>();
                    app.put("id", rs.getInt("id"));
                    app.put("requested_amount", rs.getDouble("requested_amount"));
                    app.put("purpose", rs.getString("purpose"));
                    app.put("status", rs.getString("status"));
                    app.put("approved_amount", rs.getDouble("approved_amount"));
                    app.put("rejection_reason", rs.getString("rejection_reason"));
                    app.put("created_at", rs.getTimestamp("created_at"));
                    app.put("reviewed_at", rs.getTimestamp("reviewed_at"));
                    applications.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    /**
     * Check if user has pending loan application
     * @param userId User ID
     * @return true if pending application exists
     */
    public static boolean hasPendingApplication(int userId) {
        String query = "SELECT COUNT(*) FROM loan_applications WHERE user_id = ? AND status = 'pending'";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get count of pending applications (for admin)
     * @return Count of pending applications
     */
    public static int getPendingApplicationCount() {
        String query = "SELECT COUNT(*) FROM loan_applications WHERE status = 'pending'";
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

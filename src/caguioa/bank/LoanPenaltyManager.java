package caguioa.bank;

import java.sql.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 * Manages loan penalties for late/unpaid loans
 */
public class LoanPenaltyManager {

    /**
     * Calculate penalty for a loan (5% per month of remaining balance after due date)
     * @param loanId Loan ID
     * @return Penalty amount, 0 if no penalty
     */
    public static double calculatePenalty(int loanId) {
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call calculate_loan_penalty(?)}")) {
            
            stmt.registerOutParameter(1, Types.DOUBLE);
            stmt.setInt(2, loanId);
            stmt.execute();
            
            return stmt.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Apply penalty to a loan (creates penalty record)
     * @param loanId Loan ID
     * @return Penalty ID, or 0 if no penalty needed
     */
    public static int applyPenalty(int loanId) {
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call apply_loan_penalty(?)}")) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, loanId);
            stmt.execute();
            
            int penaltyId = stmt.getInt(1);
            if (penaltyId > 0) {
                System.out.println("✓ Penalty applied to loan " + loanId);
            }
            return penaltyId;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get all active penalties for a user
     * @param userId User ID
     * @return List of penalties
     */
    public static List<Map<String, Object>> getUserPenalties(int userId) {
        List<Map<String, Object>> penalties = new ArrayList<>();
        String query = "SELECT id, loan_id, penalty_amount, penalty_reason, due_date, paid, " +
                       "created_at FROM loan_penalties WHERE user_id = ? AND paid = FALSE " +
                       "ORDER BY due_date ASC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> penalty = new HashMap<>();
                    penalty.put("id", rs.getInt("id"));
                    penalty.put("loan_id", rs.getInt("loan_id"));
                    penalty.put("penalty_amount", rs.getDouble("penalty_amount"));
                    penalty.put("penalty_reason", rs.getString("penalty_reason"));
                    penalty.put("due_date", rs.getDate("due_date"));
                    penalty.put("paid", rs.getBoolean("paid"));
                    penalty.put("created_at", rs.getTimestamp("created_at"));
                    penalties.add(penalty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return penalties;
    }

    /**
     * Get all pending penalties for a loan
     * @param loanId Loan ID
     * @return List of penalties
     */
    public static List<Map<String, Object>> getLoanPenalties(int loanId) {
        List<Map<String, Object>> penalties = new ArrayList<>();
        String query = "SELECT id, penalty_amount, penalty_reason, due_date, paid, created_at " +
                       "FROM loan_penalties WHERE loan_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> penalty = new HashMap<>();
                    penalty.put("id", rs.getInt("id"));
                    penalty.put("penalty_amount", rs.getDouble("penalty_amount"));
                    penalty.put("penalty_reason", rs.getString("penalty_reason"));
                    penalty.put("due_date", rs.getDate("due_date"));
                    penalty.put("paid", rs.getBoolean("paid"));
                    penalty.put("created_at", rs.getTimestamp("created_at"));
                    penalties.add(penalty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return penalties;
    }

    /**
     * Get total pending penalties for a user
     * @param userId User ID
     * @return Total penalty amount
     */
    public static double getTotalPendingPenalties(int userId) {
        String query = "SELECT SUM(penalty_amount) FROM loan_penalties " +
                       "WHERE user_id = ? AND paid = FALSE";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mark penalty as paid
     * @param penaltyId Penalty ID
     * @return true if successful
     */
    public static boolean markPenaltyAsPaid(int penaltyId) {
        String query = "UPDATE loan_penalties SET paid = TRUE WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, penaltyId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✓ Penalty marked as paid: ID=" + penaltyId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get penalty details
     * @param penaltyId Penalty ID
     * @return Penalty information
     */
    public static Map<String, Object> getPenaltyDetails(int penaltyId) {
        String query = "SELECT id, loan_id, user_id, penalty_amount, penalty_reason, " +
                       "due_date, paid, created_at FROM loan_penalties WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, penaltyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> penalty = new HashMap<>();
                    penalty.put("id", rs.getInt("id"));
                    penalty.put("loan_id", rs.getInt("loan_id"));
                    penalty.put("user_id", rs.getInt("user_id"));
                    penalty.put("penalty_amount", rs.getDouble("penalty_amount"));
                    penalty.put("penalty_reason", rs.getString("penalty_reason"));
                    penalty.put("due_date", rs.getDate("due_date"));
                    penalty.put("paid", rs.getBoolean("paid"));
                    penalty.put("created_at", rs.getTimestamp("created_at"));
                    return penalty;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check for overdue loans and apply penalties automatically
     * @return Number of penalties applied
     */
    public static int applyAutomaticPenalties() {
        int count = 0;
        String query = "SELECT DISTINCT l.id FROM loans l " +
                       "WHERE l.status = 'active' AND l.due_date < CURDATE() AND " +
                       "l.remaining_balance > 0 AND NOT EXISTS " +
                       "(SELECT 1 FROM loan_penalties WHERE loan_id = l.id AND paid = FALSE)";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int loanId = rs.getInt("id");
                int penaltyId = applyPenalty(loanId);
                if (penaltyId > 0) count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Get overdue loans needing penalty assessment
     * @return List of overdue loans
     */
    public static List<Map<String, Object>> getOverdueLoans() {
        List<Map<String, Object>> loans = new ArrayList<>();
        String query = "SELECT l.id, l.user_id, u.username, l.amount, l.remaining_balance, " +
                       "l.due_date, DATEDIFF(CURDATE(), l.due_date) as days_overdue " +
                       "FROM loans l JOIN users u ON l.user_id = u.id " +
                       "WHERE l.status = 'active' AND l.due_date < CURDATE() AND " +
                       "l.remaining_balance > 0 ORDER BY l.due_date ASC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("user_id", rs.getInt("user_id"));
                loan.put("username", rs.getString("username"));
                loan.put("amount", rs.getDouble("amount"));
                loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                loan.put("due_date", rs.getDate("due_date"));
                loan.put("days_overdue", rs.getInt("days_overdue"));
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * Get penalty report formatted as string
     * @param penaltyId Penalty ID
     * @return Formatted penalty report
     */
    public static String getPenaltyReport(int penaltyId) {
        Map<String, Object> penalty = getPenaltyDetails(penaltyId);
        if (penalty == null) return "Penalty not found";
        
        DecimalFormat df = new DecimalFormat("0.00");
        
        return "═══════════════════════════════════════\n" +
               "         LOAN PENALTY NOTICE\n" +
               "═══════════════════════════════════════\n" +
               "Penalty ID: " + penalty.get("id") + "\n" +
               "Loan ID: " + penalty.get("loan_id") + "\n" +
               "Penalty Amount: PHP " + df.format(penalty.get("penalty_amount")) + "\n" +
               "Reason: " + penalty.get("penalty_reason") + "\n" +
               "Due Date: " + penalty.get("due_date") + "\n" +
               "Status: " + ((penalty.get("paid") != null && (boolean) penalty.get("paid")) ? "PAID" : "PENDING") + "\n" +
               "═══════════════════════════════════════\n";
    }
}

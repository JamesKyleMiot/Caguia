package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Helper class for AdminDashboard to manage loan applications and operations
 */
public class AdminLoanHelper {

    /**
     * Get all pending loan applications for admin dashboard
     * @return List of pending applications with user details
     */
    public static List<Map<String, Object>> getPendingLoanApplications() {
        List<Map<String, Object>> applications = new ArrayList<>();
        String query = "SELECT la.id, la.user_id, u.username, u.fullname, u.email, " +
                       "la.requested_amount, la.purpose, la.status, la.created_at " +
                       "FROM loan_applications la " +
                       "INNER JOIN users u ON la.user_id = u.id " +
                       "WHERE la.status = 'pending' " +
                       "ORDER BY la.created_at ASC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> app = new HashMap<>();
                app.put("id", rs.getInt("id"));
                app.put("user_id", rs.getInt("user_id"));
                app.put("username", rs.getString("username"));
                app.put("fullname", rs.getString("fullname"));
                app.put("email", rs.getString("email"));
                app.put("requested_amount", rs.getDouble("requested_amount"));
                app.put("purpose", rs.getString("purpose"));
                app.put("status", rs.getString("status"));
                app.put("created_at", rs.getTimestamp("created_at"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    /**
     * Get all active loans with user details
     * @return List of active loans
     */
    public static List<Map<String, Object>> getAllActiveLoans() {
        List<Map<String, Object>> loans = new ArrayList<>();
        String query = "SELECT l.id, l.user_id, u.username, u.fullname, " +
                       "l.amount, l.interest_rate, l.total_payable, l.remaining_balance, " +
                       "l.due_date, l.status, l.created_at " +
                       "FROM loans l " +
                       "INNER JOIN users u ON l.user_id = u.id " +
                       "WHERE l.status IN ('active', 'paid') " +
                       "ORDER BY l.id DESC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("user_id", rs.getInt("user_id"));
                loan.put("username", rs.getString("username"));
                loan.put("fullname", rs.getString("fullname"));
                loan.put("amount", rs.getDouble("amount"));
                loan.put("interest_rate", rs.getDouble("interest_rate"));
                loan.put("total_payable", rs.getDouble("total_payable"));
                loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                loan.put("due_date", rs.getDate("due_date"));
                loan.put("status", rs.getString("status"));
                loan.put("created_at", rs.getTimestamp("created_at"));
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    /**
     * Get all loan payments for admin dashboard
     * @return List of all payments
     */
    public static List<Map<String, Object>> getAllLoanPayments() {
        List<Map<String, Object>> payments = new ArrayList<>();
        String query = "SELECT lp.id, lp.loan_id, lp.user_id, u.username, " +
                       "lp.payment_amount, lp.payment_method, lp.payment_status, " +
                       "lp.paid_date " +
                       "FROM loan_payments lp " +
                       "INNER JOIN users u ON lp.user_id = u.id " +
                       "ORDER BY lp.paid_date DESC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> payment = new HashMap<>();
                payment.put("id", rs.getInt("id"));
                payment.put("loan_id", rs.getInt("loan_id"));
                payment.put("user_id", rs.getInt("user_id"));
                payment.put("username", rs.getString("username"));
                payment.put("payment_amount", rs.getDouble("payment_amount"));
                payment.put("payment_method", rs.getString("payment_method"));
                payment.put("payment_status", rs.getString("payment_status"));
                payment.put("paid_date", rs.getTimestamp("paid_date"));
                payments.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Get all loan receipts for admin dashboard
     * @return List of all receipts
     */
    public static List<Map<String, Object>> getAllLoanReceipts() {
        List<Map<String, Object>> receipts = new ArrayList<>();
        String query = "SELECT lr.id, lr.receipt_number, lr.loan_id, lr.user_id, " +
                       "u.username, lr.amount_paid, lr.new_balance, lr.payment_method, " +
                       "lr.generated_at " +
                       "FROM loan_receipts lr " +
                       "INNER JOIN users u ON lr.user_id = u.id " +
                       "ORDER BY lr.generated_at DESC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> receipt = new HashMap<>();
                receipt.put("id", rs.getInt("id"));
                receipt.put("receipt_number", rs.getString("receipt_number"));
                receipt.put("loan_id", rs.getInt("loan_id"));
                receipt.put("user_id", rs.getInt("user_id"));
                receipt.put("username", rs.getString("username"));
                receipt.put("amount_paid", rs.getDouble("amount_paid"));
                receipt.put("new_balance", rs.getDouble("new_balance"));
                receipt.put("payment_method", rs.getString("payment_method"));
                receipt.put("generated_at", rs.getTimestamp("generated_at"));
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Get all loan penalties for admin dashboard
     * @return List of all penalties
     */
    public static List<Map<String, Object>> getAllLoanPenalties() {
        List<Map<String, Object>> penalties = new ArrayList<>();
        String query = "SELECT lp.id, lp.loan_id, lp.user_id, u.username, " +
                       "lp.penalty_amount, lp.penalty_reason, lp.due_date, lp.paid, " +
                       "lp.created_at " +
                       "FROM loan_penalties lp " +
                       "INNER JOIN users u ON lp.user_id = u.id " +
                       "WHERE lp.paid = FALSE " +
                       "ORDER BY lp.due_date ASC";
        
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> penalty = new HashMap<>();
                penalty.put("id", rs.getInt("id"));
                penalty.put("loan_id", rs.getInt("loan_id"));
                penalty.put("user_id", rs.getInt("user_id"));
                penalty.put("username", rs.getString("username"));
                penalty.put("penalty_amount", rs.getDouble("penalty_amount"));
                penalty.put("penalty_reason", rs.getString("penalty_reason"));
                penalty.put("due_date", rs.getDate("due_date"));
                penalty.put("paid", rs.getBoolean("paid"));
                penalty.put("created_at", rs.getTimestamp("created_at"));
                penalties.add(penalty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return penalties;
    }

    /**
     * Get count of pending loan applications
     * @return Number of pending applications
     */
    public static int getPendingApplicationCount() {
        String query = "SELECT COUNT(*) as cnt FROM loan_applications WHERE status = 'pending'";
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get count of active loans
     * @return Number of active loans
     */
    public static int getActiveLoanCount() {
        String query = "SELECT COUNT(*) as cnt FROM loans WHERE status = 'active'";
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get total pending penalties amount
     * @return Total penalty amount
     */
    public static double getTotalPendingPenalties() {
        String query = "SELECT SUM(penalty_amount) as total FROM loan_penalties WHERE paid = FALSE";
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get overdue loans needing attention
     * @return List of overdue loans
     */
    public static List<Map<String, Object>> getOverdueLoans() {
        List<Map<String, Object>> loans = new ArrayList<>();
        String query = "SELECT l.id, l.user_id, u.username, l.amount, " +
                       "l.remaining_balance, l.due_date, " +
                       "DATEDIFF(CURDATE(), l.due_date) as days_overdue " +
                       "FROM loans l " +
                       "INNER JOIN users u ON l.user_id = u.id " +
                       "WHERE l.status = 'active' AND l.due_date < CURDATE() " +
                       "AND l.remaining_balance > 0 " +
                       "ORDER BY l.due_date ASC";
        
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
     * Get admin dashboard summary statistics
     * @return Map with summary data
     */
    public static Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try (Connection conn = DB.connect()) {
            // Pending applications
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM loan_applications WHERE status = 'pending'")) {
                if (rs.next()) {
                    summary.put("pending_applications", rs.getInt("cnt"));
                }
            }
            
            // Active loans
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM loans WHERE status = 'active'")) {
                if (rs.next()) {
                    summary.put("active_loans", rs.getInt("cnt"));
                }
            }
            
            // Total loan amount outstanding
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(remaining_balance) as total FROM loans WHERE status = 'active'")) {
                if (rs.next()) {
                    summary.put("outstanding_amount", rs.getDouble("total"));
                }
            }
            
            // Unpaid penalties
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM loan_penalties WHERE paid = FALSE")) {
                if (rs.next()) {
                    summary.put("unpaid_penalties", rs.getInt("cnt"));
                }
            }
            
            // Total payments collected today
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT SUM(payment_amount) as total FROM loan_payments WHERE DATE(paid_date) = CURDATE()")) {
                if (rs.next()) {
                    summary.put("payments_today", rs.getDouble("total"));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return summary;
    }
}

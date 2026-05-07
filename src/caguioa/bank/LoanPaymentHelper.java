package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Helper class for processing loan payments
 */
public class LoanPaymentHelper {

    // Payment method constants
    public static final String METHOD_ONLINE = "Online Banking";
    public static final String METHOD_BANK_COUNTER = "Bank Counter";
    public static final String METHOD_ATM = "ATM";
    public static final String METHOD_MOBILE_APP = "Mobile App";
    public static final String METHOD_AUTO_DEBIT = "Auto-debit";

    /**
     * Process a loan payment
     * @param loanId Loan ID
     * @param userId User ID
     * @param paymentAmount Payment amount
     * @param paymentMethod Payment method (Online, Bank Counter, ATM, etc.)
     * @param transactionReference Transaction reference (for online payments)
     * @return Payment ID, or -1 if failed
     */
    public static int processLoanPayment(int loanId, int userId, double paymentAmount, 
                                         String paymentMethod, String transactionReference) {
        int paymentId = LoanManager.processLoanPayment(
            loanId,
            userId,
            paymentAmount,
            paymentMethod,
            transactionReference
        );

        if (paymentId > 0) {
            System.out.println("✓ Payment processed: ID=" + paymentId + ", Amount=" + paymentAmount + 
                             ", Method=" + paymentMethod);
        }

        return paymentId;
    }

    /**
     * Convenience wrapper for callers that only know the loan ID and amount.
     */
    public static boolean processLoanPayment(int loanId, double paymentAmount) {
        return LoanManager.processLoanPayment(loanId, paymentAmount);
    }
    /**
     * Get all payments for a loan
     * @param loanId Loan ID
     * @return List of payments
     */
    public static List<Map<String, Object>> getLoanPayments(int loanId) {
        List<Map<String, Object>> payments = new ArrayList<>();
        String query = "SELECT id, loan_id, payment_amount, payment_method, payment_status, " +
                       "transaction_reference, paid_date FROM loan_payments WHERE loan_id = ? " +
                       "ORDER BY paid_date DESC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("id", rs.getInt("id"));
                    payment.put("loan_id", rs.getInt("loan_id"));
                    payment.put("payment_amount", rs.getDouble("payment_amount"));
                    payment.put("payment_method", rs.getString("payment_method"));
                    payment.put("payment_status", rs.getString("payment_status"));
                    payment.put("transaction_reference", rs.getString("transaction_reference"));
                    payment.put("paid_date", rs.getTimestamp("paid_date"));
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Get payment history for a user
     * @param userId User ID
     * @return List of payments
     */
    public static List<Map<String, Object>> getUserPaymentHistory(int userId) {
        List<Map<String, Object>> payments = new ArrayList<>();
        String query = "SELECT lp.id, l.id as loan_id, lp.payment_amount, lp.payment_method, " +
                       "lp.payment_status, l.remaining_balance, lp.paid_date FROM loan_payments lp " +
                       "JOIN loans l ON lp.loan_id = l.id WHERE lp.user_id = ? " +
                       "ORDER BY lp.paid_date DESC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("id", rs.getInt("id"));
                    payment.put("loan_id", rs.getInt("loan_id"));
                    payment.put("payment_amount", rs.getDouble("payment_amount"));
                    payment.put("payment_method", rs.getString("payment_method"));
                    payment.put("payment_status", rs.getString("payment_status"));
                    payment.put("remaining_balance", rs.getDouble("remaining_balance"));
                    payment.put("paid_date", rs.getTimestamp("paid_date"));
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    /**
     * Verify payment was successfully posted
     * @param paymentId Payment ID
     * @return Payment details if found
     */
    public static Map<String, Object> verifyPayment(int paymentId) {
        String query = "SELECT id, loan_id, payment_amount, payment_method, payment_status, " +
                       "paid_date FROM loan_payments WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("id", rs.getInt("id"));
                    payment.put("loan_id", rs.getInt("loan_id"));
                    payment.put("payment_amount", rs.getDouble("payment_amount"));
                    payment.put("payment_method", rs.getString("payment_method"));
                    payment.put("payment_status", rs.getString("payment_status"));
                    payment.put("paid_date", rs.getTimestamp("paid_date"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get total amount paid on a loan
     * @param loanId Loan ID
     * @return Total paid amount
     */
    public static double getTotalAmountPaid(int loanId) {
        String query = "SELECT SUM(payment_amount) FROM loan_payments WHERE loan_id = ? AND payment_status = 'completed'";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, loanId);
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
     * Get payment methods available
     * @return Array of payment methods
     */
    public static String[] getAvailablePaymentMethods() {
        return new String[]{
            METHOD_ONLINE,
            METHOD_BANK_COUNTER,
            METHOD_ATM,
            METHOD_MOBILE_APP,
            METHOD_AUTO_DEBIT
        };
    }

    /**
     * Validate payment amount
     * @param loanId Loan ID
     * @param paymentAmount Payment amount
     * @return true if valid
     */
    public static boolean validatePaymentAmount(int loanId, double paymentAmount) {
        if (paymentAmount <= 0) return false;
        
        // Get remaining balance
        String query = "SELECT remaining_balance FROM loans WHERE id = ?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double remainingBalance = rs.getDouble("remaining_balance");
                    return paymentAmount <= remainingBalance;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

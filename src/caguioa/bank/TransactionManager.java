package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Manager class for user transactions
 * Tracks all financial activities: loans, payments, deposits, withdrawals, transfers, etc.
 */
public class TransactionManager {

    // Transaction type constants
    public static final String TYPE_LOAN_APPLICATION = "Loan Application";
    public static final String TYPE_LOAN_DISBURSEMENT = "Loan Disbursement";
    public static final String TYPE_LOAN_PAYMENT = "Loan Payment";
    public static final String TYPE_DEPOSIT = "Deposit";
    public static final String TYPE_WITHDRAWAL = "Withdrawal";
    public static final String TYPE_TRANSFER = "Transfer";
    public static final String TYPE_TRANSFER_TO_SAVINGS = "Transfer to Savings";
    public static final String TYPE_PENALTY = "Penalty";
    public static final String TYPE_INTEREST = "Interest";

    /**
     * Record a transaction for a user
     */
    public static int recordTransaction(int userId, String type, double amount, String method) {
        if (amount < 0) {
            System.out.println("Invalid amount: " + amount);
            return -1;
        }

        try (Connection conn = DB.connect()) {
            String query = "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, userId);
                pst.setString(2, type);
                pst.setDouble(3, amount);
                pst.setString(4, method != null ? method : type);
                pst.executeUpdate();

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int txnId = generatedKeys.getInt(1);
                        System.out.println("✓ Transaction recorded: ID=" + txnId + ", Type=" + type + ", Amount=" + amount);
                        return txnId;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error recording transaction: " + e);
        }
        return -1;
    }

    /**
     * Get all transactions for a user
     */
    public static List<Map<String, Object>> getUserTransactions(int userId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions " +
                          "WHERE user_id = ? ORDER BY created_at DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("user_id", rs.getInt("user_id"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("method", rs.getString("method"));
                        txn.put("created_at", rs.getTimestamp("created_at"));
                        transactions.add(txn);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user transactions: " + e);
        }
        return transactions;
    }

    /**
     * Get transactions by type for a user
     */
    public static List<Map<String, Object>> getUserTransactionsByType(int userId, String type) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions " +
                          "WHERE user_id = ? AND type = ? ORDER BY created_at DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setString(2, type);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("user_id", rs.getInt("user_id"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("method", rs.getString("method"));
                        txn.put("created_at", rs.getTimestamp("created_at"));
                        transactions.add(txn);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user transactions by type: " + e);
        }
        return transactions;
    }

    /**
     * Get transaction summary for a user
     */
    public static Map<String, Object> getUserTransactionSummary(int userId) {
        Map<String, Object> summary = new HashMap<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT " +
                          "COUNT(*) as total_transactions, " +
                          "SUM(CASE WHEN type IN (?, ?, ?) THEN amount ELSE 0 END) as total_deposits, " +
                          "SUM(CASE WHEN type IN (?, ?, ?) THEN amount ELSE 0 END) as total_payments, " +
                          "SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_loan_disbursement, " +
                          "SUM(amount) as total_amount_transacted " +
                          "FROM transactions WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, TYPE_DEPOSIT);
                pst.setString(2, TYPE_TRANSFER);
                pst.setString(3, TYPE_TRANSFER_TO_SAVINGS);
                pst.setString(4, TYPE_LOAN_PAYMENT);
                pst.setString(5, TYPE_WITHDRAWAL);
                pst.setString(6, TYPE_PENALTY);
                pst.setString(7, TYPE_LOAN_DISBURSEMENT);
                pst.setInt(8, userId);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("total_transactions", rs.getInt("total_transactions"));
                        summary.put("total_deposits", rs.getDouble("total_deposits"));
                        summary.put("total_payments", rs.getDouble("total_payments"));
                        summary.put("total_loan_disbursement", rs.getDouble("total_loan_disbursement"));
                        summary.put("total_amount_transacted", rs.getDouble("total_amount_transacted"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction summary: " + e);
        }
        return summary;
    }

    /**
     * Get all transactions for admin (all users)
     */
    public static List<Map<String, Object>> getAllTransactions(int limit) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT t.id, t.user_id, u.username, u.fullname, t.type, t.amount, " +
                          "t.method, t.created_at FROM transactions t " +
                          "JOIN users u ON t.user_id = u.id " +
                          "ORDER BY t.created_at DESC LIMIT ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, limit);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("user_id", rs.getInt("user_id"));
                        txn.put("username", rs.getString("username"));
                        txn.put("fullname", rs.getString("fullname"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("method", rs.getString("method"));
                        txn.put("created_at", rs.getTimestamp("created_at"));
                        transactions.add(txn);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting all transactions: " + e);
        }
        return transactions;
    }

    /**
     * Get transactions by date range for a user
     */
    public static List<Map<String, Object>> getUserTransactionsByDateRange(int userId, String startDate, String endDate) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions " +
                          "WHERE user_id = ? AND DATE(created_at) >= ? AND DATE(created_at) <= ? " +
                          "ORDER BY created_at DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setString(2, startDate);
                pst.setString(3, endDate);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("user_id", rs.getInt("user_id"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("method", rs.getString("method"));
                        txn.put("created_at", rs.getTimestamp("created_at"));
                        transactions.add(txn);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transactions by date range: " + e);
        }
        return transactions;
    }

    /**
     * Get total transaction amount for a user
     */
    public static double getUserTotalTransactionAmount(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting total transaction amount: " + e);
        }
        return 0;
    }

    /**
     * Get transaction count for a user
     */
    public static int getUserTransactionCount(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT COUNT(*) as count FROM transactions WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction count: " + e);
        }
        return 0;
    }

    /**
     * Get monthly transaction summary for a user
     */
    public static Map<String, Double> getUserMonthlyTransactions(int userId, int year, int month) {
        Map<String, Double> summary = new HashMap<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT type, SUM(amount) as total FROM transactions " +
                          "WHERE user_id = ? AND YEAR(created_at) = ? AND MONTH(created_at) = ? " +
                          "GROUP BY type ORDER BY total DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setInt(2, year);
                pst.setInt(3, month);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        summary.put(rs.getString("type"), rs.getDouble("total"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting monthly transactions: " + e);
        }
        return summary;
    }

    /**
     * Delete a transaction (admin only)
     */
    public static boolean deleteTransaction(int transactionId) {
        try (Connection conn = DB.connect()) {
            String query = "DELETE FROM transactions WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, transactionId);
                int result = pst.executeUpdate();
                return result > 0;
            }
        } catch (Exception e) {
            System.out.println("Error deleting transaction: " + e);
        }
        return false;
    }
}

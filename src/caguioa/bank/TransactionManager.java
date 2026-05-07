package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Manager class for all transaction records
 * Handles recording and retrieving transactions for users
 */
public class TransactionManager {

    // Transaction type constants
    public static final String TYPE_DEPOSIT = "Deposit";
    public static final String TYPE_WITHDRAWAL = "Withdrawal";
    public static final String TYPE_LOAN_DISBURSEMENT = "Loan Disbursement";
    public static final String TYPE_LOAN_PAYMENT = "Loan Payment";
    public static final String TYPE_LOAN_APPLICATION = "Loan Application";
    public static final String TYPE_TRANSFER = "Transfer";
    public static final String TYPE_SAVINGS_TRANSFER = "Transfer to Savings";
    public static final String TYPE_BILL_PAYMENT = "Bill Payment";
    public static final String TYPE_PENALTY = "Penalty";
    public static final String TYPE_INTEREST = "Interest";
    public static final String TYPE_ACCOUNT_FEE = "Account Fee";

    /**
     * Record a new transaction
     */
    public static int recordTransaction(int userId, String type, double amount, String method) {
        try (Connection conn = DB.connect()) {
            if (conn == null) return -1;

            String insertSQL = "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, userId);
                pst.setString(2, type);
                pst.setDouble(3, amount);
                pst.setString(4, method);
                pst.executeUpdate();

                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int transactionId = generatedKeys.getInt(1);
                        System.out.println("✓ Transaction recorded: ID=" + transactionId + ", Type=" + type + ", Amount=" + amount);
                        return transactionId;
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
                          "WHERE user_id = ? ORDER BY created_at DESC LIMIT 100";
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
     * Get transactions for a user filtered by type
     */
    public static List<Map<String, Object>> getUserTransactionsByType(int userId, String type) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions " +
                          "WHERE user_id = ? AND type = ? ORDER BY created_at DESC LIMIT 100";
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
            // Total transaction count
            String countQuery = "SELECT COUNT(*) as total_count FROM transactions WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(countQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("total_transactions", rs.getInt("total_count"));
                    }
                }
            }

            // Total amount
            String amountQuery = "SELECT COALESCE(SUM(amount), 0) as total_amount FROM transactions WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(amountQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("total_amount", rs.getDouble("total_amount"));
                    }
                }
            }

            // Deposits sum
            String depositsQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = ? AND type = ?";
            try (PreparedStatement pst = conn.prepareStatement(depositsQuery)) {
                pst.setInt(1, userId);
                pst.setString(2, TYPE_DEPOSIT);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("total_deposits", rs.getDouble("total"));
                    }
                }
            }

            // Loan payments sum
            String paymentsQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = ? AND type = ?";
            try (PreparedStatement pst = conn.prepareStatement(paymentsQuery)) {
                pst.setInt(1, userId);
                pst.setString(2, TYPE_LOAN_PAYMENT);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("total_loan_payments", rs.getDouble("total"));
                    }
                }
            }

            // Last transaction
            String lastQuery = "SELECT created_at FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
            try (PreparedStatement pst = conn.prepareStatement(lastQuery)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        summary.put("last_transaction_date", rs.getTimestamp("created_at"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user transaction summary: " + e);
        }
        return summary;
    }

    /**
     * Get all transactions across all users (admin view)
     */
    public static List<Map<String, Object>> getAllTransactions(int limit) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT t.id, t.user_id, u.username, u.fullname, t.type, t.amount, t.method, t.created_at " +
                          "FROM transactions t " +
                          "JOIN users u ON t.user_id = u.id " +
                          "ORDER BY t.created_at DESC LIMIT ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, limit > 0 ? limit : 1000);
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
     * Get transactions for a date range
     */
    public static List<Map<String, Object>> getTransactionsByDateRange(int userId, java.util.Date startDate, java.util.Date endDate) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions " +
                          "WHERE user_id = ? AND created_at BETWEEN ? AND ? " +
                          "ORDER BY created_at DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setTimestamp(2, new java.sql.Timestamp(startDate.getTime()));
                pst.setTimestamp(3, new java.sql.Timestamp(endDate.getTime()));
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
     * Get transaction by ID
     */
    public static Map<String, Object> getTransactionById(int transactionId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, type, amount, method, created_at FROM transactions WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, transactionId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("id", rs.getInt("id"));
                        txn.put("user_id", rs.getInt("user_id"));
                        txn.put("type", rs.getString("type"));
                        txn.put("amount", rs.getDouble("amount"));
                        txn.put("method", rs.getString("method"));
                        txn.put("created_at", rs.getTimestamp("created_at"));
                        return txn;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction by ID: " + e);
        }
        return null;
    }

    /**
     * Get transaction statistics by type for a user
     */
    public static Map<String, Object> getTransactionStatsByType(int userId) {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT type, COUNT(*) as count, SUM(amount) as total FROM transactions " +
                          "WHERE user_id = ? GROUP BY type ORDER BY total DESC";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        Map<String, Object> data = new HashMap<>();
                        data.put("count", rs.getInt("count"));
                        data.put("total", rs.getDouble("total"));
                        stats.put(type, data);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction stats by type: " + e);
        }
        return stats;
    }

    /**
     * Delete a transaction (admin only)
     */
    public static boolean deleteTransaction(int transactionId) {
        try (Connection conn = DB.connect()) {
            String deleteSQL = "DELETE FROM transactions WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(deleteSQL)) {
                pst.setInt(1, transactionId);
                int result = pst.executeUpdate();
                return result > 0;
            }
        } catch (Exception e) {
            System.out.println("Error deleting transaction: " + e);
        }
        return false;
    }

    /**
     * Get monthly transaction summary for a user
     */
    public static List<Map<String, Object>> getMonthlyTransactionSummary(int userId, int monthsBack) {
        List<Map<String, Object>> summary = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, type, COUNT(*) as count, " +
                          "SUM(amount) as total FROM transactions " +
                          "WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? MONTH) " +
                          "GROUP BY month, type ORDER BY month DESC, type";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                pst.setInt(2, monthsBack);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("month", rs.getString("month"));
                        data.put("type", rs.getString("type"));
                        data.put("count", rs.getInt("count"));
                        data.put("total", rs.getDouble("total"));
                        summary.add(data);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting monthly transaction summary: " + e);
        }
        return summary;
    }
}

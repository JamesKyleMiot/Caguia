package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * Manager class for user bank account information
 */
public class UserBankAccountManager {

    /**
     * Ensure the user_bank_accounts table exists
     */
    public static void ensureTableExists() {
        try (Connection conn = DB.connect()) {
            if (conn != null) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS user_bank_accounts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL UNIQUE," +
                    "account_number VARCHAR(50) NOT NULL UNIQUE," +
                    "account_holder_name VARCHAR(255) NOT NULL," +
                    "bank_name VARCHAR(255) NOT NULL," +
                    "account_type VARCHAR(50)," +
                    "branch_code VARCHAR(20)," +
                    "is_primary BOOLEAN DEFAULT TRUE," +
                    "verified BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "INDEX idx_user_id (user_id)," +
                    "INDEX idx_account_number (account_number)" +
                    ")";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL);
                    System.out.println("✓ user_bank_accounts table ready");
                }
            }
        } catch (Exception e) {
            System.out.println("Error ensuring user_bank_accounts table: " + e);
        }
    }

    /**
     * Save or update bank account information for a user
     */
    public static boolean saveBankAccount(int userId, String accountNumber, String accountHolderName,
                                         String bankName, String accountType, String branchCode) {
        try (Connection conn = DB.connect()) {
            ensureTableExists();

            // Check if account already exists for this user
            String checkSQL = "SELECT id FROM user_bank_accounts WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Update existing
                        return updateBankAccount(userId, accountNumber, accountHolderName, bankName, accountType, branchCode);
                    }
                }
            }

            // Insert new
            String insertSQL = "INSERT INTO user_bank_accounts (user_id, account_number, account_holder_name, " +
                             "bank_name, account_type, branch_code, is_primary, verified) " +
                             "VALUES (?, ?, ?, ?, ?, ?, TRUE, FALSE)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, accountNumber.trim());
                insertStmt.setString(3, accountHolderName.trim());
                insertStmt.setString(4, bankName.trim());
                insertStmt.setString(5, accountType != null ? accountType.trim() : "Savings");
                insertStmt.setString(6, branchCode != null ? branchCode.trim() : null);
                insertStmt.executeUpdate();
                System.out.println("✓ Bank account saved for user " + userId);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error saving bank account: " + e);
            return false;
        }
    }

    /**
     * Update existing bank account information
     */
    public static boolean updateBankAccount(int userId, String accountNumber, String accountHolderName,
                                           String bankName, String accountType, String branchCode) {
        try (Connection conn = DB.connect()) {
            String updateSQL = "UPDATE user_bank_accounts SET account_number = ?, account_holder_name = ?, " +
                             "bank_name = ?, account_type = ?, branch_code = ?, updated_at = CURRENT_TIMESTAMP " +
                             "WHERE user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, accountNumber.trim());
                updateStmt.setString(2, accountHolderName.trim());
                updateStmt.setString(3, bankName.trim());
                updateStmt.setString(4, accountType != null ? accountType.trim() : "Savings");
                updateStmt.setString(5, branchCode != null ? branchCode.trim() : null);
                updateStmt.setInt(6, userId);
                int result = updateStmt.executeUpdate();
                if (result > 0) {
                    System.out.println("✓ Bank account updated for user " + userId);
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating bank account: " + e);
        }
        return false;
    }

    /**
     * Get bank account information for a user
     */
    public static Map<String, Object> getUserBankAccount(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, user_id, account_number, account_holder_name, bank_name, " +
                          "account_type, branch_code, is_primary, verified, created_at FROM user_bank_accounts " +
                          "WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> account = new HashMap<>();
                        account.put("id", rs.getInt("id"));
                        account.put("user_id", rs.getInt("user_id"));
                        account.put("account_number", rs.getString("account_number"));
                        account.put("account_holder_name", rs.getString("account_holder_name"));
                        account.put("bank_name", rs.getString("bank_name"));
                        account.put("account_type", rs.getString("account_type"));
                        account.put("branch_code", rs.getString("branch_code"));
                        account.put("is_primary", rs.getBoolean("is_primary"));
                        account.put("verified", rs.getBoolean("verified"));
                        account.put("created_at", rs.getTimestamp("created_at"));
                        return account;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting bank account: " + e);
        }
        return null;
    }

    /**
     * Get account number by user ID
     */
    public static String getAccountNumberByUserId(int userId) {
        Map<String, Object> account = getUserBankAccount(userId);
        if (account != null) {
            return (String) account.get("account_number");
        }
        return null;
    }

    /**
     * Get user ID by account number
     */
    public static Integer getUserIdByAccountNumber(String accountNumber) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT user_id FROM user_bank_accounts WHERE account_number = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, accountNumber.trim());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("user_id");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user ID by account number: " + e);
        }
        return null;
    }

    /**
     * Verify bank account (mark as verified)
     */
    public static boolean verifyBankAccount(int userId) {
        try (Connection conn = DB.connect()) {
            String updateSQL = "UPDATE user_bank_accounts SET verified = TRUE WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(updateSQL)) {
                pst.setInt(1, userId);
                int result = pst.executeUpdate();
                return result > 0;
            }
        } catch (Exception e) {
            System.out.println("Error verifying bank account: " + e);
        }
        return false;
    }

    /**
     * Delete bank account information for a user
     */
    public static boolean deleteBankAccount(int userId) {
        try (Connection conn = DB.connect()) {
            String deleteSQL = "DELETE FROM user_bank_accounts WHERE user_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(deleteSQL)) {
                pst.setInt(1, userId);
                int result = pst.executeUpdate();
                if (result > 0) {
                    System.out.println("✓ Bank account deleted for user " + userId);
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error deleting bank account: " + e);
        }
        return false;
    }

    /**
     * Check if user has a bank account registered
     */
    public static boolean hasBankAccount(int userId) {
        return getUserBankAccount(userId) != null;
    }

    /**
     * Get all verified bank accounts (for admin view)
     */
    public static List<Map<String, Object>> getAllVerifiedBankAccounts() {
        List<Map<String, Object>> accounts = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT uba.id, uba.user_id, u.username, u.fullname, uba.account_number, " +
                          "uba.account_holder_name, uba.bank_name, uba.account_type, uba.verified, uba.created_at " +
                          "FROM user_bank_accounts uba " +
                          "JOIN users u ON uba.user_id = u.id " +
                          "WHERE uba.verified = TRUE " +
                          "ORDER BY uba.created_at DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Map<String, Object> account = new HashMap<>();
                    account.put("id", rs.getInt("id"));
                    account.put("user_id", rs.getInt("user_id"));
                    account.put("username", rs.getString("username"));
                    account.put("fullname", rs.getString("fullname"));
                    account.put("account_number", rs.getString("account_number"));
                    account.put("account_holder_name", rs.getString("account_holder_name"));
                    account.put("bank_name", rs.getString("bank_name"));
                    account.put("account_type", rs.getString("account_type"));
                    account.put("verified", rs.getBoolean("verified"));
                    account.put("created_at", rs.getTimestamp("created_at"));
                    accounts.add(account);
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting all verified bank accounts: " + e);
        }
        return accounts;
    }
}

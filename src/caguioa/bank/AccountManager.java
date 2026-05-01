package caguioa.bank;

import java.sql.*;

public class AccountManager {
    
    /**
     * Deactivate/Suspend a user account
     */
    public static boolean suspendAccount(int userId, String reason) {
        try (Connection conn = DB.connect()) {
            String query = "UPDATE users SET role = 'suspended' WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            int result = pst.executeUpdate();
            pst.close();
            
            if (result > 0) {
                logAccountAction(userId, "SUSPENDED", reason);
            }
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error suspending account: " + e);
            return false;
        }
    }
    
    /**
     * Reactivate a suspended account
     */
    public static boolean reactivateAccount(int userId, String reason) {
        try (Connection conn = DB.connect()) {
            String query = "UPDATE users SET role = 'user' WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            int result = pst.executeUpdate();
            pst.close();
            
            if (result > 0) {
                logAccountAction(userId, "REACTIVATED", reason);
            }
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error reactivating account: " + e);
            return false;
        }
    }
    
    /**
     * Check if account is suspended
     */
    public static boolean isAccountSuspended(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT role FROM users WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String role = rs.getString("role");
                rs.close();
                pst.close();
                return "suspended".equalsIgnoreCase(role);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error checking account status: " + e);
        }
        return false;
    }
    
    /**
     * Get user account status
     */
    public static String getAccountStatus(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT role FROM users WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String role = rs.getString("role");
                rs.close();
                pst.close();
                return role;
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error getting account status: " + e);
        }
        return null;
    }
    
    /**
     * Log account actions for audit trail
     */
    private static void logAccountAction(int userId, String action, String reason) {
        try (Connection conn = DB.connect()) {
            // Create audit log table if it doesn't exist
            String createTableQuery = "CREATE TABLE IF NOT EXISTS account_audit_log (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "action VARCHAR(50) NOT NULL," +
                    "reason VARCHAR(255)," +
                    "admin_id INT," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            
            PreparedStatement createPst = conn.prepareStatement(createTableQuery);
            createPst.executeUpdate();
            createPst.close();
            
            // Insert log entry
            String insertQuery = "INSERT INTO account_audit_log (user_id, action, reason, admin_id) VALUES (?, ?, ?, ?)";
            PreparedStatement insertPst = conn.prepareStatement(insertQuery);
            insertPst.setInt(1, userId);
            insertPst.setString(2, action);
            insertPst.setString(3, reason);
            insertPst.setInt(4, Session.adminId > 0 ? Session.adminId : 0);
            insertPst.executeUpdate();
            insertPst.close();
        } catch (Exception e) {
            System.out.println("Error logging account action: " + e);
        }
    }
}

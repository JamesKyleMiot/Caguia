package caguioa.bank;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

/**
 * Helper class for Admin Dashboard - PIN Reset Management
 * Provides methods to manage PIN reset requests with OTP functionality
 */
public class AdminPINResetHelper {

    /**
     * Get all pending PIN reset requests as table data
     * @return 2D array of pending requests for JTable
     */
    public static Object[][] getPendingRequests() {
        String sql = "SELECT p.id, u.username, u.fullname, p.email, " +
                     "DATE_FORMAT(p.created_at, '%Y-%m-%d %H:%i:%s') as requested_date " +
                     "FROM pin_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'pending' " +
                     "ORDER BY p.created_at DESC";
        
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement()) {
            
            ResultSet rs = st.executeQuery(sql);
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Full Name");
            model.addColumn("Email");
            model.addColumn("Requested Date");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("requested_date")
                });
            }
            
            Object[][] data = new Object[model.getRowCount()][5];
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < 5; j++) {
                    data[i][j] = model.getValueAt(i, j);
                }
            }
            
            return data;
            
        } catch (Exception e) {
            System.out.println("Error fetching pending PIN reset requests: " + e);
            return new Object[0][5];
        }
    }

    /**
     * Get count of pending PIN reset requests
     * @return Number of pending requests
     */
    public static int getPendingRequestCount() {
        return PINResetManager.getPendingRequestCount();
    }

    /**
     * Approve a PIN reset request and generate OTP
     * @param requestId Request ID to approve
     * @param adminId Admin ID approving the request
     * @return OTP code if successful, null otherwise
     */
    public static String approveAndGenerateOTP(int requestId, int adminId) {
        // Get request details
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(
                "SELECT p.user_id, u.fullname, u.username, p.email " +
                "FROM pin_reset_requests p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.id = ?")) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullname = rs.getString("fullname");
                String username = rs.getString("username");
                String email = rs.getString("email");
                
                // Approve the request
                if (PINResetManager.approveRequest(requestId, adminId)) {
                    // Generate and send OTP
                    String otp = PINResetManager.generateAndSendOTP(requestId, fullname, email);
                    
                    if (otp != null) {
                        System.out.println("✓ PIN reset approved for user: " + username);
                        System.out.println("✓ OTP generated: " + otp);
                        System.out.println("✓ OTP sent to: " + email);
                        
                        // Send email notification
                        EmailNotifier.sendPINResetApprovalNotification(email, fullname);
                        
                        return otp;
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error approving PIN reset: " + e);
        }
        return null;
    }

    /**
     * Deny a PIN reset request
     * @param requestId Request ID to deny
     * @param adminId Admin ID denying the request
     * @param reason Reason for denial
     * @return true if denied successfully
     */
    public static boolean denyRequest(int requestId, int adminId, String reason) {
        // Get request details for email
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(
                "SELECT p.email, u.fullname FROM pin_reset_requests p " +
                "JOIN users u ON p.user_id = u.id WHERE p.id = ?")) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String email = rs.getString("email");
                String fullname = rs.getString("fullname");
                
                // Deny the request
                if (PINResetManager.denyRequest(requestId, adminId, reason)) {
                    System.out.println("✓ PIN reset denied for request ID: " + requestId);
                    System.out.println("✓ Reason: " + reason);
                    
                    // Send denial notification
                    EmailNotifier.sendPINResetDenialNotification(email, fullname, reason);
                    
                    return true;
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error denying PIN reset: " + e);
        }
        return false;
    }

    /**
     * Get status of a specific request
     * @param requestId Request ID
     * @return Status string (pending, approved, denied, completed)
     */
    public static String getRequestStatus(int requestId) {
        String sql = "SELECT status FROM pin_reset_requests WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
            
        } catch (Exception e) {
            System.out.println("Error getting request status: " + e);
        }
        return "unknown";
    }

    /**
     * Resend OTP to user
     * @param requestId Request ID
     * @return New OTP if successful, null otherwise
     */
    public static String resendOTP(int requestId) {
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(
                "SELECT p.user_id, u.fullname, p.email FROM pin_reset_requests p " +
                "JOIN users u ON p.user_id = u.id WHERE p.id = ?")) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String fullname = rs.getString("fullname");
                String email = rs.getString("email");
                
                // Generate new OTP
                String otp = PINResetManager.generateAndSendOTP(requestId, fullname, email);
                
                if (otp != null) {
                    System.out.println("✓ New OTP generated and sent to: " + email);
                    return otp;
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error resending OTP: " + e);
        }
        return null;
    }

    /**
     * Get approved requests with OTP status
     * @return 2D array of approved requests
     */
    public static Object[][] getApprovedRequests() {
        String sql = "SELECT p.id, u.username, u.fullname, p.email, " +
                     "CASE WHEN p.otp_verified = 1 THEN 'Verified' ELSE 'Pending' END as otp_status, " +
                     "DATE_FORMAT(p.otp_generated_at, '%Y-%m-%d %H:%i:%s') as otp_sent_date " +
                     "FROM pin_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'approved' AND p.expires_at > NOW() " +
                     "ORDER BY p.otp_generated_at DESC";
        
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement()) {
            
            ResultSet rs = st.executeQuery(sql);
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Full Name");
            model.addColumn("Email");
            model.addColumn("OTP Status");
            model.addColumn("OTP Sent Date");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("otp_status"),
                    rs.getString("otp_sent_date")
                });
            }
            
            Object[][] data = new Object[model.getRowCount()][6];
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < 6; j++) {
                    data[i][j] = model.getValueAt(i, j);
                }
            }
            
            return data;
            
        } catch (Exception e) {
            System.out.println("Error fetching approved PIN reset requests: " + e);
            return new Object[0][6];
        }
    }

    /**
     * Get completed PIN reset requests
     * @return 2D array of completed requests
     */
    public static Object[][] getCompletedRequests() {
        String sql = "SELECT p.id, u.username, u.fullname, p.email, " +
                     "DATE_FORMAT(p.completed_at, '%Y-%m-%d %H:%i:%s') as completion_date " +
                     "FROM pin_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'completed' " +
                     "ORDER BY p.completed_at DESC LIMIT 50";
        
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement()) {
            
            ResultSet rs = st.executeQuery(sql);
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Full Name");
            model.addColumn("Email");
            model.addColumn("Completed Date");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("completion_date")
                });
            }
            
            Object[][] data = new Object[model.getRowCount()][5];
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < 5; j++) {
                    data[i][j] = model.getValueAt(i, j);
                }
            }
            
            return data;
            
        } catch (Exception e) {
            System.out.println("Error fetching completed PIN reset requests: " + e);
            return new Object[0][5];
        }
    }
}

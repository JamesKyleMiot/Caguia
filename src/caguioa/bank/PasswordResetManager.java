package caguioa.bank;

import java.sql.*;

public class PasswordResetManager {

    /**
     * Submit a password reset request
     * @param userId User requesting password reset
     * @param email Email address for contact
     * @param phone Phone number for contact
     * @return true if request submitted successfully
     */
    public static boolean submitPasswordResetRequest(int userId, String email, String phone) {
        String sql = "INSERT INTO password_reset_requests (user_id, email, phone, status) " +
                     "VALUES (?, ?, ?, 'pending')";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            pst.setString(2, email.trim());
            pst.setString(3, phone.trim());
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error submitting password reset request: " + e);
            return false;
        }
    }

    /**
     * Get pending password reset requests
     * @return ResultSet of pending requests
     */
    public static ResultSet getPendingResetRequests() {
        String sql = "SELECT p.*, u.username, u.fullname FROM password_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'pending' " +
                     "ORDER BY p.created_at DESC";
        
        try {
            Connection conn = DB.connect();
            PreparedStatement pst = conn.prepareStatement(sql);
            return pst.executeQuery();
            
        } catch (Exception e) {
            System.out.println("Error fetching pending requests: " + e);
            return null;
        }
    }

    /**
     * Check if user has pending password reset request
     * @param userId User ID to check
     * @return true if user has pending request
     */
    public static boolean hasPendingRequest(int userId) {
        String sql = "SELECT COUNT(*) FROM password_reset_requests " +
                     "WHERE user_id = ? AND status = 'pending'";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (Exception e) {
            System.out.println("Error checking pending request: " + e);
        }
        return false;
    }

    /**
     * Check if user has approved password reset request
     * @param userId User ID to check
     * @return Request ID if approved, -1 if none
     */
    public static int getApprovedRequestId(int userId) {
        String sql = "SELECT id FROM password_reset_requests " +
                     "WHERE user_id = ? AND status = 'approved' AND expires_at > NOW()";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            
        } catch (Exception e) {
            System.out.println("Error checking approved request: " + e);
        }
        return -1;
    }

    /**
     * Approve a password reset request
     * @param requestId Request ID to approve
     * @param adminId Admin ID approving the request
     * @return true if approved successfully
     */
    public static boolean approveRequest(int requestId, int adminId) {
        String sql = "UPDATE password_reset_requests " +
                     "SET status = 'approved', admin_id = ?, reviewed_at = NOW(), " +
                     "expires_at = DATE_ADD(NOW(), INTERVAL 1 HOUR) " +
                     "WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, adminId);
            pst.setInt(2, requestId);
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error approving request: " + e);
            return false;
        }
    }

    /**
     * Deny a password reset request
     * @param requestId Request ID to deny
     * @param adminId Admin ID denying the request
     * @param reason Reason for denial
     * @return true if denied successfully
     */
    public static boolean denyRequest(int requestId, int adminId, String reason) {
        String sql = "UPDATE password_reset_requests " +
                     "SET status = 'denied', admin_id = ?, reviewed_at = NOW(), admin_response = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, adminId);
            pst.setString(2, reason.trim());
            pst.setInt(3, requestId);
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error denying request: " + e);
            return false;
        }
    }

    /**
     * Update user password
     * @param userId User ID
     * @param newPassword New password (will be hashed)
     * @param requestId Request ID to mark as completed
     * @return true if password updated successfully
     */
    public static boolean updatePassword(int userId, String newPassword, int requestId) {
        String hashedPassword = SecurityUtil.hashPassword(newPassword);
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        String updateRequest = "UPDATE password_reset_requests SET status = 'completed' WHERE id = ?";
        
        try (Connection conn = DB.connect()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pst1 = conn.prepareStatement(sql)) {
                pst1.setString(1, hashedPassword);
                pst1.setInt(2, userId);
                pst1.executeUpdate();
            }
            
            try (PreparedStatement pst2 = conn.prepareStatement(updateRequest)) {
                pst2.setInt(1, requestId);
                pst2.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error updating password: " + e);
            return false;
        }
    }

    /**
     * Check if email belongs to user
     * @param userId User ID
     * @param email Email to verify
     * @return true if email matches user's email
     */
    public static boolean verifyUserEmail(int userId, String email) {
        String sql = "SELECT email FROM users WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String storedEmail = rs.getString("email");
                return email.trim().equalsIgnoreCase(storedEmail);
            }
            
        } catch (Exception e) {
            System.out.println("Error verifying email: " + e);
        }
        return false;
    }
}

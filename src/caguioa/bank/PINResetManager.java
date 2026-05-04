package caguioa.bank;

import java.sql.*;

public class PINResetManager {

    /**
     * Submit a PIN reset request
     * @param userId User requesting PIN reset
     * @param email Email address for contact
     * @return true if request submitted successfully
     */
    public static boolean submitPINResetRequest(int userId, String email) {
        String sql = "INSERT INTO pin_reset_requests (user_id, email, status) " +
                     "VALUES (?, ?, 'pending')";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            pst.setString(2, email.trim());
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error submitting PIN reset request: " + e);
            return false;
        }
    }

    /**
     * Get pending PIN reset requests
     * @return ResultSet of pending requests
     */
    public static ResultSet getPendingResetRequests() {
        String sql = "SELECT p.id, p.user_id, p.email, p.status, p.created_at, p.admin_response, " +
                     "u.username, u.fullname FROM pin_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'pending' " +
                     "ORDER BY p.created_at DESC";
        
        try {
            Connection conn = DB.connect();
            PreparedStatement pst = conn.prepareStatement(sql);
            return pst.executeQuery();
            
        } catch (Exception e) {
            System.out.println("Error fetching pending PIN requests: " + e);
            return null;
        }
    }

    /**
     * Check if user has pending PIN reset request
     * @param userId User ID to check
     * @return true if user has pending request
     */
    public static boolean hasPendingRequest(int userId) {
        String sql = "SELECT COUNT(*) FROM pin_reset_requests " +
                     "WHERE user_id = ? AND status = 'pending'";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (Exception e) {
            System.out.println("Error checking pending PIN request: " + e);
        }
        return false;
    }

    /**
     * Check if user has approved PIN reset request
     * @param userId User ID to check
     * @return Request ID if approved, -1 if none
     */
    public static int getApprovedRequestId(int userId) {
        String sql = "SELECT id FROM pin_reset_requests " +
                     "WHERE user_id = ? AND status = 'approved' AND expires_at > NOW()";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            
        } catch (Exception e) {
            System.out.println("Error checking approved PIN request: " + e);
        }
        return -1;
    }

    /**
     * Approve a PIN reset request
     * @param requestId Request ID to approve
     * @param adminId Admin ID approving the request
     * @return true if approved successfully
     */
    public static boolean approveRequest(int requestId, int adminId) {
        String sql = "UPDATE pin_reset_requests " +
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
            System.out.println("Error approving PIN request: " + e);
            return false;
        }
    }

    /**
     * Deny a PIN reset request
     * @param requestId Request ID to deny
     * @param adminId Admin ID denying the request
     * @param reason Reason for denial
     * @return true if denied successfully
     */
    public static boolean denyRequest(int requestId, int adminId, String reason) {
        String sql = "UPDATE pin_reset_requests " +
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
            System.out.println("Error denying PIN request: " + e);
            return false;
        }
    }

    /**
     * Update user PIN after approval
     * @param userId User ID to update PIN for
     * @param newPIN New PIN (should be hashed before calling this method)
     * @return true if PIN updated successfully
     */
    public static boolean updateUserPIN(int userId, String newPIN) {
        String sql = "UPDATE users SET pin = ? WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Hash the PIN for security
            String hashedPIN = SecurityUtil.hashPin(newPIN);
            pst.setString(1, hashedPIN);
            pst.setInt(2, userId);
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error updating PIN: " + e);
            return false;
        }
    }

    /**
     * Mark a request as completed
     * @param requestId Request ID to mark as completed
     * @return true if marked successfully
     */
    public static boolean markAsCompleted(int requestId) {
        String sql = "UPDATE pin_reset_requests " +
                     "SET status = 'completed', completed_at = NOW() " +
                     "WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, requestId);
            
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error marking PIN request as completed: " + e);
            return false;
        }
    }

    /**
     * Get count of pending PIN reset requests
     * @return Number of pending requests
     */
    public static int getPendingRequestCount() {
        String sql = "SELECT COUNT(*) FROM pin_reset_requests WHERE status = 'pending'";
        
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement()) {
            
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting pending PIN request count: " + e);
        }
        return 0;
    }

    /**
     * Generate and send OTP for approved PIN reset request
     * @param requestId Request ID to generate OTP for
     * @param userName User's name
     * @param email User's email
     * @return OTP code if successful, null otherwise
     */
    public static String generateAndSendOTP(int requestId, String userName, String email) {
        String otp = OTPGenerator.generateOTP();
        
        String sql = "UPDATE pin_reset_requests " +
                     "SET otp = ?, otp_generated_at = NOW() " +
                     "WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, otp);
            pst.setInt(2, requestId);
            
            int rowsUpdated = pst.executeUpdate();
            
            if (rowsUpdated > 0) {
                // Send OTP via email
                EmailNotifier.sendPINResetOTP(email, userName, otp);
                return otp;
            }
            
        } catch (Exception e) {
            System.out.println("Error generating OTP: " + e);
        }
        return null;
    }

    /**
     * Verify OTP for PIN reset request
     * @param requestId Request ID
     * @param enteredOTP OTP entered by user
     * @return true if OTP is valid
     */
    public static boolean verifyOTP(int requestId, String enteredOTP) {
        String sql = "SELECT otp, otp_generated_at FROM pin_reset_requests WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String storedOTP = rs.getString("otp");
                java.sql.Timestamp otpGeneratedAt = rs.getTimestamp("otp_generated_at");
                
                // Check if OTP matches
                if (!OTPGenerator.validateOTP(storedOTP, enteredOTP)) {
                    return false;
                }
                
                // Check if OTP has not expired (10 minutes)
                if (otpGeneratedAt != null) {
                    long currentTime = System.currentTimeMillis();
                    long otpTime = otpGeneratedAt.getTime();
                    long diffMinutes = (currentTime - otpTime) / (60 * 1000);
                    
                    if (diffMinutes > OTPGenerator.getOTPValidityMinutes()) {
                        return false; // OTP expired
                    }
                }
                
                // Mark OTP as verified
                markOTPVerified(requestId);
                return true;
            }
            
        } catch (Exception e) {
            System.out.println("Error verifying OTP: " + e);
        }
        return false;
    }

    /**
     * Mark OTP as verified
     * @param requestId Request ID
     * @return true if marked successfully
     */
    private static boolean markOTPVerified(int requestId) {
        String sql = "UPDATE pin_reset_requests SET otp_verified = TRUE WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, requestId);
            pst.executeUpdate();
            return true;
            
        } catch (Exception e) {
            System.out.println("Error marking OTP as verified: " + e);
            return false;
        }
    }

    /**
     * Check if OTP is verified for a request
     * @param requestId Request ID
     * @return true if OTP is verified
     */
    public static boolean isOTPVerified(int requestId) {
        String sql = "SELECT otp_verified FROM pin_reset_requests WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, requestId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("otp_verified");
            }
            
        } catch (Exception e) {
            System.out.println("Error checking OTP verification status: " + e);
        }
        return false;
    }

    /**
     * Get request details for OTP verification
     * @param requestId Request ID
     * @return ResultSet with request details
     */
    public static ResultSet getRequestDetails(int requestId) {
        String sql = "SELECT p.id, p.user_id, p.email, p.status, u.username, u.fullname " +
                     "FROM pin_reset_requests p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.id = ?";
        
        try {
            Connection conn = DB.connect();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, requestId);
            return pst.executeQuery();
            
        } catch (Exception e) {
            System.out.println("Error fetching request details: " + e);
            return null;
        }
    }
}

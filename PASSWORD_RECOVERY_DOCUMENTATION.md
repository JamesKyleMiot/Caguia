# Password Recovery System Documentation

## Overview
The Caguioa Bank Password Recovery System provides a secure, admin-approved process for users who forget their passwords to regain access to their accounts.

## Feature Flow

### User Perspective: Forgot Password Process

1. **Access Forgot Password**
   - User clicks "Forgot Password?" button on the login screen (SignInUsers)
   - A dialog opens requesting contact information

2. **Submit Reset Request**
   - User provides either:
     - Email address, OR
     - Phone number (or both)
   - Submit button sends request to database
   - User receives confirmation message

3. **Wait for Admin Approval**
   - User must wait for admin to review and approve the request
   - Admin typically approves within 24 hours
   - No automatic approval - all requests require manual admin review

4. **Receive Approval**
   - Once admin approves, an approval record is created with 1-hour expiration
   - User needs to attempt password change within 1 hour
   - After 1 hour, request expires and user must submit a new request

5. **Change Password**
   - User logs back in with their username
   - If they have an approved request, they can click "Change Password"
   - Enter new password and confirm it
   - New password is hashed and stored securely
   - Request is marked as completed

### Admin Perspective: Manage Password Reset Requests

1. **Access Management Panel**
   - Click "🔑 Password Resets" button in Admin Dashboard header
   - Opens "Manage Password Reset Requests" dialog

2. **Review Pending Requests**
   - Table displays all pending password reset requests
   - Shows: User ID, Username, Full Name, Email, Phone, Request Date, Status
   - Requests sorted by date (newest first)

3. **Approve Request**
   - Select a request from the table
   - Click "Approve Request" button
   - Confirm the approval
   - User gets 1 hour to change their password
   - Request status changes to "approved"

4. **Deny Request**
   - Select a request from the table
   - Click "Deny Request" button
   - Optionally enter reason for denial (e.g., "Account under investigation")
   - Request status changes to "denied"
   - User receives notification

5. **Refresh**
   - Click "Refresh" to reload pending requests
   - Shows updated list of pending/processed requests

## Database Schema

### password_reset_requests Table
```sql
CREATE TABLE password_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(20),
  status VARCHAR(20) DEFAULT 'pending',  -- pending, approved, denied, completed
  request_reason VARCHAR(255),
  admin_id INT,
  admin_response VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL
) ENGINE=InnoDB;
```

**Fields:**
- `id`: Unique identifier for the request
- `user_id`: ID of the user requesting password reset
- `email`: Email address provided by user (if provided)
- `phone`: Phone number provided by user (if provided)
- `status`: Current status of the request
- `request_reason`: Optional reason from user for password reset
- `admin_id`: ID of admin who approved/denied the request
- `admin_response`: Admin's response message (for denials)
- `created_at`: When the request was submitted
- `reviewed_at`: When admin reviewed the request
- `expires_at`: When the approval expires (1 hour after approval)

## Classes and Components

### 1. PasswordResetManager.java
**Business Logic Manager**
- `submitPasswordResetRequest()` - User submits password reset request
- `getPendingResetRequests()` - Admin retrieves pending requests
- `approveRequest()` - Admin approves a request (sets 1-hour expiration)
- `denyRequest()` - Admin denies a request
- `updatePassword()` - Update user password after approval
- `verifyUserEmail()` - Verify email belongs to user
- `getApprovedRequestId()` - Check if user has active approved request
- `hasPendingRequest()` - Check if user has pending request

### 2. ForgotPassword.java
**User Dialog for Requesting Password Reset**
- Displays form for email/phone input
- Validates input before submission
- Shows success/error messages
- Submits request to database via PasswordResetManager

### 3. ChangePassword.java
**User Dialog for Changing Password After Approval**
- Only accessible to users with approved requests
- Validates password requirements (min 6 characters)
- Confirms password match
- Updates password via PasswordResetManager
- Marks request as completed

### 4. PasswordResetRequestDialog.java
**Admin Management Interface**
- Displays table of pending password reset requests
- Approve button - grants permission for 1 hour
- Deny button - rejects request with optional reason
- Refresh button - reload pending requests
- Shows pending request count

### 5. SignInUsers.java (Modified)
**User Login Screen**
- Added "Forgot Password?" button below login buttons
- Opens ForgotPassword dialog when clicked
- Allows users to initiate password reset process

### 6. AdminDashboard.java (Modified)
**Admin Dashboard**
- Added "🔑 Password Resets" button in header
- Allows admin quick access to manage password reset requests
- Integrated with existing admin workflow

## Security Features

1. **Email/Phone Verification**: Users must provide contact information for verification
2. **Admin Approval**: No automatic password resets - all require human admin review
3. **Time Expiration**: Approved requests expire after 1 hour for security
4. **Password Hashing**: New passwords are hashed using SecurityUtil before storing
5. **Audit Trail**: All requests logged with timestamps and admin actions
6. **Status Tracking**: Complete history of request lifecycle

## User Experience Flow

```
User Forgets Password
         ↓
Clicks "Forgot Password?"
         ↓
Enters Email/Phone
         ↓
Submits Request
         ↓
"Thank you! Waiting for admin approval"
         ↓
[Admin receives request]
         ↓
[Admin reviews and approves]
         ↓
User sees approval (1 hour window)
         ↓
User clicks "Change Password"
         ↓
User enters new password
         ↓
Password updated successfully
         ↓
User can now login with new password
```

## Installation Steps

1. **Add Database Table**
   ```sql
   -- Run password_reset_migration.sql on your lawbank database
   source password_reset_migration.sql;
   ```

2. **Files to Add to Project**
   - PasswordResetManager.java
   - ForgotPassword.java
   - ChangePassword.java
   - PasswordResetRequestDialog.java

3. **Modified Files**
   - SignInUsers.java (added Forgot Password button)
   - AdminDashboard.java (added Password Resets button)

## Testing Checklist

- [ ] User can click "Forgot Password?" on login screen
- [ ] ForgotPassword dialog appears with email/phone fields
- [ ] Validation rejects empty submissions
- [ ] Validation rejects invalid email format
- [ ] Request successfully stored in database
- [ ] Admin can see pending requests in dashboard
- [ ] Admin can approve a request
- [ ] Admin can deny a request with reason
- [ ] User with approved request can change password
- [ ] New password is verified after change
- [ ] Old password no longer works after change
- [ ] Request expires after 1 hour if not used
- [ ] Cannot reuse same password reset request twice

## Troubleshooting

**Issue: "No pending requests shown"**
- Check database connection
- Verify password_reset_requests table exists
- Run password_reset_migration.sql if table doesn't exist

**Issue: "User cannot change password after approval"**
- Check if approval hasn't expired (1 hour window)
- Verify request status is "approved" in database
- Check user ID in request matches logged-in user

**Issue: "Admin buttons not visible"**
- Ensure AdminDashboard.java is properly modified
- Check that PasswordResetRequestDialog.java is in classpath
- Recompile project

## Future Enhancements

1. Email notifications when:
   - Request is submitted
   - Request is approved/denied
   - Password is successfully changed

2. SMS notifications for phone-based requests

3. Security questions as additional verification

4. IP address logging for suspicious activity detection

5. Failed attempt tracking and lockout after N failures

6. Admin dashboard statistics on password reset trends

7. Bulk password reset approval for multiple users

## Support

For issues or questions:
1. Check database logs for errors
2. Review System.out.println() console output
3. Verify all database tables exist with correct schema
4. Ensure all Java classes are properly compiled
5. Check user permissions in database

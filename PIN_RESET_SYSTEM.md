# PIN Reset Request System - Implementation Guide

## Overview
A user-friendly PIN reset request system has been implemented to allow users who forget their PIN to submit requests for admin approval before resetting their PIN.

## Features

### User Features
1. **Forgot PIN Option** - Users can click "Forgot PIN?" button on the login screen
2. **Email Verification** - Users must provide their email address with the request
3. **Pending Request Status** - Users cannot submit duplicate requests while one is pending
4. **Request Confirmation** - Users receive confirmation when their request is submitted

### Admin Features
1. **PIN Reset Management** - Admin can access "🔐 PIN Reset Requests" button in AdminDashboard
2. **View Pending Requests** - Display all pending PIN reset requests in a table
3. **Approve Requests** - Admin can approve requests, allowing users to set new PIN (1-hour expiration)
4. **Deny Requests** - Admin can deny requests with a reason
5. **Track Requests** - View user info (username, fullname, email) and request status
6. **Request History** - See creation date and admin response for each request

## Database Changes

### New Field in Users Table
```sql
ALTER TABLE users ADD COLUMN pin VARCHAR(255);
```

### New Table: pin_reset_requests
```sql
CREATE TABLE pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
  admin_id INT,
  admin_response VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## Files Created/Modified

### New Files
1. **PINResetManager.java** - Manager class for PIN reset requests
   - `submitPINResetRequest()` - Submit a new request
   - `getPendingResetRequests()` - Get all pending requests
   - `approveRequest()` - Approve a request
   - `denyRequest()` - Deny a request
   - `updateUserPIN()` - Update user's PIN
   - `markAsCompleted()` - Mark request as completed

2. **ForgotPIN.java** - Dialog for users to request PIN reset
   - Email validation
   - Duplicate request checking
   - Request submission

3. **PINResetRequestDialog.java** - Admin interface for managing requests
   - View pending requests in table
   - Approve/deny requests
   - Real-time request count

### Modified Files
1. **SignInUsers.java**
   - Added "Forgot PIN?" button next to "Forgot Password?" button
   - Added `ForgotPINBtnActionPerformed()` method
   - Prompts user to enter username before showing PIN reset dialog

2. **AdminDashboard.java**
   - Added "🔐 PIN Reset Requests" button in header
   - Added `openPINResetRequestsDialog()` method
   - Button opens management dialog with pending requests

3. **database_schema.sql**
   - Added PIN field to users table
   - Added pin_reset_requests table
   - Added password_reset_requests table (if not exists)

## Workflow

### User Workflow
1. User clicks "Forgot PIN?" button on login screen
2. System asks for username (if not already entered)
3. User enters their email address
4. System checks for duplicate pending requests
5. If valid, request is submitted with status "pending"
6. User receives confirmation message
7. User waits for admin approval (max 1 hour validity)
8. Once approved, user can set new PIN

### Admin Workflow
1. Admin opens AdminDashboard
2. Admin clicks "🔐 PIN Reset Requests" button
3. Dialog shows all pending PIN reset requests
4. Admin can:
   - **Approve**: Click "✓ Approve Selected" to approve request
   - **Deny**: Click "✗ Deny Selected" and enter reason
5. Dialog shows pending request count
6. Admin can refresh to see updates

## Security Considerations

1. **Email Verification** - Users must provide email address
2. **Admin Approval** - All PIN resets require admin approval
3. **Request Expiration** - Approved requests expire after 1 hour
4. **Status Tracking** - All request actions are logged with admin ID and timestamp
5. **Prevention of Abuse** - Users cannot submit multiple pending requests

## Implementation Notes

- Session.adminId should be set during admin login
- PIN field in users table stores hashed PIN (encrypted password)
- Admin response field can store reason for denial
- All timestamps are automatically managed by database
- The system prevents concurrent/duplicate PIN reset requests

## Testing Checklist

- [ ] User can click "Forgot PIN?" button on login
- [ ] System validates email format
- [ ] System prevents duplicate pending requests
- [ ] Admin sees pending requests in management dialog
- [ ] Admin can approve requests successfully
- [ ] Admin can deny requests with reason
- [ ] Approved requests expire after 1 hour
- [ ] Request count displays correctly
- [ ] Database tables created successfully
- [ ] PIN field added to users table

## Next Steps

1. Run database schema to create new tables and fields
2. Test the PIN reset flow as user
3. Test the PIN reset management as admin
4. Verify email validation works correctly
5. Test request expiration logic
6. Implement email notification to admin when new request is submitted
7. Implement in-app notification to user when request is approved/denied

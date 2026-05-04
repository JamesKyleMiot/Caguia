# Password Recovery System - Quick Setup Guide

## What Was Implemented

A complete password recovery system that allows users who forgot their passwords to securely regain access through an admin-approval process.

## Components Created

### New Java Classes
1. **PasswordResetManager.java** - Core business logic for password reset operations
2. **ForgotPassword.java** - Dialog for users to request password reset
3. **ChangePassword.java** - Dialog for users to change password after approval
4. **PasswordResetRequestDialog.java** - Admin interface to manage reset requests

### Modified Files
1. **SignInUsers.java** - Added "Forgot Password?" button on login screen
2. **AdminDashboard.java** - Added "🔑 Password Resets" management button

### Database
1. **password_reset_migration.sql** - SQL file to create password_reset_requests table

### Documentation
1. **PASSWORD_RECOVERY_DOCUMENTATION.md** - Complete feature documentation

## Installation

### Step 1: Add Database Table
```bash
# Connect to your MySQL database (lawbank)
mysql -u root -p lawbank < password_reset_migration.sql
```

Or run directly in MySQL:
```sql
CREATE TABLE IF NOT EXISTS password_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(20),
  status VARCHAR(20) DEFAULT 'pending',
  request_reason VARCHAR(255),
  admin_id INT,
  admin_response VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX IF NOT EXISTS idx_reset_userid ON password_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_status ON password_reset_requests(status);
```

### Step 2: Add Files to Project
Copy these files to `src/caguioa/bank/`:
- PasswordResetManager.java
- ForgotPassword.java
- ChangePassword.java
- PasswordResetRequestDialog.java

The following files have already been modified:
- SignInUsers.java (Forgot Password button added)
- AdminDashboard.java (Password Resets management button added)

### Step 3: Compile Project
```bash
cd "NetBeansProjects/Caguioa Bank"
ant clean
ant build
```

## Usage

### For Users

1. **Forgot Password?**
   - Click "Forgot Password?" button on login screen
   - Enter email address and/or phone number
   - Click "Submit Request"
   - Wait for admin approval (typically within 24 hours)

2. **Change Password** (After Approval)
   - Once admin approves your request
   - Click "Change Password" option
   - Enter new password (min 6 characters)
   - Confirm password
   - Password is changed and you can now login

### For Admins

1. **Manage Password Reset Requests**
   - In Admin Dashboard, click "🔑 Password Resets" button
   - View table of pending requests
   - Select a request
   - Click "Approve Request" to grant user permission (1 hour window)
   - Click "Deny Request" to reject request

2. **Approval Details**
   - When approved, user has 1 hour to set new password
   - After 1 hour, request expires and user must submit new request
   - Admin can optionally provide reason for denial

## Key Features

✅ **Security**
- Admin-approved password resets (not automatic)
- 1-hour expiration on approvals
- Password hashing with SecurityUtil
- Audit trail of all requests

✅ **User-Friendly**
- Simple dialog interface
- Email or phone number submission
- Clear status messages
- Automatic validation

✅ **Admin Control**
- Complete visibility into all reset requests
- Approve or deny with optional reasons
- Timestamp tracking of all actions
- User contact information for verification

✅ **Audit & Compliance**
- Complete request history stored
- Admin approval tracking
- Timestamp logging
- Database audit trail

## Database Query Examples

### View All Pending Requests
```sql
SELECT * FROM password_reset_requests 
WHERE status = 'pending' 
ORDER BY created_at DESC;
```

### View Request History for User
```sql
SELECT * FROM password_reset_requests 
WHERE user_id = 1 
ORDER BY created_at DESC;
```

### View Admin Actions
```sql
SELECT * FROM password_reset_requests 
WHERE admin_id IS NOT NULL 
AND status IN ('approved', 'denied');
```

### Check Expired Approvals
```sql
SELECT * FROM password_reset_requests 
WHERE status = 'approved' 
AND expires_at < NOW();
```

## Testing Scenarios

### Scenario 1: Normal Flow
1. User clicks "Forgot Password?"
2. User enters email
3. User submits request
4. Admin sees request in dashboard
5. Admin approves request
6. User changes password successfully
7. User logs in with new password

### Scenario 2: Admin Denial
1. User submits password reset request
2. Admin reviews and denies request
3. User cannot change password
4. User must submit new request

### Scenario 3: Request Expiration
1. User submits request and gets approved
2. User waits more than 1 hour
3. User tries to change password
4. System shows request expired
5. User must submit new request

### Scenario 4: Invalid Input
1. User leaves email/phone empty → Rejected
2. User enters invalid email format → Rejected
3. User enters mismatched passwords → Rejected
4. User enters password < 6 chars → Rejected

## Troubleshooting

### "Forgot Password button not showing"
- Check SignInUsers.java has ForgotPasswordBtn added
- Recompile project
- Check for errors in IDE console

### "Password Reset button not showing in Admin Dashboard"
- Verify AdminDashboard.java modifications
- Check PasswordResetRequestDialog.java is in classpath
- Recompile project

### "Database table not found"
- Run password_reset_migration.sql
- Verify database name is 'lawbank'
- Check MySQL user has CREATE TABLE privileges

### "Admin cannot approve requests"
- Verify admin is logged in with Session.isAdmin = true
- Check database connection
- Verify admin exists in admin table

## Performance Notes

- password_reset_requests table has indexes on user_id and status for fast queries
- Expired requests can be archived periodically for performance
- Consider cleaning up completed/denied requests older than 30 days

## Next Steps

1. ✅ Add database table (password_reset_migration.sql)
2. ✅ Copy new Java classes to project
3. ✅ Compile and test each component
4. ✅ Test complete user flow
5. ✅ Test admin approval/denial workflow
6. ✅ Monitor logs for any errors
7. Optional: Add email notification system
8. Optional: Add SMS notification for phone-based requests

## Files Summary

| File | Type | Purpose |
|------|------|---------|
| PasswordResetManager.java | Class | Business logic manager |
| ForgotPassword.java | Dialog | User request interface |
| ChangePassword.java | Dialog | User password change interface |
| PasswordResetRequestDialog.java | Dialog | Admin management interface |
| password_reset_migration.sql | SQL | Database schema |
| SignInUsers.java | Modified | Added Forgot Password button |
| AdminDashboard.java | Modified | Added Password Resets button |
| PASSWORD_RECOVERY_DOCUMENTATION.md | Doc | Complete documentation |
| PASSWORD_RECOVERY_SETUP.md | Doc | This file |

## Support & Maintenance

- Check application logs for errors
- Monitor password_reset_requests table growth
- Archive old completed requests periodically
- Review admin actions for security compliance
- Update documentation as features evolve

---

**Implementation Date**: May 2026  
**Version**: 1.0  
**Status**: Ready for Testing

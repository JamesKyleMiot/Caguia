# Forgot PIN with OTP Email Functionality - Complete Documentation

## Overview
The Forgot PIN feature allows users to request a PIN reset. Once an admin approves the request, an OTP (One-Time Password) is generated and sent to the user's registered email. The user enters this OTP to verify their identity before setting a new PIN.

## System Workflow

### User Flow
1. **User clicks "Forgot Pin?" on Login Screen** → `SignInUsers.java`
   - User enters username
   - "Forgot Pin?" label is clickable and opens ForgotPIN dialog

2. **ForgotPIN Dialog** → `ForgotPIN.java`
   - User enters their email address
   - Validates email format
   - Submits PIN reset request
   - Request status: `pending`

3. **Admin Reviews Request** → `AdminDashboard.java`
   - Admin sees pending PIN reset requests
   - Can approve or deny request
   - If approved:
     - System generates 6-digit OTP
     - OTP sent to user's email
     - Request status: `approved`

4. **User Verifies OTP** → `PINResetOTPDialog.java`
   - User receives OTP in email
   - Opens application
   - Enters OTP (6 digits)
   - OTP validity: 10 minutes
   - If valid: `otp_verified = TRUE`

5. **User Sets New PIN** → `ResetPINDialog.java`
   - After OTP verification
   - User enters new PIN (6 digits)
   - Confirms PIN
   - PIN is hashed using SHA-256
   - Saved to database
   - Request status: `completed`

## Database Schema Updates

### New Columns in `pin_reset_requests` table:
```sql
otp VARCHAR(10)                    -- 6-digit OTP code
otp_generated_at TIMESTAMP NULL    -- Time OTP was generated
otp_verified BOOLEAN DEFAULT FALSE -- Whether OTP was verified by user
```

### Full table structure:
```sql
CREATE TABLE pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',  -- pending, approved, denied, completed
  admin_id INT,
  admin_response VARCHAR(255),
  otp VARCHAR(10),
  otp_generated_at TIMESTAMP NULL,
  otp_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## New Classes Created

### 1. **OTPGenerator.java**
- Generates 6-digit random OTP
- Validates OTP entered by user
- Checks OTP expiration (10 minutes)
- Methods:
  - `generateOTP()` - Creates random 6-digit OTP
  - `validateOTP(generatedOTP, enteredOTP)` - Compares OTPs
  - `isOTPExpired(generatedTime)` - Checks if OTP expired
  - `getOTPValidityMinutes()` - Returns 10 minutes

### 2. **PINResetOTPDialog.java**
- Dialog for OTP verification
- Displays OTP entry field
- Shows remaining time (10 minutes)
- Methods:
  - `verifyOTP()` - Validates entered OTP
  - `isOTPVerified()` - Returns verification status
- Features:
  - Real-time validation
  - Expiration timer display
  - Clear error messages

## Updated Classes

### 1. **EmailNotifier.java** (Extended)
New methods for PIN reset notifications:
- `sendPINResetOTP(recipientEmail, userName, otp)`
  - Sends OTP to user's email
  - Includes 10-minute expiration warning
  
- `sendPINResetApprovalNotification(recipientEmail, userName)`
  - Notifies user that request was approved
  - Prompts them to check for OTP email
  
- `sendPINResetDenialNotification(recipientEmail, userName, reason)`
  - Notifies user that request was denied
  - Includes reason for denial

### 2. **PINResetManager.java** (Extended)
New methods for OTP management:
- `generateAndSendOTP(requestId, userName, email)`
  - Generates OTP and stores in database
  - Sends OTP via email
  - Returns OTP string
  
- `verifyOTP(requestId, enteredOTP)`
  - Validates OTP format and value
  - Checks expiration (10 minutes)
  - Marks OTP as verified if valid
  
- `markOTPVerified(requestId)` (private)
  - Updates database to mark OTP as verified
  
- `isOTPVerified(requestId)`
  - Checks if OTP has been verified
  
- `getRequestDetails(requestId)`
  - Returns request information for OTP verification

### 3. **SignInUsers.java** (Updated)
Changes to Login Screen:
- "Forgot Pin?" label is now clickable
- Added mouse listener with hand cursor
- Shows underline on hover
- Calls ForgotPINBtnActionPerformed()
- Already has method: `ForgotPINBtnActionPerformed(ActionEvent evt)`

## Admin Dashboard Integration

The Admin Dashboard needs to handle PIN reset requests:

### PIN Reset Management Features:
1. **View Pending Requests**
   - Table showing all pending PIN reset requests
   - Columns: Username, Email, Requested Date, Status

2. **Approve Request**
   - Admin clicks "Approve" button
   - System generates OTP
   - OTP sent to user's email
   - Request status changes to "approved"
   - Email sent to user

3. **Deny Request**
   - Admin clicks "Deny" button
   - Admin enters reason for denial
   - Email notification sent to user
   - Request status changes to "denied"

### Database Queries for Admin Dashboard:

**Get all pending PIN reset requests:**
```sql
SELECT p.id, p.user_id, u.username, u.fullname, u.email, p.email, 
       p.status, p.created_at, p.reviewed_at
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE p.status = 'pending'
ORDER BY p.created_at DESC;
```

**Get approved requests with OTP:**
```sql
SELECT p.id, p.user_id, u.username, p.email, p.otp, p.otp_generated_at,
       p.otp_verified, p.expires_at
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE p.status = 'approved' AND p.expires_at > NOW()
ORDER BY p.otp_generated_at DESC;
```

## Implementation Steps

1. **Run Migration Script**
   - Execute `OTP_PIN_RESET_MIGRATION.sql`
   - Adds OTP columns to database

2. **Compile New Classes**
   - OTPGenerator.java
   - PINResetOTPDialog.java
   - Updated: EmailNotifier.java
   - Updated: PINResetManager.java
   - Updated: SignInUsers.java

3. **Update Admin Dashboard**
   - Add PIN reset request management panel
   - Add approve/deny buttons
   - Display pending requests in table

4. **Test Workflow**
   - Test user PIN reset request submission
   - Test admin approval and OTP generation
   - Test OTP email delivery
   - Test OTP verification
   - Test new PIN setting

## OTP Features

### Security Features:
- 6-digit random OTP
- Time-limited validity (10 minutes)
- One-time use only
- Cannot be reused
- Email delivery confirmation

### Error Handling:
- Invalid OTP format
- Expired OTP
- OTP mismatch
- User not found
- Email not found

### User-Friendly Features:
- Clear error messages
- Timer showing expiration
- Resend OTP option (admin can re-generate)
- Understandable instructions
- Multiple language support ready

## Configuration

### Email Settings (in EmailNotifier.java):
```java
SENDER_EMAIL = "your_email@gmail.com"
SENDER_PASSWORD = "your_app_password"  // Gmail App Password
```

### OTP Settings (in OTPGenerator.java):
- OTP Length: 6 digits
- Validity: 10 minutes
- Format: Digits only (0-9)

## API Reference

### OTPGenerator Methods:
```java
public static String generateOTP()
public static boolean validateOTP(String generatedOTP, String enteredOTP)
public static boolean isOTPExpired(LocalDateTime generatedTime)
public static int getOTPValidityMinutes()
```

### PINResetManager Methods:
```java
public static String generateAndSendOTP(int requestId, String userName, String email)
public static boolean verifyOTP(int requestId, String enteredOTP)
public static boolean isOTPVerified(int requestId)
public static ResultSet getRequestDetails(int requestId)
```

### EmailNotifier Methods:
```java
public static boolean sendPINResetOTP(String recipientEmail, String userName, String otp)
public static boolean sendPINResetApprovalNotification(String recipientEmail, String userName)
public static boolean sendPINResetDenialNotification(String recipientEmail, String userName, String reason)
```

## Troubleshooting

### OTP Not Received by User:
1. Check email address in pin_reset_requests table
2. Verify SMTP credentials in EmailNotifier
3. Check email spam folder
4. Verify mail library is installed (javax.mail)

### OTP Verification Failing:
1. Check if OTP_VALIDITY_MINUTES (10) has passed
2. Verify OTP digits are correct
3. Check otp_verified flag in database
4. Ensure requestId is correct

### PIN Reset Incomplete:
1. Check if otp_verified = TRUE
2. Verify new PIN meets requirements (6 digits)
3. Check if ResetPINDialog opens correctly
4. Verify SecurityUtil.hashPin() works

## Future Enhancements

1. **Multi-attempt OTP Lock**
   - Lock after 3 failed attempts
   - 15-minute cooldown

2. **OTP Resend**
   - Allow users to request new OTP
   - Admin can regenerate OTP

3. **SMS OTP Option**
   - Send OTP via SMS instead of email
   - Fallback mechanism

4. **OTP Templates**
   - Customizable email template
   - Multiple language support

5. **Audit Logging**
   - Log all PIN reset activities
   - Track admin actions
   - Compliance reporting

## Support & Contact

For issues or questions regarding the PIN Reset OTP system:
- Check error messages in console
- Review database tables for status
- Contact admin for access issues
- Verify email configuration

---
**Last Updated:** May 4, 2026
**Version:** 1.0
**System:** Caguioa Bank Management System

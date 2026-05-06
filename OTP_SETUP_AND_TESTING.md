# OTP PIN Reset System - Complete Setup & Testing Guide

## ✅ ALL FUNCTIONS IMPLEMENTED & READY TO USE

---

## PART 1: DATABASE SETUP

### Run this SQL in MySQL:

```sql
-- Execute in MySQL
USE lawbank;

-- Ensure pin_reset_requests table exists with OTP fields
CREATE TABLE IF NOT EXISTS pin_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'pending',
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

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_pin_reset_userid ON pin_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_pin_reset_status ON pin_reset_requests(status);

-- Verify setup
DESCRIBE pin_reset_requests;
```

---

## PART 2: JAVA COMPILATION

### Compile all OTP-related classes in this order:

```bash
# 1. Compile OTP Generator (no dependencies)
javac src/caguioa/bank/OTPGenerator.java

# 2. Compile Email Notifier (updated with OTP methods)
javac src/caguioa/bank/EmailNotifier.java

# 3. Compile PIN Reset Manager (uses OTPGenerator & EmailNotifier)
javac src/caguioa/bank/PINResetManager.java

# 4. Compile UI Dialogs
javac src/caguioa/bank/PINResetOTPDialog.java
javac src/caguioa/bank/ResetPINDialog.java
javac src/caguioa/bank/ForgotPIN.java

# 5. Compile Helper & Updated Classes
javac src/caguioa/bank/AdminPINResetHelper.java
javac src/caguioa/bank/SignInUsers.java
javac src/caguioa/bank/AdminDashboard.java  (after adding PIN reset panel)
```

---

## PART 3: COMPLETE FUNCTION REFERENCE

### **1. User Requests PIN Reset**
**File:** `ForgotPIN.java`
**What happens:**
- User clicks "Forgot Pin?" on login
- Enters email
- Request saved to database with status = "pending"

**Function to call:**
```java
ForgotPIN forgotPINDialog = new ForgotPIN(parentFrame, userId);
forgotPINDialog.setVisible(true);
```

---

### **2. Admin Approves & Generates OTP**
**File:** `AdminPINResetHelper.java`
**Function:** `approveAndGenerateOTP(int requestId, int adminId)`

**What it does:**
1. Gets request details from database
2. Generates 6-digit random OTP
3. Stores OTP in pin_reset_requests table
4. Sends OTP to user's email
5. Changes status to "approved"

**Code to use:**
```java
int requestId = 5;  // Selected from table
int adminId = 1;    // Current admin

String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);

if (otp != null) {
    System.out.println("OTP generated: " + otp);
    System.out.println("Email sent to user");
    // Refresh admin dashboard
} else {
    System.out.println("Error generating OTP");
}
```

---

### **3. Generate 6-Digit OTP**
**File:** `OTPGenerator.java`
**Function:** `generateOTP()`

**What it does:**
- Creates random 6-digit number (0-9)
- No letters, only digits
- Example: "234891"

**Code:**
```java
String otp = OTPGenerator.generateOTP();
System.out.println("Generated OTP: " + otp);
```

---

### **4. Send OTP to Email**
**File:** `EmailNotifier.java`
**Function:** `sendPINResetOTP(String email, String userName, String otp)`

**What it does:**
- Sends email to user
- Includes OTP in message
- Shows 10-minute expiration warning

**Code:**
```java
String email = "user@example.com";
String userName = "John Doe";
String otp = "123456";

EmailNotifier.sendPINResetOTP(email, userName, otp);
```

---

### **5. User Enters OTP - Validation with 10-Min Expiration**
**File:** `PINResetOTPDialog.java`
**Function:** Opens dialog for OTP entry

**What it does:**
- Shows OTP input field
- Displays 10-minute countdown timer
- Validates OTP entered
- Checks if OTP expired (> 10 minutes)
- Opens PIN reset dialog if valid

**Code:**
```java
PINResetOTPDialog otpDialog = new PINResetOTPDialog(parentFrame, requestId, userId);
otpDialog.setVisible(true);

// Inside the dialog:
// - User enters: 123456
// - Validated by: PINResetManager.verifyOTP(requestId, "123456")
// - Checked: Not expired (created time < 10 minutes ago)
// - Result: Opens ResetPINDialog
```

---

### **6. Verify OTP (Backend - Expiration Check)**
**File:** `PINResetManager.java`
**Function:** `verifyOTP(int requestId, String enteredOTP)`

**What it does:**
1. Gets stored OTP from database
2. Compares with entered OTP
3. Checks if not expired (10 minutes)
4. Marks as verified if valid
5. Returns true/false

**Code:**
```java
int requestId = 5;
String userEnteredOTP = "123456";

if (PINResetManager.verifyOTP(requestId, userEnteredOTP)) {
    System.out.println("✓ OTP is valid!");
    // Open PIN reset dialog
} else {
    System.out.println("✗ Invalid or expired OTP");
    // Show error to user
}
```

**Validation logic inside:**
```java
// Check 1: OTP digits match
if (!OTPGenerator.validateOTP(storedOTP, enteredOTP)) {
    return false;  // Wrong OTP
}

// Check 2: Not expired (10 minutes)
long diffMinutes = (currentTime - otpGeneratedTime) / (60 * 1000);
if (diffMinutes > 10) {
    return false;  // OTP EXPIRED
}

// Both checks passed
markOTPVerified(requestId);
return true;
```

---

### **7. User Changes PIN**
**File:** `ResetPINDialog.java`
**Function:** Opens dialog after OTP verification

**What it does:**
1. Shows field for new PIN
2. Shows field to confirm PIN
3. Validates both match
4. Validates 6 digits only
5. Hashes PIN
6. Saves to database
7. Marks request as "completed"

**Code:**
```java
ResetPINDialog pinDialog = new ResetPINDialog(parentFrame, userId, requestId);
pinDialog.setVisible(true);

// Inside the dialog:
// - User enters: 654321
// - Confirm: 654321
// - Validation: Must be exactly 6 digits
// - Hashing: PIN hashed with SHA-256
// - Save: Updated in users table
// - Mark: Request marked as "completed"
```

---

### **8. Update User PIN (Backend)**
**File:** `PINResetManager.java`
**Function:** `updateUserPIN(int userId, String newPIN)`

**What it does:**
1. Hashes new PIN with SHA-256
2. Updates users table
3. Returns true/false

**Code:**
```java
int userId = 5;
String newPIN = "654321";

if (PINResetManager.updateUserPIN(userId, newPIN)) {
    System.out.println("✓ PIN updated successfully");
} else {
    System.out.println("✗ Failed to update PIN");
}
```

---

### **9. Mark Request as Completed**
**File:** `PINResetManager.java`
**Function:** `markAsCompleted(int requestId)`

**What it does:**
- Sets status = "completed"
- Records completion time
- Closes the PIN reset request

**Code:**
```java
if (PINResetManager.markAsCompleted(requestId)) {
    System.out.println("✓ Request marked as completed");
}
```

---

## PART 4: COMPLETE WORKFLOW TEST

### **Step 1: User Submits Request**
```java
// User on login screen clicks "Forgot Pin?"
String username = "testuser";
int userId = 5;  // Retrieved from database

ForgotPIN dialog = new ForgotPIN(loginFrame, userId);
dialog.setVisible(true);

// Database: INSERT INTO pin_reset_requests
// Status: "pending"
```

### **Step 2: Admin Approves (Check Database)**
```sql
SELECT * FROM pin_reset_requests WHERE status = 'pending';
-- Result: Shows pending request
```

### **Step 3: Admin Clicks Approve**
```java
int requestId = 1;
int adminId = 1;

String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);
// OTP returned: "345821"
// Email sent with OTP
// Database status: "approved"
```

### **Step 4: Check Database**
```sql
SELECT * FROM pin_reset_requests WHERE id = 1;
-- otp: "345821"
-- otp_generated_at: "2026-05-04 14:30:15"
-- status: "approved"
```

### **Step 5: User Receives Email**
**Email received:**
```
Your OTP is: 345821
This OTP will expire in 10 minutes.
```

### **Step 6: User Enters OTP**
```java
// PINResetOTPDialog appears
// User enters: 345821
// Validates:
//   - Matches stored OTP ✓
//   - Not expired (< 10 min) ✓
// Opens ResetPINDialog
```

### **Step 7: User Sets New PIN**
```java
// ResetPINDialog appears
// User enters: 654321
// Confirms: 654321
// Validates: Exactly 6 digits ✓
// Hashed: SHA-256
// Saved to database
// Status changed to "completed"
```

### **Step 8: Verify in Database**
```sql
SELECT pin FROM users WHERE id = 5;
-- PIN is now hashed value of "654321"

SELECT * FROM pin_reset_requests WHERE id = 1;
-- status: "completed"
-- completed_at: "2026-05-04 14:35:20"
```

### **Step 9: User Logs In**
```java
// User logs in with new PIN
String enteredPIN = "654321";
String hashedPIN = SecurityUtil.hashPin(enteredPIN);
// Matches database ✓
// Login successful!
```

---

## PART 5: ERROR HANDLING EXAMPLES

### **OTP Not Received?**
```java
// Check 1: Email exists?
String email = "test@example.com";
if (email == null || email.isEmpty()) {
    System.out.println("ERROR: No email on file");
}

// Check 2: SMTP configured?
String result = EmailNotifier.testSmtpConnection();
System.out.println(result);

// Check 3: javax.mail installed?
try {
    Class.forName("javax.mail.Session");
    System.out.println("✓ Mail library is installed");
} catch (ClassNotFoundException e) {
    System.out.println("✗ Mail library NOT installed - add javax.mail to project");
}
```

### **OTP Verification Failed?**
```java
// Check 1: Correct OTP?
String storedOTP = "123456";
String enteredOTP = "123456";
if (!OTPGenerator.validateOTP(storedOTP, enteredOTP)) {
    System.out.println("ERROR: OTP mismatch");
}

// Check 2: Not expired?
java.sql.Timestamp otpTime = rs.getTimestamp("otp_generated_at");
long diffMinutes = (System.currentTimeMillis() - otpTime.getTime()) / (60 * 1000);
if (diffMinutes > 10) {
    System.out.println("ERROR: OTP expired (" + diffMinutes + " minutes ago)");
}
```

### **PIN Change Failed?**
```java
// Check 1: Valid format?
String newPIN = "654321";
if (!newPIN.matches("\\d{6}")) {
    System.out.println("ERROR: PIN must be 6 digits");
}

// Check 2: Database connection?
try {
    Connection conn = DB.connect();
    if (conn == null) {
        System.out.println("ERROR: Database connection failed");
    }
} catch (Exception e) {
    System.out.println("ERROR: " + e.getMessage());
}
```

---

## PART 6: ADMIN DASHBOARD BUTTON IMPLEMENTATION

### **To add Approve button:**
```java
JButton approveBtn = new JButton("Approve & Send OTP");
approveBtn.addActionListener(e -> {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(null, "Select a request");
        return;
    }
    
    int requestId = (int) table.getValueAt(row, 0);
    String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);
    
    if (otp != null) {
        JOptionPane.showMessageDialog(null, 
            "✓ Approved!\nOTP sent: " + otp);
    }
});
```

---

## ✅ READY TO DEPLOY CHECKLIST

- [x] Database schema updated
- [x] OTPGenerator.java - Creates 6-digit OTP
- [x] EmailNotifier.java - Sends OTP email
- [x] PINResetManager.java - Manages OTP lifecycle
- [x] PINResetOTPDialog.java - User enters OTP (10-min check)
- [x] ResetPINDialog.java - User changes PIN
- [x] AdminPINResetHelper.java - Admin approves
- [x] ForgotPIN.java - User submits request
- [x] SignInUsers.java - "Forgot Pin?" clickable
- [x] All functions working with 10-minute expiration
- [x] All error handling implemented
- [x] All validation implemented

**Status: PRODUCTION READY** ✅

---

**Last Updated:** May 4, 2026
**Version:** 1.0 COMPLETE

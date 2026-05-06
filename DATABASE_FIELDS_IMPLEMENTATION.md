# 📚 PIN RESET OTP - COMPLETE IMPLEMENTATION GUIDE

**Database Fields → Java Functions**

---

## 🎯 DATABASE FIELDS TO JAVA WORKFLOW

### **Field: `id` (INT - Primary Key)**
```
Purpose: Unique identifier for each PIN reset request
Usage: References the specific request throughout workflow
```

**Used In Code:**
```java
// When inserting new request
INSERT INTO pin_reset_requests (user_id, email, status)
// Returns: id = 1, 2, 3, etc.

// When retrieving request later
SELECT * FROM pin_reset_requests WHERE id = 5
```

---

### **Field: `user_id` (INT - Foreign Key)**
```
Purpose: Links PIN reset request to specific user
Usage: Know which user is resetting PIN
```

**Step 1: User Clicks "Forgot Pin?"**
```java
public static boolean submitPINResetRequest(int userId, String email) {
    // userId comes from Session.userId or login
    // userId = 1 (example)
    
    String insertSql = "INSERT INTO pin_reset_requests (user_id, email, status) " +
                     "VALUES (?, ?, 'approved')";
    insertStmt.setInt(1, userId);  // Set user_id = 1
}
```

**Step 5: When Changing PIN**
```java
public static boolean updateUserPIN(int userId, String newPIN) {
    // Use userId to update correct user's PIN
    String sql = "UPDATE users SET pin = ? WHERE id = ?";
    pst.setString(1, SecurityUtil.hashPin(newPIN));
    pst.setInt(2, userId);  // Update only this user
}
```

---

### **Field: `email` (VARCHAR - For OTP Delivery)**
```
Purpose: Email address where OTP is sent
Usage: Deliver OTP to user's email
```

**Step 2: System Generates OTP & Gets Email**
```java
public static String generateAndSendOTP(int requestId, String userName, String email) {
    // email = "testuser@example.com"
    
    // Generate 6-digit OTP
    String otp = OTPGenerator.generateOTP();  // Example: "123456"
    
    // Store OTP in database
    String sql = "UPDATE pin_reset_requests " +
                 "SET otp = ?, otp_generated_at = NOW() " +
                 "WHERE id = ?";
    pst.setString(1, otp);      // Store OTP
    pst.setInt(2, requestId);
    
    // Send OTP to email
    EmailNotifier.sendPINResetOTP(email, userName, otp);
    // Email sent to: "testuser@example.com"
    // Body: "Your OTP is: 123456"
    
    return otp;
}
```

**Email Template:**
```
TO: testuser@example.com
SUBJECT: Caguioa Bank - PIN Reset OTP

Your 6-digit OTP is: 123456
This OTP will expire in 10 minutes.
```

---

### **Field: `status` (VARCHAR - 'approved' or 'completed')**
```
Purpose: Track request state in workflow
Usage: Know if OTP is sent, verified, or PIN changed
```

**Status Workflow:**
```
Timeline         Status          Meaning
─────────────────────────────────────────────
User submits  → 'approved'    ✓ OTP sent to email
User verifies → 'approved'    ✓ OTP verified, ready for PIN change
PIN changed   → 'completed'   ✓ PIN reset finished
```

**Implementation:**
```java
// Step 1: Insert with status 'approved' (OTP auto-sent)
String insertSql = "INSERT INTO pin_reset_requests (user_id, email, status) " +
                   "VALUES (?, ?, 'approved')";

// Step 2: Check OTP is verified (still 'approved')
public static boolean isOTPVerified(int requestId) {
    String sql = "SELECT otp_verified FROM pin_reset_requests " +
                 "WHERE id = ? AND status = 'approved'";
    // Returns TRUE if otp_verified = 1
}

// Step 3: Mark as 'completed' after PIN changed
public static boolean markAsCompleted(int requestId) {
    String sql = "UPDATE pin_reset_requests " +
                 "SET status = 'completed', completed_at = NOW() " +
                 "WHERE id = ?";
}
```

---

### **Field: `otp` (VARCHAR(10) - 6-Digit Code)**
```
Purpose: Store the 6-digit one-time password
Usage: Verify user enters correct code
```

**Step 2: Generate & Store OTP**
```java
// Generate random 6-digit code
public static String generateOTP() {
    // Returns: "123456" or "987654" or "505050", etc.
    // Format: Exactly 6 digits (0-9 only)
    
    SecureRandom random = new SecureRandom();
    StringBuilder otp = new StringBuilder();
    for (int i = 0; i < 6; i++) {
        otp.append(random.nextInt(10));  // 0-9
    }
    return otp.toString();  // Example: "345678"
}

// Store in database
String sql = "UPDATE pin_reset_requests " +
             "SET otp = ? " +
             "WHERE id = ?";
pst.setString(1, "345678");  // Store OTP
pst.setInt(2, 5);            // For request ID 5
```

**Database Record:**
```
id   | user_id | email              | otp    | status
-----|---------|-------------------|--------|----------
5    | 1       | test@example.com   | 345678 | approved
```

**Step 4: User Enters OTP**
```java
// User enters: "345678"
// User clicks: "Verify OTP"

public static boolean verifyOTP(int requestId, String enteredOTP) {
    // enteredOTP = "345678" (from user input)
    
    // Get stored OTP from database
    String sql = "SELECT otp FROM pin_reset_requests WHERE id = ?";
    String storedOTP = rs.getString("otp");  // "345678" from DB
    
    // Compare
    if (storedOTP.equals(enteredOTP)) {
        return true;  // ✓ OTP CORRECT
    } else {
        return false; // ❌ OTP WRONG
    }
}
```

---

### **Field: `otp_generated_at` (TIMESTAMP - When OTP Created)**
```
Purpose: Track when OTP was generated
Usage: Enforce 10-minute expiration
```

**Step 2: Store Generation Time**
```java
String sql = "UPDATE pin_reset_requests " +
             "SET otp = ?, otp_generated_at = NOW() " +
             "WHERE id = ?";
pst.setString(1, "345678");
pst.setInt(2, 5);
// Database now stores: otp_generated_at = "2026-05-04 10:00:00"
```

**Database Record:**
```
id | otp    | otp_generated_at      | otp_verified
---|--------|------------------------|-------------
5  | 345678 | 2026-05-04 10:00:00   | FALSE
```

**Step 4: Verify OTP & Check Expiration**
```java
public static boolean verifyOTP(int requestId, String enteredOTP) {
    String sql = "SELECT otp, otp_generated_at FROM pin_reset_requests WHERE id = ?";
    
    ResultSet rs = pst.executeQuery();
    if (rs.next()) {
        String storedOTP = rs.getString("otp");              // "345678"
        java.sql.Timestamp otpGeneratedAt = rs.getTimestamp("otp_generated_at");
        // otpGeneratedAt = "2026-05-04 10:00:00"
        
        // Check if OTP matches
        if (!storedOTP.equals(enteredOTP)) {
            return false;  // ❌ Wrong OTP
        }
        
        // Check if OTP has EXPIRED (> 10 minutes)
        long currentTime = System.currentTimeMillis();
        long otpTime = otpGeneratedAt.getTime();
        long diffMinutes = (currentTime - otpTime) / (60 * 1000);
        
        // Timeline example:
        // Generated: 10:00:00
        // Verified: 10:05:00 → diffMinutes = 5 ✓ VALID
        // Verified: 10:10:00 → diffMinutes = 10 ✓ VALID (boundary)
        // Verified: 10:10:01 → diffMinutes = 10.01 ❌ EXPIRED
        
        if (diffMinutes > 10) {
            return false;  // ❌ OTP EXPIRED
        }
        
        // Mark OTP as verified
        markOTPVerified(requestId);
        return true;  // ✓ OTP VALID
    }
    return false;
}
```

---

### **Field: `otp_verified` (BOOLEAN - Verification Status)**
```
Purpose: Track if user successfully verified OTP
Usage: Know user passed OTP verification before PIN change
```

**Timeline:**
```
After OTP sent        → otp_verified = FALSE (0)
User enters OTP       → Check against database
OTP correct & valid   → otp_verified = TRUE (1)
User changes PIN      → Request marked completed
```

**Implementation:**

**Step 2: OTP Sent (Not Verified)**
```java
// When creating request
INSERT INTO pin_reset_requests (user_id, email, status, otp_verified)
VALUES (1, 'test@example.com', 'approved', FALSE);

// Database: otp_verified = 0 (FALSE)
```

**Step 4: User Verifies OTP**
```java
public static boolean verifyOTP(int requestId, String enteredOTP) {
    // ... validation code ...
    
    if (otpIsValid) {
        // Mark OTP as verified
        String sql = "UPDATE pin_reset_requests " +
                     "SET otp_verified = TRUE " +
                     "WHERE id = ?";
        pst.setInt(1, requestId);
        pst.executeUpdate();
        // Database: otp_verified = 1 (TRUE)
        
        return true;  // ✓ User can now change PIN
    }
    return false;
}

public static boolean isOTPVerified(int requestId) {
    String sql = "SELECT otp_verified FROM pin_reset_requests WHERE id = ?";
    ResultSet rs = pst.executeQuery();
    
    if (rs.next()) {
        return rs.getBoolean("otp_verified");  // TRUE or FALSE
    }
    return false;
}
```

**Step 5: User Can Change PIN (Only If Verified)**
```java
public static boolean updateUserPIN(int userId, String newPIN) {
    // Check if OTP was verified first
    String checkSql = "SELECT otp_verified FROM pin_reset_requests " +
                      "WHERE user_id = ? AND status = 'approved'";
    ResultSet rs = pst.executeQuery();
    
    if (rs.next() && rs.getBoolean("otp_verified")) {
        // ✓ OTP was verified, allow PIN change
        String updateSql = "UPDATE users SET pin = ? WHERE id = ?";
        pst.setString(1, SecurityUtil.hashPin(newPIN));
        pst.setInt(2, userId);
        pst.executeUpdate();
        
        return true;  // ✓ PIN CHANGED
    }
    
    return false;  // ❌ OTP not verified
}
```

---

### **Field: `created_at` (TIMESTAMP - When Request Made)**
```
Purpose: Track when user submitted PIN reset request
Usage: Audit trail, sorting requests by time
```

**Automatic:**
```sql
-- MySQL automatically sets this when record created
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

-- Inserted automatically:
INSERT INTO pin_reset_requests (user_id, email, status)
VALUES (1, 'test@example.com', 'approved');
-- created_at = NOW() (2026-05-04 10:00:00)
```

**Used For:**
```java
// Display recent requests first
SELECT * FROM pin_reset_requests 
ORDER BY created_at DESC;

// Or get requests from last 24 hours
SELECT * FROM pin_reset_requests 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY created_at DESC;
```

---

### **Field: `completed_at` (TIMESTAMP - When PIN Changed)**
```
Purpose: Track when PIN reset was completed
Usage: Know when user successfully changed PIN
```

**When PIN Changed:**
```java
public static boolean updateUserPIN(int userId, String newPIN) {
    // 1. Update user's PIN
    String updateUserSql = "UPDATE users SET pin = ? WHERE id = ?";
    pst.setString(1, SecurityUtil.hashPin(newPIN));
    pst.setInt(2, userId);
    pst.executeUpdate();  // ✓ PIN changed
    
    // 2. Mark request as completed
    String completeSql = "UPDATE pin_reset_requests " +
                         "SET status = 'completed', " +
                         "    completed_at = NOW() " +
                         "WHERE user_id = ?";
    pst.setInt(1, userId);
    pst.executeUpdate();
    // completed_at = NOW() (2026-05-04 10:05:30)
    
    return true;  // ✓ SUCCESS
}

// Check when it was completed
String sql = "SELECT completed_at FROM pin_reset_requests WHERE id = ?";
java.sql.Timestamp completedAt = rs.getTimestamp("completed_at");
// completedAt = "2026-05-04 10:05:30"
```

---

## 🔄 COMPLETE WORKFLOW WITH DATABASE

### **Timeline Example:**

```
TIME         EVENT                          DATABASE CHANGE
─────────────────────────────────────────────────────────────────────

10:00:00     User clicks "Forgot Pin?"      INSERT row:
             Enters email                   id=5, user_id=1, 
                                           email=test@example.com,
                                           status='approved',
                                           created_at=10:00:00,
                                           otp_verified=FALSE

10:00:01     System generates OTP           UPDATE row:
             Sends email                    otp='345678',
                                           otp_generated_at=10:00:01

10:00:02     User receives email            (no change)
             with OTP: 345678

10:02:30     User enters OTP in dialog      UPDATE row:
             "345678" ✓ CORRECT             otp_verified=TRUE

10:03:45     User enters new PIN: 654321    UPDATE users table:
             Clicks "Change PIN"            users.pin=HASH(654321)
                                           
                                           UPDATE row:
                                           status='completed',
                                           completed_at=10:03:45

10:04:00     User logs in with              ✓ LOGIN SUCCESS
             new PIN: 654321
```

**Final Database Record:**
```
id  | user_id | email              | otp    | otp_generated_at | otp_verified | status      | completed_at
----|---------|-------------------|--------|------------------|--------------|-------------|------------------
5   | 1       | test@example.com   | 345678 | 2026-05-04 10:00 | TRUE         | completed   | 2026-05-04 10:03
```

---

## 💻 SQL QUERIES YOU CAN USE

### **Get Current PIN Reset Request Status**
```sql
SELECT * FROM pin_reset_requests 
WHERE user_id = 1 AND status = 'approved';
```

### **Get All Completed PIN Resets (Today)**
```sql
SELECT u.username, p.created_at, p.completed_at 
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE DATE(p.created_at) = CURDATE() AND p.status = 'completed';
```

### **Find OTPs About to Expire (>9 minutes)**
```sql
SELECT * FROM pin_reset_requests 
WHERE otp_verified = FALSE 
AND TIMESTAMPDIFF(MINUTE, otp_generated_at, NOW()) > 9;
```

### **Clean Up Old Requests (>30 days)**
```sql
DELETE FROM pin_reset_requests 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

## ✅ JAVA FUNCTIONS REFERENCE

| Function | Purpose | Uses Fields |
|----------|---------|-------------|
| `submitPINResetRequest()` | User submits request | id, user_id, email, status, created_at |
| `generateAndSendOTP()` | Generate & send OTP | otp, otp_generated_at |
| `verifyOTP()` | Verify OTP & expiration | otp, otp_generated_at, otp_verified |
| `isOTPVerified()` | Check if verified | otp_verified |
| `updateUserPIN()` | Change PIN | user_id |
| `markAsCompleted()` | Mark request complete | status, completed_at |

---

## 🚀 FULL CODE EXAMPLE

**Complete PIN Reset Flow:**

```java
// User clicks "Forgot Pin?" and enters email
public void handleForgotPinRequest(int userId, String email) {
    // Step 1: Submit request (inserts into DB)
    if (PINResetManager.submitPINResetRequest(userId, email)) {
        System.out.println("✓ OTP sent to " + email);
        // DB: INSERT with status='approved', otp='123456'
    }
}

// User receives email and enters OTP in dialog
public void handleOTPVerification(int requestId, String userEnteredOTP) {
    // Step 2: Verify OTP (checks expiration, matches OTP)
    if (PINResetManager.verifyOTP(requestId, userEnteredOTP)) {
        System.out.println("✓ OTP valid, proceed to PIN change");
        // DB: UPDATE otp_verified=TRUE
    } else {
        System.out.println("❌ OTP expired or incorrect");
    }
}

// User enters new PIN
public void handlePINChange(int userId, String newPIN) {
    // Step 3: Update PIN (hashes and saves)
    if (PINResetManager.updateUserPIN(userId, newPIN)) {
        System.out.println("✓ PIN changed successfully!");
        // DB: UPDATE users.pin, UPDATE status='completed'
    }
}
```

---

## 🎯 KEY POINTS

✅ **Each database field has a specific purpose**
✅ **All fields work together in the workflow**
✅ **10-minute expiration checked using `otp_generated_at`**
✅ **OTP verification prevents unauthorized PIN changes**
✅ **Timestamps provide audit trail**
✅ **Status tracks progress through workflow**


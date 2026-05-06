# Complete OTP PIN Reset - End-to-End Testing Scenario

## 🧪 STEP-BY-STEP TEST (Copy & Paste Ready)

---

## SETUP: Prepare Test Environment

### 1. Create Test User in Database
```sql
-- Run in MySQL
INSERT INTO users (username, fullname, email, password, pin, role)
VALUES ('testuser', 'Test User', 'testuser@example.com', 'test123', '111111', 'user');

-- Get the user ID (usually 2 or higher)
SELECT id FROM users WHERE username = 'testuser';
-- Result: id = 2 (use this in tests below)
```

### 2. Verify Admin Account
```sql
SELECT * FROM admin WHERE username = 'admin';
-- Should exist with id = 1
```

### 3. Empty PIN Reset Requests Table (Optional - for clean test)
```sql
DELETE FROM pin_reset_requests;
```

---

## TEST SCENARIO 1: User Submits PIN Reset Request

### Expected Outcome:
- Request saved to database
- Status: "pending"

### Test Code:
```java
// Simulate user submitting PIN reset request
int userId = 2;  // testuser
String email = "testuser@example.com";

boolean submitted = PINResetManager.submitPINResetRequest(userId, email);

if (submitted) {
    System.out.println("✓ Request submitted successfully");
} else {
    System.out.println("✗ Failed to submit request");
}

// Verify in database
System.out.println("\nDatabase check:");
```

### SQL Verification:
```sql
SELECT * FROM pin_reset_requests WHERE user_id = 2;
-- Expected:
-- id: 1
-- user_id: 2
-- email: testuser@example.com
-- status: pending
-- created_at: current timestamp
```

---

## TEST SCENARIO 2: Admin Approves & Generates OTP

### Expected Outcome:
- OTP generated (6 digits: 000000-999999)
- Status: "approved"
- Email sent (logged to console)

### Test Code:
```java
// Simulate admin approving request
int requestId = 1;  // From previous test
int adminId = 1;    // Admin ID

String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);

if (otp != null && otp.length() == 6 && otp.matches("\\d+")) {
    System.out.println("✓ OTP generated: " + otp);
    System.out.println("✓ Email sent to user");
} else {
    System.out.println("✗ Failed to generate OTP");
}
```

### SQL Verification:
```sql
SELECT * FROM pin_reset_requests WHERE id = 1;
-- Expected:
-- status: approved
-- otp: 123456 (6 digits)
-- otp_generated_at: current timestamp
-- admin_id: 1
-- reviewed_at: current timestamp
```

---

## TEST SCENARIO 3: Verify OTP (Valid Case)

### Expected Outcome:
- OTP matches
- Not expired (< 10 minutes)
- otp_verified: TRUE
- Returns: true

### Test Code:
```java
// Simulate user entering correct OTP
int requestId = 1;
String enteredOTP = "123456";  // Use the OTP from TEST 2

boolean isValid = PINResetManager.verifyOTP(requestId, enteredOTP);

if (isValid) {
    System.out.println("✓ OTP verified successfully");
    System.out.println("✓ User can now reset PIN");
} else {
    System.out.println("✗ OTP verification failed");
}
```

### SQL Verification:
```sql
SELECT otp, otp_verified FROM pin_reset_requests WHERE id = 1;
-- Expected:
-- otp_verified: 1 (TRUE)
```

---

## TEST SCENARIO 4: Test OTP Expiration (Wrong OTP)

### Expected Outcome:
- OTP doesn't match
- Returns: false
- Error message shown

### Test Code:
```java
// Simulate user entering WRONG OTP
int requestId = 1;
String wrongOTP = "999999";  // Wrong OTP

boolean isValid = PINResetManager.verifyOTP(requestId, wrongOTP);

if (!isValid) {
    System.out.println("✓ Invalid OTP correctly rejected");
} else {
    System.out.println("✗ Wrong OTP was accepted (BUG!)");
}
```

---

## TEST SCENARIO 5: Test 10-Minute Expiration

### Expected Outcome:
- OTP checked for expiration
- After 10+ minutes: EXPIRED
- Returns: false

### Test Code:
```java
// Create expired OTP manually (for testing)
int requestId = 1;

// Manually set otp_generated_at to 11 minutes ago
java.sql.Timestamp elevenMinutesAgo = new java.sql.Timestamp(
    System.currentTimeMillis() - (11 * 60 * 1000)
);

// Update database
String sql = "UPDATE pin_reset_requests SET otp_generated_at = ? WHERE id = ?";
try (Connection conn = DB.connect();
     PreparedStatement pst = conn.prepareStatement(sql)) {
    pst.setTimestamp(1, elevenMinutesAgo);
    pst.setInt(2, requestId);
    pst.executeUpdate();
}

// Now verify OTP - should be expired
boolean isValid = PINResetManager.verifyOTP(requestId, "123456");

if (!isValid) {
    System.out.println("✓ Expired OTP correctly rejected");
} else {
    System.out.println("✗ Expired OTP was accepted (BUG!)");
}
```

### SQL Check:
```sql
SELECT 
    otp,
    otp_generated_at,
    TIMESTAMPDIFF(MINUTE, otp_generated_at, NOW()) as minutes_since_generation
FROM pin_reset_requests WHERE id = 1;
-- If minutes_since_generation > 10: EXPIRED
```

---

## TEST SCENARIO 6: User Changes PIN

### Expected Outcome:
- New PIN saved (hashed)
- Status: "completed"
- User can login with new PIN

### Test Code:
```java
// Simulate user setting new PIN
int userId = 2;
int requestId = 1;
String newPIN = "654321";

// First verify OTP (if not already verified)
if (!PINResetManager.isOTPVerified(requestId)) {
    System.out.println("ERROR: OTP not verified yet");
    return;
}

// Update PIN
boolean updated = PINResetManager.updateUserPIN(userId, newPIN);

if (updated) {
    System.out.println("✓ PIN updated successfully");
    
    // Mark as completed
    if (PINResetManager.markAsCompleted(requestId)) {
        System.out.println("✓ Request marked as completed");
    }
} else {
    System.out.println("✗ Failed to update PIN");
}
```

### SQL Verification:
```sql
SELECT * FROM pin_reset_requests WHERE id = 1;
-- Expected:
-- status: completed
-- completed_at: current timestamp

SELECT pin FROM users WHERE id = 2;
-- Expected:
-- pin: hashed value (looks like: "a5f3b4c7d8e9f0g1h2i3j4k5l6m7n8o9p0q1r2s3")
-- (NOT "654321" - it should be hashed)
```

---

## TEST SCENARIO 7: User Logs In With New PIN

### Expected Outcome:
- PIN hashed
- Matches database
- Login successful

### Test Code:
```java
// Simulate user login
String username = "testuser";
String enteredPIN = "654321";  // The new PIN from TEST 6

// Hash the entered PIN (same way as in login)
String hashedPIN = SecurityUtil.hashPin(enteredPIN);

// Query database
try (Connection conn = DB.connect();
     PreparedStatement pst = conn.prepareStatement(
         "SELECT * FROM users WHERE username = ? AND pin = ?"
     )) {
    pst.setString(1, username);
    pst.setString(2, hashedPIN);
    
    ResultSet rs = pst.executeQuery();
    
    if (rs.next()) {
        System.out.println("✓ Login successful with new PIN!");
        System.out.println("  User ID: " + rs.getInt("id"));
        System.out.println("  Username: " + rs.getString("username"));
    } else {
        System.out.println("✗ Login failed - PIN doesn't match");
    }
}
```

---

## TEST SCENARIO 8: Admin Denies Request

### Expected Outcome:
- Status: "denied"
- Reason recorded
- Email sent

### Test Code:
```java
// Create new request for testing denial
PINResetManager.submitPINResetRequest(2, "testuser@example.com");

// Get the new request ID
// (For this test, assume it's ID 2)
int requestId = 2;
int adminId = 1;
String denyReason = "User account requires verification";

boolean denied = AdminPINResetHelper.denyRequest(requestId, adminId, denyReason);

if (denied) {
    System.out.println("✓ Request denied successfully");
    System.out.println("✓ User notified via email");
} else {
    System.out.println("✗ Failed to deny request");
}
```

### SQL Verification:
```sql
SELECT * FROM pin_reset_requests WHERE id = 2;
-- Expected:
-- status: denied
-- admin_response: User account requires verification
```

---

## TEST SCENARIO 9: Check Pending Requests Count

### Expected Outcome:
- Returns number of pending requests
- Used for admin notification badge

### Test Code:
```java
// Submit a new pending request
PINResetManager.submitPINResetRequest(2, "testuser@example.com");

// Get pending count
int count = AdminPINResetHelper.getPendingRequestCount();

System.out.println("Pending PIN reset requests: " + count);

if (count > 0) {
    System.out.println("✓ " + count + " request(s) need admin review");
} else {
    System.out.println("✓ No pending requests");
}
```

---

## TEST SCENARIO 10: Test OTP 6-Digit Format

### Expected Outcome:
- Generated OTP is exactly 6 digits
- No letters
- No special characters

### Test Code:
```java
// Generate multiple OTPs and verify format
for (int i = 0; i < 10; i++) {
    String otp = OTPGenerator.generateOTP();
    
    // Check format
    if (otp.length() == 6 && otp.matches("\\d{6}")) {
        System.out.println("✓ OTP #" + (i+1) + ": " + otp + " (VALID)");
    } else {
        System.out.println("✗ OTP #" + (i+1) + ": " + otp + " (INVALID FORMAT)");
    }
}
```

### Expected Output:
```
✓ OTP #1: 345821 (VALID)
✓ OTP #2: 012345 (VALID)
✓ OTP #3: 999999 (VALID)
✓ OTP #4: 000000 (VALID)
✓ OTP #5: 567890 (VALID)
... all should be VALID
```

---

## COMPLETE WORKFLOW TEST (All Steps)

### Test Script:
```java
public static void testCompleteOTPWorkflow() {
    System.out.println("=== COMPLETE OTP WORKFLOW TEST ===\n");
    
    try {
        // Step 1: Submit request
        System.out.println("STEP 1: User submits PIN reset request");
        int userId = 2;
        boolean submitted = PINResetManager.submitPINResetRequest(
            userId, "testuser@example.com"
        );
        System.out.println(submitted ? "✓ PASS\n" : "✗ FAIL\n");
        
        // Get request ID
        int requestId = 1;
        
        // Step 2: Admin approves
        System.out.println("STEP 2: Admin approves and generates OTP");
        String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, 1);
        System.out.println(otp != null ? "✓ PASS (OTP: " + otp + ")\n" : "✗ FAIL\n");
        
        // Step 3: Verify OTP
        System.out.println("STEP 3: User enters OTP");
        boolean verified = PINResetManager.verifyOTP(requestId, otp);
        System.out.println(verified ? "✓ PASS\n" : "✗ FAIL\n");
        
        // Step 4: Update PIN
        System.out.println("STEP 4: User sets new PIN");
        boolean updated = PINResetManager.updateUserPIN(userId, "654321");
        System.out.println(updated ? "✓ PASS\n" : "✗ FAIL\n");
        
        // Step 5: Mark completed
        System.out.println("STEP 5: Mark request as completed");
        boolean completed = PINResetManager.markAsCompleted(requestId);
        System.out.println(completed ? "✓ PASS\n" : "✗ FAIL\n");
        
        System.out.println("=== ALL TESTS COMPLETED ===");
        
    } catch (Exception e) {
        System.out.println("ERROR: " + e.getMessage());
    }
}

// Run the test
testCompleteOTPWorkflow();
```

---

## ✅ TEST CHECKLIST

- [ ] User can submit PIN reset request
- [ ] Request appears in pending list
- [ ] Admin can approve request
- [ ] OTP generated (6 digits)
- [ ] OTP sent to email (check logs)
- [ ] User can enter OTP
- [ ] Correct OTP verifies successfully
- [ ] Wrong OTP rejected
- [ ] Expired OTP (>10 min) rejected
- [ ] User can change PIN after verification
- [ ] New PIN saved (hashed)
- [ ] User can login with new PIN
- [ ] Request marked as completed
- [ ] Denied requests work
- [ ] Admin can resend OTP
- [ ] All database fields correct

---

**Last Updated:** May 4, 2026
**Version:** 1.0 COMPLETE

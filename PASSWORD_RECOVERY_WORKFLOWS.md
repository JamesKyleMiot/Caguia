# Password Recovery System - User & Admin Workflows

## Complete User Workflow Example

### Scenario: Kyle Forgets His Password

#### Step 1: Login Screen
```
User: Kyle tries to login with username "kyle_santos"
System: Asks for username and PIN
Kyle: Realizes he forgot his PIN and sees "Forgot Password?" button
```

#### Step 2: Click "Forgot Password?"
```
Action: Kyle clicks the "Forgot Password?" button at bottom of login screen
Result: "Password Reset Request" dialog opens
```

#### Step 3: Enter Contact Information
```
Dialog shows:
- Email Address field: [_________________]
- Phone Number field: [_________________]

Kyle's choices:
Option A: Enter email → kyle@email.com
Option B: Enter phone → 09123456789
Option C: Enter both for redundancy

Kyle enters: 
Email: kyle.santos@email.com
Phone: (empty)
```

#### Step 4: Submit Request
```
Kyle clicks "Submit Request" button

System validates:
✓ Email format is valid (contains @)
✓ At least one field is filled
✓ User exists in database

Database action:
INSERT INTO password_reset_requests (user_id, email, phone, status)
VALUES (123, 'kyle.santos@email.com', '', 'pending')

Result message:
"✓ Request submitted successfully! Admin will review shortly."

Kyle sees:
- Email sent confirmation
- Message to wait for admin approval
- Instruction to return later to change password
```

#### Step 5: Waiting for Admin Approval (User's Perspective)

```
Kyle: Waits for admin to review his request

Behind the scenes (Status in Database):
- created_at: 2026-05-04 10:30:00
- status: 'pending' ← Waiting for admin action
- admin_id: NULL ← No admin has acted yet
```

#### Step 6: Admin Reviews and Approves (Next Section)

---

## Complete Admin Workflow Example

### Scenario: Admin Reviews Kyle's Password Reset Request

#### Step 1: Admin Opens Dashboard
```
Admin logs in → Sees Admin Dashboard
Available buttons in header:
- Refresh
- 🔒 Loan Management
- My Details
- 🔑 Password Resets ← Clicks this
- Init DB
- Logout
```

#### Step 2: Click "Password Resets" Button
```
Action: Admin clicks "🔑 Password Resets" button
Result: "Manage Password Reset Requests" dialog opens

Dialog shows:
- Title: "Password Reset Requests Management"
- Info: "Total pending requests: 1"
- Table with columns:
  | ID | User ID | Username | Full Name | Email | Phone | Requested | Status |
  | 1  | 123     | kyle_santos | Kyle Santos | kyle.santos@email.com | | 2026-05-04 10:30 | pending |
```

#### Step 3: Select Request
```
Admin: Clicks on Kyle's request row to select it
Row highlights in blue
Status shows: "Total pending requests: 1"

Request Details visible:
- ID: 1
- User ID: 123
- Username: kyle_santos
- Full Name: Kyle Santos
- Email: kyle.santos@email.com
- Phone: (none provided)
- Requested: 2026-05-04 10:30:00
- Status: pending
```

#### Step 4: Admin Reviews Context
```
Admin thinks:
- Is this a legitimate request? YES (email matches our records)
- Has user had recent security issues? NO
- Is Kyle a good account holder? YES

Decision: APPROVE
```

#### Step 5: Click "Approve Request"
```
Action: Admin clicks "Approve Request" button
Dialog appears: "Approve password reset for user: kyle_santos?"
             "User will have 1 hour to set a new password."
             [YES] [NO]

Admin clicks: YES

Behind the scenes (Database Update):
UPDATE password_reset_requests 
SET status = 'approved', 
    admin_id = (admin's ID), 
    reviewed_at = NOW(), 
    expires_at = DATE_ADD(NOW(), INTERVAL 1 HOUR)
WHERE id = 1

Result message:
"✓ Request approved successfully!"
"User: kyle_santos can now change their password."

Table updates automatically - request no longer shows as "pending"
```

#### Alternative: Admin Denies Request
```
Action: Admin clicks "Deny Request" instead
Dialog appears: "Reason for denial (optional):" [____________]

Admin enters reason: "Account under investigation"
Admin clicks OK

Database Update:
UPDATE password_reset_requests 
SET status = 'denied', 
    admin_id = (admin's ID), 
    reviewed_at = NOW(), 
    admin_response = 'Account under investigation'
WHERE id = 1

Result message:
"✓ Request denied successfully!"
"User: kyle_santos has been notified."

Kyle cannot change password - must submit new request if issue resolved
```

---

## User Completes Password Change (After Approval)

### Step 1: Kyle Returns After Approval

```
Kyle goes back to application
Login screen appears

Kyle: Enters username "kyle_santos" and tries to login
System: Recognizes Kyle has approved password reset request
Option appears: "Change Password"

Kyle: Clicks "Change Password" option
Dialog opens: "Set Your New Password"
```

### Step 2: Enter New Password

```
Dialog shows:
- New Password: [__________________]
- Confirm Password: [__________________]
- Requirements: At least 6 characters
- Buttons: [Change Password] [Cancel]

Kyle enters:
New Password: MyNewPin@123
Confirm Password: MyNewPin@123

Validation:
✓ Both fields filled
✓ Length >= 6 characters
✓ Passwords match
```

### Step 3: Submit New Password

```
Kyle: Clicks "Change Password" button

System actions:
1. Validate passwords match ✓
2. Validate length >= 6 ✓
3. Hash new password using SecurityUtil
4. Update user record:
   UPDATE users SET password = 'hashed_value' WHERE id = 123

5. Mark request as completed:
   UPDATE password_reset_requests 
   SET status = 'completed' 
   WHERE id = 1

Result message:
"✓ Password changed successfully!"
"You can now login with your new password."

Dialog closes after 1.5 seconds
```

### Step 4: Kyle Logs In With New Password

```
Login screen appears again

Kyle enters:
Username: kyle_santos
PIN: MyNewPin@123

System checks:
✓ Username exists
✓ PIN matches (compared with hashed value)

Action:
- Create Session with Kyle's user ID
- Load UserDashboard
- Kyle is now logged in successfully
```

---

## Complete Timeline View

### Request Timeline for Approval Scenario

```
10:30 AM
├─ Kyle forgets password
└─ Clicks "Forgot Password?" button
   
10:31 AM
├─ Kyle enters email: kyle.santos@email.com
└─ Submits request
   ├─ Database: INSERT INTO password_reset_requests (pending)
   └─ Kyle sees: "Request submitted!"

10:32 AM - 11:00 AM
├─ Kyle waits
└─ Admin has until user checks (no time limit for admin)

11:05 AM
├─ Admin opens dashboard
├─ Clicks "Password Resets" button
├─ Sees Kyle's request (5 minutes old)
└─ Reviews and clicks "Approve Request"
   ├─ Database: UPDATE status='approved', expires_at=12:05 PM
   └─ Admin sees: "Request approved!"

11:06 AM
├─ Kyle logs back in
├─ Sees "Change Password" option appears
└─ Clicks it and enters new password
   ├─ Database: UPDATE users password
   ├─ Database: UPDATE request status='completed'
   └─ Kyle sees: "Password changed!"

11:07 AM
├─ Kyle logs in with new password
└─ UserDashboard loads successfully

12:05 PM (1 hour after approval)
├─ Approval would expire IF Kyle hadn't used it
└─ But Kyle already changed password at 11:06, so no issue
```

### Request Timeline for Denial Scenario

```
10:30 AM
├─ Kyle submits password reset request
└─ Database: status='pending'

11:00 AM
├─ Admin reviews request
├─ Sees suspicious activity on Kyle's account
└─ Clicks "Deny Request"
   ├─ Enters reason: "Account flagged for security review"
   ├─ Database: UPDATE status='denied', admin_response='...'
   └─ Admin sees: "Request denied!"

11:01 AM
├─ Kyle tries to change password
└─ System shows: "Your password reset request was denied"
   └─ Kyle sees reason: "Account flagged for security review"

11:02 AM
├─ Kyle can still submit NEW password reset request
└─ Admin can approve next request once issue is resolved
```

### Request Timeline for Expiration Scenario

```
10:30 AM
├─ Kyle submits password reset request
└─ Database: status='pending'

11:00 AM
├─ Admin approves request
├─ Database: expires_at = 12:00 PM
└─ Kyle has 1 hour window

11:15 AM
├─ Kyle goes to sleep, forgets about approval
└─ Doesn't change password

12:05 PM (Approval expired)
├─ Kyle wakes up and tries to change password
└─ System shows: "Your approval has expired"
   └─ Kyle must submit NEW request and wait for admin again
```

---

## Database State Examples

### After User Submits Request

```sql
SELECT * FROM password_reset_requests WHERE user_id = 123;

+----+---------+--------+-------+-------------------------+
| id | user_id | email  | phone | status | admin_id | ...  |
+----+---------+--------+-------+--------+----------+------+
| 1  | 123     | kyle.s@email.com | NULL | pending | NULL | ...  |
+----+---------+--------+-------+--------+----------+------+
```

### After Admin Approves

```sql
SELECT * FROM password_reset_requests WHERE user_id = 123;

+----+---------+--------+-------+---------+----------+----------------------+
| id | user_id | email  | phone | status  | admin_id | expires_at           |
+----+---------+--------+-------+---------+----------+----------------------+
| 1  | 123     | kyle.s@email.com | NULL | approved | 2 | 2026-05-04 12:05:00 |
+----+---------+--------+-------+---------+----------+----------------------+
```

### After User Changes Password

```sql
SELECT * FROM password_reset_requests WHERE user_id = 123;

+----+---------+--------+-------+-----------+----------+
| id | user_id | email  | phone | status    | admin_id |
+----+---------+--------+-------+-----------+----------+
| 1  | 123     | kyle.s@email.com | NULL | completed | 2 |
+----+---------+--------+-------+-----------+----------+
```

---

## Key Points to Remember

### For Users

✅ **DO:**
- Provide real email or phone number
- Wait for admin approval (typically within 24 hours)
- Remember 1-hour window to change password after approval
- Use strong password (at least 6 characters)

❌ **DON'T:**
- Submit multiple requests (wastes admin time)
- Try to guess the process (always get admin approval first)
- Share password reset requests with others
- Try to access after 1-hour expiration

### For Admins

✅ **DO:**
- Review requests within 24 hours
- Verify email/phone matches records before approving
- Keep notes on denied requests
- Monitor for suspicious password reset patterns

❌ **DON'T:**
- Auto-approve all requests (security risk)
- Approve without reviewing
- Forget to provide reason when denying
- Leave requests in pending state indefinitely

---

## Integration with Existing System

```
Caguioa Bank System
├── Login (SignInUsers.java)
│   └── [NEW] "Forgot Password?" button
│       └── Opens ForgotPassword dialog
│           └── Creates password_reset_requests record
│
├── User Dashboard (UserDashboard.java)
│   └── [FUTURE] Show pending password reset requests
│
└── Admin Dashboard (AdminDashboard.java)
    └── [NEW] "🔑 Password Resets" button
        └── Opens PasswordResetRequestDialog
            ├── View pending requests
            ├── Approve requests
            └── Deny requests
```

---

## Status Flowchart

```
        ┌──────────────┐
        │  NEW REQUEST │
        │   (pending)  │
        └──────┬───────┘
               │
        ┌──────▼──────────┐
        │  ADMIN REVIEWS  │
        └──────┬──────────┘
               │
         ┌─────┴─────┐
         │           │
    ┌────▼───┐  ┌───▼────┐
    │APPROVE │  │ DENY   │
    └────┬───┘  └───┬────┘
         │          │
    ┌────▼──────┐  │
    │ 1-HR WAIT │  │
    │(approved) │  │
    └────┬──────┘  │
         │         │
    ┌────▼──────────▼─────┐
    │ USER ATTEMPTS CHANGE │
    └────┬──────────┬─────┘
         │          │
    ┌────▼────┐ ┌──▼────────┐
    │SUCCESS  │ │ REJECTED  │
    │COMPLETED│ │(denied)   │
    └─────────┘ └───┬───────┘
                    │
                ┌───▼──────────┐
                │ MUST SUBMIT  │
                │  NEW REQUEST │
                └──────────────┘
```

---

## Next Steps

1. Copy all files to project
2. Run password_reset_migration.sql
3. Compile project
4. Test user flow end-to-end
5. Test admin approval flow
6. Test admin denial flow
7. Test expiration scenario
8. Monitor logs for issues
9. Gather user feedback
10. Consider enhancements (email notifications, etc.)

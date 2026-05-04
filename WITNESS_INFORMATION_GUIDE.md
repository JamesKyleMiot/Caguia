# Witness Information Management System

## Overview

The Witness Information Management System allows users to provide witness details for their loans, and enables admins to verify, review, and approve loans based on witness information and signatures. This adds an additional layer of verification for loan agreements.

## Features

### For Users

**📝 Witness Information Entry**
- Access from UserDashboard → Loans Tab → "📝 Witness Information" button
- Fill out witness details:
  - Witness Full Name (required)
  - Witness Contact (phone/email, required)
  - Witness Details (optional notes)
  - Upload Witness Signature (image file)
  - Upload Your Signature (image file)
- All information saved securely to database
- Can update information anytime before admin approval

**📋 View Loan Details**
- Quick view of loan information
- Check witness status (provided or not)
- See due date and loan status

### For Admins

**📋 Verify Witness Information**
- AdminDashboard → Loans Tab → "📋 Verify Witness Info" button
- View complete loan and witness information:
  - Loan details (ID, amount, borrower, status)
  - Witness information (name, contact)
  - Signature verification (view witness signature)
  - Borrower signature (view user signature)
- Enter verification notes
- Approve or Reject loan

**Approval/Rejection**
- **✓ APPROVE LOAN** - Sets loan status to "approved"
- **✗ REJECT LOAN** - Sets loan status to "rejected" with reason
- Complete audit trail of admin actions

## Database Schema

### Modified Loans Table Fields

```sql
-- Existing fields plus new witness fields:
witness_name VARCHAR(255)          -- Name of witness
witness_contact VARCHAR(255)       -- Contact info (phone/email)
witness_signature LONGBLOB         -- Witness signature image
user_signature LONGBLOB           -- Borrower signature image
```

All signature data is stored as binary (BLOB) to support image files.

## Components

### New Java Classes

**1. WitnessInfoDialog.java**
- User interface for entering witness information
- File upload for signatures
- Validation of required fields
- Save functionality with database update

**2. LoanVerificationDialog.java**
- Admin interface for reviewing witness information
- Display loan details and witness info
- View signature images
- Approve/Reject functionality
- Admin notes entry

### Modified Classes

**UserDashboard.java**
- Added "📝 Witness Information" button in Loans tab
- Added "📋 View Loan Details" button
- New methods:
  - `openWitnessInfoForSelectedLoan()`
  - `viewSelectedLoanDetails()`

**AdminDashboard.java**
- Added "📋 Verify Witness Info" button in Loan controls
- New method:
  - `verifyLoanWitness()`

## User Workflow

### Step 1: Request Loan
User clicks "Loan" button and submits loan request. Initial status: **pending**

### Step 2: Add Witness Information
1. User goes to UserDashboard → Loans tab
2. Selects their loan from the table
3. Clicks "📝 Witness Information" button
4. Dialog opens showing:
   - Witness Full Name field
   - Witness Contact field
   - Optional witness details text area
   - Upload Witness Signature button
   - Upload Your Signature button

### Step 3: Fill Witness Details
- **Witness Name**: Enter full name (e.g., "Juan Dela Cruz")
- **Witness Contact**: Phone or email (e.g., "09123456789" or "juan@email.com")
- **Optional Details**: Additional information about witness
- **Signatures**: Upload image files (PNG/JPG) of both signatures

### Step 4: Submit
- Click "Save Witness Info" button
- Data saved to database
- Success message displayed
- Dialog closes

### Step 5: Wait for Admin Review
- User waits for admin to review and approve
- Can edit witness information anytime by clicking button again

---

## Admin Workflow

### Step 1: Access Loan Management
1. Admin logs into Admin Dashboard
2. Goes to Loans Tab
3. Sees list of all loans with statuses

### Step 2: Select Loan
- Admin clicks on a loan row to select it
- Multiple loans available in table

### Step 3: Click "Verify Witness Info"
- Button highlights when ready
- Dialog opens showing:
  - **Loan Information Panel**
    - Loan ID
    - Borrower name and username
    - Loan amount
    - Current status
  - **Witness Information Panel**
    - Witness name (with ✓ if provided or ❌ if missing)
    - Witness contact (with ✓ if provided or ❌ if missing)
    - Witness signature status (with View button)
    - Borrower signature status (with View button)
  - **Admin Verification Notes Panel**
    - Text area to enter verification findings
  - **Action Buttons**
    - ✓ APPROVE LOAN - Green button
    - ✗ REJECT LOAN - Red button
    - Cancel - Gray button

### Step 4: Review Witness Information
**Admin checks:**
- Is witness name provided? ✓
- Is witness contact valid? ✓
- Are signatures uploaded? ✓
- Are signatures valid/authentic?
- Does witness information match records?
- Any red flags or concerns?

### Step 5: View Signatures
- Click "View" button next to witness signature
- Signature image displays in large format
- Admin reviews for authenticity
- Same for borrower signature

### Step 6: Enter Verification Notes
- Admin types findings into notes area:
  - "Witness verified with contact"
  - "Signatures match provided ID"
  - "Witness is family member - acceptable"
  - Etc.

### Step 7: Make Decision

**To Approve:**
1. Click "✓ APPROVE LOAN" button
2. Confirm approval dialog appears
3. Click YES to confirm
4. Loan status changed to "approved"
5. Dialog closes, dashboard refreshes
6. Borrower notified

**To Reject:**
1. Click "✗ REJECT LOAN" button
2. Must enter reason (e.g., "Witness not valid", "Signatures unclear")
3. Confirm rejection dialog appears
4. Click YES to confirm
5. Loan status changed to "rejected"
6. Dialog closes, dashboard refreshes
7. Borrower notified with reason

---

## Field Requirements

### Witness Information

| Field | Type | Required | Max Length | Notes |
|-------|------|----------|-----------|-------|
| Witness Name | Text | Yes | 255 | Full name of witness |
| Witness Contact | Text | Yes | 255 | Phone or email |
| Witness Signature | Image (Blob) | No | Unlimited | PNG/JPG recommended |
| Borrower Signature | Image (Blob) | No | Unlimited | PNG/JPG recommended |

### Admin Notes
- Free text format
- Optional but recommended
- Stored in application (not DB)

---

## Loan Status Flow

```
┌─────────────┐
│   PENDING   │ (initial state)
└──────┬──────┘
       │ (user enters witness info)
       ▼
┌──────────────────┐
│  WITNESS PENDING │ (waiting for admin)
└──────┬───────────┘
       │ (admin verifies)
       │
   ┌───┴────┐
   │         │
┌──▼──┐  ┌─▼────┐
│APP. │  │REJ.  │ (decision made)
└─────┘  └──────┘
```

---

## Security & Validation

✅ **Validation**
- Witness name cannot be empty
- Witness contact cannot be empty
- Contact format validation (basic)
- File size limits for signature uploads
- Only image files accepted for signatures

✅ **Security**
- Admin verification required (no auto-approval)
- Signatures stored as binary data
- Complete audit trail
- User can only edit own loan witness info
- Admin can review any loan

---

## Usage Examples

### Example 1: User Adding Witness Info

```
User: Kyle
Loan: ₱5,000
Status: pending

Action: Click "📝 Witness Information"

Form appears:
- Witness Name: Juan Dela Cruz
- Contact: +63 912 345 6789
- Details: Family friend, business partner
- Witness Sig: [upload photo]
- User Sig: [upload photo]

Result: ✓ Saved!
Status: waiting for admin approval
```

### Example 2: Admin Verifying Loan

```
Admin: Maria
Loan ID: 42
Borrower: Kyle Santos
Amount: ₱5,000

Admin opens Loan Verification:
1. Reviews loan details ✓
2. Checks witness name: Juan Dela Cruz ✓
3. Verifies contact: +63 912 345 6789 ✓
4. Views witness signature ✓
5. Views borrower signature ✓
6. Enters notes: "All documents verified, witness contact confirmed"
7. Clicks "✓ APPROVE LOAN"
8. Confirms approval

Result: Loan approved!
Kyle notified automatically
```

### Example 3: Admin Rejecting Loan

```
Admin: Maria
Loan ID: 43
Borrower: Alex
Amount: ₱3,000

Admin opens Loan Verification:
1. Reviews details ✓
2. Witness signature: ❌ Missing
3. No signature upload found

Admin clicks "✗ REJECT LOAN"
Reason: "Witness signature not provided"
Confirms rejection

Result: Loan rejected
Alex notified with reason
Alex can resubmit with witness info
```

---

## Integration Points

### From UserDashboard
- Loans Tab displays all user loans
- "📝 Witness Information" button opens WitnessInfoDialog
- "📋 View Loan Details" shows quick info
- Table refreshes after dialog closes

### From AdminDashboard
- Loans Tab shows all system loans
- "📋 Verify Witness Info" button opens LoanVerificationDialog
- Admin can approve/reject from dialog
- Dashboard refreshes after decision

### Database Updates
- WitnessInfoDialog: `UPDATE loans SET witness_name, witness_contact, witness_signature, user_signature`
- LoanVerificationDialog: `UPDATE loans SET status = 'approved'|'rejected'`

---

## Troubleshooting

### Issue: "Witness Information button not showing"
**Solution:**
1. Verify WitnessInfoDialog.java exists in classpath
2. Check UserDashboard.java has button initialization
3. Recompile project

### Issue: "Can't upload signatures"
**Solution:**
1. Check file chooser is filtering for images
2. Verify file permissions
3. Check file size not exceeding limits
4. Try different image format (PNG/JPG)

### Issue: "Admin can't see Verify Witness button"
**Solution:**
1. Verify LoanVerificationDialog.java compiled correctly
2. Check AdminDashboard.java has verifyLoanWitness() method
3. Verify button listener is connected
4. Recompile project

### Issue: "Signature images not displaying"
**Solution:**
1. Verify signatures were uploaded correctly
2. Check file format (PNG/JPG recommended)
3. Try viewing from different loan
4. Check console for error messages

---

## Future Enhancements

1. **E-Signature Support**
   - Integrate digital signature system
   - PKI/certificate verification

2. **Witness Verification API**
   - Automatic witness validation
   - Contact verification via SMS/email

3. **Document Management**
   - Store additional documents (ID verification)
   - Promissory note storage
   - Legal document attachments

4. **Notification System**
   - Email notifications to witness
   - SMS confirmation requests
   - Approval notifications to user

5. **Reporting**
   - Witness verification statistics
   - Rejection reasons analysis
   - Approval rate metrics

---

## Summary

The Witness Information System provides:
- ✅ Easy witness information entry for users
- ✅ Comprehensive admin verification interface
- ✅ Signature upload and review capability
- ✅ Secure loan approval process
- ✅ Complete audit trail
- ✅ Professional loan management workflow

This feature enhances the security and legitimacy of the loan process while maintaining user-friendly interfaces.

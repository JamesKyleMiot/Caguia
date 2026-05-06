# Caguioa Bank - UI Components & Architecture Analysis

## Executive Summary

The Caguioa Bank system uses **Java Swing** with a mix of:
- **NetBeans GUI Builder** (.form files) for login/registration screens
- **Programmatic layouts** (BorderLayout, GridLayout, GridBagLayout) for dashboards and dialogs
- **No responsive design** - all components use hardcoded pixel dimensions
- **13+ dialog components** for various banking operations
- **5 main JFrame windows** (login, registration, user dashboard, admin dashboard)

---

## 1. Main UI Components (JFrame - Top-Level Windows)

### 1.1 User Login Screen
**File:** [SignInUsers.form](SignInUsers.form) / [SignInUsers.java](src/caguioa/bank/SignInUsers.java)
- **Type:** JFrame
- **Purpose:** User authentication
- **Layout:** GroupLayout (NetBeans auto-generated)
- **Components:**
  - Email/Username TextField
  - PIN PasswordField
  - Sign In Button
  - Sign Up Button (links to registration)
- **Styling:** Dark green panel (RGB 0, 102, 51) on left side

### 1.2 User Registration Screen
**File:** [RegesterUser.form](RegesterUser.form) / [RegesterUser.java](src/caguioa/bank/RegesterUser.java)
- **Type:** JFrame
- **Purpose:** New user account creation
- **Layout:** GroupLayout (NetBeans auto-generated)
- **Components:**
  - Full Name
  - Username
  - Email
  - Age
  - Address
  - Gender (ComboBox)
  - Nationality (ComboBox - dynamically loaded)
  - PIN/Password (PasswordField)
  - PIN Confirmation
- **Buttons:** Sign Up, Sign In

### 1.3 Admin Login Screen
**File:** [AdminLogin.form](AdminLogin.form) / [AdminLogin.java](src/caguioa/bank/AdminLogin.java)
- **Type:** JFrame
- **Purpose:** Administrator authentication
- **Layout:** GroupLayout (NetBeans auto-generated)
- **Components:**
  - Email TextField
  - PIN PasswordField
  - Login Button
- **Styling:** Similar to user login with green accent panel

### 1.4 User Dashboard
**File:** [UserDashboard.form](UserDashboard.form) / [UserDashboard.java](src/caguioa/bank/UserDashboard.java)
- **Type:** JFrame
- **Size:** 1360x780 (minimum)
- **Layout:** BorderLayout (primary) with nested panels
- **Sections:**
  1. **Header Area** - Logo, greeting, navigation buttons
  2. **Summary Cards** - 4 stat cards in GridLayout
     - Current Balance
     - Savings
     - Total Transactions
     - Active Loans
  3. **Left Sidebar** (250px wide) - Navigation menu
  4. **Main Content Area**
     - Transaction history table
     - Active loan details
     - Notifications table
     - JScrollPane for all tables
  5. **Footer Area** - Status/logout options

**Key Buttons:**
- 📊 View More (Dashboard features)
- 💳 Apply for Loan
- 💰 Pay Loan
- 🔑 Change PIN
- 🔒 Forgot PIN
- 📱 Change Password
- 👥 Witness Information
- 🚪 Logout

**Data Tables:**
- `dashboardTransactionsTable` - Recent transactions
- `dashboardLoansTable` - Current loans
- `notificationsTable` - User notifications

### 1.5 Admin Dashboard
**File:** [AdminDashboard.java](src/caguioa/bank/AdminDashboard.java)
- **Type:** JFrame
- **Size:** 1400x800 (minimum)
- **Layout:** BorderLayout with GridLayout for summary cards
- **Access Control:** Admin verification on startup (Session.isAdmin check)
- **Sections:**
  1. **Header** - Title, subtitle, logo
  2. **Summary Cards** (GridLayout 1x4)
     - Total Users
     - Total Transactions
     - Total Loans
     - Total Balance
  3. **Data Tables**
     - All Users Table
     - All Transactions Table
     - All Loans Table
  4. **Control Buttons:**
     - 🔄 Refresh
     - 🔒 Loan Management (dialog)
     - 📋 Loan Applications (dialog)
     - My Details (dialog)
     - Init DB (database initialization)
     - Logout

**Color Scheme:**
- Background: RGB(240, 248, 245) - Light mint
- Header: RGB(230, 245, 240) - Mint
- Header Border: RGB(100, 200, 150)
- Title Text: RGB(34, 139, 34) - Dark green
- Subtitle: RGB(70, 130, 100) - Teal

---

## 2. Dialog Components (JDialog - Modal Windows)

### 2.1 Loan Management Dialogs

#### LoanPaymentDialog
**File:** [LoanPaymentDialog.java](src/caguioa/bank/LoanPaymentDialog.java)
- **Size:** 840x600 (minimum 760x550)
- **Type:** Multi-step wizard dialog
- **Layout:** BorderLayout with GridBagLayout for each step
- **Steps:**
  1. **Loan Summary** - Shows loan details, remaining balance, due date
  2. **Payment Method Selection** - ComboBox with 4 methods:
     - 📱 Online Banking / Mobile App
     - 🏦 Bank Counter (Teller)
     - 🏪 Payment Center
     - 💳 Auto-debit from Account
  3. **Payment Instructions** - TextArea with method-specific instructions
  4. **Payment Amount Entry** - TextField for amount input
  5. **Payment Summary & Receipt** - Confirmation dialog
- **Navigation:** Previous/Next/Cancel buttons
- **Features:** Real-time calculation, receipt generation

#### LoanApplicationDialog
**File:** [LoanApplicationDialog.java](src/caguioa/bank/LoanApplicationDialog.java)
- **Size:** 900x1000 (minimum 850x900)
- **Layout:** GridBagLayout for complex form
- **Form Sections:**
  1. **Personal Information**
     - Full Name
     - Date of Birth
     - Gender (ComboBox)
     - Address
     - Contact Number
     - Email Address
  2. **Employment Information**
     - Employment Status (ComboBox)
     - Company Name
     - Monthly Income
     - Work Address
  3. **Loan Details**
     - Loan Amount
     - Loan Purpose (TextArea)
     - Loan Term
  4. **Bank Information**
     - Account Number
     - Account Type (ComboBox)
  5. **Requirements Checklist**
     - Valid ID Checkbox
     - Proof of Income Checkbox
     - Proof of Address Checkbox
     - Declaration Checkbox
- **Buttons:** Submit, Cancel
- **Validation:** Real-time amount validation (MIN: 50,000, MAX: 300,000)

#### LoanApplicationReviewDialog
**File:** [LoanApplicationReviewDialog.java](src/caguioa/bank/LoanApplicationReviewDialog.java)
- **Purpose:** Admin review of pending loan applications
- **Features:** Approve/Reject with comments

#### LoanManagementDialog
**File:** [LoanManagementDialog.java](src/caguioa/bank/LoanManagementDialog.java)
- **Size:** 1000x600
- **Purpose:** Admin view/manage all loans
- **Layout:** Table-based display

#### LoanVerificationDialog
**File:** [LoanVerificationDialog.java](src/caguioa/bank/LoanVerificationDialog.java)
- **Purpose:** Verify loan details with witness info
- **Features:** Display witness signatures, user verification

### 2.2 Authentication & Password Reset Dialogs

#### ForgotPassword
**File:** [ForgotPassword.java](src/caguioa/bank/ForgotPassword.java)
- **Type:** JDialog
- **Purpose:** Password recovery workflow
- **Features:** Email verification, OTP handling

#### ForgotPIN
**File:** [ForgotPIN.java](src/caguioa/bank/ForgotPIN.java)
- **Type:** JDialog
- **Purpose:** PIN recovery
- **Features:** Security questions or OTP verification

#### ChangePassword
**File:** [ChangePassword.java](src/caguioa/bank/ChangePassword.java)
- **Layout:** GridBagLayout
- **Components:**
  - Old Password field
  - New Password field
  - Confirm Password field
- **Buttons:** Change (150x35), Cancel (100x35)

#### PINResetRequestDialog
**File:** [PINResetRequestDialog.java](src/caguioa/bank/PINResetRequestDialog.java)
- **Purpose:** Request PIN reset from user dashboard

#### PINResetOTPDialog
**File:** [PINResetOTPDialog.java](src/caguioa/bank/PINResetOTPDialog.java)
- **Purpose:** Verify OTP for PIN reset
- **Features:** OTP input, countdown timer

#### ResetPINDialog
**File:** [ResetPINDialog.java](src/caguioa/bank/ResetPINDialog.java)
- **Purpose:** Direct PIN reset interface
- **Components:** PIN fields, validation

#### PasswordResetRequestDialog
**File:** [PasswordResetRequestDialog.java](src/caguioa/bank/PasswordResetRequestDialog.java)
- **Purpose:** Request password reset

### 2.3 Witness & Transaction Dialogs

#### WitnessInfoDialog
**File:** [WitnessInfoDialog.java](src/caguioa/bank/WitnessInfoDialog.java)
- **Size:** 550x500 (minimum)
- **Layout:** GridBagLayout
- **Purpose:** Capture witness information for loan verification
- **Components:**
  - Witness name, contact, address fields
  - Signature capture area
  - User signature verification
- **Buttons:** Add Signature, Save (140x35), Cancel (100x35)

#### OnlineLoanPaymentDialog
**File:** [OnlineLoanPaymentDialog.java](src/caguioa/bank/OnlineLoanPaymentDialog.java)
- **Purpose:** Online payment confirmation and instructions

---

## 3. Layout Managers Summary

### 3.1 BorderLayout (Most Common)
**Used in:** UserDashboard, AdminDashboard, main dialog content areas
**Structure:**
```
NORTH   (Header with buttons)
|
WEST-+-CENTER-+-EAST
(Nav) | (Main) | (Info)
|     |       |
SOUTH (Footer/Status)
```

### 3.2 GridLayout
**Used in:** Summary card panels, stat displays
**Example:** AdminDashboard summary
```
GridLayout(1, 4)  // 1 row, 4 columns for 4 stat cards
```

### 3.3 GridBagLayout
**Used in:** Form dialogs (LoanApplicationDialog, ChangePassword, WitnessInfoDialog)
**Advantages:**
- Complex multi-column forms
- Variable component widths
- Row/column spanning
- Precise alignment control

### 3.4 FlowLayout
**Used in:** Button groups, headers
**Example:** Header action buttons (RIGHT aligned, 10px horizontal gap)
```
JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
```

### 3.5 GroupLayout (Auto-generated by NetBeans)
**Used in:** SignInUsers.form, RegesterUser.form, AdminLogin.form
**Generated by:** NetBeans GUI Builder
**Advantages:** Visual designer integration, automatic layout

---

## 4. Size & Dimension Strategy

### 4.1 Main Windows (JFrame)
| Component | Width | Height | Min Width | Min Height |
|-----------|-------|--------|-----------|------------|
| UserDashboard | (auto) | (auto) | 1360 | 780 |
| AdminDashboard | (auto) | (auto) | 1400 | 800 |
| Dialogs (avg) | 840-900 | 600-1000 | 760-850 | 550-900 |

### 4.2 Component Sizing Patterns
```java
// Main window sizing
setMinimumSize(new Dimension(1360, 780));

// Dialog sizing
setSize(840, 600);
setMinimumSize(new Dimension(760, 550));
pack();

// Panel sizing
leftWrapper.setPreferredSize(new Dimension(250, 0));
notificationsScroll.setPreferredSize(new Dimension(420, 200));

// Button sizing
button.setPreferredSize(new Dimension(150, 35));

// Field sizing in forms
field.setPreferredSize(new Dimension(300, 25));
```

### 4.3 Positioning
```java
// Center on screen
setLocationRelativeTo(null);

// Center relative to parent
setLocationRelativeTo(ownerFrame);
dialog.setLocationRelativeTo(this);
```

---

## 5. Responsive Design Assessment

### ❌ NO RESPONSIVE DESIGN DETECTED

**Current State:**
- ❌ All sizes hardcoded in pixels
- ❌ No percentage-based sizing
- ❌ No dynamic layout recalculation
- ❌ No resolution-aware component scaling
- ❌ No mobile/tablet adaptation
- ❌ Minimum sizes prevent very small windows
- ❌ Fixed column widths in JTables
- ❌ No DPI/scaling factor awareness
- ❌ No viewport resizing listeners

**Issues for Modern Use:**
- 1360x780 dashboard doesn't work well on laptops < 13" or high-DPI displays
- Fixed font sizes don't scale with screen size
- 900x1000 loan form may be too large for small windows
- No touch-friendly sizing for future mobile/tablet versions
- Horizontal scrolling on small screens

**Improvement Opportunities:**
1. Implement dynamic layout with `ComponentListener`
2. Use percentage-based sizing (e.g., 0.6 * screenWidth)
3. Add DPI scaling factors
4. Create responsive breakpoints
5. Scale fonts based on component size
6. Use relative sizing in JTables

---

## 6. Color & Styling Scheme

### 6.1 Primary Colors
| Purpose | RGB | Hex |
|---------|-----|-----|
| Action Buttons | (76, 175, 80) | #4CAF50 |
| Danger/Reject | (244, 67, 54) | #F44336 |
| Primary Info | (63, 81, 181) | #3F51B5 |
| Secondary Info | (33, 150, 243) | #2196F3 |
| Dark/Close | (0, 0, 51) | #000033 |

### 6.2 Theme Colors
| Element | Color | Use |
|---------|-------|-----|
| Button Backgrounds | RGB(0, 102, 0) | LoginButtons, MainCTAs |
| Button Text | White | All buttons |
| Panel Backgrounds | RGB(240, 248, 245) | Dashboard panels |
| Header Background | RGB(230, 245, 240) | Headers |
| Header Border | RGB(100, 200, 150) | Header frames |
| Title Text | RGB(34, 139, 34) | Headings |
| Subtitle Text | RGB(70, 130, 100) | Subheadings |
| Dialog Background | RGB(242, 248, 252) | Dialog panels |

### 6.3 Typography
```java
// Headers
new Font("SansSerif", Font.BOLD, 24)  // Main title
new Font("SansSerif", Font.BOLD, 20)  // Section title
new Font("SansSerif", Font.BOLD, 14)  // Labels

// Regular text
new Font("SansSerif", Font.PLAIN, 13)  // Subtitle
new Font("SansSerif", Font.PLAIN, 12)  // Body text

// Form fields
new Font("Segoe UI", Font.PLAIN, 12)  // Input fields
new Font("Segoe UI", Font.BOLD, 14)   // Field labels
```

---

## 7. Component Libraries & Patterns

### 7.1 JTable Usage
- **In UserDashboard:** 
  - `dashboardTransactionsTable` - recent transactions
  - `dashboardLoansTable` - current loans
  - `notificationsTable` - notifications
- **In AdminDashboard:**
  - `allUsersTable` - all system users
  - `allTransactionsTable` - all transactions
  - `allLoansTable` - all loans
- **DefaultTableModel** for dynamic data
- **JScrollPane** wrapping for scrollable tables

### 7.2 Borders & Spacing
```java
// Padding
new EmptyBorder(14, 14, 14, 14)
new EmptyBorder(16, 16, 16, 16)

// Borders
BorderFactory.createLineBorder(Color)
BorderFactory.createEmptyBorder()
BorderFactory.createCompoundBorder()

// Gaps in layout
new BorderLayout(12, 12)    // 12px horizontal/vertical gap
new GridLayout(1, 4, 12, 12) // 12px gaps between components
```

### 7.3 Other Swing Components
- **JTabbedPane** - Tab navigation (UserDashboard)
- **JComboBox** - Dropdown selections
- **JPasswordField** - Secure PIN entry
- **JTextArea** - Multi-line text input
- **JScrollPane** - Scrollable content
- **JOptionPane** - Dialogs and alerts

---

## 8. File Structure Summary

### Entry Points
```
src/caguioa/bank/
├── SignInUsers.form       (.form file)
├── SignInUsers.java       (JFrame - User Login)
├── RegesterUser.form
├── RegesterUser.java      (JFrame - User Registration)
├── AdminLogin.form
├── AdminLogin.java        (JFrame - Admin Login)
├── UserDashboard.form
├── UserDashboard.java     (JFrame - User Dashboard)
└── AdminDashboard.java    (JFrame - Admin Dashboard)
```

### Dialogs
```
├── LoanPaymentDialog.java (Multi-step payment wizard)
├── LoanApplicationDialog.java (Loan application form)
├── LoanApplicationReviewDialog.java (Admin review)
├── LoanManagementDialog.java (Admin management)
├── LoanVerificationDialog.java (Verification)
├── PINResetRequestDialog.java
├── PINResetOTPDialog.java
├── ResetPINDialog.java
├── ForgotPIN.java
├── ForgotPassword.java
├── PasswordResetRequestDialog.java
├── ChangePassword.java
├── WitnessInfoDialog.java
└── OnlineLoanPaymentDialog.java
```

### Utilities
```
├── LogoUtil.java (Logo handling)
├── Session.java (User session management)
├── DB.java (Database connection)
└── ... (other helpers)
```

---

## 9. Key Observations

### ✅ Strengths
1. **Consistent color scheme** - Professional green/blue palette
2. **Clear separation** - Main windows vs dialogs
3. **Grid-based layouts** - Good use of BorderLayout + nested panels
4. **User-friendly buttons** - Emoji indicators for features
5. **Form validation** - Amount checks, required fields
6. **Multi-step wizards** - LoanPaymentDialog has clear steps
7. **Access control** - Admin verification in AdminDashboard

### ⚠️ Weaknesses
1. **No responsive design** - Fixed pixel sizes
2. **NetBeans dependency** - .form files need NetBeans to edit
3. **Hardcoded sizes** - Difficult to adapt to different screens
4. **No theme system** - Colors hardcoded throughout
5. **Limited accessibility** - No font size adjustments
6. **No night mode** - Only light theme available
7. **No multi-language** - English only

### 🎯 Modernization Opportunities
1. Implement responsive layouts (percentage-based)
2. Create a theme/style system
3. Add DPI scaling
4. Replace .form files with code-based layouts
5. Implement ComponentListener for window resize
6. Add configuration file for colors/sizes
7. Create reusable dialog templates

---

## 10. All UI Classes Reference

| Class | Type | Purpose | Location |
|-------|------|---------|----------|
| SignInUsers | JFrame | User login | [src/caguioa/bank/SignInUsers.java](src/caguioa/bank/SignInUsers.java) |
| RegesterUser | JFrame | User registration | [src/caguioa/bank/RegesterUser.java](src/caguioa/bank/RegesterUser.java) |
| AdminLogin | JFrame | Admin login | [src/caguioa/bank/AdminLogin.java](src/caguioa/bank/AdminLogin.java) |
| UserDashboard | JFrame | User main dashboard | [src/caguioa/bank/UserDashboard.java](src/caguioa/bank/UserDashboard.java) |
| AdminDashboard | JFrame | Admin main dashboard | [src/caguioa/bank/AdminDashboard.java](src/caguioa/bank/AdminDashboard.java) |
| LoanPaymentDialog | JDialog | Loan payment wizard | [src/caguioa/bank/LoanPaymentDialog.java](src/caguioa/bank/LoanPaymentDialog.java) |
| LoanApplicationDialog | JDialog | Loan application form | [src/caguioa/bank/LoanApplicationDialog.java](src/caguioa/bank/LoanApplicationDialog.java) |
| LoanApplicationReviewDialog | JDialog | Admin loan review | [src/caguioa/bank/LoanApplicationReviewDialog.java](src/caguioa/bank/LoanApplicationReviewDialog.java) |
| LoanManagementDialog | JDialog | Loan management | [src/caguioa/bank/LoanManagementDialog.java](src/caguioa/bank/LoanManagementDialog.java) |
| LoanVerificationDialog | JDialog | Loan verification | [src/caguioa/bank/LoanVerificationDialog.java](src/caguioa/bank/LoanVerificationDialog.java) |
| ForgotPIN | JDialog | PIN recovery | [src/caguioa/bank/ForgotPIN.java](src/caguioa/bank/ForgotPIN.java) |
| ForgotPassword | JDialog | Password recovery | [src/caguioa/bank/ForgotPassword.java](src/caguioa/bank/ForgotPassword.java) |
| ChangePassword | JDialog | Password change | [src/caguioa/bank/ChangePassword.java](src/caguioa/bank/ChangePassword.java) |
| PINResetRequestDialog | JDialog | PIN reset request | [src/caguioa/bank/PINResetRequestDialog.java](src/caguioa/bank/PINResetRequestDialog.java) |
| PINResetOTPDialog | JDialog | OTP verification | [src/caguioa/bank/PINResetOTPDialog.java](src/caguioa/bank/PINResetOTPDialog.java) |
| ResetPINDialog | JDialog | PIN reset | [src/caguioa/bank/ResetPINDialog.java](src/caguioa/bank/ResetPINDialog.java) |
| WitnessInfoDialog | JDialog | Witness information | [src/caguioa/bank/WitnessInfoDialog.java](src/caguioa/bank/WitnessInfoDialog.java) |
| PasswordResetRequestDialog | JDialog | Password reset request | [src/caguioa/bank/PasswordResetRequestDialog.java](src/caguioa/bank/PasswordResetRequestDialog.java) |
| OnlineLoanPaymentDialog | JDialog | Online payment | [src/caguioa/bank/OnlineLoanPaymentDialog.java](src/caguioa/bank/OnlineLoanPaymentDialog.java) |

---

## Summary

The Caguioa Bank system has a **well-structured Swing-based UI** with:
- **5 main windows** (logins, dashboards)
- **14+ dialog components** for various operations
- **Consistent styling** with green/blue color scheme
- **Multiple layout managers** (BorderLayout, GridLayout, GridBagLayout, FlowLayout, GroupLayout)
- **Zero responsive design** - all pixel-based sizing

This is a **desktop-first application** without mobile/responsive capabilities. To modernize it, implementing percentage-based layouts and DPI scaling would be the first priorities.

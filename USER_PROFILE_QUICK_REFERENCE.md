# Caguioa Bank - User Profile Quick Reference Guide

## Quick Facts

| Aspect | Details |
|--------|---------|
| **User Fields** | 14 total (fullname, username, sex, age, nationality, address, pin, password, email, role, balance, savings, total_deposit, created_at) |
| **Fields Editable** | 0 (No profile editing implemented) |
| **Session Data** | 5 fields (userId, adminId, role, fullname, isAdmin) |
| **Registration Form** | 9 input fields |
| **Admin User View** | 8 columns (id, username, balance, savings, total_deposit, loans_total, active_loans_total, loans_count) |
| **Database Schemas** | 4 different implementations (inconsistent) |
| **Update Operations** | 6 types (balance, pin, password, role, blocking) |
| **Profile View** | Read-only table with all fields |

---

## Essential Code Snippets

### Load User Data
```java
Connection con = DB.connect();
PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
pst.setInt(1, Session.userId);
ResultSet rs = pst.executeQuery();
```

### Display Profile (Read-Only)
```java
private JTable buildProfileTable(Connection con) throws Exception {
    PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
    pst.setInt(1, Session.userId);
    ResultSet rs = pst.executeQuery();
    
    DefaultTableModel model = new DefaultTableModel(
        new Object[]{"Field", "Value"}, 0
    ) {
        public boolean isCellEditable(int row, int column) {
            return false;  // Non-editable
        }
    };
    
    if (rs.next()) {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            model.addRow(new Object[]{
                metaData.getColumnLabel(i), 
                rs.getObject(i)
            });
        }
    }
    return new JTable(model);
}
```

### Update PIN (Currently Implemented)
```java
String sql = "UPDATE users SET pin = ? WHERE id = ?";
PreparedStatement pst = connection.prepareStatement(sql);
pst.setString(1, hashedNewPin);
pst.setInt(2, userId);
pst.executeUpdate();
```

### Query: Get Admin Dashboard Users
```sql
SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, 
       COALESCE(SUM(l.amount),0) AS loans_total, 
       COALESCE(SUM(CASE WHEN l.status='active' THEN l.amount ELSE 0 END),0) AS active_loans_total, 
       COUNT(l.id) AS loans_count 
FROM users u LEFT JOIN loans l ON l.user_id = u.id 
GROUP BY u.id ORDER BY u.id
```

---

## File Locations

### Critical Files
| File | Purpose | Lines |
|------|---------|-------|
| `UserDashboard.java` | User profile view & dashboard | 1,600+ |
| `RegesterUser.java` | User registration form | 400+ |
| `AdminDashboard.java` | Admin user management | 800+ |
| `Session.java` | Session storage | 8 |
| `DB.java` | Database connection | - |

### Database Schema Files
| File | Status | Coverage |
|------|--------|----------|
| `CreateLawBank.java` | ✅ PRIMARY | Complete (14 fields) |
| `COMPLETE_DATABASE_SETUP.sql` | ⚠️ Secondary | Basic (11 fields) |
| `DBInit.java` | ⚠️ Alternative | Basic (11 fields) |
| `database_schema.sql` | ⚠️ Reference | Basic (11 fields) |

### Management Classes
| Class | Function |
|-------|----------|
| `PINResetManager.java` | PIN reset/change |
| `PasswordResetManager.java` | Password recovery |
| `AccountManager.java` | Account suspension |
| `LoanManager.java` | Loan management |

---

## User Information Mapping

```java
// Session.java - What's stored in memory
public class Session {
    public static int userId;           // User ID
    public static String fullname;      // User's name
    public static String role;          // User role
    public static int adminId;          // Admin ID
    public static boolean isAdmin;      // Is admin?
}

// UserDashboard gets these from DB
SELECT * FROM users WHERE id=?
↓
Results in Profile Table:
- id
- fullname
- username
- sex
- nationality
- address
- age
- pin (masked)
- password (not shown)
- email (if present)
- role
- balance
- savings
- total_deposit
- created_at
```

---

## Database Queries Quick Reference

### SELECT Queries Used in Code
```sql
-- Get all user fields
SELECT * FROM users WHERE id=?

-- Get profile for display
SELECT * FROM users WHERE id=?

-- Get financial summary
SELECT balance, savings, total_deposit FROM users WHERE id=?

-- Get admin user list
SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, 
       COALESCE(SUM(l.amount),0) AS loans_total...

-- Login verification
SELECT * FROM users WHERE username=? AND pin=?
```

### UPDATE Queries (Currently Implemented)
```sql
-- Update balance
UPDATE users SET balance=balance+?, total_deposit=total_deposit+? WHERE id=?

-- Update PIN
UPDATE users SET pin=? WHERE id=?

-- Update password
UPDATE users SET password=? WHERE id=?

-- Suspend account
UPDATE users SET role='suspended' WHERE id=?

-- Reactivate account
UPDATE users SET role='user' WHERE id=?
```

### UPDATE Queries (NOT Implemented - Needed)
```sql
-- ❌ Update name
UPDATE users SET fullname=? WHERE id=?

-- ❌ Update address
UPDATE users SET address=? WHERE id=?

-- ❌ Update email
UPDATE users SET email=? WHERE id=?

-- ❌ Update age/sex/nationality
UPDATE users SET age=?, sex=?, nationality=? WHERE id=?
```

---

## UI Components Structure

### UserDashboard Layout
```
UserDashboard (JFrame)
├─ Dashboard Panel (refresh, buttons)
├─ Transaction Button → showAllRecordsDialog()
│  └─ Profile Dialog (JDialog)
│     ├─ Header with info cards (Balance, Savings, etc.)
│     └─ Tabs:
│        ├─ Profile (buildProfileTable - READ-ONLY)
│        ├─ Transactions (buildResultTable)
│        └─ Loans (buildResultTable)
└─ Other buttons (Deposit, Withdraw, Transfer, etc.)
```

### AdminDashboard Layout
```
AdminDashboard (JFrame)
├─ Header with summary cards
├─ TabbedPane:
│  ├─ Users Tab (allUsersTable - READ-ONLY)
│  ├─ Loans Tab (allLoansTable)
│  ├─ Transactions Tab (allTransactionsTable)
│  └─ Admin controls (Refresh, Init DB, Logout)
└─ Dialog launchers (Loan Management, Applications, etc.)
```

---

## Table Editability

| Table | Class | Editable | Method |
|-------|-------|----------|--------|
| Profile (User) | UserDashboard | ❌ No | `isCellEditable() → false` |
| All Users (Admin) | AdminDashboard | ❌ No | `isCellEditable() → false` |
| Transactions | UserDashboard | ❌ No | `isCellEditable() → false` |
| Loans | AdminDashboard | ❌ No | `isCellEditable() → false` |

---

## Common Patterns

### Pattern 1: Load and Display User Data
```java
try {
    Connection con = DB.connect();
    PreparedStatement pst = con.prepareStatement("SELECT ... FROM users WHERE id=?");
    pst.setInt(1, Session.userId);
    ResultSet rs = pst.executeQuery();
    // Process ResultSet
    rs.close();
    pst.close();
} catch (Exception e) {
    System.out.println("Error: " + e);
}
```

### Pattern 2: Update User Data
```java
try {
    Connection con = DB.connect();
    PreparedStatement pst = con.prepareStatement("UPDATE users SET ? = ? WHERE id=?");
    pst.setString(1, newValue);
    pst.setInt(2, Session.userId);
    pst.executeUpdate();
    pst.close();
} catch (Exception e) {
    JOptionPane.showMessageDialog(null, "Error updating user");
}
```

### Pattern 3: Display in JDialog
```java
JDialog dialog = new JDialog(this, "Title", true);
dialog.setSize(new Dimension(width, height));
dialog.setLocationRelativeTo(this);
dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
dialog.setVisible(true);
```

---

## For Implementation Tasks

### To Add User Profile Editing:

**1. Create New Class**: `UserProfileEditDialog.java`
   - Extends JDialog
   - Fields: fullname, address, age, sex (dropdown), nationality (dropdown)
   - Update query: `UPDATE users SET fullname=?, address=?, age=?, sex=?, nationality=? WHERE id=?`

**2. Update UserDashboard.java**
   - Add "Edit Profile" button to profile dialog
   - Launch UserProfileEditDialog on button click

**3. Create Admin Edit**: `AdminUserEditDialog.java`
   - Allow admin to edit user details
   - Add button in AdminDashboard when user selected

**4. Schema Standardization**
   - Update COMPLETE_DATABASE_SETUP.sql
   - Update DBInit.java
   - Ensure all have: sex, address, age, nationality, email

---

## Debugging Tips

### Check Current User Session
```java
System.out.println("User ID: " + Session.userId);
System.out.println("Fullname: " + Session.fullname);
System.out.println("Role: " + Session.role);
System.out.println("Is Admin: " + Session.isAdmin);
```

### Verify Database Connection
```java
try {
    Connection con = DB.connect();
    if (con != null && !con.isClosed()) {
        System.out.println("Database connected!");
    }
} catch (Exception e) {
    System.out.println("DB Error: " + e.getMessage());
}
```

### Check User Exists
```java
PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
pst.setInt(1, userId);
ResultSet rs = pst.executeQuery();
if (rs.next()) {
    System.out.println("User found: " + rs.getString("fullname"));
} else {
    System.out.println("User not found!");
}
```

---

## Documentation Reference

- **Detailed Report**: `USER_PROFILE_EXPLORATION_REPORT.md` (12 sections, 500+ lines)
- **Visual Summary**: `USER_DATA_VISUAL_SUMMARY.md` (ASCII diagrams, flows, tables)
- **This File**: Quick reference for developers
- **Session Memory**: `/memories/session/user-data-exploration.md`

---

**Last Updated**: May 7, 2026  
**Created for**: Caguioa Bank Project Exploration  
**Status**: Complete & Ready for Implementation Phase

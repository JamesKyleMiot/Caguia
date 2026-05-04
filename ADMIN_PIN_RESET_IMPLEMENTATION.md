# Admin Dashboard - PIN Reset Management Implementation Guide

## Quick Start for Admin Dashboard Integration

### 1. Import Helper Class
```java
import caguioa.bank.AdminPINResetHelper;
```

### 2. Display Pending PIN Reset Requests (in Admin Dashboard)

**Create a JTable to display pending requests:**
```java
// In AdminDashboard form setup
JTable pinResetTable = new JTable();

// Load pending requests
Object[][] pendingData = AdminPINResetHelper.getPendingRequests();
String[] columnNames = {"ID", "Username", "Full Name", "Email", "Requested Date"};

DefaultTableModel model = new DefaultTableModel(pendingData, columnNames);
pinResetTable.setModel(model);
pinResetTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

// Add to your admin dashboard JPanel
```

### 3. Approve Request with OTP

**Create an Approve button:**
```java
JButton approveBtn = new JButton("Approve & Send OTP");
approveBtn.addActionListener(e -> {
    int selectedRow = pinResetTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a request");
        return;
    }
    
    int requestId = (int) pinResetTable.getValueAt(selectedRow, 0);
    int adminId = Session.adminId; // Current admin ID
    
    String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);
    
    if (otp != null) {
        JOptionPane.showMessageDialog(this,
            "✓ Request approved!\n\n" +
            "OTP generated: " + otp + "\n\n" +
            "OTP has been sent to user's email.\n" +
            "Validity: 10 minutes",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh table
        refreshPendingRequests();
    } else {
        JOptionPane.showMessageDialog(this,
            "✗ Error approving request",
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
});
```

### 4. Deny Request

**Create a Deny button:**
```java
JButton denyBtn = new JButton("Deny Request");
denyBtn.addActionListener(e -> {
    int selectedRow = pinResetTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a request");
        return;
    }
    
    // Get reason from user
    String reason = JOptionPane.showInputDialog(this,
        "Enter reason for denial:",
        "Enter Reason");
    
    if (reason != null && !reason.isEmpty()) {
        int requestId = (int) pinResetTable.getValueAt(selectedRow, 0);
        int adminId = Session.adminId;
        
        if (AdminPINResetHelper.denyRequest(requestId, adminId, reason)) {
            JOptionPane.showMessageDialog(this,
                "✓ Request denied and user notified",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh table
            refreshPendingRequests();
        }
    }
});
```

### 5. Resend OTP

**Create a Resend OTP button (for approved requests):**
```java
JButton resendOtpBtn = new JButton("Resend OTP");
resendOtpBtn.addActionListener(e -> {
    int selectedRow = approvedTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a request");
        return;
    }
    
    int requestId = (int) approvedTable.getValueAt(selectedRow, 0);
    
    String newOtp = AdminPINResetHelper.resendOTP(requestId);
    
    if (newOtp != null) {
        JOptionPane.showMessageDialog(this,
            "✓ New OTP generated and sent\n\n" +
            "New OTP: " + newOtp,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
});
```

### 6. Refresh Pending Requests

**Helper method to refresh table:**
```java
private void refreshPendingRequests() {
    Object[][] data = AdminPINResetHelper.getPendingRequests();
    String[] columnNames = {"ID", "Username", "Full Name", "Email", "Requested Date"};
    DefaultTableModel model = new DefaultTableModel(data, columnNames);
    pinResetTable.setModel(model);
}
```

### 7. Show Notification Badge

**Display count of pending requests:**
```java
int pendingCount = AdminPINResetHelper.getPendingRequestCount();

JLabel notificationLabel = new JLabel();
if (pendingCount > 0) {
    notificationLabel.setText("PIN Reset Requests: " + pendingCount + " pending");
    notificationLabel.setForeground(new Color(255, 0, 0));
    notificationLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
} else {
    notificationLabel.setText("No pending PIN reset requests");
}
```

## Complete Example: Simple PIN Reset Management Panel

```java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminPINResetPanel extends JPanel {
    private JTable pendingTable;
    private JButton approveBtn, denyBtn, refreshBtn;

    public AdminPINResetPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("PIN Reset Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // Table for pending requests
        pendingTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(pendingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        approveBtn = new JButton("Approve & Send OTP");
        approveBtn.setBackground(new Color(0, 102, 0));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> approveRequest());
        
        denyBtn = new JButton("Deny Request");
        denyBtn.setBackground(new Color(153, 0, 0));
        denyBtn.setForeground(Color.WHITE);
        denyBtn.addActionListener(e -> denyRequest());
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadPendingRequests());
        
        buttonPanel.add(approveBtn);
        buttonPanel.add(denyBtn);
        buttonPanel.add(refreshBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        loadPendingRequests();
    }

    private void loadPendingRequests() {
        Object[][] data = AdminPINResetHelper.getPendingRequests();
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Requested Date"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        model.setColumnIdentifiers(columnNames);
        pendingTable.setModel(model);
    }

    private void approveRequest() {
        int row = pendingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request");
            return;
        }
        
        int requestId = (int) pendingTable.getValueAt(row, 0);
        String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, 1); // Admin ID = 1
        
        if (otp != null) {
            JOptionPane.showMessageDialog(this,
                "✓ Approved! OTP sent: " + otp, "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadPendingRequests();
        }
    }

    private void denyRequest() {
        int row = pendingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request");
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, "Enter denial reason:");
        if (reason != null && !reason.isEmpty()) {
            int requestId = (int) pendingTable.getValueAt(row, 0);
            AdminPINResetHelper.denyRequest(requestId, 1, reason);
            loadPendingRequests();
        }
    }
}
```

## Database Monitoring

### Check pending requests status:
```sql
SELECT * FROM pin_reset_requests 
WHERE status = 'pending' 
ORDER BY created_at DESC;
```

### Check approved requests with OTP:
```sql
SELECT * FROM pin_reset_requests 
WHERE status = 'approved' AND expires_at > NOW() 
ORDER BY otp_generated_at DESC;
```

### Check verification status:
```sql
SELECT username, email, otp_verified, 
       CASE WHEN otp_verified = 1 THEN 'Verified' ELSE 'Pending' END as status
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE status = 'approved';
```

## Testing Checklist

- [ ] User submits PIN reset request
- [ ] Admin sees request in "Pending" list
- [ ] Admin approves request
- [ ] OTP is generated (check database)
- [ ] Email with OTP is sent (check EmailNotifier logs)
- [ ] User receives email with OTP
- [ ] User enters OTP in dialog
- [ ] OTP is validated
- [ ] OTP status shows "Verified" in admin panel
- [ ] User can set new PIN
- [ ] New PIN is saved to database
- [ ] User can login with new PIN
- [ ] Request status is marked "completed"

## Troubleshooting

### No OTP sent to email:
1. Check EmailNotifier configuration
2. Verify email address is correct in database
3. Check mail library is installed
4. Review console logs for errors

### OTP verification failing:
1. Verify OTP hasn't expired (10 minutes)
2. Check exact OTP digits
3. Confirm requestId is correct
4. Review database otp_verified flag

### Admin panel not showing requests:
1. Check database connection
2. Verify pin_reset_requests table exists
3. Confirm table has OTP columns (run migration)
4. Check user permissions

---
**Version:** 1.0
**Last Updated:** May 4, 2026

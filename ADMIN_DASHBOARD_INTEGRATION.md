# Quick Integration Guide - Add PIN Reset Panel to Admin Dashboard

## Location in AdminDashboard

Add a new tab or panel for PIN Reset Management. Here's the exact code:

## Step 1: Add Imports to AdminDashboard.java

```java
import caguioa.bank.AdminPINResetHelper;
import javax.swing.table.DefaultTableModel;
```

## Step 2: Add Component Variables (in class declaration)

```java
private JPanel pinResetPanel;
private JTabbedPane mainTabbedPane;  // If not already present
private JTable pendingPinResetTable;
private JTable approvedPinResetTable;
private JButton approvePinResetBtn;
private JButton denyPinResetBtn;
private JButton resendOtpBtn;
private JButton refreshPinResetBtn;
private JLabel pendingCountLabel;
```

## Step 3: Create PIN Reset Panel Method

Add this method to AdminDashboard:

```java
private JPanel createPINResetPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Title Panel
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel titleLabel = new JLabel("PIN Reset Management");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    titleLabel.setForeground(new Color(34, 139, 34));
    
    pendingCountLabel = new JLabel();
    updatePendingCount();
    
    titlePanel.add(titleLabel);
    titlePanel.add(Box.createHorizontalStrut(50));
    titlePanel.add(pendingCountLabel);
    
    // Tabbed Pane for different views
    JTabbedPane tabbedPane = new JTabbedPane();
    
    // Tab 1: Pending Requests
    JPanel pendingPanel = createPendingRequestsPanel();
    tabbedPane.addTab("Pending Requests", pendingPanel);
    
    // Tab 2: Approved Requests (with OTP)
    JPanel approvedPanel = createApprovedRequestsPanel();
    tabbedPane.addTab("Approved (Awaiting Verification)", approvedPanel);
    
    // Tab 3: Completed
    JPanel completedPanel = createCompletedRequestsPanel();
    tabbedPane.addTab("Completed", completedPanel);
    
    // Assemble
    mainPanel.add(titlePanel, BorderLayout.NORTH);
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    
    return mainPanel;
}
```

## Step 4: Create Pending Requests Panel

```java
private JPanel createPendingRequestsPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Table for pending requests
    pendingPinResetTable = new JTable();
    pendingPinResetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pendingPinResetTable.setRowHeight(25);
    
    JScrollPane scrollPane = new JScrollPane(pendingPinResetTable);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    // Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    
    approvePinResetBtn = new JButton("Approve & Send OTP");
    approvePinResetBtn.setBackground(new Color(0, 102, 0));
    approvePinResetBtn.setForeground(Color.WHITE);
    approvePinResetBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    approvePinResetBtn.setPreferredSize(new Dimension(180, 35));
    approvePinResetBtn.addActionListener(e -> approvePinResetRequest());
    
    denyPinResetBtn = new JButton("Deny Request");
    denyPinResetBtn.setBackground(new Color(153, 0, 0));
    denyPinResetBtn.setForeground(Color.WHITE);
    denyPinResetBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    denyPinResetBtn.setPreferredSize(new Dimension(140, 35));
    denyPinResetBtn.addActionListener(e -> denyPinResetRequest());
    
    refreshPinResetBtn = new JButton("Refresh");
    refreshPinResetBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    refreshPinResetBtn.setPreferredSize(new Dimension(100, 35));
    refreshPinResetBtn.addActionListener(e -> refreshPinReset());
    
    buttonPanel.add(approvePinResetBtn);
    buttonPanel.add(denyPinResetBtn);
    buttonPanel.add(Box.createHorizontalStrut(20));
    buttonPanel.add(refreshPinResetBtn);
    
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    // Load initial data
    refreshPinReset();
    
    return panel;
}
```

## Step 5: Create Approved Requests Panel

```java
private JPanel createApprovedRequestsPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Table for approved requests
    approvedPinResetTable = new JTable();
    approvedPinResetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    approvedPinResetTable.setRowHeight(25);
    
    JScrollPane scrollPane = new JScrollPane(approvedPinResetTable);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    // Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    
    resendOtpBtn = new JButton("Resend OTP");
    resendOtpBtn.setBackground(new Color(0, 102, 153));
    resendOtpBtn.setForeground(Color.WHITE);
    resendOtpBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    resendOtpBtn.setPreferredSize(new Dimension(140, 35));
    resendOtpBtn.addActionListener(e -> resendOTPToUser());
    
    JButton refreshApprovedBtn = new JButton("Refresh");
    refreshApprovedBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    refreshApprovedBtn.setPreferredSize(new Dimension(100, 35));
    refreshApprovedBtn.addActionListener(e -> refreshApprovedRequests());
    
    buttonPanel.add(resendOtpBtn);
    buttonPanel.add(Box.createHorizontalStrut(20));
    buttonPanel.add(refreshApprovedBtn);
    
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    // Load initial data
    refreshApprovedRequests();
    
    return panel;
}
```

## Step 6: Create Completed Requests Panel

```java
private JPanel createCompletedRequestsPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JTable completedTable = new JTable();
    completedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    completedTable.setRowHeight(25);
    
    JScrollPane scrollPane = new JScrollPane(completedTable);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    // Refresh button
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(e -> {
        Object[][] data = AdminPINResetHelper.getCompletedRequests();
        String[] cols = {"ID", "Username", "Full Name", "Email", "Completion Date"};
        completedTable.setModel(new DefaultTableModel(data, cols));
    });
    buttonPanel.add(refreshBtn);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    // Load initial data
    Object[][] data = AdminPINResetHelper.getCompletedRequests();
    String[] cols = {"ID", "Username", "Full Name", "Email", "Completion Date"};
    completedTable.setModel(new DefaultTableModel(data, cols));
    
    return panel;
}
```

## Step 7: Add Action Handlers

```java
private void approvePinResetRequest() {
    int row = pendingPinResetTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a pending request first",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int requestId = (int) pendingPinResetTable.getValueAt(row, 0);
    int adminId = Session.adminId;  // Assuming Session stores admin ID
    
    String otp = AdminPINResetHelper.approveAndGenerateOTP(requestId, adminId);
    
    if (otp != null) {
        JOptionPane.showMessageDialog(this,
            "✓ Request Approved!\n\n" +
            "OTP Generated: " + otp + "\n" +
            "OTP sent to user's email\n" +
            "Validity: 10 minutes",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        
        refreshPinReset();
        updatePendingCount();
    } else {
        JOptionPane.showMessageDialog(this,
            "✗ Error approving request",
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

private void denyPinResetRequest() {
    int row = pendingPinResetTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a pending request first",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    String reason = JOptionPane.showInputDialog(this,
        "Enter reason for denial:",
        "");
    
    if (reason != null && !reason.trim().isEmpty()) {
        int requestId = (int) pendingPinResetTable.getValueAt(row, 0);
        int adminId = Session.adminId;
        
        if (AdminPINResetHelper.denyRequest(requestId, adminId, reason)) {
            JOptionPane.showMessageDialog(this,
                "✓ Request Denied\nUser has been notified",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            refreshPinReset();
            updatePendingCount();
        } else {
            JOptionPane.showMessageDialog(this,
                "✗ Error denying request",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void resendOTPToUser() {
    int row = approvedPinResetTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Please select a request first",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int requestId = (int) approvedPinResetTable.getValueAt(row, 0);
    
    String newOtp = AdminPINResetHelper.resendOTP(requestId);
    
    if (newOtp != null) {
        JOptionPane.showMessageDialog(this,
            "✓ New OTP Generated and Sent\n\n" +
            "New OTP: " + newOtp,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this,
            "✗ Error resending OTP",
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

private void refreshPinReset() {
    Object[][] data = AdminPINResetHelper.getPendingRequests();
    String[] columnNames = {"ID", "Username", "Full Name", "Email", "Requested Date"};
    DefaultTableModel model = new DefaultTableModel(data, columnNames);
    pendingPinResetTable.setModel(model);
}

private void refreshApprovedRequests() {
    Object[][] data = AdminPINResetHelper.getApprovedRequests();
    String[] columnNames = {"ID", "Username", "Full Name", "Email", "OTP Status", "OTP Sent Date"};
    DefaultTableModel model = new DefaultTableModel(data, columnNames);
    approvedPinResetTable.setModel(model);
}

private void updatePendingCount() {
    int count = AdminPINResetHelper.getPendingRequestCount();
    if (count > 0) {
        pendingCountLabel.setText("⚠ " + count + " pending request(s)");
        pendingCountLabel.setForeground(new Color(255, 0, 0));
        pendingCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    } else {
        pendingCountLabel.setText("No pending requests");
        pendingCountLabel.setForeground(new Color(0, 102, 0));
    }
}
```

## Step 8: Add to Admin Dashboard Layout

In your AdminDashboard's main constructor or initComponents(), add:

```java
// Assuming you have a JTabbedPane called adminTabbedPane
pinResetPanel = createPINResetPanel();
adminTabbedPane.addTab("PIN Reset Management", pinResetPanel);
```

## Step 9: Add Refresh on Dashboard Open

Add this to make the PIN reset data refresh when admin switches to the tab:

```java
// Add a change listener to the tabbed pane
adminTabbedPane.addChangeListener(e -> {
    if (adminTabbedPane.getSelectedComponent() == pinResetPanel) {
        refreshPinReset();
        refreshApprovedRequests();
        updatePendingCount();
    }
});
```

## Complete File Structure

After integration, your admin dashboard will have:

```
Admin Dashboard
  ├── Users Management
  ├── Loans Management
  ├── Account Audit
  ├── Messages
  ├── PIN Reset Management ← NEW TAB
  │   ├── Pending Requests
  │   ├── Approved (Awaiting Verification)
  │   └── Completed
  └── ...other tabs...
```

---

## Testing the Integration

1. Compile AdminDashboard with new code
2. Run admin dashboard
3. Navigate to "PIN Reset Management" tab
4. Should show 0 pending requests initially
5. Submit a test PIN reset from user login
6. Refresh admin dashboard
7. Should see pending request in table
8. Click "Approve & Send OTP"
9. Verify OTP appears in console/logs
10. Verify user receives email (or check logs)

---

**Ready to integrate!** Follow these steps in order and your admin dashboard will have full PIN reset management functionality.

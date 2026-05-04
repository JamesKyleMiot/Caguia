package caguioa.bank;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.ResultSet;

public class PINResetRequestDialog extends JDialog {
    
    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JButton approveBtn;
    private JButton denyBtn;
    private JButton refreshBtn;
    private JButton closeBtn;
    private JLabel countLabel;

    public PINResetRequestDialog(Frame owner) {
        super(owner, "PIN Reset Requests Management", true);
        
        initializeUI();
        setupListeners();
        loadRequests();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        mainPanel.setBackground(new Color(240, 248, 245));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("PIN Reset Requests");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(34, 139, 34));

        countLabel = new JLabel("Pending: 0");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        countLabel.setForeground(new Color(220, 53, 69));
        
        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);
        headerLeft.add(titleLabel, BorderLayout.WEST);
        headerLeft.add(countLabel, BorderLayout.EAST);
        
        headerPanel.add(headerLeft, BorderLayout.WEST);
        
        // Table
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Email", "Status", "Requested", "Admin Response"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        requestsTable = new JTable(tableModel);
        requestsTable.setRowHeight(26);
        requestsTable.setShowGrid(true);
        requestsTable.setGridColor(new Color(188, 226, 158));
        requestsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        requestsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        requestsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        requestsTable.getTableHeader().setForeground(Color.BLACK);
        requestsTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pending PIN Reset Requests"));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonPanel.setOpaque(false);

        approveBtn = new JButton("✓ Approve Selected");
        approveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusPainted(false);
        approveBtn.setPreferredSize(new Dimension(160, 35));

        denyBtn = new JButton("✗ Deny Selected");
        denyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        denyBtn.setBackground(new Color(220, 53, 69));
        denyBtn.setForeground(Color.WHITE);
        denyBtn.setFocusPainted(false);
        denyBtn.setPreferredSize(new Dimension(160, 35));

        refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(33, 150, 243));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setPreferredSize(new Dimension(120, 35));

        closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeBtn.setBackground(new Color(100, 100, 100));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(approveBtn);
        buttonPanel.add(denyBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 500);
    }

    private void setupListeners() {
        approveBtn.addActionListener(e -> approveSelectedRequest());
        denyBtn.addActionListener(e -> denySelectedRequest());
        refreshBtn.addActionListener(e -> loadRequests());
        closeBtn.addActionListener(e -> dispose());
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        
        try {
            ResultSet rs = PINResetManager.getPendingResetRequests();
            
            int count = 0;
            if (rs != null) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getString("created_at"),
                        rs.getString("admin_response") != null ? rs.getString("admin_response") : ""
                    });
                    count++;
                }
            }
            
            countLabel.setText("Pending: " + count);
            if (count == 0) {
                JOptionPane.showMessageDialog(this, "✓ No pending PIN reset requests.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("Error loading PIN reset requests: " + e);
            JOptionPane.showMessageDialog(this, "Error loading requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveSelectedRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "❌ Please select a request to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve PIN reset request for: " + username + "?\n\nThe user will be able to set a new PIN.",
            "Confirm Approval",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (PINResetManager.approveRequest(requestId, Session.adminId != 0 ? Session.adminId : 1)) {
                JOptionPane.showMessageDialog(this,
                    "✓ PIN reset request approved!\n" +
                    "User " + username + " can now set their new PIN.",
                    "Approved",
                    JOptionPane.INFORMATION_MESSAGE);
                loadRequests();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error approving request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void denySelectedRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "❌ Please select a request to deny.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        String reason = JOptionPane.showInputDialog(this,
            "Enter reason for denying PIN reset for " + username + ":",
            "Suspicious activity detected");
        
        if (reason != null && !reason.trim().isEmpty()) {
            if (PINResetManager.denyRequest(requestId, Session.adminId != 0 ? Session.adminId : 1, reason)) {
                JOptionPane.showMessageDialog(this,
                    "✓ PIN reset request denied.",
                    "Denied",
                    JOptionPane.INFORMATION_MESSAGE);
                loadRequests();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error denying request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            PINResetRequestDialog dialog = new PINResetRequestDialog(frame);
            dialog.setVisible(true);
        });
    }
}

package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PasswordResetRequestDialog extends JDialog {
    
    private JTable requestsTable;
    private JButton approveBtn;
    private JButton denyBtn;
    private JButton refreshBtn;
    private JLabel infoLabel;

    public PasswordResetRequestDialog(Frame owner) {
        super(owner, "Manage Password Reset Requests", true);
        
        initializeUI();
        setupListeners();
        loadRequests();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(240, 248, 245));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Password Reset Requests Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        infoLabel = new JLabel("Loading requests...");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(infoLabel, BorderLayout.EAST);

        // Table
        requestsTable = new JTable();
        requestsTable.setRowHeight(28);
        requestsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        requestsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        requestsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        requestsTable.getTableHeader().setForeground(Color.WHITE);
        requestsTable.setAutoCreateRowSorter(true);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pending Requests"));
        scrollPane.setPreferredSize(new Dimension(900, 400));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        approveBtn = new JButton("Approve Request");
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        approveBtn.setPreferredSize(new Dimension(140, 35));

        denyBtn = new JButton("Deny Request");
        denyBtn.setBackground(new Color(244, 67, 54));
        denyBtn.setForeground(Color.WHITE);
        denyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        denyBtn.setPreferredSize(new Dimension(130, 35));

        refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(33, 150, 243));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(approveBtn);
        buttonPanel.add(denyBtn);
        buttonPanel.add(refreshBtn);

        // Instructions
        JPanel instructionPanel = new JPanel();
        instructionPanel.setOpaque(false);
        JLabel instructionLabel = new JLabel(
            "<html>" +
            "<b>Instructions:</b><br>" +
            "1. Review pending password reset requests from users<br>" +
            "2. Select a request and click 'Approve' to allow password change<br>" +
            "3. Or click 'Deny' to reject the request<br>" +
            "4. User will receive notification and be able to set new password if approved<br>" +
            "</html>"
        );
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        instructionPanel.add(instructionLabel);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(mainPanel, BorderLayout.CENTER);
        wrapperPanel.add(instructionPanel, BorderLayout.SOUTH);

        setContentPane(wrapperPanel);
        setResizable(true);
        setMinimumSize(new Dimension(900, 500));
    }

    private void setupListeners() {
        approveBtn.addActionListener(e -> approveSelectedRequest());
        denyBtn.addActionListener(e -> denySelectedRequest());
        refreshBtn.addActionListener(e -> loadRequests());
    }

    private void loadRequests() {
        try {
            ResultSet rs = PasswordResetManager.getPendingResetRequests();
            DefaultTableModel model = new DefaultTableModel();
            
            model.addColumn("ID");
            model.addColumn("User ID");
            model.addColumn("Username");
            model.addColumn("Full Name");
            model.addColumn("Email");
            model.addColumn("Phone");
            model.addColumn("Requested");
            model.addColumn("Status");

            int count = 0;
            if (rs != null) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("created_at"),
                        rs.getString("status")
                    });
                    count++;
                }
            }

            requestsTable.setModel(model);
            infoLabel.setText("Total pending requests: " + count);

            // Adjust column widths
            requestsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            requestsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            requestsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            requestsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            requestsTable.getColumnModel().getColumn(4).setPreferredWidth(120);
            requestsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
            requestsTable.getColumnModel().getColumn(6).setPreferredWidth(140);
            requestsTable.getColumnModel().getColumn(7).setPreferredWidth(60);

        } catch (Exception e) {
            System.out.println("Error loading requests: " + e);
            infoLabel.setText("Error loading requests");
            infoLabel.setForeground(Color.RED);
        }
    }

    private void approveSelectedRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a request to approve.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int requestId = (Integer) requestsTable.getValueAt(selectedRow, 0);
            int userId = (Integer) requestsTable.getValueAt(selectedRow, 1);
            String username = (String) requestsTable.getValueAt(selectedRow, 2);

            int result = JOptionPane.showConfirmDialog(this,
                "Approve password reset for user: " + username + "?\n\n" +
                "User will have 1 hour to set a new password.",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                int adminId = getAdminId();
                
                if (PasswordResetManager.approveRequest(requestId, adminId)) {
                    JOptionPane.showMessageDialog(this,
                        "Request approved successfully!\n" +
                        "User: " + username + " can now change their password.",
                        "Approved",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadRequests();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error approving request.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println("Error approving request: " + e);
            JOptionPane.showMessageDialog(this,
                "Error approving request: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void denySelectedRequest() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a request to deny.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int requestId = (Integer) requestsTable.getValueAt(selectedRow, 0);
            String username = (String) requestsTable.getValueAt(selectedRow, 2);

            // Get reason for denial
            String reason = JOptionPane.showInputDialog(this,
                "Reason for denial (optional):",
                "Deny Request");

            if (reason == null) return; // User cancelled

            int adminId = getAdminId();
            
            if (PasswordResetManager.denyRequest(requestId, adminId, reason)) {
                JOptionPane.showMessageDialog(this,
                    "Request denied successfully!\n" +
                    "User: " + username + " has been notified.",
                    "Denied",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadRequests();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error denying request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("Error denying request: " + e);
            JOptionPane.showMessageDialog(this,
                "Error denying request: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getAdminId() {
        try {
            Connection conn = DB.connect();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT id FROM admin WHERE username = ?"
            );
            pst.setString(1, Session.fullname);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            System.out.println("Error getting admin ID: " + e);
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            PasswordResetRequestDialog dialog = new PasswordResetRequestDialog(frame);
            dialog.setVisible(true);
        });
    }
}

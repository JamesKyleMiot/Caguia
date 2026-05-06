package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Dialog for AdminDashboard to review and approve/reject loan applications
 */
public class LoanApplicationReviewDialog extends JDialog {

    private JTable applicationsTable;
    private JButton approveBtn, rejectBtn, refreshBtn, closeBtn;
    private JTextArea detailsArea;
    private DecimalFormat df = new DecimalFormat("0.00");

    public LoanApplicationReviewDialog(JFrame parent) {
        super(parent, "Loan Application Review", true);
        setupUI();
        loadApplications();
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("📋 Pending Loan Applications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        applicationsTable = new JTable();
        applicationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicationsTable.getSelectionModel().addListSelectionListener(e -> showApplicationDetails());
        JScrollPane tableScroll = new JScrollPane(applicationsTable);
        tableScroll.setPreferredSize(new Dimension(900, 300));

        // Details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Application Details"));
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(900, 150));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        approveBtn = new JButton("✓ Approve Application");
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> approveApplication());

        rejectBtn = new JButton("✗ Reject Application");
        rejectBtn.setBackground(new Color(244, 67, 54));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.addActionListener(e -> rejectApplication());

        refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadApplications());

        closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        // Assembly
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(tableScroll, BorderLayout.CENTER);
        contentPanel.add(detailsPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadApplications() {
        List<Map<String, Object>> applications = AdminLoanHelper.getPendingLoanApplications();
        
        String[] columns = {
            "ID", "Username", "Full Name", "Amount", "Purpose", "Applied Date"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Map<String, Object> app : applications) {
            Object[] row = {
                app.get("id"),
                app.get("username"),
                app.get("fullname"),
                "PHP " + df.format(app.get("requested_amount")),
                app.get("purpose"),
                app.get("created_at")
            };
            model.addRow(row);
        }

        applicationsTable.setModel(model);
        applicationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        applicationsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        applicationsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        applicationsTable.getColumnModel().getColumn(4).setPreferredWidth(300);
        applicationsTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        detailsArea.setText("Select an application to view details.");
    }

    private void showApplicationDetails() {
        int selectedRow = applicationsTable.getSelectedRow();
        if (selectedRow < 0) return;

        int appId = (int) applicationsTable.getValueAt(selectedRow, 0);
        String username = (String) applicationsTable.getValueAt(selectedRow, 1);
        String fullname = (String) applicationsTable.getValueAt(selectedRow, 2);

        List<Map<String, Object>> apps = AdminLoanHelper.getPendingLoanApplications();
        for (Map<String, Object> app : apps) {
            if ((int) app.get("id") == appId) {
                String details = "APPLICATION DETAILS\n" +
                               "═══════════════════════════════════════\n" +
                               "Application ID: " + app.get("id") + "\n" +
                               "Username: " + app.get("username") + "\n" +
                               "Full Name: " + app.get("fullname") + "\n" +
                               "Email: " + app.get("email") + "\n" +
                               "Requested Amount: PHP " + df.format(app.get("requested_amount")) + "\n" +
                               "Purpose: " + app.get("purpose") + "\n" +
                               "Applied Date: " + app.get("created_at") + "\n" +
                               "Status: " + app.get("status") + "\n" +
                               "═══════════════════════════════════════\n\n" +
                               "Click 'Approve' or 'Reject' to proceed.";
                detailsArea.setText(details);
                break;
            }
        }
    }

    private void approveApplication() {
        int selectedRow = applicationsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appId = (int) applicationsTable.getValueAt(selectedRow, 0);
        String amountStr = ((String) applicationsTable.getValueAt(selectedRow, 3)).replace("PHP ", "").trim();
        double requestedAmount = Double.parseDouble(amountStr);

        String approvedAmountStr = JOptionPane.showInputDialog(this,
            "Enter approved amount (default: " + requestedAmount + "):",
            requestedAmount);

        if (approvedAmountStr == null) return;

        try {
            double approvedAmount = Double.parseDouble(approvedAmountStr);
            int loanId = LoanApplicationHelper.approveLoanApplication(appId, Session.adminId, approvedAmount);

            if (loanId > 0) {
                JOptionPane.showMessageDialog(this,
                    "✓ Application approved!\nLoan ID: #" + loanId + "\nAmount: PHP " + df.format(approvedAmount),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadApplications();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve application.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectApplication() {
        int selectedRow = applicationsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appId = (int) applicationsTable.getValueAt(selectedRow, 0);
        String reason = JOptionPane.showInputDialog(this,
            "Enter rejection reason:",
            "Insufficient credit history");

        if (reason == null) return;

        boolean ok = LoanApplicationHelper.rejectLoanApplication(appId, Session.adminId, reason);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "✓ Application rejected.\nReason: " + reason,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadApplications();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reject application.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

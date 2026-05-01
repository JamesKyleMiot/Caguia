package caguioa.bank;

import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class LoanManagementDialog extends JDialog {
    private JTable overdueLoansTable;
    private JButton deactivateBtn;
    private JButton processPaymentBtn;
    private JButton reactivateBtn;
    private JButton sendReminderBtn;
    private JLabel selectedLoanLabel;
    private int selectedLoanId = -1;
    private int selectedUserId = -1;

    public LoanManagementDialog(Frame owner) {
        super(owner, "Loan Management - Overdue & Account Control", true);
        initializeUI();
        loadOverdueLoans();
        setSize(1100, 700);
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 248, 245));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(230, 245, 240));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel("Loan Management & Account Control");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(34, 139, 34));

        JLabel descLabel = new JLabel("Manage overdue loans, deactivate accounts, and process payments");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(70, 130, 100));

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        headerText.add(titleLabel, BorderLayout.NORTH);
        headerText.add(descLabel, BorderLayout.SOUTH);
        headerPanel.add(headerText, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Table Panel
        overdueLoansTable = createStyledTable();
        JScrollPane tableScroll = new JScrollPane(overdueLoansTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            "Overdue Loans (Active & Not Blocked)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            new Color(34, 139, 34)
        ));

        // Selection listener
        overdueLoansTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = overdueLoansTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedLoanId = (Integer) overdueLoansTable.getValueAt(selectedRow, 0);
                selectedUserId = (Integer) overdueLoansTable.getValueAt(selectedRow, 1);
                String userName = (String) overdueLoansTable.getValueAt(selectedRow, 2);
                String loanAmount = "₱" + overdueLoansTable.getValueAt(selectedRow, 5);
                selectedLoanLabel.setText("Selected: Loan #" + selectedLoanId + " | User: " + userName + " | Amount: " + loanAmount);
                updateButtonStates();
            }
        });

        contentPanel.add(tableScroll, BorderLayout.CENTER);

        // Selected Loan Info
        selectedLoanLabel = new JLabel("No loan selected");
        selectedLoanLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        selectedLoanLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        selectedLoanLabel.setBackground(new Color(245, 255, 245));
        selectedLoanLabel.setOpaque(true);
        contentPanel.add(selectedLoanLabel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        sendReminderBtn = createStyledButton("📧 Send Reminder", new Color(33, 150, 243));
        sendReminderBtn.addActionListener(e -> sendReminderEmail());

        deactivateBtn = createStyledButton("🔒 Deactivate Account", new Color(244, 67, 54));
        deactivateBtn.addActionListener(e -> deactivateAccount());

        processPaymentBtn = createStyledButton("💳 Process Payment", new Color(76, 175, 80));
        processPaymentBtn.addActionListener(e -> processPayment());

        reactivateBtn = createStyledButton("✅ Reactivate Account", new Color(56, 142, 60));
        reactivateBtn.addActionListener(e -> reactivateAccount());

        JButton refreshBtn = createStyledButton("� Refresh", new Color(158, 158, 158));
        refreshBtn.addActionListener(e -> loadOverdueLoans());

        JButton closeBtn = createStyledButton("Close", new Color(33, 150, 243));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(sendReminderBtn);
        buttonPanel.add(deactivateBtn);
        buttonPanel.add(processPaymentBtn);
        buttonPanel.add(reactivateBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        updateButtonStates();
    }

    private JTable createStyledTable() {
        JTable table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 230, 201));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(76, 175, 80));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedLoanId > 0;
        sendReminderBtn.setEnabled(hasSelection);
        deactivateBtn.setEnabled(hasSelection);
        processPaymentBtn.setEnabled(hasSelection);
        reactivateBtn.setEnabled(hasSelection);
    }

    private void loadOverdueLoans() {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "Loan ID", "User ID", "Full Name", "Username", "Email", 
            "Amount", "Due Date", "Remaining", "Days Overdue", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        java.util.List<Map<String, Object>> overdueLoans = LoanManager.getOverdueLoans();
        
        for (Map<String, Object> loan : overdueLoans) {
            int loanId = (Integer) loan.get("id");
            int userId = (Integer) loan.get("user_id");
            String fullname = (String) loan.get("fullname");
            String username = (String) loan.get("username");
            String email = (String) loan.get("email");
            double amount = (Double) loan.get("amount");
            String dueDate = (String) loan.get("due_date");
            double remaining = (Double) loan.get("remaining_balance");
            
            // Calculate days overdue
            try {
                java.time.LocalDate due = java.time.LocalDate.parse(dueDate);
                java.time.LocalDate today = java.time.LocalDate.now();
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(due, today);
                
                model.addRow(new Object[]{
                    loanId, userId, fullname, username, email,
                    String.format("₱%.2f", amount), dueDate,
                    String.format("₱%.2f", remaining),
                    daysOverdue + " days", "⚠️ OVERDUE"
                });
            } catch (Exception e) {
                model.addRow(new Object[]{
                    loanId, userId, fullname, username, email,
                    String.format("₱%.2f", amount), dueDate,
                    String.format("₱%.2f", remaining), "N/A", "⚠️ OVERDUE"
                });
            }
        }

        overdueLoansTable.setModel(model);
        selectedLoanLabel.setText("No loan selected");
        selectedLoanId = -1;
        selectedUserId = -1;
        updateButtonStates();
    }

    private void sendReminderEmail() {
        if (selectedLoanId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Object> loanDetails = LoanManager.getLoanDetails(selectedLoanId);
        if (loanDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unable to fetch loan details", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = (String) loanDetails.get("email");
        String fullname = (String) loanDetails.get("fullname");
        String loanAmount = String.format("%.2f", loanDetails.get("amount"));
        String dueDate = (String) loanDetails.get("due_date");

        // Send email and store admin message in DB for in-app display
        boolean sent = EmailNotifier.sendLoanDueReminder(email, fullname, loanAmount, dueDate);

        String subject = "Loan Due Reminder";
        String body = "Hello " + fullname + ",\n\nThis is a reminder that your loan (Loan ID: " + selectedLoanId + ") with remaining balance ₱" + loanAmount + " is due on " + dueDate + ".\n\nPlease settle your payment to avoid account suspension.\n\nRegards,\nCaguioa Bank";

        // store admin message regardless of email delivery result
        MessageManager.sendMessageToUser(selectedUserId, Session.adminId, subject, body);

        if (sent) {
            JOptionPane.showMessageDialog(this, "Reminder email sent to: " + email, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Reminder saved for user but email failed. Check email settings.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deactivateAccount() {
        if (selectedLoanId <= 0 || selectedUserId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to DEACTIVATE this user's account?\n\n" +
            "This action will:\n" +
            "• Suspend the user account\n" +
            "• Mark the loan as blocked\n" +
            "• Send a suspension notice email\n\n" +
            "This cannot be undone without reactivation.",
            "Confirm Account Deactivation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = LoanManager.deactivateAccountForUnpaidLoan(selectedLoanId, selectedUserId);
            
            if (success) {
                // Send suspension email
                Map<String, Object> loanDetails = LoanManager.getLoanDetails(selectedLoanId);
                String email = (String) loanDetails.get("email");
                String fullname = (String) loanDetails.get("fullname");
                String loanAmount = String.format("%.2f", loanDetails.get("remaining_balance"));
                
                java.time.LocalDate dueDate = java.time.LocalDate.parse((String) loanDetails.get("due_date"));
                java.time.LocalDate today = java.time.LocalDate.now();
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                
                EmailNotifier.sendAccountSuspensionWarning(email, fullname, loanAmount, String.valueOf(daysOverdue));

                String subject = "Account Suspended - Overdue Loan";
                String body = "Hello " + fullname + ",\n\nYour account has been suspended due to an overdue loan (Loan ID: " + selectedLoanId + ") with remaining balance ₱" + loanAmount + ". Days overdue: " + daysOverdue + ".\n\nPlease contact the bank to resolve this.\n\nRegards,\nCaguioa Bank";
                MessageManager.sendMessageToUser(selectedUserId, Session.adminId, subject, body);

                JOptionPane.showMessageDialog(this, "Account deactivated and suspension notice sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOverdueLoans();
            } else {
                JOptionPane.showMessageDialog(this, "Error deactivating account", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processPayment() {
        if (selectedLoanId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Object> loanDetails = LoanManager.getLoanDetails(selectedLoanId);
        double remaining = (Double) loanDetails.get("remaining_balance");

        String input = JOptionPane.showInputDialog(this,
            "Enter payment amount (Remaining: ₱" + String.format("%.2f", remaining) + "):",
            "Process Payment",
            JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double paymentAmount = Double.parseDouble(input);
                if (paymentAmount <= 0) {
                    JOptionPane.showMessageDialog(this, "Payment must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = LoanManager.processLoanPayment(selectedLoanId, paymentAmount);
                if (success) {
                    // Send confirmation email
                    String email = (String) loanDetails.get("email");
                    String fullname = (String) loanDetails.get("fullname");
                    
                    double newRemaining = remaining - paymentAmount;
                    if (newRemaining <= 0) {
                        EmailNotifier.sendAccountReactivationEmail(email, fullname, String.format("%.2f", paymentAmount));
                        String subject = "Account Reactivated";
                        String body = "Hello " + fullname + ",\n\nYour account has been reactivated after receiving payment of ₱" + String.format("%.2f", paymentAmount) + ".\n\nThank you.\nCaguioa Bank";
                        MessageManager.sendMessageToUser(selectedUserId, Session.adminId, subject, body);
                    } else {
                        String subject = "Payment Received";
                        String body = "Hello " + fullname + ",\n\nWe have received your payment of ₱" + String.format("%.2f", paymentAmount) + ". Remaining balance: ₱" + String.format("%.2f", newRemaining) + ".\n\nThank you.\nCaguioa Bank";
                        MessageManager.sendMessageToUser(selectedUserId, Session.adminId, subject, body);
                    }

                    JOptionPane.showMessageDialog(this, "Payment processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadOverdueLoans();
                } else {
                    JOptionPane.showMessageDialog(this, "Error processing payment", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reactivateAccount() {
        if (selectedLoanId <= 0 || selectedUserId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to REACTIVATE this account?\n\n" +
            "Make sure the loan has been fully paid before reactivating.",
            "Confirm Account Reactivation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = LoanManager.reactivateAccountAfterPayment(selectedLoanId, selectedUserId);
            if (success) {
                Map<String, Object> loanDetails = LoanManager.getLoanDetails(selectedLoanId);
                String fullname = (String) loanDetails.get("fullname");
                String subject = "Account Reactivated by Admin";
                String body = "Hello " + fullname + ",\n\nYour account has been reactivated by admin. If you believe this is an error, please contact support.\n\nRegards,\nCaguioa Bank";
                MessageManager.sendMessageToUser(selectedUserId, Session.adminId, subject, body);

                JOptionPane.showMessageDialog(this, "Account reactivated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadOverdueLoans();
            } else {
                JOptionPane.showMessageDialog(this, "Error reactivating account", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

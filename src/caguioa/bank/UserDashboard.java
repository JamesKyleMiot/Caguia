
package caguioa.bank;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.print.PrinterException;
public class UserDashboard extends javax.swing.JFrame {

    private JPanel dashboardPanel;
    private JLabel balanceValueLabel;
    private JLabel savingsValueLabel;
    private JLabel transactionCountValueLabel;
    private JLabel loanCountValueLabel;
    private JTable dashboardTransactionsTable;
    private JTable dashboardLoansTable;
    private JTable notificationsTable;

    /**
     * Creates new form UserDashboard
     */
    public UserDashboard() {
        initComponents();
        buildLiveDashboard();
        refreshLiveDashboard();
    }

    private double computeLoanAmount(int userId, double loanBase) {
        // Rule: if base (balance or total_deposit) is >= 1000, grant flat 10000.
        // Otherwise grant 10% of base.
        if (loanBase >= 1000) {
            return 10000.0;
        }
        return loanBase * 0.10;
    }

    private Map<String, Object> getActiveLoanForCurrentUser() {


        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT id, amount, total_payable, remaining_balance, due_date, status " +
                "FROM loans WHERE user_id=? AND status='active' ORDER BY id DESC LIMIT 1"
            );
            pst.setInt(1, Session.userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("amount", rs.getDouble("amount"));
                loan.put("total_payable", rs.getDouble("total_payable"));
                loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                loan.put("due_date", rs.getString("due_date"));
                loan.put("status", rs.getString("status"));
                rs.close();
                pst.close();
                return loan;
            }

            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error fetching active loan: " + e);
        }
        return null;
    }

    private boolean promptPaymentForActiveLoan(Map<String, Object> activeLoan) {
        int loanId = (Integer) activeLoan.get("id");
        double remainingBalance = (Double) activeLoan.get("remaining_balance");

        int payNow = JOptionPane.showConfirmDialog(
            this,
            "You already have an active loan.\n\n" +
            "Remaining balance: ₱" + String.format("%.2f", remainingBalance) + "\n\n" +
            "Would you like to pay your loan now?",
            "Existing Loan Found",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (payNow != JOptionPane.YES_OPTION) {
            return false;
        }

        String input = JOptionPane.showInputDialog(
            this,
            "Enter payment amount\nRemaining balance: ₱" + String.format("%.2f", remainingBalance),
            String.format("%.2f", remainingBalance)
        );

        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Payment cancelled");
            return false;
        }

        double paymentAmount;
        try {
            paymentAmount = Double.parseDouble(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid payment amount");
            return false;
        }

        if (paymentAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Payment amount must be greater than zero");
            return false;
        }

        if (paymentAmount > remainingBalance) {
            int capPayment = JOptionPane.showConfirmDialog(
                this,
                "The amount is greater than the remaining balance.\n" +
                "Do you want to pay the full remaining balance instead?",
                "Adjust Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (capPayment != JOptionPane.YES_OPTION) {
                return false;
            }
            paymentAmount = remainingBalance;
        }

        boolean paid = LoanManager.processLoanPayment(loanId, paymentAmount);
        if (!paid) {
            JOptionPane.showMessageDialog(this, "Unable to process your loan payment.");
            return false;
        }

        double newRemaining = Math.max(0, remainingBalance - paymentAmount);
        if (newRemaining <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Your loan has been fully paid. You can now apply for another loan.",
                "Loan Paid",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Payment successful. Remaining balance: ₱" + String.format("%.2f", newRemaining) +
                "\nYou must fully pay the loan before applying for a new one.",
                "Payment Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
        }

        refreshLiveDashboard();
        return true;
    }

    private void buildLiveDashboard() {
        // Create a wrapper panel for the left side with logo and buttons
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setBackground(new Color(0, 0, 51));

        // Create text-based logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(0, 0, 51));
        logoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(76, 175, 80)),
            new EmptyBorder(18, 10, 18, 10)
        ));

        JLabel bankName = new JLabel("CAGUIOA BANK");
        bankName.setFont(new Font("Serif", Font.BOLD, 20));
        bankName.setForeground(new Color(134, 222, 121));
        bankName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel bankTag = new JLabel("Trusted Digital Banking");
        bankTag.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bankTag.setForeground(new Color(182, 245, 170));
        bankTag.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel logoContent = new JPanel(new GridLayout(2, 1, 0, 4));
        logoContent.setOpaque(false);
        logoContent.add(bankName);
        logoContent.add(bankTag);

        logoPanel.add(logoContent, BorderLayout.CENTER);

        // Add logo at top and buttons at center
        leftWrapper.add(logoPanel, BorderLayout.NORTH);
        leftWrapper.add(jPanel1, BorderLayout.CENTER);

        dashboardPanel = new JPanel(new BorderLayout(12, 12));
        dashboardPanel.setBackground(new Color(240, 248, 245));
        dashboardPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(230, 245, 240));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("User Transaction Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(34, 139, 34));

        JLabel subtitle = new JLabel("Track transactions, loan history, and admin notifications in one place.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(70, 130, 100));

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);
        header.add(headerText, BorderLayout.CENTER);

        JPanel headerActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        // Add Refresh button to header
        JButton applyLoanBtn = new JButton("📋 Apply for Loan");
        applyLoanBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        applyLoanBtn.setBackground(new Color(33, 150, 243));
        applyLoanBtn.setForeground(Color.WHITE);
        applyLoanBtn.setFocusPainted(false);
        applyLoanBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        applyLoanBtn.addActionListener(evt -> openLoanApplication());

        JButton payLoanBtn = new JButton("💳 Pay Loan");
        payLoanBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        payLoanBtn.setBackground(new Color(76, 175, 80));
        payLoanBtn.setForeground(Color.WHITE);
        payLoanBtn.setFocusPainted(false);
        payLoanBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        payLoanBtn.addActionListener(evt -> openLoanPayment());

        JButton profileBtn = new JButton("👤 My Profile");
        profileBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        profileBtn.setBackground(new Color(156, 39, 176));
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        profileBtn.addActionListener(evt -> openUserProfile());

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(76, 175, 80));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        refreshBtn.addActionListener(evt -> refreshLiveDashboard());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(0, 0, 51));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        logoutBtn.addActionListener(evt -> logoutToSignInUsers());

        headerActions.add(applyLoanBtn);
        headerActions.add(payLoanBtn);
        headerActions.add(profileBtn);
        headerActions.add(refreshBtn);
        headerActions.add(logoutBtn);
        header.add(headerActions, BorderLayout.EAST);

        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        summaryPanel.setOpaque(false);

        balanceValueLabel = new JLabel("₱0.00", SwingConstants.CENTER);
        savingsValueLabel = new JLabel("₱0.00", SwingConstants.CENTER);
        transactionCountValueLabel = new JLabel("0", SwingConstants.CENTER);
        loanCountValueLabel = new JLabel("0", SwingConstants.CENTER);

        summaryPanel.add(createInfoCard("Current Balance", balanceValueLabel, new Color(34, 180, 100)));
        summaryPanel.add(createInfoCard("Savings", savingsValueLabel, new Color(46, 204, 113)));
        summaryPanel.add(createInfoCard("Transactions", transactionCountValueLabel, new Color(56, 142, 60)));
        summaryPanel.add(createInfoCard("Loans", loanCountValueLabel, new Color(27, 94, 32)));

        dashboardTransactionsTable = new JTable();
        dashboardTransactionsTable.setRowHeight(26);
        dashboardTransactionsTable.setShowGrid(true);
        dashboardTransactionsTable.setGridColor(new Color(188, 226, 158));
        dashboardTransactionsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dashboardTransactionsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        dashboardTransactionsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        dashboardTransactionsTable.getTableHeader().setForeground(Color.BLACK);
        dashboardTransactionsTable.getTableHeader().setReorderingAllowed(false);
        dashboardTransactionsTable.setAutoCreateRowSorter(true);

        JScrollPane transactionScroll = new JScrollPane(dashboardTransactionsTable);
        transactionScroll.setBorder(BorderFactory.createTitledBorder("All User Transactions"));
        transactionScroll.getViewport().setBackground(new Color(245, 252, 250));

        dashboardLoansTable = new JTable();
        dashboardLoansTable.setRowHeight(26);
        dashboardLoansTable.setShowGrid(true);
        dashboardLoansTable.setGridColor(new Color(188, 226, 158));
        dashboardLoansTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dashboardLoansTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        dashboardLoansTable.getTableHeader().setBackground(new Color(76, 175, 80));
        dashboardLoansTable.getTableHeader().setForeground(Color.BLACK);
        dashboardLoansTable.getTableHeader().setReorderingAllowed(false);
        dashboardLoansTable.setAutoCreateRowSorter(true);

        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.addTab("Transactions", transactionScroll);

        // Notifications table (admin -> user)
        notificationsTable = new JTable();
        notificationsTable.setRowHeight(26);
        notificationsTable.setShowGrid(true);
        notificationsTable.setGridColor(new Color(188, 226, 158));
        notificationsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        notificationsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        notificationsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        notificationsTable.getTableHeader().setForeground(Color.BLACK);
        notificationsTable.getTableHeader().setReorderingAllowed(false);
        notificationsTable.setAutoCreateRowSorter(true);

        JScrollPane notificationsScroll = new JScrollPane(notificationsTable);
        notificationsScroll.setBorder(BorderFactory.createTitledBorder("All Notifications"));
        notificationsScroll.setPreferredSize(new Dimension(420, 200));

        JButton privacyBtn = new JButton("Privacy");
        privacyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        privacyBtn.setBackground(new Color(33, 150, 243));
        privacyBtn.setForeground(Color.WHITE);
        privacyBtn.setFocusPainted(false);
        privacyBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        privacyBtn.addActionListener(evt -> showPrivacyNotice());

        JPanel notificationsHeader = new JPanel(new BorderLayout());
        notificationsHeader.setOpaque(false);
        notificationsHeader.add(privacyBtn, BorderLayout.EAST);

        JPanel notificationsPanel = new JPanel(new BorderLayout(8, 8));
        notificationsPanel.setOpaque(false);
        notificationsPanel.add(notificationsHeader, BorderLayout.NORTH);
        notificationsPanel.add(notificationsScroll, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.add(summaryPanel, BorderLayout.NORTH);
        content.add(mainTabs, BorderLayout.CENTER);
        content.add(notificationsPanel, BorderLayout.EAST);

        dashboardPanel.add(header, BorderLayout.NORTH);
        dashboardPanel.add(content, BorderLayout.CENTER);

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout(12, 12));
        leftWrapper.setPreferredSize(new Dimension(250, 0));
        contentPane.add(leftWrapper, BorderLayout.WEST);
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
        pack();
        setMinimumSize(new Dimension(1360, 780));
    }

    private void refreshLiveDashboard() {
        try {
            Connection con = DB.connect();

            PreparedStatement account = con.prepareStatement(
                "SELECT balance, savings, total_deposit FROM users WHERE id=?"
            );
            account.setInt(1, Session.userId);
            ResultSet accountRs = account.executeQuery();

            double balance = 0;
            double savings = 0;
            double totalDeposit = 0;

            if (accountRs.next()) {
                balance = accountRs.getDouble("balance");
                savings = accountRs.getDouble("savings");
                totalDeposit = accountRs.getDouble("total_deposit");
            }

            PreparedStatement txCountStmt = con.prepareStatement(
                "SELECT COUNT(*) AS total_transactions FROM transactions WHERE user_id=?"
            );
            txCountStmt.setInt(1, Session.userId);
            ResultSet txCountRs = txCountStmt.executeQuery();
            int totalTransactions = txCountRs.next() ? txCountRs.getInt("total_transactions") : 0;

            PreparedStatement loanCountStmt = con.prepareStatement(
                "SELECT COUNT(*) AS total_loans FROM loans WHERE user_id=?"
            );
            loanCountStmt.setInt(1, Session.userId);
            ResultSet loanCountRs = loanCountStmt.executeQuery();
            int totalLoans = loanCountRs.next() ? loanCountRs.getInt("total_loans") : 0;

            balanceValueLabel.setText(formatMoney(balance));
            savingsValueLabel.setText(formatMoney(savings));
            transactionCountValueLabel.setText(String.valueOf(totalTransactions));
            loanCountValueLabel.setText(String.valueOf(totalLoans));

            dashboardTransactionsTable.setModel(buildTableModel(
                con,
                "SELECT user_id, type, amount, method, created_at FROM transactions WHERE user_id=? ORDER BY created_at DESC",
                Session.userId
            ));
            // load all admin notifications for this user
            refreshNotifications();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private DefaultTableModel buildTableModel(Connection con, String sql, int userId) throws Exception {
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columns[i - 1] = metaData.getColumnLabel(i);
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }

        return model;
    }

    private DefaultTableModel buildLoanTableModel() {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "Loan ID", "Amount", "Interest", "Total Payable", "Remaining", "Status", "Due Date", "Blocked"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        java.util.List<java.util.Map<String, Object>> loans = LoanManager.getUserLoans(Session.userId);
        for (java.util.Map<String, Object> loan : loans) {
            model.addRow(new Object[]{
                loan.get("id"),
                formatMoney(toDouble(loan.get("amount"))),
                String.format("%.2f%%", toDouble(loan.get("interest_rate")) * 100),
                formatMoney(toDouble(loan.get("total_payable"))),
                formatMoney(toDouble(loan.get("remaining_balance"))),
                loan.get("status"),
                loan.get("due_date"),
                Boolean.TRUE.equals(loan.get("is_blocked")) ? "Yes" : "No"
            });
        }

        if (loans.isEmpty()) {
            model.addRow(new Object[]{"-", "-", "-", "-", "-", "No loans yet", "-", "-"});
        }

        return model;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void refreshNotifications() {
        try {
            java.util.List<java.util.Map<String, Object>> msgs = MessageManager.getAllUserMessages(Session.userId);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Status", "Date", "Subject", "Preview"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (java.util.Map<String, Object> m : msgs) {
                boolean read = Boolean.TRUE.equals(m.get("is_read"));
                String status = read ? "Read" : "Unread";
                String subject = m.get("subject") == null ? "" : String.valueOf(m.get("subject"));
                String body = m.get("body") == null ? "" : String.valueOf(m.get("body"));
                String preview = body.length() > 80 ? body.substring(0, 80) + "..." : body;
                model.addRow(new Object[]{status, m.get("created_at"), subject, preview});
            }

            if (msgs.isEmpty()) {
                model.addRow(new Object[]{"-", "-", "No notifications", "Admin notifications will appear here."});
            }

            notificationsTable.setModel(model);
        } catch (Exception e) {
            System.out.println("refreshNotifications error: " + e);
        }
    }

    /**
     * Send an admin message to a user and refresh the notifications table.
     * This is a small helper that UI actions can call after MessageManager.sendMessageToUser.
     */
    public void sendAdminMessage(int targetUserId, String subject, String body) {
        if (targetUserId <= 0) return;
        boolean saved = MessageManager.sendMessageToUser(targetUserId, Session.adminId, subject, body);
        refreshNotifications();
        if (saved) {
            JOptionPane.showMessageDialog(this, "Message sent to user.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Message saved but delivery may have failed.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showPrivacyNotice() {
        String privacyText = "Privacy Notice\n\n"
            + "- Your notifications are shown only inside your account.\n"
            + "- Admin messages may include loan reminders, account status, and payment updates.\n"
            + "- Do not share your account details with anyone.\n"
            + "- If you log out, other users cannot see your notifications.\n\n"
            + "This is a local banking dashboard privacy reminder.";

        JTextArea textArea = new JTextArea(privacyText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JOptionPane.showMessageDialog(
            this,
            textArea,
            "Privacy",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JPanel createInfoCard(String labelText, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 1, 1, 1, accentColor),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(Color.BLACK);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(label, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        DepositBtn = new javax.swing.JButton();
        WithdrawBtn = new javax.swing.JButton();
        LoanBtn = new javax.swing.JButton();
        TransactionBtn = new javax.swing.JButton();
        TransferSavingsBtn = new javax.swing.JButton();
        WithdrawSavingsBtn = new javax.swing.JButton();
        ViewSavingsBtn = new javax.swing.JButton();
        CheckBalanceBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("UserDashboard");

        jPanel1.setBackground(new java.awt.Color(0, 102, 51));

        DepositBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        DepositBtn.setText("Deposit");
        DepositBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DepositBtnActionPerformed(evt);
            }
        });

        WithdrawBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        WithdrawBtn.setText("Withdraw");
        WithdrawBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WithdrawBtnActionPerformed(evt);
            }
        });

        LoanBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        LoanBtn.setText("Loan");
        LoanBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoanBtnActionPerformed(evt);
            }
        });

        TransactionBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        TransactionBtn.setText("All Information");
        TransactionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TransactionBtnActionPerformed(evt);
            }
        });

        TransferSavingsBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        TransferSavingsBtn.setText("Transfer Savings");
        TransferSavingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TransferSavingsBtnActionPerformed(evt);
            }
        });

        WithdrawSavingsBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        WithdrawSavingsBtn.setText("Withdraw Savings");
        WithdrawSavingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WithdrawSavingsBtnActionPerformed(evt);
            }
        });

        ViewSavingsBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ViewSavingsBtn.setText("View Savings");
        ViewSavingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ViewSavingsBtnActionPerformed(evt);
            }
        });

        CheckBalanceBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        CheckBalanceBtn.setText("Check Balance");
        CheckBalanceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckBalanceBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(CheckBalanceBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(ViewSavingsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(WithdrawSavingsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TransferSavingsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(TransactionBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(WithdrawBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(LoanBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(DepositBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(134, 134, 134)
                .addComponent(DepositBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(WithdrawBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(LoanBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(TransactionBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(TransferSavingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(WithdrawSavingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(CheckBalanceBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ViewSavingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 539, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void DepositBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DepositBtnActionPerformed
        if (Session.userId <= 0) {
            JOptionPane.showMessageDialog(this, "No user session found. Please log in again.");
            return;
        }

        String amount = JOptionPane.showInputDialog(this, "Enter Amount");

        if (amount == null || amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        String[] depositMethods = {"GCash", "PayMaya", "Bank"};
        String method = (String) JOptionPane.showInputDialog(
            this,
            "Select Deposit Method",
            "Deposit",
            JOptionPane.QUESTION_MESSAGE,
            null,
            depositMethods,
            depositMethods[0]
        );

        if (method == null) {
            JOptionPane.showMessageDialog(this, "Deposit cancelled");
            return;
        }

        String finalMethod = method;
        if ("Bank".equals(method)) {
            String[] bankOptions = {"BDO", "LandBank", "UnionBank", "GoTymeBank", "SeaBank"};
            String bank = (String) JOptionPane.showInputDialog(
                this,
                "Select Bank",
                "Deposit Bank",
                JOptionPane.QUESTION_MESSAGE,
                null,
                bankOptions,
                bankOptions[0]
            );

            if (bank == null) {
                JOptionPane.showMessageDialog(this, "Deposit cancelled");
                return;
            }

            finalMethod = "Bank - " + bank;
        }

        double depositAmount;

        try {
            depositAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        try {
            Connection con = DB.connect();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed. Please try again.");
                return;
            }

            PreparedStatement check = con.prepareStatement(
                "SELECT balance FROM users WHERE id=?"
            );
            check.setInt(1, Session.userId);
            ResultSet rsCheck = check.executeQuery();
            double previousBalance = 0;
            if (rsCheck.next()) {
                previousBalance = rsCheck.getDouble("balance");
            }

            PreparedStatement pst = con.prepareStatement(
                "UPDATE users SET balance=balance+?, total_deposit=total_deposit+? WHERE id=?"
            );

            pst.setDouble(1, depositAmount);
            pst.setDouble(2, depositAmount);
            pst.setInt(3, Session.userId);
            pst.executeUpdate();

            // Record transaction using TransactionManager
            TransactionManager.recordTransaction(Session.userId, TransactionManager.TYPE_DEPOSIT, depositAmount, finalMethod);

            JOptionPane.showMessageDialog(this, "Deposit Successful");

            double newBalance = previousBalance + depositAmount;

            String receipt = "CAGUIOA BANK\n"
                + "Deposit Receipt\n"
                + "-------------------------\n"
                + "User ID: " + Session.userId + "\n"
                + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                + "Transaction: Deposit\n"
                + "Method: " + finalMethod + "\n"
                + "Amount: " + depositAmount + "\n"
                + "Previous Balance: " + previousBalance + "\n"
                + "New Balance: " + newBalance + "\n"
                + "-------------------------\n"
                + "Thank you.";

            JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);

            int printOption = JOptionPane.showConfirmDialog(
                this,
                "Do you want to print this receipt?",
                "Print Receipt",
                JOptionPane.YES_NO_OPTION
            );

            if (printOption == JOptionPane.YES_OPTION) {
                printReceipt(receipt);
            }

            refreshLiveDashboard();

        } catch (Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Deposit failed");
        }
    }//GEN-LAST:event_DepositBtnActionPerformed

    private void LoanBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoanBtnActionPerformed
        openLoanApplication();
    }//GEN-LAST:event_LoanBtnActionPerformed

    private void WithdrawBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WithdrawBtnActionPerformed
        String amount = JOptionPane.showInputDialog(this, "Enter Amount");

        if (amount == null || amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        double withdrawAmount;
        try {
            withdrawAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        if (withdrawAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than zero");
            return;
        }

        // Select withdrawal method (similar to deposit)
        String[] withdrawMethods = {"ATM/Cash Pickup", "GCash Transfer", "PayMaya Transfer", "Bank Transfer"};
        String method = (String) JOptionPane.showInputDialog(
            this,
            "Select Withdrawal Method",
            "Withdrawal",
            JOptionPane.QUESTION_MESSAGE,
            null,
            withdrawMethods,
            withdrawMethods[0]
        );

        if (method == null) {
            JOptionPane.showMessageDialog(this, "Withdrawal cancelled");
            return;
        }

        String finalMethod = method;
        if ("Bank Transfer".equals(method)) {
            String[] bankOptions = {"BDO", "LandBank", "UnionBank", "GoTymeBank", "SeaBank"};
            String bank = (String) JOptionPane.showInputDialog(
                this,
                "Select Bank",
                "Select Bank for Transfer",
                JOptionPane.QUESTION_MESSAGE,
                null,
                bankOptions,
                bankOptions[0]
            );

            if (bank == null) {
                JOptionPane.showMessageDialog(this, "Withdrawal cancelled");
                return;
            }

            finalMethod = "Bank Transfer - " + bank;
        }

        try {
            Connection con = DB.connect();

            PreparedStatement check = con.prepareStatement(
                "SELECT balance FROM users WHERE id=?"
            );
            check.setInt(1, Session.userId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "User account not found");
                return;
            }

            double currentBalance = rs.getDouble("balance");

            if (withdrawAmount > currentBalance) {
                JOptionPane.showMessageDialog(this, "Insufficient balance. Current balance: " + currentBalance);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Withdraw " + withdrawAmount + " via " + finalMethod + "?",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Withdrawal cancelled");
                return;
            }

            PreparedStatement pst = con.prepareStatement(
                "UPDATE users SET balance=balance-? WHERE id=?"
            );

            pst.setDouble(1, withdrawAmount);
            pst.setInt(2, Session.userId);
            pst.executeUpdate();

            // Record transaction using TransactionManager
            TransactionManager.recordTransaction(Session.userId, TransactionManager.TYPE_WITHDRAWAL, withdrawAmount, finalMethod);

            double newBalance = currentBalance - withdrawAmount;

            // Ask if user wants to pick up money or have it transferred
            String[] pickupOptions = {"Pick Up Cash at Counter", "Confirm Transfer"};
            String pickupChoice = (String) JOptionPane.showInputDialog(
                this,
                "How do you want to receive your money?",
                "Get Money",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pickupOptions,
                pickupOptions[0]
            );

            if (pickupChoice == null) {
                pickupChoice = "Confirm Transfer";
            }

            JOptionPane.showMessageDialog(this, "Withdrawal successful - " + pickupChoice);

            String receipt = "CAGUIOA BANK\n"
                + "Withdrawal Receipt\n"
                + "=========================\n"
                + "User ID: " + Session.userId + "\n"
                + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                + "Transaction: Withdrawal\n"
                + "Method: " + finalMethod + "\n"
                + "Pickup Option: " + pickupChoice + "\n"
                + "Amount: ₱" + withdrawAmount + "\n"
                + "Previous Balance: ₱" + currentBalance + "\n"
                + "New Balance: ₱" + newBalance + "\n"
                + "=========================\n"
                + "Status: APPROVED\n"
                + "Thank you.";

            JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);

            int printOption = JOptionPane.showConfirmDialog(
                this,
                "Do you want to print this receipt?",
                "Print Receipt",
                JOptionPane.YES_NO_OPTION
            );

            if (printOption == JOptionPane.YES_OPTION) {
                printReceipt(receipt);
            }

            refreshLiveDashboard();

        } catch(Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Withdrawal failed");
        }

    }//GEN-LAST:event_WithdrawBtnActionPerformed

    private void printReceipt(String receiptText) {
        JTextArea printableReceipt = new JTextArea(receiptText);
        printableReceipt.setEditable(false);
        printableReceipt.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        try {
            boolean printed = printableReceipt.print(null, null, true, null, null, true);
            if (printed) {
                JOptionPane.showMessageDialog(this, "Receipt sent to printer successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled");
            }
        } catch (PrinterException e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Unable to print receipt");
        }
    }

    private void TransferSavingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TransferSavingsBtnActionPerformed
        String input = JOptionPane.showInputDialog(this, "Enter Amount to Transfer to Savings");

        if (input == null || input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        double transferAmount;
        try {
            transferAmount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        if (transferAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than zero");
            return;
        }

        try {
            Connection con = DB.connect();

            PreparedStatement check = con.prepareStatement(
                "SELECT balance FROM users WHERE id=?"
            );
            check.setInt(1, Session.userId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "User account not found");
                return;
            }

            double currentBalance = rs.getDouble("balance");

            if (transferAmount > currentBalance) {
                JOptionPane.showMessageDialog(this, "Insufficient balance. Current balance: " + currentBalance);
                return;
            }

            PreparedStatement pst = con.prepareStatement(
                "UPDATE users SET balance = balance - ?, savings = savings + ? WHERE id=?"
            );

            pst.setDouble(1, transferAmount);
            pst.setDouble(2, transferAmount);
            pst.setInt(3, Session.userId);
            pst.executeUpdate();

            // Record transaction using TransactionManager
            TransactionManager.recordTransaction(Session.userId, TransactionManager.TYPE_SAVINGS_TRANSFER, transferAmount, "Savings Transfer");

            double newBalance = currentBalance - transferAmount;

            JOptionPane.showMessageDialog(this, "Transfer Successful. New balance: " + newBalance);

        } catch(Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Transfer failed");
        }
    }//GEN-LAST:event_TransferSavingsBtnActionPerformed

    private void WithdrawSavingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WithdrawSavingsBtnActionPerformed
        String input = JOptionPane.showInputDialog(this, "Enter Amount to Withdraw from Savings");

        if (input == null || input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        double withdrawAmount;
        try {
            withdrawAmount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
            return;
        }

        if (withdrawAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than zero");
            return;
        }

        try {
            Connection con = DB.connect();

            PreparedStatement check = con.prepareStatement(
                "SELECT savings FROM users WHERE id=?"
            );
            check.setInt(1, Session.userId);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "User account not found");
                return;
            }

            double currentSavings = rs.getDouble("savings");

            if (withdrawAmount > currentSavings) {
                JOptionPane.showMessageDialog(this, "Insufficient savings. Current savings: " + currentSavings);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Withdraw " + withdrawAmount + " from your savings?",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Withdrawal cancelled");
                return;
            }

            PreparedStatement pst = con.prepareStatement(
                "UPDATE users SET savings = savings - ?, balance = balance + ? WHERE id=?"
            );

            pst.setDouble(1, withdrawAmount);
            pst.setDouble(2, withdrawAmount);
            pst.setInt(3, Session.userId);
            pst.executeUpdate();

            // Record transaction using TransactionManager
            TransactionManager.recordTransaction(Session.userId, TransactionManager.TYPE_WITHDRAWAL, withdrawAmount, "Savings Withdrawal");

            double newSavings = currentSavings - withdrawAmount;

            JOptionPane.showMessageDialog(this, "Withdraw from Savings Successful");

            String receipt = "CAGUIOA BANK\n"
                + "Savings Withdrawal Receipt\n"
                + "-------------------------\n"
                + "User ID: " + Session.userId + "\n"
                + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                + "Transaction: Withdraw from Savings\n"
                + "Amount: " + withdrawAmount + "\n"
                + "Previous Savings: " + currentSavings + "\n"
                + "New Savings: " + newSavings + "\n"
                + "-------------------------\n"
                + "Thank you.";

            JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);

            int printOption = JOptionPane.showConfirmDialog(
                this,
                "Do you want to print this receipt?",
                "Print Receipt",
                JOptionPane.YES_NO_OPTION
            );

            if (printOption == JOptionPane.YES_OPTION) {
                printReceipt(receipt);
            }

        } catch(Exception e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "Withdraw from savings failed");
        }
    }//GEN-LAST:event_WithdrawSavingsBtnActionPerformed

    private void ViewSavingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewSavingsBtnActionPerformed
try {

        Connection con = DB.connect();

        PreparedStatement pst = con.prepareStatement(
            "SELECT savings FROM users WHERE id=?"
        );

        pst.setInt(1, Session.userId);

        ResultSet rs = pst.executeQuery();

        if(rs.next()){
            JOptionPane.showMessageDialog(this,
                "Your Savings: " + rs.getDouble("savings"));
        }

    } catch(Exception e){
        System.out.println(e);
    }    }//GEN-LAST:event_ViewSavingsBtnActionPerformed

    private void TransactionBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TransactionBtnActionPerformed
        showAllRecordsDialog();
    }//GEN-LAST:event_TransactionBtnActionPerformed

    private void showAllRecordsDialog() {
        JDialog dialog = new JDialog(this, "Profile", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(new Dimension(1120, 780));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 247, 250));
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 0, 51));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("  User Profile");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("  Profile, balance, savings, transactions, and loans in one view");
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(title, BorderLayout.NORTH);
        titleWrap.add(subtitle, BorderLayout.SOUTH);
        header.add(titleWrap, BorderLayout.CENTER);
        dialog.add(header, BorderLayout.NORTH);

        JPanel overviewPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        overviewPanel.setBorder(new EmptyBorder(12, 12, 0, 12));
        overviewPanel.setOpaque(false);

        try {
            Connection con = DB.connect();

            PreparedStatement account = con.prepareStatement(
                "SELECT balance, savings, total_deposit FROM users WHERE id=?"
            );
            account.setInt(1, Session.userId);
            ResultSet accountRs = account.executeQuery();

            double balance = 0;
            double savings = 0;
            double totalDeposit = 0;

            if (accountRs.next()) {
                balance = accountRs.getDouble("balance");
                savings = accountRs.getDouble("savings");
                totalDeposit = accountRs.getDouble("total_deposit");
            }

            PreparedStatement txCountStmt = con.prepareStatement(
                "SELECT COUNT(*) AS total_transactions FROM transactions WHERE user_id=?"
            );
            txCountStmt.setInt(1, Session.userId);
            ResultSet txCountRs = txCountStmt.executeQuery();
            int totalTransactions = txCountRs.next() ? txCountRs.getInt("total_transactions") : 0;

            PreparedStatement loanCountStmt = con.prepareStatement(
                "SELECT COUNT(*) AS total_loans FROM loans WHERE user_id=?"
            );
            loanCountStmt.setInt(1, Session.userId);
            ResultSet loanCountRs = loanCountStmt.executeQuery();
            int totalLoans = loanCountRs.next() ? loanCountRs.getInt("total_loans") : 0;

            overviewPanel.add(createInfoCard("Current Balance", formatMoney(balance), new Color(52, 152, 219)));
            overviewPanel.add(createInfoCard("Savings", formatMoney(savings), new Color(46, 204, 113)));
            overviewPanel.add(createInfoCard("Total Deposit", formatMoney(totalDeposit), new Color(155, 89, 182)));
            overviewPanel.add(createInfoCard("Transactions", String.valueOf(totalTransactions), new Color(241, 196, 15)));
            overviewPanel.add(createInfoCard("Loans", String.valueOf(totalLoans), new Color(231, 76, 60)));
            overviewPanel.add(createInfoCard("Account Health", totalDeposit >= 50000 ? "Loan Ready" : "Build Deposits", new Color(127, 140, 141)));

            JTabbedPane tabs = new JTabbedPane();

            JTable profileTable = buildProfileTable(con);
            JTable transactionTable = buildResultTable(con, "SELECT id, user_id, type, amount, method, created_at FROM transactions WHERE user_id=? ORDER BY id DESC");
            JTable loanTable = buildResultTable(con, "SELECT * FROM loans WHERE user_id=? ORDER BY 1 DESC");

            tabs.addTab("Profile", new JScrollPane(profileTable));
            tabs.addTab("Transactions", new JScrollPane(transactionTable));
            tabs.addTab("Loans", new JScrollPane(loanTable));

            JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
            centerPanel.setOpaque(false);
            centerPanel.add(overviewPanel, BorderLayout.NORTH);
            centerPanel.add(tabs, BorderLayout.CENTER);
            dialog.add(centerPanel, BorderLayout.CENTER);

        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Unable to load records: " + e.getMessage()), BorderLayout.CENTER);
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Error", errorPanel);
            JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
            centerPanel.setOpaque(false);
            centerPanel.add(overviewPanel, BorderLayout.NORTH);
            centerPanel.add(tabs, BorderLayout.CENTER);
            dialog.add(centerPanel, BorderLayout.CENTER);
            System.out.println(e);
        }

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.dispose();
            }
        });
        footer.add(closeButton);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showAllBalancesDialog() {
        JDialog dialog = new JDialog(this, "My Balance", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(new Dimension(1120, 720));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 247, 250));
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 0, 51));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("  My Balance Overview");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("  Balance, savings, and total deposits for your logged-in account only");
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(title, BorderLayout.NORTH);
        titleWrap.add(subtitle, BorderLayout.SOUTH);
        header.add(titleWrap, BorderLayout.CENTER);
        dialog.add(header, BorderLayout.NORTH);

        JPanel overviewPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        overviewPanel.setBorder(new EmptyBorder(12, 12, 0, 12));
        overviewPanel.setOpaque(false);

        try {
            Connection con = DB.connect();

            PreparedStatement userStmt = con.prepareStatement(
                "SELECT balance, savings, total_deposit FROM users WHERE id=?"
            );
            userStmt.setInt(1, Session.userId);
            ResultSet userRs = userStmt.executeQuery();

            double balance = 0;
            double savings = 0;
            double totalDeposit = 0;

            if (userRs.next()) {
                balance = userRs.getDouble("balance");
                savings = userRs.getDouble("savings");
                totalDeposit = userRs.getDouble("total_deposit");
            }

            overviewPanel.add(createInfoCard("My Balance", formatMoney(balance), new Color(52, 152, 219)));
            overviewPanel.add(createInfoCard("My Savings", formatMoney(savings), new Color(46, 204, 113)));
            overviewPanel.add(createInfoCard("My Total Deposit", formatMoney(totalDeposit), new Color(155, 89, 182)));

            JTable balanceTable = buildCurrentUserBalanceTable(con);

            JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
            centerPanel.setOpaque(false);
            centerPanel.add(overviewPanel, BorderLayout.NORTH);
            centerPanel.add(new JScrollPane(balanceTable), BorderLayout.CENTER);
            dialog.add(centerPanel, BorderLayout.CENTER);

        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Unable to load balances: " + e.getMessage()), BorderLayout.CENTER);
            dialog.add(errorPanel, BorderLayout.CENTER);
            System.out.println(e);
        }

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.dispose();
            }
        });
        footer.add(closeButton);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createInfoCard(String labelText, String valueText, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 1, 1, 1, accentColor),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(90, 90, 90));

        JLabel value = new JLabel(valueText, SwingConstants.CENTER);
        value.setFont(new Font("SansSerif", Font.BOLD, 24));
        value.setForeground(accentColor);

        card.add(label, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private String formatMoney(double amount) {
        return String.format("₱%,.2f", amount);
    }

    private JTable buildProfileTable(Connection con) throws Exception {
        PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE id=?");
        pst.setInt(1, Session.userId);
        ResultSet rs = pst.executeQuery();

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Field", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (rs.next()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addRow(new Object[]{metaData.getColumnLabel(i), rs.getObject(i)});
            }
        }

        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private JTable buildResultTable(Connection con, String sql) throws Exception {
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, Session.userId);
        ResultSet rs = pst.executeQuery();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columns[i - 1] = metaData.getColumnLabel(i);
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private JTable buildCurrentUserBalanceTable(Connection con) throws Exception {
        PreparedStatement pst = con.prepareStatement(
            "SELECT id, username, balance, savings, total_deposit FROM users WHERE id=?"
        );
        pst.setInt(1, Session.userId);
        ResultSet rs = pst.executeQuery();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columns[i - 1] = metaData.getColumnLabel(i);
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);
    }

    private void logoutToSignInUsers() {
        Session.isAdmin = false;
        Session.userId = 0;
        Session.role = null;
        Session.fullname = null;
        JOptionPane.showMessageDialog(this, "Logged out successfully.");
        new SignInUsers().setVisible(true);
        dispose();
    }

    private void openLoanApplication() {
        Map<String, Object> activeLoan = getActiveLoanForCurrentUser();
        if (activeLoan != null) {
            boolean paidLoan = promptPaymentForActiveLoan(activeLoan);
            if (!paidLoan) {
                return;
            }

            Map<String, Object> refreshedLoan = getActiveLoanForCurrentUser();
            if (refreshedLoan != null) {
                JOptionPane.showMessageDialog(this,
                    "You still have an active loan. Please pay it in full before applying for another loan.",
                    "Active Loan Exists",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        new LoanApplicationDialog(this, Session.userId).setVisible(true);
    }

    private void openLoanPayment() {
        Map<String, Object> activeLoan = getActiveLoanForCurrentUser();
        if (activeLoan == null) {
            JOptionPane.showMessageDialog(this,
                "You don't have any active loans to pay.\n\nWould you like to apply for a new loan?",
                "No Active Loan",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = {"Online Payment", "Walk-in Payment", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose how you want to pay your loan.\n\nOnline payment uses bank app / digital transfer.\nWalk-in payment uses branch or payment center instructions.",
            "Pay Loan",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 0) {
            new OnlineLoanPaymentDialog(this, Session.userId).setVisible(true);
        } else if (choice == 1) {
            new LoanPaymentDialog(this, Session.userId).setVisible(true);
        }
    }

    private void openUserProfile() {
        UserProfileDialog.open(this);
        refreshLiveDashboard();
    }

    private void CheckBalanceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckBalanceBtnActionPerformed
        showAllBalancesDialog();
    }//GEN-LAST:event_CheckBalanceBtnActionPerformed

    private void LoanStatusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoanStatusBtnActionPerformed
        showLoanStatusDialog();
    }//GEN-LAST:event_LoanStatusBtnActionPerformed

    private void showLoanStatusDialog() {
        JDialog dialog = new JDialog(this, "My Loan Status", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(new Dimension(900, 600));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 247, 250));
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 0, 51));
        header.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("  My Loan Status");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("  All active and inactive loans for your account");
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(title, BorderLayout.NORTH);
        titleWrap.add(subtitle, BorderLayout.SOUTH);
        header.add(titleWrap, BorderLayout.CENTER);
        dialog.add(header, BorderLayout.NORTH);

        try {
            Connection con = DB.connect();
            JTable loanTable = buildResultTable(con, "SELECT id, amount, interest_rate, total_payable, status, created_at FROM loans WHERE user_id=? ORDER BY id DESC");
            loanTable.setRowHeight(26);
            loanTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
            loanTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
            loanTable.getTableHeader().setBackground(new Color(76, 175, 80));
            loanTable.getTableHeader().setForeground(Color.BLACK);

            JScrollPane scrollPane = new JScrollPane(loanTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Loan Records"));
            dialog.add(scrollPane, BorderLayout.CENTER);
        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Unable to load loans: " + e.getMessage()), BorderLayout.CENTER);
            dialog.add(errorPanel, BorderLayout.CENTER);
            System.out.println(e);
        }

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.dispose();
            }
        });
        footer.add(closeButton);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CheckBalanceBtn;
    private javax.swing.JButton DepositBtn;
    private javax.swing.JButton LoanBtn;
    private javax.swing.JButton TransactionBtn;
    private javax.swing.JButton TransferSavingsBtn;
    private javax.swing.JButton ViewSavingsBtn;
    private javax.swing.JButton WithdrawBtn;
    private javax.swing.JButton WithdrawSavingsBtn;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

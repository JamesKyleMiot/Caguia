package caguioa.bank;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboard extends javax.swing.JFrame {

    private JPanel dashboardPanel;
    private JLabel totalUsersValueLabel;
    private JLabel totalTransactionsValueLabel;
    private JLabel totalLoansValueLabel;
    private JLabel totalBalanceValueLabel;
    private JTable allUsersTable;
    private JTable allTransactionsTable;
    private JTable allLoansTable;

    public AdminDashboard() {
        initComponents();
        
        // ⚠️ ADMIN VERIFICATION - Only admins can access this dashboard
        if(!Session.isAdmin) {
            JOptionPane.showMessageDialog(null,
                "❌ ACCESS DENIED!\nAdmin privileges required.\nOnly administrators can access this dashboard.",
                "Admin Dashboard - Access Denied",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);  // Exit the application if non-admin tries to access
        }
        
        buildAdminDashboard();
        refreshAdminDashboard();
    }

    private void buildAdminDashboard() {
        dashboardPanel = new JPanel(new BorderLayout(12, 12));
        dashboardPanel.setBackground(new Color(240, 248, 245));
        dashboardPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(230, 245, 240));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("Admin Dashboard - System Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(34, 139, 34));

        JLabel subtitle = new JLabel("Complete overview of all users, transactions, and system statistics (Admin Only Access)");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(70, 130, 100));

        JPanel headerText = new JPanel(new BorderLayout());
        headerText.setOpaque(false);
        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);
        header.add(createLogoPanel(96, 96), BorderLayout.WEST);
        header.add(headerText, BorderLayout.CENTER);

        JPanel headerActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(76, 175, 80));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        refreshBtn.addActionListener(evt -> refreshAdminDashboard());

        JButton loanMgmtBtn = new JButton("🔒 Loan Management");
        loanMgmtBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        loanMgmtBtn.setBackground(new Color(244, 67, 54));
        loanMgmtBtn.setForeground(Color.WHITE);
        loanMgmtBtn.setFocusPainted(false);
        loanMgmtBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        loanMgmtBtn.addActionListener(evt -> openLoanManagementDialog());

        JButton loanApprovalsBtn = new JButton("📋 Loan Applications");
        loanApprovalsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        loanApprovalsBtn.setBackground(new Color(63, 81, 181));
        loanApprovalsBtn.setForeground(Color.WHITE);
        loanApprovalsBtn.setFocusPainted(false);
        loanApprovalsBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        loanApprovalsBtn.addActionListener(evt -> openLoanApplicationsDialog());

        JButton myDetailsBtn = new JButton("My Details");
        myDetailsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        myDetailsBtn.setBackground(new Color(33, 150, 243));
        myDetailsBtn.setForeground(Color.WHITE);
        myDetailsBtn.setFocusPainted(false);
        myDetailsBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        myDetailsBtn.addActionListener(evt -> showAdminDetailsDialog());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(0, 0, 51));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        logoutBtn.addActionListener(evt -> logoutAdmin());

        headerActions.add(refreshBtn);
        headerActions.add(loanMgmtBtn);
        headerActions.add(loanApprovalsBtn);
        headerActions.add(myDetailsBtn);
        // Init DB button to create all required tables
        JButton initDbBtn = new JButton("Init DB");
        initDbBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        initDbBtn.setBackground(new Color(33, 150, 243));
        initDbBtn.setForeground(Color.WHITE);
        initDbBtn.setFocusPainted(false);
        initDbBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        initDbBtn.addActionListener(evt -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Create/ensure all database tables?", "Init DB", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = DBInit.ensureAllTables();
                if (ok) JOptionPane.showMessageDialog(this, "Database tables created/verified."); else JOptionPane.showMessageDialog(this, "Database initialization failed. Check console.");
                refreshAdminDashboard();
            }
        });
        headerActions.add(initDbBtn);
        headerActions.add(logoutBtn);
        header.add(headerActions, BorderLayout.EAST);

        // Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 12, 12));
        summaryPanel.setOpaque(false);

        totalUsersValueLabel = new JLabel("0", SwingConstants.CENTER);
        totalTransactionsValueLabel = new JLabel("0", SwingConstants.CENTER);
        totalLoansValueLabel = new JLabel("0", SwingConstants.CENTER);
        totalBalanceValueLabel = new JLabel("₱0.00", SwingConstants.CENTER);

        summaryPanel.add(createInfoCard("Total Users", totalUsersValueLabel, new Color(34, 180, 100)));
        summaryPanel.add(createInfoCard("Total Transactions", totalTransactionsValueLabel, new Color(56, 142, 60)));
        summaryPanel.add(createInfoCard("Total Loans", totalLoansValueLabel, new Color(27, 94, 32)));
        summaryPanel.add(createInfoCard("System Balance", totalBalanceValueLabel, new Color(129, 199, 132)));

        // Tables with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);

        // All Users Table
        allUsersTable = new JTable();
        allUsersTable.setRowHeight(26);
        allUsersTable.setShowGrid(true);
        allUsersTable.setGridColor(new Color(188, 226, 158));
        allUsersTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        allUsersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        allUsersTable.getTableHeader().setBackground(new Color(76, 175, 80));
        allUsersTable.getTableHeader().setForeground(Color.BLACK);
        allUsersTable.getTableHeader().setReorderingAllowed(false);
        allUsersTable.setAutoCreateRowSorter(true);

        // Add double-click listener for user profile
        allUsersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int selectedRow = allUsersTable.getSelectedRow();
                    if (selectedRow != -1) {
                        // Get the user ID from the first column
                        Object userId = allUsersTable.getValueAt(selectedRow, 0);
                        if (userId != null) {
                            int userIdInt = Integer.parseInt(userId.toString());
                            openUserProfileForEdit(userIdInt);
                        }
                    }
                }
            }
        });

        JScrollPane usersScroll = new JScrollPane(allUsersTable);
        usersScroll.setBorder(BorderFactory.createTitledBorder("All Users"));
        tabbedPane.addTab("Users", usersScroll);

        // All Loans Table
        allLoansTable = new JTable();
        allLoansTable.setRowHeight(26);
        allLoansTable.setShowGrid(true);
        allLoansTable.setGridColor(new Color(188, 226, 158));
        allLoansTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        allLoansTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        allLoansTable.getTableHeader().setBackground(new Color(76, 175, 80));
        allLoansTable.getTableHeader().setForeground(Color.BLACK);
        allLoansTable.getTableHeader().setReorderingAllowed(false);
        allLoansTable.setAutoCreateRowSorter(true);

        JScrollPane loansScroll = new JScrollPane(allLoansTable);
        loansScroll.setBorder(BorderFactory.createTitledBorder("All Loans"));

        // Loan controls panel (change status, message borrower)
        JPanel loanControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton changeStatusBtn = new JButton("Change Loan Status");
        changeStatusBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        changeStatusBtn.setBackground(new Color(255, 193, 7));
        changeStatusBtn.setForeground(Color.BLACK);
        changeStatusBtn.setFocusPainted(false);
        changeStatusBtn.addActionListener(evt -> changeSelectedLoanStatus());

        JButton messageBorrowerBtn = new JButton("Message Borrower");
        messageBorrowerBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageBorrowerBtn.setBackground(new Color(33, 150, 243));
        messageBorrowerBtn.setForeground(Color.WHITE);
        messageBorrowerBtn.setFocusPainted(false);
        messageBorrowerBtn.addActionListener(evt -> messageSelectedLoanUser());

        loanControlsPanel.add(changeStatusBtn);
        loanControlsPanel.add(messageBorrowerBtn);
        // place controls below the loans tab
        JPanel loansWrapper = new JPanel(new BorderLayout());
        loansWrapper.setOpaque(false);
        loansWrapper.add(loanControlsPanel, BorderLayout.NORTH);
        loansWrapper.add(loansScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Loans", loansWrapper);

        // All Transactions Table
        allTransactionsTable = new JTable();
        allTransactionsTable.setRowHeight(26);
        allTransactionsTable.setShowGrid(true);
        allTransactionsTable.setGridColor(new Color(188, 226, 158));
        allTransactionsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        allTransactionsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        allTransactionsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        allTransactionsTable.getTableHeader().setForeground(Color.BLACK);
        allTransactionsTable.getTableHeader().setReorderingAllowed(false);
        allTransactionsTable.setAutoCreateRowSorter(true);

        JScrollPane transactionsScroll = new JScrollPane(allTransactionsTable);
        transactionsScroll.setBorder(BorderFactory.createTitledBorder("All Transactions"));
        tabbedPane.addTab("Transactions", transactionsScroll);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.add(summaryPanel, BorderLayout.NORTH);
        content.add(tabbedPane, BorderLayout.CENTER);

        dashboardPanel.add(header, BorderLayout.NORTH);
        dashboardPanel.add(content, BorderLayout.CENTER);

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(dashboardPanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Admin Dashboard");
        pack();
        setMinimumSize(new Dimension(1400, 800));
        setLocationRelativeTo(null);
    }

    private JPanel createLogoPanel(int width, int height) {
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(0, 0, 0, 12));
        logoPanel.add(LogoUtil.createLogoLabel(width, height), BorderLayout.CENTER);
        return logoPanel;
    }

    private void logoutAdmin() {
        Session.isAdmin = false;
        Session.userId = 0;
        Session.role = null;
        Session.fullname = null;
        JOptionPane.showMessageDialog(this, "Logged out successfully.");
        new AdminLogin().setVisible(true);
        dispose();
    }

    private void openLoanManagementDialog() {
        LoanManagementDialog dialog = new LoanManagementDialog(this);
        dialog.setVisible(true);
        // Refresh dashboard after closing the dialog
        refreshAdminDashboard();
    }

    private void openUserProfileForEdit(int userId) {
        AdminUserProfileDialog profileDialog = new AdminUserProfileDialog(this, userId);
        profileDialog.showDialog();
        refreshAdminDashboard();
    }

    private void showAdminDetailsDialog() {
        if (Session.fullname == null || Session.fullname.isBlank()) {
            JOptionPane.showMessageDialog(this,
                "No admin session details found. Please log in again.",
                "My Details",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String details = "Admin Details\n"
            + "-------------------------\n"
            + "Username: " + Session.fullname + "\n"
            + "Role: Admin\n"
            + "Admin ID: N/A\n";

        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT id, username FROM admin WHERE username=?"
            );
            pst.setString(1, Session.fullname);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                details = "Admin Details\n"
                    + "-------------------------\n"
                    + "Admin ID: " + rs.getInt("id") + "\n"
                    + "Username: " + rs.getString("username") + "\n"
                    + "Role: Admin\n"
                    + "Access: Full System Overview\n";
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setBackground(new Color(245, 250, 248));
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(
            this,
            textArea,
            "My Details",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void refreshAdminDashboard() {
        try {
            Connection con = DB.connect();

            // Count total users
            Statement userCountStmt = con.createStatement();
            ResultSet userCountRs = userCountStmt.executeQuery("SELECT COUNT(*) AS total FROM users");
            int totalUsers = userCountRs.next() ? userCountRs.getInt("total") : 0;

            // Count total transactions
            Statement transCountStmt = con.createStatement();
            ResultSet transCountRs = transCountStmt.executeQuery("SELECT COUNT(*) AS total FROM transactions");
            int totalTransactions = transCountRs.next() ? transCountRs.getInt("total") : 0;

            // Count total loans
            Statement loanCountStmt = con.createStatement();
            ResultSet loanCountRs = loanCountStmt.executeQuery("SELECT COUNT(*) AS total FROM loans");
            int totalLoans = loanCountRs.next() ? loanCountRs.getInt("total") : 0;

            // Sum all balances
            Statement balanceStmt = con.createStatement();
            ResultSet balanceRs = balanceStmt.executeQuery("SELECT SUM(balance) AS total FROM users");
            double totalBalance = balanceRs.next() ? balanceRs.getDouble("total") : 0;

            totalUsersValueLabel.setText(String.valueOf(totalUsers));
            totalTransactionsValueLabel.setText(String.valueOf(totalTransactions));
            totalLoansValueLabel.setText(String.valueOf(totalLoans));
            totalBalanceValueLabel.setText(formatMoney(totalBalance));

            // Load all users table
            allUsersTable.setModel(buildTableModel(con,
                "SELECT u.id, u.username, u.balance, u.savings, u.total_deposit, \n"
                + "COALESCE(SUM(l.amount),0) AS loans_total, \n"
                + "COALESCE(SUM(CASE WHEN l.status='active' THEN l.amount ELSE 0 END),0) AS active_loans_total, \n"
                + "COUNT(l.id) AS loans_count \n"
                + "FROM users u LEFT JOIN loans l ON l.user_id = u.id \n"
                + "GROUP BY u.id ORDER BY u.id"));

            // Load all transactions table (exclude details column)
            allTransactionsTable.setModel(buildTableModel(con,
                "SELECT txn.id, txn.user_id, usr.username, txn.type, txn.amount, txn.method, txn.created_at "
                + "FROM transactions txn LEFT JOIN users usr ON txn.user_id = usr.id ORDER BY txn.id DESC"));

            // Load all loans table (full loan attributes with username)
            allLoansTable.setModel(buildTableModel(con,
                "SELECT l.id, l.user_id, u.username, l.amount, l.interest_rate, l.total_payable, l.status, l.created_at "
                + "FROM loans l LEFT JOIN users u ON l.user_id = u.id ORDER BY l.id DESC"));

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void changeSelectedLoanStatus() {
        try {
            int viewRow = allLoansTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a loan from the Loans tab first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = allLoansTable.convertRowIndexToModel(viewRow);
            Object idObj = allLoansTable.getModel().getValueAt(modelRow, 0);
            Object userIdObj = allLoansTable.getModel().getValueAt(modelRow, 1);
            if (idObj == null) {
                JOptionPane.showMessageDialog(this, "Unable to determine selected loan ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int loanId = Integer.parseInt(String.valueOf(idObj));
            int targetUserId = userIdObj == null ? 0 : Integer.parseInt(String.valueOf(userIdObj));

            String[] options = new String[]{"active", "paid", "suspended", "blocked"};
            String newStatus = (String) JOptionPane.showInputDialog(this,
                "Select new loan status:",
                "Change Loan Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (newStatus == null || newStatus.isBlank()) return;

            boolean ok = LoanManager.setLoanStatus(loanId, newStatus);
            if (ok) {
                // notify borrower via in-app message
                if (targetUserId > 0) {
                    String subject = "Loan Status Updated";
                    String body = "Your loan (Loan ID: " + loanId + ") status has been set to '" + newStatus + "'.\nPlease contact support for questions.";
                    MessageManager.sendMessageToUser(targetUserId, Session.adminId, subject, body);
                }
                JOptionPane.showMessageDialog(this, "Loan status updated to '" + newStatus + "'.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAdminDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update loan status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("changeSelectedLoanStatus error: " + e);
            JOptionPane.showMessageDialog(this, "An error occurred while updating loan status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void messageSelectedLoanUser() {
        try {
            int viewRow = allLoansTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a loan from the Loans tab first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = allLoansTable.convertRowIndexToModel(viewRow);
            Object userIdObj = allLoansTable.getModel().getValueAt(modelRow, 1);
            Object idObj = allLoansTable.getModel().getValueAt(modelRow, 0);
            if (userIdObj == null) {
                JOptionPane.showMessageDialog(this, "Unable to determine selected user ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int targetUserId = Integer.parseInt(String.valueOf(userIdObj));
            int loanId = idObj == null ? -1 : Integer.parseInt(String.valueOf(idObj));

            String subject = JOptionPane.showInputDialog(this, "Message Subject", "Loan Update");
            if (subject == null) return;
            String body = JOptionPane.showInputDialog(this, "Message Body", "Regarding your loan (Loan ID: " + loanId + "):");
            if (body == null) return;

            boolean sent = MessageManager.sendMessageToUser(targetUserId, Session.adminId, subject, body);
            if (sent) {
                JOptionPane.showMessageDialog(this, "Message sent to user.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Message saved but may not have been delivered.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
            refreshAdminDashboard();
        } catch (Exception e) {
            System.out.println("messageSelectedLoanUser error: " + e);
            JOptionPane.showMessageDialog(this, "An error occurred while sending message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultTableModel buildTableModel(Connection con, String sql) throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

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

    private String formatMoney(double amount) {
        return String.format("₱%.2f", amount);
    }

    private void openLoanApplicationsDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "📋 Loan Applications - Pending Approvals", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        mainPanel.setBackground(new Color(240, 248, 245));

        // Title
        JLabel titleLabel = new JLabel("Pending Loan Applications - Approve or Reject");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 102, 51));

        // Applications table
        JTable applicationsTable = new JTable();
        applicationsTable.setRowHeight(28);
        applicationsTable.setShowGrid(true);
        applicationsTable.setGridColor(new Color(188, 226, 158));
        applicationsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        applicationsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        applicationsTable.getTableHeader().setBackground(new Color(76, 175, 80));
        applicationsTable.getTableHeader().setForeground(Color.WHITE);
        applicationsTable.setAutoCreateRowSorter(true);

        // Load pending applications
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT la.id, la.user_id, la.full_name, la.requested_amount, la.purpose, la.employment_status, " +
                "la.monthly_income, la.loan_term_months, la.created_at FROM loan_applications la WHERE la.status='pending' ORDER BY la.created_at ASC"
            );
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
            applicationsTable.setModel(model);
        } catch (Exception e) {
            System.out.println("Error loading applications: " + e);
        }

        JScrollPane scrollPane = new JScrollPane(applicationsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Applications"));

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.setOpaque(false);

        JButton approveBtn = new JButton("✓ Approve Application");
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        approveBtn.setFocusPainted(false);
        approveBtn.addActionListener(e -> {
            int selectedRow = applicationsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an application to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = applicationsTable.convertRowIndexToModel(selectedRow);
            int appId = (Integer) applicationsTable.getModel().getValueAt(modelRow, 0);
            approveLoanApplication(appId, dialog, applicationsTable);
        });

        JButton rejectBtn = new JButton("✗ Reject Application");
        rejectBtn.setBackground(new Color(244, 67, 54));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        rejectBtn.setFocusPainted(false);
        rejectBtn.addActionListener(e -> {
            int selectedRow = applicationsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an application to reject.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = applicationsTable.convertRowIndexToModel(selectedRow);
            int appId = (Integer) applicationsTable.getModel().getValueAt(modelRow, 0);
            rejectLoanApplication(appId, dialog, applicationsTable);
        });

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(33, 150, 243));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> openLoanApplicationsDialog());

        controlPanel.add(approveBtn);
        controlPanel.add(rejectBtn);
        controlPanel.add(refreshBtn);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private void approveLoanApplication(int applicationId, javax.swing.JDialog dialog, JTable table) {
        try {
            Connection con = DB.connect();

            // Get application details
            PreparedStatement getPst = con.prepareStatement(
                "SELECT user_id, requested_amount FROM loan_applications WHERE id=?"
            );
            getPst.setInt(1, applicationId);
            ResultSet rs = getPst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(dialog, "Application not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = rs.getInt("user_id");
            double loanAmount = rs.getDouble("requested_amount");

            // Ask for approved amount (can be different from requested)
            String amountStr = JOptionPane.showInputDialog(dialog,
                "Enter approved amount (₱" + String.format("%.2f", loanAmount) + " requested):",
                String.format("%.2f", loanAmount));

            if (amountStr == null || amountStr.trim().isEmpty()) {
                return;
            }

            double approvedAmount = Double.parseDouble(amountStr);

            // Update the loan application directly
            PreparedStatement updatePst = con.prepareStatement(
                "UPDATE loan_applications SET status='approved', admin_id=?, approved_at=NOW(), approved_amount=? WHERE id=?"
            );
            updatePst.setInt(1, Session.adminId);
            updatePst.setDouble(2, approvedAmount);
            updatePst.setInt(3, applicationId);
            updatePst.executeUpdate();

            // Create loan record with required fields
            double interestRate = 5.0; // Default 5% interest
            double totalPayable = approvedAmount + (approvedAmount * interestRate / 100);
            
            PreparedStatement loanPst = con.prepareStatement(
                "INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, status, created_at) VALUES (?, ?, ?, ?, ?, 'active', NOW())",
                Statement.RETURN_GENERATED_KEYS
            );
            loanPst.setInt(1, userId);
            loanPst.setDouble(2, approvedAmount);
            loanPst.setDouble(3, interestRate);
            loanPst.setDouble(4, totalPayable);
            loanPst.setDouble(5, totalPayable);
            loanPst.executeUpdate();

            int loanId = -1;
            ResultSet loanRs = loanPst.getGeneratedKeys();
            if (loanRs.next()) {
                loanId = loanRs.getInt(1);
            }

            // Record transaction
            PreparedStatement txnPst = con.prepareStatement(
                "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, 'Loan Disbursement', ?, 'Loan Approval')"
            );
            txnPst.setInt(1, userId);
            txnPst.setDouble(2, approvedAmount);
            txnPst.executeUpdate();

            JOptionPane.showMessageDialog(dialog,
                "✓ Application approved!\n\n" +
                "Loan ID: " + loanId + "\n" +
                "Amount: ₱" + String.format("%.2f", approvedAmount) + "\n" +
                "Interest: 2% (₱" + String.format("%.2f", approvedAmount * 0.02) + ")\n" +
                "Total Payable: ₱" + String.format("%.2f", approvedAmount * 1.02) + "\n" +
                "Term: 6 months\n" +
                "Status: Active",
                "Approval Successful",
                JOptionPane.INFORMATION_MESSAGE);

            // Refresh the table
            openLoanApplicationsDialog();
            dialog.dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(dialog, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.out.println("Error approving application: " + e);
            JOptionPane.showMessageDialog(dialog, "Error approving application: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectLoanApplication(int applicationId, javax.swing.JDialog dialog, JTable table) {
        try {
            String reason = JOptionPane.showInputDialog(dialog,
                "Enter rejection reason:",
                "Insufficient income");

            if (reason == null || reason.trim().isEmpty()) {
                return;
            }

            Connection con = DB.connect();
            PreparedStatement rejectPst = con.prepareStatement(
                "UPDATE loan_applications SET status='rejected', admin_id=?, rejected_at=NOW(), rejection_reason=? WHERE id=?"
            );
            rejectPst.setInt(1, Session.adminId);
            rejectPst.setString(2, reason);
            rejectPst.setInt(3, applicationId);
            rejectPst.executeUpdate();

            JOptionPane.showMessageDialog(dialog,
                "✓ Application rejected\n\n" +
                "Reason: " + reason,
                "Rejection Complete",
                JOptionPane.INFORMATION_MESSAGE);

            // Refresh
            openLoanApplicationsDialog();
            dialog.dispose();
        } catch (Exception e) {
            System.out.println("Error rejecting application: " + e);
            JOptionPane.showMessageDialog(dialog, "Error rejecting application: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Admin Dashboard");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboard().setVisible(true);
            }
        });
    }
}

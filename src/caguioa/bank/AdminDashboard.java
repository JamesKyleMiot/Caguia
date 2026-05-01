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
        tabbedPane.addTab("Loans", loansScroll);

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
        tabbedPane.setComponentAt(tabbedPane.indexOfComponent(loansScroll), loansWrapper);

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
                "SELECT t.id, t.user_id, u.username, t.type, t.amount, t.method, t.created_at "
                + "FROM transactions t LEFT JOIN users u ON t.user_id = u.id ORDER BY t.id DESC"));

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

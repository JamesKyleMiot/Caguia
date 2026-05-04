package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class LoanVerificationDialog extends JDialog {
    
    private JLabel loanIdLabel;
    private JLabel userInfoLabel;
    private JLabel loanAmountLabel;
    private JLabel loanStatusLabel;
    private JLabel witnessNameLabel;
    private JLabel witnessContactLabel;
    private JTextArea witnessNotesArea;
    private JLabel witnessSignatureLabel;
    private JLabel userSignatureLabel;
    private JButton viewWitnessSignBtn;
    private JButton viewUserSignBtn;
    private JTextArea adminNotesArea;
    private JButton approveBtn;
    private JButton rejectBtn;
    private JButton cancelBtn;
    private int loanId;
    private byte[] witnessSignatureData;
    private byte[] userSignatureData;

    public LoanVerificationDialog(Frame owner, int loanId) {
        super(owner, "Loan Verification - Witness Review", true);
        this.loanId = loanId;
        
        initializeUI();
        setupListeners();
        loadLoanData();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(245, 250, 255));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Loan Verification & Approval");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 0));

        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Loan Info Panel
        JPanel loanInfoPanel = new JPanel(new GridBagLayout());
        loanInfoPanel.setOpaque(false);
        loanInfoPanel.setBorder(BorderFactory.createTitledBorder("Loan Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Loan ID
        JLabel loanIdTitleLabel = new JLabel("Loan ID:");
        loanIdTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        loanInfoPanel.add(loanIdTitleLabel, gbc);

        loanIdLabel = new JLabel("Loading...");
        loanIdLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        loanInfoPanel.add(loanIdLabel, gbc);

        // User Info
        JLabel userTitleLabel = new JLabel("Borrower:");
        userTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        loanInfoPanel.add(userTitleLabel, gbc);

        userInfoLabel = new JLabel("Loading...");
        userInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        loanInfoPanel.add(userInfoLabel, gbc);

        // Loan Amount
        JLabel amountTitleLabel = new JLabel("Loan Amount:");
        amountTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        loanInfoPanel.add(amountTitleLabel, gbc);

        loanAmountLabel = new JLabel("Loading...");
        loanAmountLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        loanInfoPanel.add(loanAmountLabel, gbc);

        // Status
        JLabel statusTitleLabel = new JLabel("Current Status:");
        statusTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        loanInfoPanel.add(statusTitleLabel, gbc);

        loanStatusLabel = new JLabel("Loading...");
        loanStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        loanStatusLabel.setForeground(new Color(255, 152, 0));
        gbc.gridx = 1;
        gbc.weightx = 1;
        loanInfoPanel.add(loanStatusLabel, gbc);

        // Witness Info Panel
        JPanel witnessPanel = new JPanel(new GridBagLayout());
        witnessPanel.setOpaque(false);
        witnessPanel.setBorder(BorderFactory.createTitledBorder("Witness Information"));
        
        // Witness Name
        JLabel witnessNameTitleLabel = new JLabel("Witness Name:");
        witnessNameTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        witnessPanel.add(witnessNameTitleLabel, gbc);

        witnessNameLabel = new JLabel("Not provided");
        witnessNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        witnessPanel.add(witnessNameLabel, gbc);

        // Witness Contact
        JLabel witnessContactTitleLabel = new JLabel("Witness Contact:");
        witnessContactTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        witnessPanel.add(witnessContactTitleLabel, gbc);

        witnessContactLabel = new JLabel("Not provided");
        witnessContactLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        witnessPanel.add(witnessContactLabel, gbc);

        // Witness Signature
        JLabel witnessSigTitleLabel = new JLabel("Witness Signature:");
        witnessSigTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        witnessPanel.add(witnessSigTitleLabel, gbc);

        JPanel witnessSigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        witnessSigPanel.setOpaque(false);
        
        witnessSignatureLabel = new JLabel("Not uploaded");
        witnessSignatureLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        witnessSignatureLabel.setForeground(Color.RED);
        
        viewWitnessSignBtn = new JButton("View");
        viewWitnessSignBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        viewWitnessSignBtn.setPreferredSize(new Dimension(70, 24));
        viewWitnessSignBtn.setEnabled(false);
        
        witnessSigPanel.add(witnessSignatureLabel);
        witnessSigPanel.add(viewWitnessSignBtn);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        witnessPanel.add(witnessSigPanel, gbc);

        // User Signature
        JLabel userSigTitleLabel = new JLabel("Borrower Signature:");
        userSigTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        witnessPanel.add(userSigTitleLabel, gbc);

        JPanel userSigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userSigPanel.setOpaque(false);
        
        userSignatureLabel = new JLabel("Not uploaded");
        userSignatureLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        userSignatureLabel.setForeground(Color.RED);
        
        viewUserSignBtn = new JButton("View");
        viewUserSignBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
        viewUserSignBtn.setPreferredSize(new Dimension(70, 24));
        viewUserSignBtn.setEnabled(false);
        
        userSigPanel.add(userSignatureLabel);
        userSigPanel.add(viewUserSignBtn);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        witnessPanel.add(userSigPanel, gbc);

        // Admin Notes Panel
        JPanel notesPanel = new JPanel(new BorderLayout(8, 8));
        notesPanel.setOpaque(false);
        notesPanel.setBorder(BorderFactory.createTitledBorder("Admin Verification Notes"));
        
        JLabel notesLabel = new JLabel("Enter verification notes/findings:");
        notesLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        
        adminNotesArea = new JTextArea(4, 50);
        adminNotesArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        adminNotesArea.setLineWrap(true);
        adminNotesArea.setWrapStyleWord(true);
        adminNotesArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane notesScroll = new JScrollPane(adminNotesArea);
        
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        approveBtn = new JButton("✓ APPROVE LOAN");
        approveBtn.setBackground(new Color(76, 175, 80));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        approveBtn.setPreferredSize(new Dimension(140, 35));

        rejectBtn = new JButton("✗ REJECT LOAN");
        rejectBtn.setBackground(new Color(244, 67, 54));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        rejectBtn.setPreferredSize(new Dimension(130, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(cancelBtn);

        // Assemble panels
        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        topPanel.setOpaque(false);
        topPanel.add(loanInfoPanel, BorderLayout.NORTH);
        topPanel.add(witnessPanel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);
        mainPanel.add(notesPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(true);
        setMinimumSize(new Dimension(700, 700));
    }

    private void setupListeners() {
        approveBtn.addActionListener(e -> approveLoan());
        rejectBtn.addActionListener(e -> rejectLoan());
        cancelBtn.addActionListener(e -> dispose());
        viewWitnessSignBtn.addActionListener(e -> viewWitnessSignature());
        viewUserSignBtn.addActionListener(e -> viewUserSignature());
    }

    private void loadLoanData() {
        try {
            Connection conn = DB.connect();
            
            // Get loan details
            PreparedStatement pst = conn.prepareStatement(
                "SELECT l.amount, l.total_payable, l.status, u.id, u.fullname, u.username, " +
                "l.witness_name, l.witness_contact, l.witness_signature, l.user_signature " +
                "FROM loans l JOIN users u ON l.user_id = u.id WHERE l.id = ?"
            );
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                loanIdLabel.setText("Loan #" + loanId);
                userInfoLabel.setText(rs.getString("fullname") + " (" + rs.getString("username") + ")");
                loanAmountLabel.setText("₱" + String.format("%.2f", rs.getDouble("amount")));
                loanStatusLabel.setText(rs.getString("status").toUpperCase());
                
                String witnessName = rs.getString("witness_name");
                String witnessContact = rs.getString("witness_contact");
                
                if (witnessName != null && !witnessName.isEmpty()) {
                    witnessNameLabel.setText(witnessName);
                } else {
                    witnessNameLabel.setText("Not provided");
                    witnessNameLabel.setForeground(Color.RED);
                }
                
                if (witnessContact != null && !witnessContact.isEmpty()) {
                    witnessContactLabel.setText(witnessContact);
                } else {
                    witnessContactLabel.setText("Not provided");
                    witnessContactLabel.setForeground(Color.RED);
                }
                
                witnessSignatureData = rs.getBytes("witness_signature");
                userSignatureData = rs.getBytes("user_signature");
                
                if (witnessSignatureData != null) {
                    witnessSignatureLabel.setText("✓ Uploaded (" + witnessSignatureData.length + " bytes)");
                    witnessSignatureLabel.setForeground(new Color(76, 175, 80));
                    viewWitnessSignBtn.setEnabled(true);
                } else {
                    witnessSignatureLabel.setText("❌ Not uploaded");
                    witnessSignatureLabel.setForeground(Color.RED);
                }
                
                if (userSignatureData != null) {
                    userSignatureLabel.setText("✓ Uploaded (" + userSignatureData.length + " bytes)");
                    userSignatureLabel.setForeground(new Color(76, 175, 80));
                    viewUserSignBtn.setEnabled(true);
                } else {
                    userSignatureLabel.setText("❌ Not uploaded");
                    userSignatureLabel.setForeground(Color.RED);
                }
            }
            rs.close();
            pst.close();

        } catch (Exception e) {
            System.out.println("Error loading loan data: " + e);
            JOptionPane.showMessageDialog(this,
                "Error loading loan data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewWitnessSignature() {
        if (witnessSignatureData != null) {
            try {
                File tempFile = File.createTempFile("witness_sig_", ".png");
                java.nio.file.Files.write(tempFile.toPath(), witnessSignatureData);
                
                JLabel imageLabel = new JLabel();
                imageLabel.setIcon(new ImageIcon(new ImageIcon(tempFile.getAbsolutePath())
                    .getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH)));
                
                JOptionPane.showMessageDialog(this, imageLabel, 
                    "Witness Signature", JOptionPane.INFORMATION_MESSAGE);
                    
                tempFile.delete();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error viewing signature: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewUserSignature() {
        if (userSignatureData != null) {
            try {
                File tempFile = File.createTempFile("user_sig_", ".png");
                java.nio.file.Files.write(tempFile.toPath(), userSignatureData);
                
                JLabel imageLabel = new JLabel();
                imageLabel.setIcon(new ImageIcon(new ImageIcon(tempFile.getAbsolutePath())
                    .getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH)));
                
                JOptionPane.showMessageDialog(this, imageLabel, 
                    "Borrower Signature", JOptionPane.INFORMATION_MESSAGE);
                    
                tempFile.delete();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error viewing signature: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void approveLoan() {
        String notes = adminNotesArea.getText().trim();

        if (notes.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
                "No verification notes entered.\n\n" +
                "Continue with approval?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve this loan?\n\n" +
            "This will set the loan status to APPROVED.",
            "Confirm Approval",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DB.connect();
                String sql = "UPDATE loans SET status = 'approved' WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, loanId);
                pst.executeUpdate();
                pst.close();

                JOptionPane.showMessageDialog(this,
                    "✓ Loan APPROVED successfully!\n\n" +
                    "Loan ID: " + loanId + "\n" +
                    "Status: APPROVED\n\n" +
                    "The borrower will be notified.",
                    "Loan Approved",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error approving loan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectLoan() {
        String notes = adminNotesArea.getText().trim();

        if (notes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a reason for rejection.",
                "Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Reject this loan?\n\n" +
            "Reason: " + notes + "\n\n" +
            "This action cannot be undone.",
            "Confirm Rejection",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DB.connect();
                String sql = "UPDATE loans SET status = 'rejected' WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, loanId);
                pst.executeUpdate();
                pst.close();

                JOptionPane.showMessageDialog(this,
                    "✗ Loan REJECTED\n\n" +
                    "Loan ID: " + loanId + "\n" +
                    "Status: REJECTED\n\n" +
                    "The borrower will be notified.",
                    "Loan Rejected",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error rejecting loan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            LoanVerificationDialog dialog = new LoanVerificationDialog(frame, 1);
            dialog.setVisible(true);
        });
    }
}

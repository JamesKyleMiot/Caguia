package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoanApplicationDialog extends JDialog {

    private static final double MIN_LOAN = 50000.0;
    private static final double MAX_LOAN = 300000.0;
    private static final double INTEREST_RATE = 0.02;
    private static final int REPAYMENT_WINDOW_DAYS = 30;

    private final int userId;
    
    // Personal Information
    private JTextField fullNameField;
    private JTextField dateOfBirthField;
    private JComboBox<String> genderCombo;
    private JTextField addressField;
    private JTextField contactNumberField;
    private JTextField emailField;
    
    // Employment Information
    private JComboBox<String> employmentStatusCombo;
    private JTextField companyNameField;
    private JTextField monthlyIncomeField;
    private JTextField workAddressField;
    
    // Loan Details
    private JTextField amountField;
    private JTextField loanPurposeField;
    private JTextField loanTermField;
    
    // Bank Information
    private JTextField accountNumberField;
    private JComboBox<String> accountTypeCombo;
    
    // Requirements
    private JCheckBox validIDCheckbox;
    private JCheckBox proofOfIncomeCheckbox;
    private JCheckBox proofOfAddressCheckbox;
    private JCheckBox declarationCheckbox;
    
    private JButton submitBtn;
    private JButton cancelBtn;

    public LoanApplicationDialog(Frame owner, int userId) {
        super(owner, "Loan Application Form", true);
        this.userId = userId;

        initializeUI();

        pack();
        setSize(900, 1000);
        setMinimumSize(new Dimension(850, 900));
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(242, 248, 252));

        // Scrollable content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        int row = 0;
        
        // Header
        JLabel titleLabel = new JLabel("LOAN APPLICATION FORM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 51));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        
        // Section 1: Personal Information
        addSectionTitle(contentPanel, "1. PERSONAL INFORMATION", row++, gbc);
        
        addFormField(contentPanel, "Full Name:", fullNameField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Date of Birth:", dateOfBirthField = new JTextField("MM/DD/YYYY"), row++, gbc);
        addFormField(contentPanel, "Gender:", genderCombo = new JComboBox<>(new String[]{"Select...", "Male", "Female", "Other"}), row++, gbc);
        addFormField(contentPanel, "Address:", addressField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Contact Number:", contactNumberField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Email Address:", emailField = new JTextField(), row++, gbc);
        
        // Section 2: Employment Information
        addSectionTitle(contentPanel, "2. EMPLOYMENT INFORMATION", row++, gbc);
        
        addFormField(contentPanel, "Employment Status:", employmentStatusCombo = new JComboBox<>(new String[]{"Select...", "Employed", "Self-Employed", "Unemployed", "Retired"}), row++, gbc);
        addFormField(contentPanel, "Company Name:", companyNameField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Monthly Income:", monthlyIncomeField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Work Address:", workAddressField = new JTextField(), row++, gbc);
        
        // Section 3: Loan Details
        addSectionTitle(contentPanel, "3. LOAN DETAILS", row++, gbc);
        
        addFormField(contentPanel, "Loan Amount:", amountField = new JTextField(), row++, gbc);
        JLabel rangeLabel = new JLabel("(Allowed range: ₱50,000.00 to ₱300,000.00)");
        rangeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeLabel.setForeground(new Color(90, 90, 90));
        gbc.gridx = 1;
        gbc.gridy = row - 1;
        gbc.gridx = 1;
        contentPanel.add(rangeLabel, gbc);
        
        addFormField(contentPanel, "Loan Purpose:", loanPurposeField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Loan Term (Months):", loanTermField = new JTextField(), row++, gbc);
        
        // Section 4: Bank Information
        addSectionTitle(contentPanel, "4. BANK INFORMATION", row++, gbc);
        
        addFormField(contentPanel, "Account Number:", accountNumberField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Account Type:", accountTypeCombo = new JComboBox<>(new String[]{"Select...", "Savings", "Checking", "Money Market"}), row++, gbc);
        
        // Section 5: Requirements Submitted
        addSectionTitle(contentPanel, "5. REQUIREMENTS SUBMITTED", row++, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        validIDCheckbox = new JCheckBox("[ ] Valid ID");
        contentPanel.add(validIDCheckbox, gbc);
        
        gbc.gridy = row++;
        proofOfIncomeCheckbox = new JCheckBox("[ ] Proof of Income");
        contentPanel.add(proofOfIncomeCheckbox, gbc);
        
        gbc.gridy = row++;
        proofOfAddressCheckbox = new JCheckBox("[ ] Proof of Address");
        contentPanel.add(proofOfAddressCheckbox, gbc);
        gbc.gridwidth = 1;
        
        // Section 6: Declaration
        addSectionTitle(contentPanel, "6. DECLARATION", row++, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel declarationLabel = new JLabel("I confirm that all information provided is true and correct.");
        declarationLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contentPanel.add(declarationLabel, gbc);
        
        gbc.gridy = row++;
        declarationCheckbox = new JCheckBox("I agree to the terms and conditions");
        declarationCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contentPanel.add(declarationCheckbox, gbc);
        gbc.gridwidth = 1;
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        submitBtn.setBackground(new Color(33, 150, 243));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setBackground(new Color(120, 120, 120));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        buttonPanel.add(cancelBtn);
        buttonPanel.add(submitBtn);
        
        // Add scrollable content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // Setup listeners
        submitBtn.addActionListener(e -> submitApplication());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(submitBtn);
    }
    
    private void addSectionTitle(JPanel panel, String title, int row, GridBagConstraints gbc) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        sectionLabel.setForeground(new Color(0, 102, 51));
        sectionLabel.setBorder(new EmptyBorder(12, 0, 6, 0));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
    }
    
    private void addFormField(JPanel panel, String label, JComponent field, int row, GridBagConstraints gbc) {
        JLabel jlabel = new JLabel(label);
        jlabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(jlabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        field.setPreferredSize(new Dimension(300, 25));
        panel.add(field, gbc);
    }


    private void submitApplication() {
        // Validation
        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            fullNameField.requestFocusInWindow();
            return;
        }
        
        if (dateOfBirthField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your date of birth.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            dateOfBirthField.requestFocusInWindow();
            return;
        }
        
        if (genderCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select your gender.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your address.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            addressField.requestFocusInWindow();
            return;
        }
        
        if (contactNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your contact number.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            contactNumberField.requestFocusInWindow();
            return;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocusInWindow();
            return;
        }
        
        if (employmentStatusCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select your employment status.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (companyNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your company name.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            companyNameField.requestFocusInWindow();
            return;
        }
        
        if (monthlyIncomeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your monthly income.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            monthlyIncomeField.requestFocusInWindow();
            return;
        }
        
        String rawAmount = amountField.getText().trim().replace(",", "");
        if (rawAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a loan amount.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }
        
        double requestedAmount;
        try {
            requestedAmount = Double.parseDouble(rawAmount);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }
        
        if (requestedAmount < MIN_LOAN || requestedAmount > MAX_LOAN) {
            JOptionPane.showMessageDialog(this, "Loan amount must be between " + formatMoney(MIN_LOAN) + " and " + formatMoney(MAX_LOAN) + ".", "Out of Range", JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }
        
        if (loanPurposeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the loan purpose.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            loanPurposeField.requestFocusInWindow();
            return;
        }
        
        if (loanTermField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the loan term in months.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            loanTermField.requestFocusInWindow();
            return;
        }
        
        if (accountNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your account number.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            accountNumberField.requestFocusInWindow();
            return;
        }
        
        if (accountTypeCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select your account type.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validIDCheckbox.isSelected() || !proofOfIncomeCheckbox.isSelected() || !proofOfAddressCheckbox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please check all required documents.", "Missing Requirements", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!declarationCheckbox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please agree to the declaration.", "Missing Declaration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate loan details
        double interestCharge = requestedAmount * INTEREST_RATE;
        double totalPayable = requestedAmount + interestCharge;
        double remainingBalance = totalPayable;
        LocalDate dueDate = LocalDate.now().plusDays(REPAYMENT_WINDOW_DAYS);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Submit this loan application?\n\n" +
                "Full Name: " + fullNameField.getText() + "\n" +
                "Loan Amount: " + formatMoney(requestedAmount) + "\n" +
                "Interest: " + formatMoney(interestCharge) + "\n" +
                "Total Payable: " + formatMoney(totalPayable) + "\n" +
                "Due Date: " + dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
            "Confirm Loan Application",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(
                 "INSERT INTO loans (user_id, amount, interest_rate, total_payable, remaining_balance, due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'active')"
             )) {
            pst.setInt(1, userId);
            pst.setDouble(2, requestedAmount);
            pst.setDouble(3, INTEREST_RATE);
            pst.setDouble(4, totalPayable);
            pst.setDouble(5, remainingBalance);
            pst.setDate(6, java.sql.Date.valueOf(dueDate));
            pst.executeUpdate();

            try (PreparedStatement transactionStmt = conn.prepareStatement(
                    "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, ?, ?, ?)")) {
                transactionStmt.setInt(1, userId);
                transactionStmt.setString(2, "Loan Disbursement");
                transactionStmt.setDouble(3, totalPayable);
                transactionStmt.setString(4, "Loan Application");
                transactionStmt.executeUpdate();
            }

            String receipt = buildReceipt(requestedAmount, interestCharge, totalPayable, dueDate);
            JTextArea receiptArea = new JTextArea(receipt);
            receiptArea.setEditable(false);
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            receiptArea.setBorder(new EmptyBorder(8, 8, 8, 8));
            receiptArea.setBackground(new Color(250, 250, 250));
            receiptArea.setCaretPosition(0);

            JOptionPane.showMessageDialog(
                this,
                new JScrollPane(receiptArea),
                "Loan Submitted",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to process the loan application: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildReceipt(double requestedAmount, double interestCharge, double totalPayable, LocalDate dueDate) {
        return "CAGUIOA BANK\n"
            + "Loan Application Receipt\n"
            + "---------------------------------------------\n"
            + "Applicant: " + fullNameField.getText() + "\n"
            + "Email: " + emailField.getText() + "\n"
            + "Contact: " + contactNumberField.getText() + "\n"
            + "---------------------------------------------\n"
            + "Loan Amount: " + formatMoney(requestedAmount) + "\n"
            + "Purpose: " + loanPurposeField.getText() + "\n"
            + "Loan Term: " + loanTermField.getText() + " months\n"
            + "Interest Rate: 2.00%\n"
            + "Interest Charge: " + formatMoney(interestCharge) + "\n"
            + "Total Payable: " + formatMoney(totalPayable) + "\n"
            + "Due Date: " + dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "\n"
            + "Status: PENDING APPROVAL\n"
            + "Date Applied: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "\n"
            + "---------------------------------------------\n"
            + "Your loan application has been submitted.\n"
            + "Please wait for approval notification.";
    }

    private String formatMoney(double amount) {
        return "₱" + new DecimalFormat("#,##0.00").format(amount);
    }
}

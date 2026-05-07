package caguioa.bank;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoanApplicationDialog extends JFrame {

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
        super("Loan Application Form");
        this.userId = userId;

        initializeUI();
        loadAutoFillData();

        pack();
        setMinimumSize(new Dimension(640, 600));
        setPreferredSize(new Dimension(760, 820));
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(242, 248, 252));

        // Scrollable content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        int row = 0;
        
        // Header
        JLabel titleLabel = new JLabel("LOAN APPLICATION FORM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(new Color(0, 102, 51));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
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
        JLabel rangeLabel = new JLabel("No preset limit. Admin approval is required.");
        rangeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeLabel.setForeground(new Color(90, 90, 90));
        gbc.gridx = 1;
        gbc.gridy = row - 1;
        gbc.gridx = 1;
        contentPanel.add(rangeLabel, gbc);
        
        addFormField(contentPanel, "Loan Purpose:", loanPurposeField = new JTextField(), row++, gbc);
        addFormField(contentPanel, "Loan Term (Months):", loanTermField = new JTextField("6"), row++, gbc);
        loanTermField.setEditable(false);
        
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        submitBtn.setBackground(new Color(33, 150, 243));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(120, 120, 120));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

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
        
        // Setup listeners
        submitBtn.addActionListener(e -> submitApplication());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(submitBtn);
    }
    
    private void addSectionTitle(JPanel panel, String title, int row, GridBagConstraints gbc) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        sectionLabel.setForeground(new Color(0, 102, 51));
        sectionLabel.setBorder(new EmptyBorder(8, 0, 4, 0));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
    }
    
    private void addFormField(JPanel panel, String label, JComponent field, int row, GridBagConstraints gbc) {
        JLabel jlabel = new JLabel(label);
        jlabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        jlabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(jlabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.HORIZONTAL;
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        panel.add(field, gbc);
    }


    private void submitApplication() {
        if (LoanApplicationHelper.hasPendingApplication(userId)) {
            JOptionPane.showMessageDialog(this,
                "You already have a pending loan application. Please wait for admin review before submitting another one.",
                "Pending Application Exists",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

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

        if (requestedAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Loan amount must be greater than zero.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
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
        LocalDate dueDate = LocalDate.now().plusDays(REPAYMENT_WINDOW_DAYS);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("""
                Submit this loan application?

                Full Name: %s
                Loan Amount: %s
                Interest: %s
                Total Payable: %s
                Due Date: %s
                Approval: Admin review required
                """,
                fullNameField.getText(),
                formatMoney(requestedAmount),
                formatMoney(interestCharge),
                formatMoney(totalPayable),
                dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))),
            "Confirm Loan Application",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DB.connect();
             PreparedStatement pst = conn.prepareStatement(
                 "INSERT INTO loan_applications (user_id, full_name, date_of_birth, gender, address, contact_number, email_address, " +
                     "employment_status, company_name, monthly_income, work_address, loan_amount_requested, loan_purpose, loan_term_months, " +
                     "account_number, account_type, valid_id_submitted, proof_of_income_submitted, proof_of_address_submitted, declaration_accepted, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')"
             )) {
            pst.setInt(1, userId);
            pst.setString(2, fullNameField.getText().trim());
            pst.setDate(3, java.sql.Date.valueOf(parseDateOfBirth(dateOfBirthField.getText().trim())));
            pst.setString(4, (String) genderCombo.getSelectedItem());
            pst.setString(5, addressField.getText().trim());
            pst.setString(6, contactNumberField.getText().trim());
            pst.setString(7, emailField.getText().trim());
            pst.setString(8, (String) employmentStatusCombo.getSelectedItem());
            pst.setString(9, companyNameField.getText().trim());
            pst.setDouble(10, Double.parseDouble(monthlyIncomeField.getText().trim().replace(",", "")));
            pst.setString(11, workAddressField.getText().trim());
            pst.setDouble(12, requestedAmount);
            pst.setString(13, loanPurposeField.getText().trim());
            pst.setInt(14, Integer.parseInt(loanTermField.getText().trim()));
            pst.setString(15, accountNumberField.getText().trim());
            pst.setString(16, (String) accountTypeCombo.getSelectedItem());
            pst.setBoolean(17, validIDCheckbox.isSelected());
            pst.setBoolean(18, proofOfIncomeCheckbox.isSelected());
            pst.setBoolean(19, proofOfAddressCheckbox.isSelected());
            pst.setBoolean(20, declarationCheckbox.isSelected());
            pst.executeUpdate();

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
                "Application Submitted",
                JOptionPane.INFORMATION_MESSAGE
            );

            JOptionPane.showMessageDialog(
                this,
                "Your loan application has been sent to the admin for review.\nYou will be notified after it is approved or rejected.",
                "Pending Admin Review",
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

    private LocalDate parseDateOfBirth(String value) {
        if (value.equalsIgnoreCase("MM/DD/YYYY")) {
            throw new IllegalArgumentException("Please enter your date of birth in MM/DD/YYYY format.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Please enter your date of birth in MM/DD/YYYY format.");
        }
    }

    private String buildReceipt(double requestedAmount, double interestCharge, double totalPayable, LocalDate dueDate) {
        return String.format("""
            CAGUIOA BANK
            Loan Application Receipt
            ---------------------------------------------
            Applicant: %s
            Email: %s
            Contact: %s
            ---------------------------------------------
            Loan Amount: %s
            Purpose: %s
            Loan Term: 6 months
            Interest Rate: 2.00%%
            Interest Charge: %s
            Total Payable: %s
            Due Date: %s
            Status: PENDING ADMIN APPROVAL
            Date Applied: %s
            ---------------------------------------------
            Your loan application has been submitted.
            Please wait for approval notification.
            """,
            fullNameField.getText(),
            emailField.getText(),
            contactNumberField.getText(),
            formatMoney(requestedAmount),
            loanPurposeField.getText(),
            formatMoney(interestCharge),
            formatMoney(totalPayable),
            dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
            LocalDate.now().format(DateTimeFormatter.ISO_DATE));
    }

    private void loadAutoFillData() {
        if (Session.fullname != null && !Session.fullname.isBlank()) {
            fullNameField.setText(Session.fullname);
        }

        try (Connection con = DB.connect();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT fullname, email, sex, address FROM users WHERE id = ?"
             )) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String fullname = rs.getString("fullname");
                    String email = rs.getString("email");
                    String sex = rs.getString("sex");
                    String address = rs.getString("address");

                    if (fullname != null && !fullname.isBlank()) {
                        fullNameField.setText(fullname);
                    }
                    if (email != null && !email.isBlank()) {
                        emailField.setText(email);
                    }
                    if (sex != null && !sex.isBlank()) {
                        genderCombo.setSelectedItem(sex);
                    }
                    if (address != null && !address.isBlank()) {
                        addressField.setText(address);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error auto-filling loan application form: " + e);
        }
    }

    private String formatMoney(double amount) {
        return "₱" + new DecimalFormat("#,##0.00").format(amount);
    }
}

package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class LoanPaymentDialog extends JDialog {

    private final int userId;
    private Map<String, Object> activeLoan;
    private int currentStep = 1;

    private JPanel stepPanel;
    private JPanel detailsPanel;
    private JTextField paymentAmountField;
    private JComboBox<String> paymentMethodCombo;
    private JTextArea instructionArea;
    private JTextArea receiptArea;
    private JLabel statusLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton cancelBtn;

    private final String[] paymentMethods = {
        "📱 Online Banking / Mobile App",
        "🏦 Bank Counter (Teller)",
        "🏪 Payment Center",
        "💳 Auto-debit from Account"
    };

    private final String[] methodInstructions = {
        "Online Banking:\n" +
        "1. Open your bank app\n" +
        "2. Go to 'Pay Loans' or 'Bills Payment'\n" +
        "3. Enter your loan account number\n" +
        "4. Enter the payment amount\n" +
        "5. Confirm and complete payment\n" +
        "6. Screenshot or save your confirmation",

        "Bank Counter (Teller):\n" +
        "1. Go to your bank branch\n" +
        "2. Fill out a payment slip\n" +
        "3. Provide your loan account number\n" +
        "4. Submit cash or check\n" +
        "5. Receive your receipt\n" +
        "6. Keep the receipt as proof",

        "Payment Center (Bayad Center):\n" +
        "1. Go to a payment center near you\n" +
        "2. Tell them you're paying a loan\n" +
        "3. Provide your loan account number\n" +
        "4. Provide the payment amount\n" +
        "5. Pay cash and get receipt\n" +
        "6. Keep the receipt as proof",

        "Auto-debit from Account:\n" +
        "1. Enroll in auto-debit service\n" +
        "2. Authorize payment from account\n" +
        "3. Set payment schedule\n" +
        "4. Amount will be deducted monthly\n" +
        "5. Check your bank app for confirmation\n" +
        "6. Monitor account balance"
    };

    public LoanPaymentDialog(Frame owner, int userId) {
        super(owner, "Pay Your Loan", true);
        this.userId = userId;
        this.activeLoan = getActiveLoanForCurrentUser();

        if (activeLoan == null) {
            JOptionPane.showMessageDialog(this, "No active loan found for payment", "No Active Loan", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        initializeUI();
        updateStepPanel();

        pack();
        setSize(840, 600);
        setMinimumSize(new Dimension(760, 550));
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(242, 248, 252));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("How to Pay Your Loan - Step by Step");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 51));

        JLabel subtitleLabel = new JLabel("Follow each step to complete your loan payment.");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(70, 90, 100));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with split pane (steps on left, details on right)
        JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane.setResizeWeight(0.60);
        contentPane.setDividerLocation(504);

        stepPanel = new JPanel(new BorderLayout(12, 12));
        stepPanel.setOpaque(false);
        contentPane.setLeftComponent(stepPanel);

        detailsPanel = new JPanel(new BorderLayout(12, 12));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));
        contentPane.setRightComponent(detailsPanel);

        mainPanel.add(contentPane, BorderLayout.CENTER);

        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setOpaque(false);

        prevBtn = new JButton("← Back");
        prevBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        prevBtn.addActionListener(e -> previousStep());

        nextBtn = new JButton("Next →");
        nextBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nextBtn.addActionListener(e -> nextStep());

        cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(prevBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void updateStepPanel() {
        stepPanel.removeAll();

        switch (currentStep) {
            case 1:
                buildStep1CheckDetails();
                break;
            case 2:
                buildStep2ChooseMethod();
                break;
            case 3:
                buildStep3MakePayment();
                break;
            case 4:
                buildStep4KeepProof();
                break;
            case 5:
                buildStep5VerifyPosting();
                break;
        }

        updateButtons();
        updateDetailsPanel();
        stepPanel.revalidate();
        stepPanel.repaint();
    }

    private void buildStep1CheckDetails() {
        JLabel stepLabel = new JLabel("STEP 1: Check Your Loan Details");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stepLabel.setForeground(new Color(0, 102, 51));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        JLabel loanNumberLabel = new JLabel("Loan Number:");
        loanNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(loanNumberLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel loanNumberValue = new JLabel(String.valueOf(activeLoan.get("id")));
        loanNumberValue.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contentPanel.add(loanNumberValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel amountDueLabel = new JLabel("Amount Due:");
        amountDueLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(amountDueLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        double remainingBalance = (Double) activeLoan.get("remaining_balance");
        JLabel amountDueValue = new JLabel("₱" + String.format("%.2f", remainingBalance));
        amountDueValue.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amountDueValue.setForeground(new Color(192, 0, 0));
        contentPanel.add(amountDueValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel dueDateLabel = new JLabel("Due Date:");
        dueDateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(dueDateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel dueDateValue = new JLabel(String.valueOf(activeLoan.get("due_date")));
        dueDateValue.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contentPanel.add(dueDateValue, gbc);

        JTextArea noteArea = new JTextArea(
            "✓ Take note of your loan number\n" +
            "✓ This is the amount you need to pay\n" +
            "✓ Pay by the due date to avoid penalties\n" +
            "✓ Ready to proceed? Click Next"
        );
        noteArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteArea.setEditable(false);
        noteArea.setOpaque(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setForeground(new Color(70, 90, 100));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(noteArea, gbc);

        stepPanel.add(stepLabel, BorderLayout.NORTH);
        stepPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void buildStep2ChooseMethod() {
        JLabel stepLabel = new JLabel("STEP 2: Choose How You Will Pay");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stepLabel.setForeground(new Color(0, 102, 51));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        JLabel methodLabel = new JLabel("Select Payment Method:");
        methodLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(methodLabel, gbc);

        gbc.gridy = 1;
        paymentMethodCombo = new JComboBox<>(paymentMethods);
        paymentMethodCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        paymentMethodCombo.addActionListener(e -> updateStepPanel());
        contentPanel.add(paymentMethodCombo, gbc);

        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        instructionArea = new JTextArea();
        instructionArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        instructionArea.setEditable(false);
        instructionArea.setLineWrap(true);
        instructionArea.setWrapStyleWord(true);
        instructionArea.setBackground(new Color(240, 245, 250));
        instructionArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        instructionArea.setText(methodInstructions[0]);
        contentPanel.add(instructionArea, gbc);

        stepPanel.add(stepLabel, BorderLayout.NORTH);
        stepPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void buildStep3MakePayment() {
        JLabel stepLabel = new JLabel("STEP 3: Make the Payment");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stepLabel.setForeground(new Color(0, 102, 51));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        JLabel paymentLabel = new JLabel("Payment Amount:");
        paymentLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(paymentLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        paymentAmountField = new JTextField();
        paymentAmountField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        double remainingBalance = (Double) activeLoan.get("remaining_balance");
        paymentAmountField.setText(String.format("%.2f", remainingBalance));
        paymentAmountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateDetailsPanel(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateDetailsPanel(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateDetailsPanel(); }
        });
        contentPanel.add(paymentAmountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel dueDateLabel = new JLabel("Due Date:");
        dueDateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(dueDateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel dueDateValue = new JLabel(String.valueOf(activeLoan.get("due_date")));
        dueDateValue.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contentPanel.add(dueDateValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea noteArea = new JTextArea(
            "📌 Enter the amount you want to pay\n" +
            "📌 You can pay partial or full amount\n" +
            "📌 Follow the instructions from Step 2\n" +
            "📌 Have your loan account number ready\n" +
            "📌 After payment, go to Step 4"
        );
        noteArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteArea.setEditable(false);
        noteArea.setOpaque(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setForeground(new Color(70, 90, 100));
        contentPanel.add(noteArea, gbc);

        stepPanel.add(stepLabel, BorderLayout.NORTH);
        stepPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void buildStep4KeepProof() {
        JLabel stepLabel = new JLabel("STEP 4: Keep Proof of Payment");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stepLabel.setForeground(new Color(0, 102, 51));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;

        receiptArea = new JTextArea(
            "PROOF OF PAYMENT\n" +
            "================\n\n" +
            "Always keep your proof of payment. This could be:\n\n" +
            "✓ Receipt from bank or payment center\n" +
            "✓ Screenshot of bank app confirmation\n" +
            "✓ Reference number from transaction\n" +
            "✓ Email confirmation from bank\n\n" +
            "WHY KEEP PROOF?\n" +
            "• Proves you made the payment\n" +
            "• Useful if payment posting is delayed\n" +
            "• Needed if there's a dispute\n" +
            "• Required for your records\n\n" +
            "STORE IT SAFELY:\n" +
            "• Keep physical copies in a folder\n" +
            "• Save digital copies in cloud\n" +
            "• Take photos and store in phone\n" +
            "• Keep at least 2 copies"
        );
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        receiptArea.setEditable(false);
        receiptArea.setLineWrap(true);
        receiptArea.setWrapStyleWord(true);
        receiptArea.setBackground(new Color(240, 245, 250));
        receiptArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        contentPanel.add(receiptArea, gbc);

        stepPanel.add(stepLabel, BorderLayout.NORTH);
        stepPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void buildStep5VerifyPosting() {
        JLabel stepLabel = new JLabel("STEP 5: Verify Payment Posted & Complete");
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stepLabel.setForeground(new Color(0, 102, 51));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 218, 224)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        JLabel verifyLabel = new JLabel("How to Verify Payment:");
        verifyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        contentPanel.add(verifyLabel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea verifyArea = new JTextArea(
            "Check your updated loan balance:\n\n" +
            "📱 Via Bank App:\n" +
            "  1. Open your bank app\n" +
            "  2. Go to Loans or Accounts\n" +
            "  3. Check the balance has reduced\n\n" +
            "📞 Via Phone/SMS:\n" +
            "  1. Call your bank hotline\n" +
            "  2. Or reply to SMS from bank\n" +
            "  3. Ask about your loan status\n\n" +
            "🏦 Visit Bank Branch:\n" +
            "  1. Show your receipt\n" +
            "  2. Ask teller to verify\n" +
            "  3. Get written confirmation\n\n" +
            "✅ WHAT'S NEXT?\n" +
            "• If balance reduced → Payment successful!\n" +
            "• If fully paid → Can apply for new loan\n" +
            "• If partial paid → Account still active\n" +
            "• Payment delayed → Contact bank"
        );
        verifyArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        verifyArea.setEditable(false);
        verifyArea.setLineWrap(true);
        verifyArea.setWrapStyleWord(true);
        verifyArea.setBackground(new Color(240, 245, 250));
        verifyArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        contentPanel.add(verifyArea, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton submitPaymentBtn = new JButton("✓ Complete Payment & Update System");
        submitPaymentBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        submitPaymentBtn.setBackground(new Color(0, 153, 76));
        submitPaymentBtn.setForeground(Color.WHITE);
        submitPaymentBtn.addActionListener(e -> completePayment());
        contentPanel.add(submitPaymentBtn, gbc);

        stepPanel.add(stepLabel, BorderLayout.NORTH);
        stepPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void updateDetailsPanel() {
        detailsPanel.removeAll();

        JLabel summaryLabel = new JLabel("📋 Payment Summary");
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryLabel.setForeground(new Color(0, 102, 51));

        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        double remainingBalance = (Double) activeLoan.get("remaining_balance");

        JLabel loanNoLabel = new JLabel("Loan #:");
        loanNoLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        summaryPanel.add(loanNoLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel loanNoValue = new JLabel(String.valueOf(activeLoan.get("id")));
        loanNoValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
        summaryPanel.add(loanNoValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel balanceLabel = new JLabel("Current Due:");
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        summaryPanel.add(balanceLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel balanceValue = new JLabel("₱" + String.format("%.2f", remainingBalance));
        balanceValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
        balanceValue.setForeground(new Color(192, 0, 0));
        summaryPanel.add(balanceValue, gbc);

        if (currentStep == 3 && paymentAmountField != null) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0;
            JLabel paymentLabel = new JLabel("Payment Amt:");
            paymentLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            summaryPanel.add(paymentLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            try {
                double paymentAmount = Double.parseDouble(paymentAmountField.getText().trim());
                double newBalance = Math.max(0, remainingBalance - paymentAmount);
                JLabel paymentValue = new JLabel("₱" + String.format("%.2f", paymentAmount));
                paymentValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
                paymentValue.setForeground(new Color(0, 102, 51));
                summaryPanel.add(paymentValue, gbc);

                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.weightx = 0;
                JLabel newBalanceLabel = new JLabel("After Payment:");
                newBalanceLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
                summaryPanel.add(newBalanceLabel, gbc);

                gbc.gridx = 1;
                gbc.weightx = 1;
                JLabel newBalanceValue = new JLabel("₱" + String.format("%.2f", newBalance));
                newBalanceValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
                if (newBalance == 0) {
                    newBalanceValue.setForeground(new Color(0, 153, 76));
                    newBalanceValue.setText("₱" + String.format("%.2f", newBalance) + " ✓ PAID");
                } else {
                    newBalanceValue.setForeground(new Color(0, 102, 51));
                }
                summaryPanel.add(newBalanceValue, gbc);
            } catch (NumberFormatException e) {
                gbc.gridx = 1;
                gbc.weightx = 1;
                JLabel errorValue = new JLabel("Invalid amount");
                errorValue.setFont(new Font("SansSerif", Font.PLAIN, 11));
                errorValue.setForeground(new Color(192, 0, 0));
                summaryPanel.add(errorValue, gbc);
            }
        }

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(16, 0, 0, 0);
        JSeparator separator = new JSeparator();
        summaryPanel.add(separator, gbc);

        gbc.gridy = 5;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(8, 8, 8, 8);
        JLabel stepInfo = new JLabel("Step " + currentStep + " of 5");
        stepInfo.setFont(new Font("SansSerif", Font.BOLD, 12));
        stepInfo.setForeground(new Color(100, 100, 100));
        summaryPanel.add(stepInfo, gbc);

        detailsPanel.add(summaryLabel, BorderLayout.NORTH);
        detailsPanel.add(summaryPanel, BorderLayout.CENTER);
    }

    private void updateButtons() {
        prevBtn.setEnabled(currentStep > 1);
        nextBtn.setText(currentStep == 5 ? "Complete Payment" : "Next →");
        nextBtn.setEnabled(currentStep < 5 || (currentStep == 5 && isPaymentValid()));
    }

    private boolean isPaymentValid() {
        if (currentStep == 3 && paymentAmountField != null) {
            try {
                double amount = Double.parseDouble(paymentAmountField.getText().trim());
                double remaining = (Double) activeLoan.get("remaining_balance");
                return amount > 0 && amount <= remaining;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            updateStepPanel();
        }
    }

    private void nextStep() {
        if (currentStep == 3) {
            if (!isPaymentValid()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid payment amount (0 < amount ≤ " + activeLoan.get("remaining_balance") + ")",
                    "Invalid Amount",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (paymentMethodCombo.getSelectedIndex() >= 0) {
                methodInstructions[(int) paymentMethodCombo.getSelectedIndex()] = instructionArea.getText();
            }
        }

        if (currentStep < 5) {
            currentStep++;
            updateStepPanel();
        } else if (currentStep == 5) {
            completePayment();
        }
    }

    private void completePayment() {
        if (!isPaymentValid()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid payment amount",
                "Invalid Amount",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double paymentAmount = Double.parseDouble(paymentAmountField.getText().trim());
            int loanId = (Integer) activeLoan.get("id");
            String paymentMethod = paymentMethodCombo != null && paymentMethodCombo.getSelectedItem() != null
                ? paymentMethodCombo.getSelectedItem().toString()
                : "Loan Payment";

            boolean success = LoanManager.processLoanPayment(loanId, paymentAmount);
            if (success) {
                try (Connection conn = DB.connect();
                     PreparedStatement transactionStmt = conn.prepareStatement(
                         "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, ?, ?, ?)")) {
                    transactionStmt.setInt(1, userId);
                    transactionStmt.setString(2, "Loan Payment");
                    transactionStmt.setDouble(3, paymentAmount);
                    transactionStmt.setString(4, paymentMethod);
                    transactionStmt.executeUpdate();
                }

                // Generate receipt
                String receiptNumber = generateReceiptNumber();
                long transactionTime = System.currentTimeMillis();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new java.util.Date(transactionTime));
                
                // Display receipt dialog
                showReceiptDialog(receiptNumber, timestamp, paymentAmount, paymentMethod);

                double newBalance = Math.max(0, (Double) activeLoan.get("remaining_balance") - paymentAmount);
                String statusMessage = newBalance == 0 ? 
                    "✓ PAYMENT SUCCESSFUL!\n\nYour loan has been fully paid.\n\n" +
                    "You can now apply for a new loan.\n" +
                    "Click 'Yes' to apply immediately." :
                    "✓ PAYMENT RECORDED!\n\n" +
                    "Payment amount: ₱" + String.format("%.2f", paymentAmount) + "\n" +
                    "Remaining balance: ₱" + String.format("%.2f", newBalance) + "\n\n" +
                    "Thank you for your payment.";

                int option = JOptionPane.showConfirmDialog(this, 
                    statusMessage + "\n\nWould you like to apply for a new loan?",
                    "Payment Complete",
                    newBalance == 0 ? JOptionPane.YES_NO_OPTION : JOptionPane.CLOSED_OPTION);

                if (newBalance == 0 && option == JOptionPane.YES_OPTION) {
                    dispose();
                    // Open new loan application
                    new LoanApplicationDialog((Frame) getOwner(), userId).setVisible(true);
                } else {
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to process payment. Please try again.",
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error processing payment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Object> getActiveLoanForCurrentUser() {
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT id, amount, total_payable, remaining_balance, due_date, status " +
                "FROM loans WHERE user_id=? AND status='active' ORDER BY id DESC LIMIT 1"
            );
            pst.setInt(1, userId);
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

    private String generateReceiptNumber() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
        String dateStr = dateFormat.format(new java.util.Date());
        int randomNum = (int)(Math.random() * 100000);
        return "RECEIPT-" + dateStr + "-" + String.format("%05d", randomNum);
    }

    private void showReceiptDialog(String receiptNumber, String timestamp, double paymentAmount, String paymentMethod) {
        JDialog receiptDialog = new JDialog(this, "💰 Payment Receipt", true);
        receiptDialog.setSize(600, 650);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel headerLabel = new JLabel("PAYMENT RECEIPT");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(new Color(0, 102, 51));
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Receipt details
        double remainingBalance = Math.max(0, (Double) activeLoan.get("remaining_balance") - paymentAmount);
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        
        String receiptText = 
            "═══════════════════════════════════════\n" +
            "CAGUIOA BANK - LOAN PAYMENT RECEIPT\n" +
            "═══════════════════════════════════════\n\n" +
            "Receipt Number: " + receiptNumber + "\n" +
            "Date & Time: " + timestamp + "\n\n" +
            "───────────────────────────────────────\n" +
            "LOAN DETAILS\n" +
            "───────────────────────────────────────\n" +
            "Loan Account #: " + activeLoan.get("id") + "\n" +
            "Account Holder: User ID #" + userId + "\n\n" +
            "───────────────────────────────────────\n" +
            "PAYMENT INFORMATION\n" +
            "───────────────────────────────────────\n" +
            "Payment Method: " + paymentMethod + "\n" +
            "Payment Amount: ₱" + df.format(paymentAmount) + "\n" +
            "Previous Balance: ₱" + df.format((Double) activeLoan.get("remaining_balance")) + "\n" +
            "New Balance: ₱" + df.format(remainingBalance) + "\n\n" +
            "───────────────────────────────────────\n" +
            "STATUS\n" +
            "───────────────────────────────────────\n" +
            (remainingBalance == 0 ? "✓ LOAN FULLY PAID\n\nCongratulations! Your loan is completely paid.\n" : 
             "✓ PAYMENT RECORDED\n\nYour payment has been successfully processed.\n") +
            "\n───────────────────────────────────────\n" +
            "IMPORTANT:\n" +
            "• Keep this receipt as proof of payment\n" +
            "• Take a screenshot or print this receipt\n" +
            "• Payment may take 1-3 days to post\n" +
            "• Contact the bank if not reflected\n" +
            "═══════════════════════════════════════";

        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        receiptArea.setEditable(false);
        receiptArea.setLineWrap(true);
        receiptArea.setWrapStyleWord(true);
        receiptArea.setBackground(new Color(240, 245, 250));
        receiptArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        JButton printBtn = new JButton("🖨️ Print Receipt");
        printBtn.addActionListener(e -> printReceipt(receiptArea));
        buttonPanel.add(printBtn);

        JButton closeBtn = new JButton("✓ Close");
        closeBtn.addActionListener(e -> receiptDialog.dispose());
        buttonPanel.add(closeBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        receiptDialog.setContentPane(mainPanel);
        receiptDialog.setVisible(true);
    }

    private void printReceipt(JTextArea receiptArea) {
        try {
            receiptArea.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error printing receipt: " + e.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

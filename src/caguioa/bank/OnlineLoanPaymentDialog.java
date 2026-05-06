package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Dialog for online loan payments with step-by-step guidance
 */
public class OnlineLoanPaymentDialog extends JDialog {

    private final int userId;
    private Map<String, Object> activeLoan;
    private int currentStep = 1;
    private final int totalSteps = 5;

    private JPanel stepContentPanel;
    private JLabel stepTitleLabel;
    private JTextArea stepContentArea;
    private JTextField paymentAmountField;
    private JComboBox<String> paymentMethodCombo;
    private JButton prevBtn, nextBtn, submitBtn, cancelBtn;
    private JLabel stepIndicatorLabel;

    private final DecimalFormat df = new DecimalFormat("0.00");

    public OnlineLoanPaymentDialog(JFrame parent, int userId) {
        super(parent, "Online Loan Payment - 5 Step Process", true);
        this.userId = userId;
        
        setupUI();
        loadActiveLoan();
        showStep(1);
        
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        stepIndicatorLabel = new JLabel("Step 1 of " + totalSteps);
        stepIndicatorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stepTitleLabel = new JLabel();
        stepTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(stepTitleLabel, BorderLayout.WEST);
        headerPanel.add(stepIndicatorLabel, BorderLayout.EAST);

        // Content
        stepContentPanel = new JPanel(new BorderLayout(10, 10));
        stepContentArea = new JTextArea();
        stepContentArea.setEditable(false);
        stepContentArea.setLineWrap(true);
        stepContentArea.setWrapStyleWord(true);
        stepContentArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        stepContentArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        stepContentPanel.add(new JScrollPane(stepContentArea), BorderLayout.CENTER);

        // Payment Input Panel (for certain steps)
        JPanel paymentInputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        paymentInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        paymentInputPanel.add(new JLabel("Payment Amount (PHP):"));
        paymentAmountField = new JTextField();
        paymentInputPanel.add(paymentAmountField);
        
        paymentInputPanel.add(new JLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(LoanPaymentHelper.getAvailablePaymentMethods());
        paymentInputPanel.add(paymentMethodCombo);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        prevBtn = new JButton("← Previous");
        nextBtn = new JButton("Next →");
        submitBtn = new JButton("✓ Submit Payment");
        cancelBtn = new JButton("Cancel");

        prevBtn.addActionListener(e -> previousStep());
        nextBtn.addActionListener(e -> nextStep());
        submitBtn.addActionListener(e -> submitPayment());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(prevBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        // Assembly
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(stepContentPanel, BorderLayout.CENTER);
        mainPanel.add(paymentInputPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

        setContentPane(mainPanel);
    }

    private void loadActiveLoan() {
        activeLoan = LoanManager.getUserActiveLoan(userId);
        if (activeLoan == null) {
            JOptionPane.showMessageDialog(this, "No active loans found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private void showStep(int step) {
        currentStep = step;
        stepIndicatorLabel.setText("Step " + step + " of " + totalSteps);
        
        // Update button visibility
        prevBtn.setEnabled(step > 1);
        nextBtn.setEnabled(step < totalSteps);
        submitBtn.setVisible(step == totalSteps);
        paymentAmountField.setVisible(step == 3);
        paymentMethodCombo.setVisible(step == 2);

        switch (step) {
            case 1:
                showStep1();
                break;
            case 2:
                showStep2();
                break;
            case 3:
                showStep3();
                break;
            case 4:
                showStep4();
                break;
            case 5:
                showStep5();
                break;
        }
    }

    private void showStep1() {
        stepTitleLabel.setText("Step 1: Check Your Loan Details");
        
        if (activeLoan == null) {
            stepContentArea.setText("ERROR: No active loan found!");
            return;
        }

        double loanAmount = (double) activeLoan.get("amount");
        double remainingBalance = (double) activeLoan.get("remaining_balance");
        java.sql.Date dueDate = (java.sql.Date) activeLoan.get("due_date");
        int loanId = (int) activeLoan.get("id");

        String content = "📋 LOAN SUMMARY\n" +
                        "─────────────────────────────────\n\n" +
                        "Loan ID:           #" + loanId + "\n" +
                        "Original Amount:   PHP " + df.format(loanAmount) + "\n" +
                        "Interest Rate:     2%\n" +
                        "Remaining Balance: PHP " + df.format(remainingBalance) + "\n" +
                        "Due Date:          " + dueDate + "\n\n" +
                        "Total Amount Due:  PHP " + df.format(remainingBalance) + "\n\n" +
                        "✓ Please review your loan details above.\n" +
                        "✓ Click 'Next →' to proceed with payment.\n";

        stepContentArea.setText(content);
    }

    private void showStep2() {
        stepTitleLabel.setText("Step 2: Choose Payment Method");
        
        String content = "💳 SELECT YOUR PREFERRED PAYMENT METHOD\n" +
                        "─────────────────────────────────────────\n\n" +
                        "Select payment method from dropdown:\n\n" +
                        "📱 Online Banking / Mobile App\n" +
                        "   • Fastest and most convenient\n" +
                        "   • Real-time confirmation\n\n" +
                        "🏦 Bank Counter (Teller)\n" +
                        "   • Visit any branch during business hours\n" +
                        "   • Teller will process payment\n\n" +
                        "🏪 Payment Center (Bayad Center)\n" +
                        "   • Available at malls and convenience stores\n" +
                        "   • Quick payment processing\n\n" +
                        "💳 Auto-debit from Account\n" +
                        "   • Set up automatic monthly deduction\n" +
                        "   • No need to pay manually\n\n" +
                        "Choose your method above and click 'Next →'\n";

        stepContentArea.setText(content);
    }

    private void showStep3() {
        stepTitleLabel.setText("Step 3: Enter Payment Amount");
        
        if (activeLoan == null) return;
        
        double remainingBalance = (double) activeLoan.get("remaining_balance");
        
        String content = "💰 ENTER PAYMENT AMOUNT\n" +
                        "─────────────────────────────────────────\n\n" +
                        "Remaining Balance:  PHP " + df.format(remainingBalance) + "\n\n" +
                        "Enter the amount you want to pay:\n" +
                        "• Minimum: PHP 1.00\n" +
                        "• Maximum: PHP " + df.format(remainingBalance) + " (to pay in full)\n\n" +
                        "Partial Payment:\n" +
                        "You can pay any amount. Your remaining\n" +
                        "balance will be adjusted accordingly.\n\n" +
                        "Full Payment:\n" +
                        "Pay the full remaining balance to\n" +
                        "complete your loan.\n\n" +
                        "Enter amount and click 'Next →'\n";

        stepContentArea.setText(content);
        
        // Pre-fill with remaining balance
        paymentAmountField.setText(df.format(remainingBalance));
    }

    private void showStep4() {
        stepTitleLabel.setText("Step 4: Payment Confirmation");
        
        try {
            double paymentAmount = Double.parseDouble(paymentAmountField.getText());
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            
            if (activeLoan == null) return;
            
            double remainingBalance = (double) activeLoan.get("remaining_balance");
            double newBalance = remainingBalance - paymentAmount;
            if (newBalance < 0) newBalance = 0;
            
            String content = "✓ PLEASE REVIEW YOUR PAYMENT\n" +
                            "─────────────────────────────────────────\n\n" +
                            "Payment Amount:    PHP " + df.format(paymentAmount) + "\n" +
                            "Payment Method:    " + paymentMethod + "\n\n" +
                            "Current Balance:   PHP " + df.format(remainingBalance) + "\n" +
                            "After Payment:     PHP " + df.format(newBalance) + "\n\n" +
                            "Payment Status:    " + (newBalance <= 0 ? "✓ LOAN WILL BE FULLY PAID" : "Partial Payment") + "\n\n" +
                            "Next Step:\n" +
                            "Click 'Submit Payment' to complete\n" +
                            "or 'Previous' to make changes.\n";
            
            stepContentArea.setText(content);
        } catch (NumberFormatException ex) {
            stepContentArea.setText("ERROR: Invalid payment amount!");
        }
    }

    private void showStep5() {
        stepTitleLabel.setText("Step 5: Payment Receipt");
        
        String content = "📄 SAVE YOUR RECEIPT\n" +
                        "─────────────────────────────────────────\n\n" +
                        "After successful payment:\n\n" +
                        "✓ A receipt will be generated\n" +
                        "✓ Receipt number for your records\n" +
                        "✓ Payment details and confirmation\n\n" +
                        "What to do with your receipt:\n\n" +
                        "1. Print a copy (if available)\n" +
                        "2. Save in your email/messages\n" +
                        "3. Keep for your records\n" +
                        "4. Reference for inquiries\n\n" +
                        "Receipt Information:\n" +
                        "• Receipt number (unique ID)\n" +
                        "• Date and time of payment\n" +
                        "• Amount paid\n" +
                        "• Remaining balance\n" +
                        "• Payment method used\n\n" +
                        "Click 'Submit Payment' to proceed.\n";

        stepContentArea.setText(content);
    }

    private void previousStep() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    }

    private void nextStep() {
        if (currentStep < totalSteps) {
            if (currentStep == 2) {
                if (paymentMethodCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, "Please select a payment method.", 
                            "Selection Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (currentStep == 3) {
                try {
                    double amount = Double.parseDouble(paymentAmountField.getText());
                    if (!LoanPaymentHelper.validatePaymentAmount((int) activeLoan.get("id"), amount)) {
                        JOptionPane.showMessageDialog(this, 
                                "Invalid payment amount! Please enter a valid amount.", 
                                "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid amount.", 
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            showStep(currentStep + 1);
        }
    }

    private void submitPayment() {
        try {
            double paymentAmount = Double.parseDouble(paymentAmountField.getText());
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            int loanId = (int) activeLoan.get("id");

            // Generate transaction reference
            String txnRef = "TXN-" + System.currentTimeMillis();

            // Process payment
            int paymentId = LoanPaymentHelper.processLoanPayment(loanId, userId, paymentAmount, 
                    paymentMethod, txnRef);

            if (paymentId > 0) {
                // Generate receipt
                int receiptId = ReceiptGenerator.generateReceipt(paymentId, loanId, userId);
                
                String receipt = ReceiptGenerator.formatReceiptAsText(receiptId);
                JOptionPane.showMessageDialog(this, receipt, "Payment Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Payment processing failed. Please try again.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid payment amount!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

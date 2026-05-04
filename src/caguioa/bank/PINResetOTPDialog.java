package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for entering PIN Reset OTP
 */
public class PINResetOTPDialog extends JDialog {
    
    private JTextField otpField;
    private JButton verifyBtn;
    private JButton cancelBtn;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private int requestId;
    private int userId;
    private boolean otpVerified = false;

    public PINResetOTPDialog(Frame owner, int requestId, int userId) {
        super(owner, "Enter OTP - PIN Reset", true);
        this.requestId = requestId;
        this.userId = userId;
        
        initializeUI();
        setupListeners();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Enter One-Time Password (OTP)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        JLabel instructionLabel = new JLabel(
            "<html>" +
            "<b>OTP Verification:</b><br>" +
            "An OTP has been sent to your registered email.<br>" +
            "Enter the 6-digit OTP below to verify your identity.<br>" +
            "<span style='color:red;'>This OTP will expire in 10 minutes.</span>" +
            "</html>"
        );
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(70, 130, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // OTP field
        JLabel otpLabel = new JLabel("6-Digit OTP:");
        otpLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(otpLabel, gbc);

        otpField = new JTextField(15);
        otpField.setFont(new Font("Courier New", Font.PLAIN, 14));
        otpField.setHorizontalAlignment(JTextField.CENTER);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(otpField, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        // Timer label
        timerLabel = new JLabel("Expires in: 10:00");
        timerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timerLabel.setForeground(new Color(255, 102, 0));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(timerLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        verifyBtn = new JButton("Verify OTP");
        verifyBtn.setBackground(new Color(0, 102, 0));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        verifyBtn.setPreferredSize(new Dimension(140, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(verifyBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
    }

    private void setupListeners() {
        verifyBtn.addActionListener(e -> verifyOTP());
        cancelBtn.addActionListener(e -> dispose());
        otpField.addActionListener(e -> verifyOTP());
    }

    private void verifyOTP() {
        String otp = otpField.getText().trim();

        // Validation
        if (otp.isEmpty()) {
            statusLabel.setText("❌ Please enter the OTP");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d+")) {
            statusLabel.setText("❌ OTP must be exactly 6 digits");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Verify OTP
        verifyBtn.setEnabled(false);
        statusLabel.setText("Verifying...");
        statusLabel.setForeground(Color.BLUE);

        if (PINResetManager.verifyOTP(requestId, otp)) {
            statusLabel.setText("✓ OTP verified successfully!");
            statusLabel.setForeground(new Color(34, 139, 34));
            otpVerified = true;

            JOptionPane.showMessageDialog(this,
                "OTP verified successfully!\n\n" +
                "You can now set your new PIN.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            // Open PIN reset dialog
            ResetPINDialog resetPINDialog = new ResetPINDialog(this, userId, requestId);
            resetPINDialog.setVisible(true);
            
            this.dispose();
        } else {
            statusLabel.setText("❌ Invalid or expired OTP. Please try again.");
            statusLabel.setForeground(Color.RED);
            verifyBtn.setEnabled(true);
            otpField.setText("");
            otpField.requestFocus();
        }
    }

    public boolean isOTPVerified() {
        return otpVerified;
    }
}

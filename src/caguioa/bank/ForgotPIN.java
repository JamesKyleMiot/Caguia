package caguioa.bank;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ForgotPIN extends JDialog {
    
    private JTextField emailField;
    private JButton submitBtn;
    private JButton cancelBtn;
    private JLabel statusLabel;
    private int userId;

    public ForgotPIN(Frame owner, int userId) {
        super(owner, "Forgot PIN - Request Reset", true);
        this.userId = userId;
        
        initializeUI();
        setupListeners();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 245));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("PIN Reset Request");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        JLabel instructionLabel = new JLabel(
            "<html>" +
            "<b>To reset your PIN:</b><br>" +
            "1. Enter your email address<br>" +
            "2. Click 'Send OTP' to receive 6-digit code<br>" +
            "3. Check your email inbox (or spam folder)<br>" +
            "4. Enter the OTP and click 'OK'<br>" +
            "5. Set your new 6-digit PIN<br>" +
            "<b style='color:red'>⏱️ OTP expires after 10 minutes</b><br>" +
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
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email field
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(25);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(emailField, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        submitBtn = new JButton("Send OTP");
        submitBtn.setBackground(new Color(0, 102, 0));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        submitBtn.setPreferredSize(new Dimension(140, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
    }

    private void setupListeners() {
        submitBtn.addActionListener(e -> submitRequest());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void submitRequest() {
        String email = emailField.getText().trim();

        // Validation
        if (email.isEmpty()) {
            statusLabel.setText("❌ Please enter your email address");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (!isValidEmail(email)) {
            statusLabel.setText("❌ Please enter a valid email address");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Check if user already has pending request
        if (PINResetManager.hasPendingRequest(userId)) {
            statusLabel.setText("❌ You already have a pending PIN reset request");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Submit request
        submitBtn.setEnabled(false);
        statusLabel.setText("Processing...");
        statusLabel.setForeground(Color.BLUE);

        int requestId = PINResetManager.submitPINResetRequest(userId, email);
        
        if (requestId != -1) {  // Success - got a valid requestId
            statusLabel.setText("✓ OTP sent to your email! Expires in 10 minutes.");
            statusLabel.setForeground(new Color(34, 139, 34));
            
            JOptionPane.showMessageDialog(this,
                "✓ PIN reset request submitted successfully!\n\n" +
                "Email: " + email + "\n\n" +
                "A 6-digit OTP has been sent to your email.\n" +
                "OTP expires after 10 minutes.\n\n" +
                "Check your inbox and enter the OTP to\n" +
                "proceed with changing your PIN.\n\n" +
                "If you don't see the email, check your spam folder.",
                "OTP Sent Successfully",
                JOptionPane.INFORMATION_MESSAGE);
            

            dispose();
        } else {
            statusLabel.setText("❌ Error submitting request. Please try again.");
            statusLabel.setForeground(Color.RED);
            submitBtn.setEnabled(true);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            ForgotPIN dialog = new ForgotPIN(frame, 1);
            dialog.setVisible(true);
        });
    }
}

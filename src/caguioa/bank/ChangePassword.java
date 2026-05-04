package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePassword extends JDialog {
    
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changeBtn;
    private JButton cancelBtn;
    private JLabel statusLabel;
    private int requestId;

    public ChangePassword(Frame owner, int requestId) {
        super(owner, "Change Password", true);
        this.requestId = requestId;
        
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
        
        JLabel titleLabel = new JLabel("Set Your New Password");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        JLabel messageLabel = new JLabel(
            "Your password reset request has been approved by the admin.\n" +
            "Enter your new password below:"
        );
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(70, 130, 100));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(messageLabel, BorderLayout.SOUTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // New Password field
        JLabel newPwdLabel = new JLabel("New Password:");
        newPwdLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(newPwdLabel, gbc);

        newPasswordField = new JPasswordField(25);
        newPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(newPasswordField, gbc);

        // Confirm Password field
        JLabel confirmPwdLabel = new JLabel("Confirm Password:");
        confirmPwdLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(confirmPwdLabel, gbc);

        confirmPasswordField = new JPasswordField(25);
        confirmPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Requirements label
        JLabel requirementsLabel = new JLabel(
            "<html><font size='2' color='gray'>Requirements: At least 6 characters</font></html>"
        );
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(requirementsLabel, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        changeBtn = new JButton("Change Password");
        changeBtn.setBackground(new Color(0, 102, 0));
        changeBtn.setForeground(Color.WHITE);
        changeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        changeBtn.setPreferredSize(new Dimension(150, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(changeBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
    }

    private void setupListeners() {
        changeBtn.addActionListener(e -> changePassword());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void changePassword() {
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("❌ Please enter both passwords");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (newPassword.length() < 6) {
            statusLabel.setText("❌ Password must be at least 6 characters");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setText("❌ Passwords do not match");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Update password
        changeBtn.setEnabled(false);
        statusLabel.setText("Updating password...");
        statusLabel.setForeground(Color.BLUE);

        if (PasswordResetManager.updatePassword(Session.userId, newPassword, requestId)) {
            statusLabel.setText("✓ Password changed successfully!");
            statusLabel.setForeground(new Color(34, 139, 34));
            
            JOptionPane.showMessageDialog(this,
                "Password has been changed successfully!\n\n" +
                "You can now login with your new password.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Close after successful change
            Timer timer = new Timer(1500, e -> dispose());
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setText("❌ Error changing password. Please try again.");
            statusLabel.setForeground(Color.RED);
            changeBtn.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            ChangePassword dialog = new ChangePassword(frame, 1);
            dialog.setVisible(true);
        });
    }
}

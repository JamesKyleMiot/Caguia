package caguioa.bank;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResetPINDialog extends JDialog {
    
    private JPasswordField newPINField;
    private JPasswordField confirmPINField;
    private JButton resetBtn;
    private JButton cancelBtn;
    private JLabel statusLabel;
    private int userId;
    private int requestId;

    public ResetPINDialog(Window owner, int userId, int requestId) {
        super(owner, "Reset PIN - Set New PIN", ModalityType.APPLICATION_MODAL);
        this.userId = userId;
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
        
        JLabel titleLabel = new JLabel("Set Your New PIN");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));
        
        JLabel instructionLabel = new JLabel(
            "<html>" +
            "<b>Set Your New PIN:</b><br>" +
            "Enter and confirm your new 6-digit PIN.<br>" +
            "PIN must be 6 digits only (0-9).<br>" +
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

        // New PIN field
        JLabel newPINLabel = new JLabel("New PIN:");
        newPINLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(newPINLabel, gbc);

        newPINField = new JPasswordField(15);
        newPINField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(newPINField, gbc);

        // Confirm PIN field
        JLabel confirmPINLabel = new JLabel("Confirm PIN:");
        confirmPINLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(confirmPINLabel, gbc);

        confirmPINField = new JPasswordField(15);
        confirmPINField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(confirmPINField, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        resetBtn = new JButton("OK");
        resetBtn.setBackground(new Color(76, 175, 80));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        resetBtn.setPreferredSize(new Dimension(120, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 280);
    }

    private void setupListeners() {
        resetBtn.addActionListener(e -> resetPIN());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void resetPIN() {
        String newPIN = new String(newPINField.getPassword()).trim();
        String confirmPIN = new String(confirmPINField.getPassword()).trim();

        // Validation
        if (newPIN.isEmpty() || confirmPIN.isEmpty()) {
            statusLabel.setText("❌ Please enter PIN in both fields");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (!newPIN.equals(confirmPIN)) {
            statusLabel.setText("❌ PINs do not match");
            statusLabel.setForeground(Color.RED);
            confirmPINField.setText("");
            return;
        }

        if (!newPIN.matches("\\d{6}")) {
            statusLabel.setText("❌ PIN must be exactly 6 digits");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Update PIN
        resetBtn.setEnabled(false);
        statusLabel.setText("Processing...");
        statusLabel.setForeground(Color.BLUE);

        try {
            // Update PIN in database (PIN will be hashed by PINResetManager)
            if (PINResetManager.updateUserPIN(userId, newPIN)) {
                // Mark request as completed
                PINResetManager.markAsCompleted(requestId);
                
                statusLabel.setText("✓ PIN reset successfully!");
                statusLabel.setForeground(new Color(34, 139, 34));
                
                JOptionPane.showMessageDialog(this,
                    "✓ PIN RESET SUCCESSFULLY!\n\n" +
                    "✓ Your new PIN has been saved\n" +
                    "✓ Return to login screen\n" +
                    "✓ Login with your new PIN",
                    "PIN Reset Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } else {
                statusLabel.setText("❌ Failed to update PIN");
                statusLabel.setForeground(Color.RED);
                resetBtn.setEnabled(true);
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            resetBtn.setEnabled(true);
            System.out.println("Error resetting PIN: " + e);
        }
    }
}

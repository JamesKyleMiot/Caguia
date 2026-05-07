package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Admin User Profile Manager
 * Allows admins to view and edit user profiles
 */
public class AdminUserProfileDialog extends JDialog {
    
    private final int selectedUserId;
    private JTextField fullnameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JTextField ageField;
    private JTextField nationalityField;
    private JTextArea addressField;
    private JComboBox<String> sexCombo;
    private JLabel balanceLabel;
    private JLabel savingsLabel;
    private JLabel totalDepositLabel;
    private JLabel accountCreatedLabel;
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton suspendButton;
    private JButton closeButton;
    private boolean isEditMode = false;

    public AdminUserProfileDialog(JFrame parent, int userId) {
        super(parent, "User Profile - Admin View", true);
        this.selectedUserId = userId;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initComponents();
        loadProfileData();
        setEditMode(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 245));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        
        // Financial Info Panel
        JPanel financialPanel = createFinancialPanel();
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(financialPanel, BorderLayout.LINE_END);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(230, 245, 240));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("User Profile Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(34, 139, 34));

        JLabel subtitleLabel = new JLabel("View and manage user information");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(70, 130, 100));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Username (Read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Username:"), gbc);

        usernameField = new JTextField();
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(240, 240, 240));
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(usernameField, gbc);
        row++;

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Full Name:"), gbc);

        fullnameField = new JTextField();
        fullnameField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(fullnameField, gbc);
        row++;

        // Email
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Email:"), gbc);

        emailField = new JTextField();
        emailField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(emailField, gbc);
        row++;

        // Sex
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Sex:"), gbc);

        sexCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        sexCombo.setEnabled(false);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(sexCombo, gbc);
        row++;

        // Age
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Age:"), gbc);

        ageField = new JTextField();
        ageField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(ageField, gbc);
        row++;

        // Nationality
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("Nationality:"), gbc);

        nationalityField = new JTextField();
        nationalityField.setEditable(false);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(nationalityField, gbc);
        row++;

        // Address
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(createLabel("Address:"), gbc);

        addressField = new JTextArea();
        addressField.setEditable(false);
        addressField.setRows(3);
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(addressField), gbc);

        return formPanel;
    }

    private JPanel createFinancialPanel() {
        JPanel financialPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        financialPanel.setBackground(Color.WHITE);
        financialPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 150)),
            "Account Information",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(34, 139, 34)
        ));
        financialPanel.setPreferredSize(new Dimension(150, 0));

        // Balance Card
        JPanel balanceCard = createInfoCard("Current Balance", "₱0.00", new Color(200, 220, 240));
        balanceLabel = (JLabel) balanceCard.getComponent(1);
        financialPanel.add(balanceCard);

        // Savings Card
        JPanel savingsCard = createInfoCard("Savings", "₱0.00", new Color(200, 240, 200));
        savingsLabel = (JLabel) savingsCard.getComponent(1);
        financialPanel.add(savingsCard);

        // Total Deposit Card
        JPanel depositCard = createInfoCard("Total Deposit", "₱0.00", new Color(240, 220, 200));
        totalDepositLabel = (JLabel) depositCard.getComponent(1);
        financialPanel.add(depositCard);

        // Account Created Card
        JPanel createdCard = createInfoCard("Account Created", "N/A", new Color(240, 240, 240));
        accountCreatedLabel = (JLabel) createdCard.getComponent(1);
        financialPanel.add(createdCard);

        return financialPanel;
    }

    private JPanel createInfoCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 230, 210)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(new Color(34, 139, 34));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        editButton = new JButton("Edit Profile");
        editButton.setPreferredSize(new Dimension(110, 35));
        editButton.setBackground(new Color(33, 150, 243));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Arial", Font.BOLD, 11));
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.addActionListener(e -> toggleEditMode());

        saveButton = new JButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(110, 35));
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 11));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> saveChanges());
        saveButton.setVisible(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(110, 35));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(new Color(50, 50, 50));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 11));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> cancelEdit());
        cancelButton.setVisible(false);

        suspendButton = new JButton("Suspend Account");
        suspendButton.setPreferredSize(new Dimension(140, 35));
        suspendButton.setBackground(new Color(244, 67, 54));
        suspendButton.setForeground(Color.WHITE);
        suspendButton.setFont(new Font("Arial", Font.BOLD, 11));
        suspendButton.setFocusPainted(false);
        suspendButton.setBorderPainted(false);
        suspendButton.addActionListener(e -> suspendAccount());

        closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(110, 35));
        closeButton.setBackground(new Color(200, 200, 200));
        closeButton.setForeground(new Color(50, 50, 50));
        closeButton.setFont(new Font("Arial", Font.BOLD, 11));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(editButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(suspendButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private void loadProfileData() {
        try (Connection con = DB.connect();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT username, fullname, email, sex, age, nationality, address, balance, savings, total_deposit, created_at FROM users WHERE id = ?"
             )) {
            pst.setInt(1, selectedUserId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    usernameField.setText(rs.getString("username"));
                    fullnameField.setText(rs.getString("fullname"));
                    emailField.setText(rs.getString("email"));
                    sexCombo.setSelectedItem(rs.getString("sex"));
                    ageField.setText(String.valueOf(rs.getInt("age")));
                    nationalityField.setText(rs.getString("nationality"));
                    addressField.setText(rs.getString("address"));
                    balanceLabel.setText("₱" + String.format("%.2f", rs.getDouble("balance")));
                    savingsLabel.setText("₱" + String.format("%.2f", rs.getDouble("savings")));
                    totalDepositLabel.setText("₱" + String.format("%.2f", rs.getDouble("total_deposit")));
                    accountCreatedLabel.setText(rs.getString("created_at"));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);
    }

    private void setEditMode(boolean edit) {
        isEditMode = edit;
        
        fullnameField.setEditable(edit);
        fullnameField.setBackground(edit ? Color.WHITE : new Color(240, 240, 240));
        
        emailField.setEditable(edit);
        emailField.setBackground(edit ? Color.WHITE : new Color(240, 240, 240));
        
        ageField.setEditable(edit);
        ageField.setBackground(edit ? Color.WHITE : new Color(240, 240, 240));
        
        nationalityField.setEditable(edit);
        nationalityField.setBackground(edit ? Color.WHITE : new Color(240, 240, 240));
        
        addressField.setEditable(edit);
        addressField.setBackground(edit ? Color.WHITE : new Color(240, 240, 240));
        
        sexCombo.setEnabled(edit);

        editButton.setVisible(!edit);
        suspendButton.setEnabled(!edit);

        saveButton.setVisible(edit);
        cancelButton.setVisible(edit);
    }

    private void saveChanges() {
        // Validate input
        if (fullnameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty");
            return;
        }

        if (emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty");
            return;
        }

        if (ageField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Age cannot be empty");
            return;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 18 || age > 120) {
                JOptionPane.showMessageDialog(this, "Age must be between 18 and 120");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number");
            return;
        }

        // Update database
        try (Connection con = DB.connect();
             PreparedStatement pst = con.prepareStatement(
                 "UPDATE users SET fullname = ?, email = ?, sex = ?, age = ?, nationality = ?, address = ? WHERE id = ?"
             )) {
            pst.setString(1, fullnameField.getText().trim());
            pst.setString(2, emailField.getText().trim());
            pst.setString(3, (String) sexCombo.getSelectedItem());
            pst.setInt(4, Integer.parseInt(ageField.getText()));
            pst.setString(5, nationalityField.getText().trim());
            pst.setString(6, addressField.getText().trim());
            pst.setInt(7, selectedUserId);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "User profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            setEditMode(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + e.getMessage());
        }
    }

    private void cancelEdit() {
        loadProfileData();
        setEditMode(false);
    }

    private void suspendAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to suspend this account?\n\nThe user will not be able to access their account.",
            "Confirm Suspension",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DB.connect();
                 PreparedStatement pst = con.prepareStatement(
                     "UPDATE users SET role = 'suspended' WHERE id = ?"
                 )) {
                pst.setInt(1, selectedUserId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account suspended successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error suspending account: " + e.getMessage());
            }
        }
    }

    public void showDialog() {
        loadProfileData();
        setVisible(true);
    }
}

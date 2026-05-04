package caguioa.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class WitnessInfoDialog extends JDialog {
    
    private JTextField witnessNameField;
    private JTextField witnessContactField;
    private JTextArea witnessDetailsArea;
    private JLabel signatureStatusLabel;
    private JLabel userSignatureStatusLabel;
    private JButton addSignatureBtn;
    private JButton addUserSignatureBtn;
    private JButton saveBtn;
    private JButton cancelBtn;
    private int loanId;
    private byte[] witnessSignatureData;
    private byte[] userSignatureData;

    public WitnessInfoDialog(Frame owner, int loanId) {
        super(owner, "Witness Information - Loan #" + loanId, true);
        this.loanId = loanId;
        
        initializeUI();
        setupListeners();
        loadWitnessData();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(240, 248, 255));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Witness Information Form");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 51, 102));
        
        JLabel instructionLabel = new JLabel(
            "<html>Please provide witness information for your loan.<br>" +
            "A witness is required to verify your loan agreement.</html>"
        );
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(80, 80, 80));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Witness Name
        JLabel nameLabel = new JLabel("Witness Full Name: *");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(nameLabel, gbc);

        witnessNameField = new JTextField(30);
        witnessNameField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(witnessNameField, gbc);

        // Witness Contact
        JLabel contactLabel = new JLabel("Witness Contact: *");
        contactLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(contactLabel, gbc);

        witnessContactField = new JTextField(30);
        witnessContactField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(witnessContactField, gbc);

        // Witness Details
        JLabel detailsLabel = new JLabel("Witness Details:");
        detailsLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        formPanel.add(detailsLabel, gbc);

        witnessDetailsArea = new JTextArea(4, 30);
        witnessDetailsArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        witnessDetailsArea.setLineWrap(true);
        witnessDetailsArea.setWrapStyleWord(true);
        witnessDetailsArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane detailsScroll = new JScrollPane(witnessDetailsArea);
        gbc.gridx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(detailsScroll, gbc);

        // Witness Signature
        JLabel witnessSignLabel = new JLabel("Witness Signature:");
        witnessSignLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(witnessSignLabel, gbc);

        JPanel witnessSignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        witnessSignPanel.setOpaque(false);
        
        addSignatureBtn = new JButton("Upload Witness Signature");
        addSignatureBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        addSignatureBtn.setBackground(new Color(33, 150, 243));
        addSignatureBtn.setForeground(Color.WHITE);
        addSignatureBtn.setPreferredSize(new Dimension(160, 28));
        
        signatureStatusLabel = new JLabel("Not uploaded");
        signatureStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        signatureStatusLabel.setForeground(Color.GRAY);
        
        witnessSignPanel.add(addSignatureBtn);
        witnessSignPanel.add(signatureStatusLabel);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(witnessSignPanel, gbc);

        // User Signature
        JLabel userSignLabel = new JLabel("Your Signature:");
        userSignLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(userSignLabel, gbc);

        JPanel userSignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userSignPanel.setOpaque(false);
        
        addUserSignatureBtn = new JButton("Upload Your Signature");
        addUserSignatureBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        addUserSignatureBtn.setBackground(new Color(76, 175, 80));
        addUserSignatureBtn.setForeground(Color.WHITE);
        addUserSignatureBtn.setPreferredSize(new Dimension(160, 28));
        
        userSignatureStatusLabel = new JLabel("Not uploaded");
        userSignatureStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        userSignatureStatusLabel.setForeground(Color.GRAY);
        
        userSignPanel.add(addUserSignatureBtn);
        userSignPanel.add(userSignatureStatusLabel);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(userSignPanel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        saveBtn = new JButton("Save Witness Info");
        saveBtn.setBackground(new Color(76, 175, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveBtn.setPreferredSize(new Dimension(140, 35));

        cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(153, 153, 153));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(true);
        setMinimumSize(new Dimension(550, 500));
    }

    private void setupListeners() {
        saveBtn.addActionListener(e -> saveWitnessInfo());
        cancelBtn.addActionListener(e -> dispose());
        addSignatureBtn.addActionListener(e -> uploadWitnessSignature());
        addUserSignatureBtn.addActionListener(e -> uploadUserSignature());
    }

    private void loadWitnessData() {
        try {
            Connection conn = DB.connect();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT witness_name, witness_contact, witness_signature, user_signature FROM loans WHERE id = ?"
            );
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String witnessName = rs.getString("witness_name");
                String witnessContact = rs.getString("witness_contact");
                
                if (witnessName != null) {
                    witnessNameField.setText(witnessName);
                }
                if (witnessContact != null) {
                    witnessContactField.setText(witnessContact);
                }
                
                witnessSignatureData = rs.getBytes("witness_signature");
                userSignatureData = rs.getBytes("user_signature");
                
                if (witnessSignatureData != null) {
                    signatureStatusLabel.setText("✓ Uploaded (" + witnessSignatureData.length + " bytes)");
                    signatureStatusLabel.setForeground(new Color(76, 175, 80));
                }
                if (userSignatureData != null) {
                    userSignatureStatusLabel.setText("✓ Uploaded (" + userSignatureData.length + " bytes)");
                    userSignatureStatusLabel.setForeground(new Color(76, 175, 80));
                }
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error loading witness data: " + e);
        }
    }

    private void uploadWitnessSignature() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select Witness Signature Image");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                witnessSignatureData = java.nio.file.Files.readAllBytes(file.toPath());
                signatureStatusLabel.setText("✓ Selected (" + witnessSignatureData.length + " bytes)");
                signatureStatusLabel.setForeground(new Color(76, 175, 80));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void uploadUserSignature() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select Your Signature Image");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                userSignatureData = java.nio.file.Files.readAllBytes(file.toPath());
                userSignatureStatusLabel.setText("✓ Selected (" + userSignatureData.length + " bytes)");
                userSignatureStatusLabel.setForeground(new Color(76, 175, 80));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveWitnessInfo() {
        String witnessName = witnessNameField.getText().trim();
        String witnessContact = witnessContactField.getText().trim();
        String witnessDetails = witnessDetailsArea.getText().trim();

        // Validation
        if (witnessName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter witness name",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (witnessContact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter witness contact",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DB.connect();
            String sql = "UPDATE loans SET witness_name = ?, witness_contact = ?";
            
            if (witnessSignatureData != null) {
                sql += ", witness_signature = ?";
            }
            if (userSignatureData != null) {
                sql += ", user_signature = ?";
            }
            
            sql += " WHERE id = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, witnessName);
            pst.setString(2, witnessContact);
            
            int paramIndex = 3;
            if (witnessSignatureData != null) {
                pst.setBytes(paramIndex++, witnessSignatureData);
            }
            if (userSignatureData != null) {
                pst.setBytes(paramIndex++, userSignatureData);
            }
            pst.setInt(paramIndex, loanId);
            
            pst.executeUpdate();
            pst.close();

            JOptionPane.showMessageDialog(this,
                "✓ Witness information saved successfully!\n\n" +
                "Witness: " + witnessName + "\n" +
                "Contact: " + witnessContact + "\n\n" +
                "Admin will review and verify this information.",
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } catch (Exception e) {
            System.out.println("Error saving witness info: " + e);
            JOptionPane.showMessageDialog(this,
                "Error saving witness information: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            WitnessInfoDialog dialog = new WitnessInfoDialog(frame, 1);
            dialog.setVisible(true);
        });
    }
}

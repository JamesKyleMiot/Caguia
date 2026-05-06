package caguioa.bank;

import javax.swing.*;
import java.awt.*;

/**
 * Template for creating responsive dialogs
 * Use this as a base for all dialog components
 */
public class ResponsiveDialogTemplate extends JDialog {

    private JPanel mainPanel;
    private JPanel buttonPanel;

    public ResponsiveDialogTemplate(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        
        // Set responsive size and properties
        ResponsiveUIHelper.makeDialogResponsive(this, 50, 60); // 50% width, 60% height
        
        // Setup responsive scaling
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // Initialize UI
        initComponents();
        
        // Make responsive to resize
        ResponsiveUIHelper.addResponsiveScalingListener(mainPanel);
    }

    private void initComponents() {
        // Main container with responsive layout
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(
            ResponsiveUIHelper.getResponsivePadding(10),
            ResponsiveUIHelper.getResponsivePadding(10)
        ));
        mainPanel.setBorder(ResponsiveUIHelper.getResponsiveBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Add header (optional)
        JPanel headerPanel = createHeaderPanel();
        if (headerPanel != null) {
            mainPanel.add(headerPanel, BorderLayout.NORTH);
        }

        // Add content area
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add button panel
        buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set main panel as content pane
        setContentPane(mainPanel);
    }

    /**
     * Override this method to create header panel
     * Return null if not needed
     */
    protected JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(70, 130, 180));
        
        JLabel titleLabel = new JLabel("Dialog Title");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 
            ResponsiveUIHelper.getResponsiveFontSize(14)));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        return headerPanel;
    }

    /**
     * Override this method to create main content panel
     * This is where your form elements go
     */
    protected JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Example: Add sample label
        JLabel sampleLabel = new JLabel("Add your form elements here");
        sampleLabel.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(12)));
        
        GridBagConstraints gbc = ResponsiveUIHelper.getResponsiveGBC(0, 0, 1, 1, 1.0, 1.0);
        contentPanel.add(sampleLabel, gbc);
        
        return contentPanel;
    }

    /**
     * Create responsive button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 
            ResponsiveUIHelper.getResponsivePadding(10),
            ResponsiveUIHelper.getResponsivePadding(10)));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        // OK Button
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(12),
            ResponsiveUIHelper.getResponsiveHeight(4)
        ));
        okButton.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(11)));
        okButton.setBackground(new Color(70, 130, 180));
        okButton.setForeground(Color.WHITE);
        okButton.setBorderPainted(false);
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> onOkClick());

        // Cancel Button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(12),
            ResponsiveUIHelper.getResponsiveHeight(4)
        ));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(11)));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(new Color(50, 50, 50));
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /**
     * Override this method to handle OK button click
     */
    protected void onOkClick() {
        System.out.println("OK clicked");
        dispose();
    }

    /**
     * Display the dialog
     */
    public void show(JFrame parent) {
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }
}

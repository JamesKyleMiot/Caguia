package caguioa.bank;

import javax.swing.*;
import java.awt.*;

/**
 * Template for creating responsive panels
 * Use this as a base for all panel components within dashboards
 */
public class ResponsivePanelTemplate extends JPanel {

    public ResponsivePanelTemplate() {
        initComponents();
    }

    private void initComponents() {
        // Use BorderLayout for flexible, responsive layout
        this.setLayout(new BorderLayout(
            ResponsiveUIHelper.getResponsivePadding(10),
            ResponsiveUIHelper.getResponsivePadding(10)
        ));
        
        // Responsive padding
        this.setBorder(ResponsiveUIHelper.getResponsiveBorder(15, 15, 15, 15));
        this.setBackground(Color.WHITE);
    }

    /**
     * Create a responsive card/summary panel
     * Use for dashboard statistics/overview cards
     */
    public static JPanel createResponsiveCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setBorder(ResponsiveUIHelper.getResponsiveBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(12)));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 
            ResponsiveUIHelper.getResponsiveFontSize(20)));
        valueLabel.setForeground(new Color(30, 30, 30));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(ResponsiveUIHelper.getResponsivePadding(5)));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(ResponsiveUIHelper.getResponsivePadding(5)));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(ResponsiveUIHelper.getResponsivePadding(5)));

        return card;
    }

    /**
     * Create responsive form field (label + input)
     */
    public static JPanel createResponsiveFormField(String label, JComponent input) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout(
            ResponsiveUIHelper.getResponsivePadding(10),
            ResponsiveUIHelper.getResponsivePadding(10)
        ));
        fieldPanel.setBackground(Color.WHITE);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(11)));
        fieldLabel.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(20),
            ResponsiveUIHelper.getResponsiveHeight(3)
        ));

        input.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(70),
            ResponsiveUIHelper.getResponsiveHeight(4)
        ));

        fieldPanel.add(fieldLabel, BorderLayout.WEST);
        fieldPanel.add(input, BorderLayout.CENTER);

        return fieldPanel;
    }

    /**
     * Create responsive button with consistent sizing
     */
    public static JButton createResponsiveButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(15),
            ResponsiveUIHelper.getResponsiveHeight(4)
        ));
        button.setFont(new Font("Arial", Font.PLAIN, 
            ResponsiveUIHelper.getResponsiveFontSize(11)));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 110, 160));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }

    /**
     * Create responsive horizontal separator
     */
    public static JComponent createResponsiveSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(
            ResponsiveUIHelper.getResponsiveWidth(80),
            ResponsiveUIHelper.getResponsiveHeight(1)
        ));
        return separator;
    }

    /**
     * Create responsive GridPanel for card layout
     */
    public static JPanel createResponsiveCardGrid(int columns) {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(1, columns, 
            ResponsiveUIHelper.getResponsivePadding(10),
            ResponsiveUIHelper.getResponsivePadding(10)
        ));
        gridPanel.setBackground(Color.WHITE);
        return gridPanel;
    }

    /**
     * Create responsive scrollable list panel
     */
    public static JScrollPane createResponsiveListPanel(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setPreferredSize(ResponsiveUIHelper.getResponsiveDimension(90, 50));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(
            ResponsiveUIHelper.getResponsivePadding(10)
        );
        return scrollPane;
    }
}

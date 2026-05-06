package caguioa.bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Helper class for creating responsive UI components
 * Provides dynamic sizing, scaling, and layout utilities
 */
public class ResponsiveUIHelper {

    // Screen dimensions
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int SCREEN_WIDTH = screenSize.width;
    public static final int SCREEN_HEIGHT = screenSize.height;
    
    // DPI scaling factor (96 DPI = 1.0)
    private static final float DPI_SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 96f;

    /**
     * Get responsive width based on percentage of screen
     * @param percentOfScreen - percentage (0-100)
     * @return responsive width in pixels
     */
    public static int getResponsiveWidth(double percentOfScreen) {
        return Math.round((float) (SCREEN_WIDTH * (percentOfScreen / 100.0)) * DPI_SCALE);
    }

    /**
     * Get responsive height based on percentage of screen
     * @param percentOfScreen - percentage (0-100)
     * @return responsive height in pixels
     */
    public static int getResponsiveHeight(double percentOfScreen) {
        return Math.round((float) (SCREEN_HEIGHT * (percentOfScreen / 100.0)) * DPI_SCALE);
    }

    /**
     * Get responsive font size
     * @param baseSize - base font size for 96 DPI
     * @return scaled font size
     */
    public static int getResponsiveFontSize(int baseSize) {
        return Math.round(baseSize * DPI_SCALE);
    }

    /**
     * Get responsive padding/margin
     * @param baseSize - base padding in pixels
     * @return scaled padding
     */
    public static int getResponsivePadding(int baseSize) {
        return Math.round(baseSize * DPI_SCALE);
    }

    /**
     * Get responsive component dimension
     * @param widthPercent - width as % of screen
     * @param heightPercent - height as % of screen
     * @return Dimension object
     */
    public static Dimension getResponsiveDimension(double widthPercent, double heightPercent) {
        return new Dimension(
            getResponsiveWidth(widthPercent),
            getResponsiveHeight(heightPercent)
        );
    }

    /**
     * Make a frame responsive and centered
     * @param frame - the JFrame to make responsive
     * @param widthPercent - width as % of screen
     * @param heightPercent - height as % of screen
     */
    public static void makeFrameResponsive(JFrame frame, double widthPercent, double heightPercent) {
        int width = getResponsiveWidth(widthPercent);
        int height = getResponsiveHeight(heightPercent);
        
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(
            getResponsiveWidth(widthPercent * 0.7),  // 70% minimum
            getResponsiveHeight(heightPercent * 0.7)
        ));
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setExtendedState(JFrame.NORMAL); // Don't maximize
    }

    /**
     * Make a dialog responsive and centered on parent
     * @param dialog - the JDialog to make responsive
     * @param widthPercent - width as % of screen
     * @param heightPercent - height as % of screen
     */
    public static void makeDialogResponsive(JDialog dialog, double widthPercent, double heightPercent) {
        int width = getResponsiveWidth(widthPercent);
        int height = getResponsiveHeight(heightPercent);
        
        dialog.setSize(width, height);
        dialog.setMinimumSize(new Dimension(
            getResponsiveWidth(widthPercent * 0.8),  // 80% minimum
            getResponsiveHeight(heightPercent * 0.8)
        ));
        dialog.setLocationRelativeTo(dialog.getParent());
    }

    /**
     * Create responsive scaling listener for dynamic window resizing
     * Automatically scales components based on window size
     * @param panel - panel to add listener to
     */
    public static void addResponsiveScalingListener(JPanel panel) {
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Revalidate layout on resize
                panel.revalidate();
                panel.repaint();
            }
        });
    }

    /**
     * Get scaled icon (for responsive images)
     * @param path - path to image
     * @param maxWidth - maximum width
     * @param maxHeight - maximum height
     * @return scaled ImageIcon
     */
    public static ImageIcon getScaledIcon(String path, int maxWidth, int maxHeight) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get responsive GridBagConstraints for flexible layouts
     * @param gridx - column
     * @param gridy - row
     * @param gridwidth - column span
     * @param gridheight - row span
     * @param weightx - horizontal weight (0.0-1.0)
     * @param weighty - vertical weight (0.0-1.0)
     * @return configured GridBagConstraints
     */
    public static GridBagConstraints getResponsiveGBC(
            int gridx, int gridy, int gridwidth, int gridheight,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(
            getResponsivePadding(5),
            getResponsivePadding(5),
            getResponsivePadding(5),
            getResponsivePadding(5)
        );
        return gbc;
    }

    /**
     * Create responsive empty border padding
     * @param top - top padding
     * @param left - left padding
     * @param bottom - bottom padding
     * @param right - right padding
     * @return BorderFactory EmptyBorder
     */
    public static javax.swing.border.Border getResponsiveBorder(
            int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(
            getResponsivePadding(top),
            getResponsivePadding(left),
            getResponsivePadding(bottom),
            getResponsivePadding(right)
        );
    }

    /**
     * Get responsive preferred size for component
     * @param component - the component
     * @param widthPercent - width as % of parent
     * @param heightPercent - height as % of parent
     */
    public static void setResponsiveSize(JComponent component, double widthPercent, double heightPercent) {
        Dimension preferred = new Dimension(
            getResponsiveWidth(widthPercent),
            getResponsiveHeight(heightPercent)
        );
        component.setPreferredSize(preferred);
        component.setMaximumSize(preferred);
    }

    /**
     * Get system metrics for debugging
     */
    public static void printSystemMetrics() {
        System.out.println("=== RESPONSIVE UI METRICS ===");
        System.out.println("Screen Resolution: " + SCREEN_WIDTH + "x" + SCREEN_HEIGHT);
        System.out.println("DPI Scale Factor: " + DPI_SCALE);
        System.out.println("Screen DPI: " + Toolkit.getDefaultToolkit().getScreenResolution());
    }
}

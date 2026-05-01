package caguioa.bank;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public final class LogoUtil {

    private LogoUtil() {
    }

    public static JLabel createLogoLabel(int width, int height) {
        URL resource = LogoUtil.class.getResource("/caguioa/bank/banklogo.png");
        if (resource == null) {
            return new JLabel();
        }

        ImageIcon icon = new ImageIcon(resource);
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaledImage));
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}
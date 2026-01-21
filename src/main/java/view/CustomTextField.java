package view;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomTextField {

    public static JComponent createDarkInput(String placeholder, String icon) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(new Color(0, 200, 255));
        iconLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, iconLabel);

        field.putClientProperty(FlatClientProperties.STYLE,
                "arc: 15; " +
                        "background: #101929; " +
                        "foreground: #FFFFFF; " +
                        "caretColor: #FFFFFF; " +
                        "borderWidth: 1; " +
                        "borderColor: #253550; "
        );
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return field;
    }
}
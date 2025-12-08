package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomIconButton {

    public static JButton createIconButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(color);
        button.setPreferredSize(new Dimension(40, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(button, "You clicked: " + text);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(color);
            }
        });

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();

                g2.setColor(new Color(20, 20, 35));
                g2.fillOval(0, 0, w, h);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, w - 1, h - 1);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return button;
    }

    public static JButton createNeonButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 15, 15);

                Color baseColor = button.getModel().isPressed() ? color.darker() : color;
                g2.setColor(baseColor);
                g2.fillRoundRect(0, 0, w, h, 15, 15);

                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return button;
    }
}

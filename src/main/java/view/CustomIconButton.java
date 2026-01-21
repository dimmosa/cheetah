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

    public static JButton createNeonButton(String text, Color initialNeon, int w, int h) {
        JButton b = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ✅ צבע דינמי מהכפתור
                Color neon = (Color) getClientProperty("neonColor");
                if (neon == null) neon = initialNeon;

                // fill
                g2.setColor(neon);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // inner shadow for depth
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);

                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color neon = (Color) getClientProperty("neonColor");
                if (neon == null) neon = initialNeon;

                g2.setColor(neon.brighter());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                g2.dispose();
            }
        };

        // ✅ שמירת צבע התחלתי
        b.putClientProperty("neonColor", initialNeon);

        b.setPreferredSize(new Dimension(w, h));
        b.setMinimumSize(new Dimension(w, h));
        b.setMaximumSize(new Dimension(w, h));

        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false); // מציירים לבד
        b.setOpaque(false);

        return b;
    }

}

package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;


public class ConfirmDialog extends JDialog {

    private boolean result = false;
    private float opacity = 0f;

    private ConfirmDialog(JFrame parent, String message, String title) {
        super(parent, title, true);
        setUndecorated(true);
        setSize(520, 220);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setShape(new RoundRectangle2D.Double(0, 0, 520, 220, 26, 26));
        setBackground(new Color(0, 0, 0, 0));

        Color accent = new Color(239, 68, 68);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(18, 28, 45),
                        getWidth(), getHeight(), new Color(26, 40, 64)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

                g2.setColor(new Color(255, 255, 255, 22));
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 24, 24);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(20, 22, 18, 22));

        JLabel icon = new JLabel("🗑️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        icon.setPreferredSize(new Dimension(70, 70));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(235, 242, 255));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel msg = new JLabel("<html>" + escape(message) + "</html>");
        msg.setForeground(new Color(190, 205, 230));
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(msg);

        JButton cancel = makeGhost("Cancel");
        JButton confirm = makePrimary("Delete", accent);

        cancel.addActionListener(e -> {
            result = false;
            fadeOutAndDispose();
        });

        confirm.addActionListener(e -> {
            result = true;
            fadeOutAndDispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(cancel);
        btns.add(confirm);

        panel.add(icon, BorderLayout.WEST);
        panel.add(center, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        // fade in
        setOpacity(0f);
        Timer t = new Timer(16, e -> {
            opacity = Math.min(1f, opacity + 0.12f);
            setOpacity(opacity);
            if (opacity >= 1f) ((Timer) e.getSource()).stop();
        });
        t.start();
    }

    private void fadeOutAndDispose() {
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            opacity = Math.max(0f, opacity - 0.14f);
            setOpacity(opacity);
            if (opacity <= 0f) {
                ((Timer) e.getSource()).stop();
                dispose();
            }
        });
        t.start();
    }

    private static JButton makePrimary(String text, Color color) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 38));
        return b;
    }

    private static JButton makeGhost(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 80, 120, 120));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(new Color(235, 242, 255));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 38));
        return b;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static boolean confirm(JFrame parent, String message, String title) {
        ConfirmDialog d = new ConfirmDialog(parent, message, title);
        d.setVisible(true);
        return d.result;
    }
}

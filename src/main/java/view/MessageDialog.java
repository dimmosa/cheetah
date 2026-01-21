package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern message dialog (Success / Error / Warning / Info)
 * Usage:
 *   MessageDialog.show(frame, "Saved!", MessageDialog.Type.SUCCESS);
 */
public class MessageDialog extends JDialog {

    public enum Type { SUCCESS, ERROR, WARNING, INFO }

    private float opacity = 0f;

    private MessageDialog(JFrame parent, String message, Type type) {
        super(parent, "", true);
        setUndecorated(true);
        setSize(460, 190);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setShape(new RoundRectangle2D.Double(0, 0, 460, 190, 26, 26));
        setBackground(new Color(0, 0, 0, 0));

        Color accent = switch (type) {
            case SUCCESS -> new Color(34, 197, 94);
            case ERROR -> new Color(239, 68, 68);
            case WARNING -> new Color(251, 146, 60);
            default -> new Color(59, 130, 246);
        };

        String emoji = switch (type) {
            case SUCCESS -> "✅";
            case ERROR -> "⛔";
            case WARNING -> "⚠️";
            default -> "ℹ️";
        };

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

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 24, 24);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(14, 0));
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setPreferredSize(new Dimension(70, 70));

        JLabel title = new JLabel(typeTitle(type));
        title.setForeground(accent);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel msg = new JLabel("<html>" + escape(message) + "</html>");
        msg.setForeground(new Color(235, 242, 255));
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(8));
        text.add(msg);

        JButton okBase = new JButton("OK");
        okBase.setFocusPainted(false);
        okBase.setBorderPainted(false);
        okBase.setContentAreaFilled(false);
        okBase.setForeground(Color.WHITE);
        okBase.setFont(new Font("SansSerif", Font.BOLD, 13));
        okBase.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBase.setPreferredSize(new Dimension(110, 38));

        JButton ok = wrapPrimaryButton(okBase, accent);
        ok.addActionListener(e -> fadeOutAndDispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(ok);

        panel.add(icon, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        // Fade in
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

    private static JButton wrapPrimaryButton(JButton btn, Color accent) {
        JButton b = new JButton(btn.getText()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        // copy the base button settings
        b.setFont(btn.getFont());
        b.setForeground(btn.getForeground());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(btn.getCursor());
        b.setPreferredSize(btn.getPreferredSize());

        return b;
    }

    private static String typeTitle(Type t) {
        return switch (t) {
            case SUCCESS -> "Success";
            case ERROR -> "Error";
            case WARNING -> "Warning";
            default -> "Info";
        };
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void show(JFrame parent, String message, Type type) {
        MessageDialog d = new MessageDialog(parent, message, type);
        d.setVisible(true);
    }
}

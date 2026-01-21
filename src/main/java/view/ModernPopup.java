package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class ModernPopup {

    public enum Type { INFO, WARNING, ERROR, SUCCESS, QUESTION }

    // ===================== Public API =====================

    public static void info(Component parent, String title, String message) {
        showMessage(parent, title, message, Type.INFO);
    }

    public static void warn(Component parent, String title, String message) {
        showMessage(parent, title, message, Type.WARNING);
    }

    public static void error(Component parent, String title, String message) {
        showMessage(parent, title, message, Type.ERROR);
    }

    public static void success(Component parent, String title, String message) {
        showMessage(parent, title, message, Type.SUCCESS);
    }

    public static boolean confirm(Component parent, String title, String message) {
        return showConfirm(parent, title, message);
    }

    // ===================== Core =====================

    private static void showMessage(Component parent, String title, String message, Type type) {
        Window owner = SwingUtilities.getWindowAncestor(parent);

        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        d.setBackground(new Color(12, 16, 30));

        PopupPanel panel = new PopupPanel(type);
        panel.setLayout(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(18, 18, 16, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel icon = new JLabel(iconFor(type));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(accent(type));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 15));
        t.setForeground(new Color(235, 245, 255));

        header.add(icon, BorderLayout.WEST);
        header.add(t, BorderLayout.CENTER);

        // Message
        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msg.setForeground(new Color(200, 210, 225));
        msg.setOpaque(false);
        msg.setEditable(false);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);

        // Button
        JButton ok = createPrimaryButton("OK", type);
        ok.addActionListener(e -> animateClose(d));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(ok);

        panel.add(header, BorderLayout.NORTH);
        panel.add(msg, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(panel);
        d.setSize(420, 200);
        d.setLocationRelativeTo(owner);

        applyShapeSafe(d, 22);
        animateOpenSafe(d);

        d.setVisible(true);

        // ✅ SAFE SHAKE (after dialog is visible)
        if (type == Type.ERROR || type == Type.WARNING) {
            SwingUtilities.invokeLater(() -> shakeOnce(d));
        }
    }

    private static boolean showConfirm(Component parent, String title, String message) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        final boolean[] result = {false};

        JDialog d = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        d.setBackground(new Color(12, 16, 30));

        PopupPanel panel = new PopupPanel(Type.QUESTION);
        panel.setLayout(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(18, 18, 16, 18));

        JLabel icon = new JLabel(iconFor(Type.QUESTION));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(accent(Type.QUESTION));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 15));
        t.setForeground(new Color(235, 245, 255));

        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msg.setForeground(new Color(200, 210, 225));
        msg.setOpaque(false);
        msg.setEditable(false);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);

        JButton no = createGhostButton("NO");
        JButton yes = createPrimaryButton("YES", Type.SUCCESS);

        no.addActionListener(e -> { result[0] = false; animateClose(d); });
        yes.addActionListener(e -> { result[0] = true; animateClose(d); });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(no);
        buttons.add(yes);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.add(icon, BorderLayout.WEST);
        header.add(t, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(msg, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(panel);
        d.setSize(460, 210);
        d.setLocationRelativeTo(owner);

        applyShapeSafe(d, 22);
        animateOpenSafe(d);

        d.setVisible(true);
        return result[0];
    }

    // ===================== Shake (FIXED) =====================

    private static void shakeOnce(JDialog d) {
        if (!d.isShowing()) return; // ✅ critical guard

        final Point base = d.getLocation(); // ✅ SAFE
        Timer t = new Timer(18, null);
        final int[] i = {0};

        t.addActionListener(e -> {
            int dx = (i[0] % 2 == 0) ? 6 : -6;
            d.setLocation(base.x + dx, base.y);
            i[0]++;

            if (i[0] > 8) {
                d.setLocation(base);
                t.stop();
            }
        });

        t.start();
    }

    // ===================== Animations =====================

    private static void animateOpenSafe(JDialog d) {
        try { d.setOpacity(0f); } catch (Exception ignored) {}

        Timer t = new Timer(15, null);
        final float[] p = {0f};

        t.addActionListener(e -> {
            p[0] += 0.12f;
            float k = Math.min(1f, p[0]);
            try { d.setOpacity(k); } catch (Exception ignored) {}
            if (k >= 1f) t.stop();
        });

        t.start();
    }

    private static void animateClose(JDialog d) {
        Timer t = new Timer(12, null);
        final float[] p = {1f};

        t.addActionListener(e -> {
            p[0] -= 0.12f;
            float k = Math.max(0f, p[0]);
            try { d.setOpacity(k); } catch (Exception ignored) {}
            if (k <= 0f) {
                t.stop();
                d.dispose();
            }
        });

        t.start();
    }

    private static void applyShapeSafe(JDialog d, int arc) {
        try {
            d.setShape(new RoundRectangle2D.Double(0, 0, d.getWidth(), d.getHeight(), arc, arc));
        } catch (Exception ignored) {}
    }

    // ===================== UI =====================

    private static class PopupPanel extends JPanel {
        private final Type type;
        PopupPanel(Type type) { this.type = type; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0,0,0,150));
            g2.fillRoundRect(8,8,w-8,h-8,22,22);

            GradientPaint gp = new GradientPaint(0,0,new Color(18,26,45),w,h,new Color(12,18,34));
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,w-10,h-10,22,22);

            GradientPaint bar = new GradientPaint(0,0,accent(type),w,0,accent2(type));
            g2.setPaint(bar);
            g2.fillRoundRect(0,0,w-10,6,22,22);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===================== Helpers =====================

    private static String iconFor(Type t) {
        return switch (t) {
            case INFO -> "ℹ";
            case WARNING -> "⚠";
            case ERROR -> "⛔";
            case SUCCESS -> "✓";
            case QUESTION -> "❓";
        };
    }

    private static Color accent(Type t) {
        return switch (t) {
            case INFO -> new Color(0,195,255);
            case WARNING -> new Color(255,175,70);
            case ERROR -> new Color(255,85,95);
            case SUCCESS -> new Color(0,230,120);
            case QUESTION -> new Color(170,140,255);
        };
    }

    private static Color accent2(Type t) {
        return switch (t) {
            case INFO, SUCCESS, QUESTION -> new Color(0,195,255);
            case WARNING -> new Color(255,210,80);
            case ERROR -> new Color(255,120,130);
        };
    }

    private static JButton createPrimaryButton(String text, Type type) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private static JButton createGhostButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(new Color(210,225,255));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }
}

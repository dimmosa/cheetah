package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernDialog {

    public enum Theme { INFO, SUCCESS, WARNING, DANGER }

    private static Color themeColor(Theme t) {
        return switch (t) {
            case SUCCESS -> new Color(34, 197, 94);
            case WARNING -> new Color(251, 191, 36);
            case DANGER -> new Color(239, 68, 68);
            default -> new Color(59, 130, 246);
        };
    }

    public static void info(JFrame parent, String title, String message, Theme theme) {
        JDialog d = baseDialog(parent, title, message, theme);
        JButton ok = primaryButton("OK", theme);
        ok.addActionListener(e -> d.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(ok);

        ((JPanel) d.getContentPane()).add(btns, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    public static boolean confirm(JFrame parent, String title, String message, Theme theme) {
        final boolean[] res = {false};
        JDialog d = baseDialog(parent, title, message, theme);

        JButton cancel = ghostButton("Cancel");
        JButton ok = primaryButton("Continue", theme);

        cancel.addActionListener(e -> { res[0] = false; d.dispose(); });
        ok.addActionListener(e -> { res[0] = true; d.dispose(); });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(cancel);
        btns.add(ok);

        ((JPanel) d.getContentPane()).add(btns, BorderLayout.SOUTH);
        d.setVisible(true);
        return res[0];
    }

    public static Integer chooseOption(JFrame parent, String title, String message, String[] options, Theme theme) {
        final Integer[] res = {null};
        JDialog d = baseDialog(parent, title, message, theme);

        JPanel opts = new JPanel(new GridLayout(options.length, 1, 0, 8));
        opts.setOpaque(false);
        opts.setBorder(new EmptyBorder(10, 0, 10, 0));

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radios = new JRadioButton[options.length];

        for (int i = 0; i < options.length; i++) {
            JRadioButton r = new JRadioButton(options[i]);
            r.setOpaque(false);
            r.setForeground(new Color(230, 240, 255));
            r.setFont(EmojiHelper.getEmojiFont(14)); // ✅ FIXED!
            radios[i] = r;
            group.add(r);
            opts.add(r);
        }

        JButton cancel = ghostButton("Cancel");
        JButton ok = primaryButton("Submit", theme);

        cancel.addActionListener(e -> { res[0] = null; d.dispose(); });
        ok.addActionListener(e -> {
            for (int i = 0; i < radios.length; i++) {
                if (radios[i].isSelected()) {
                    res[0] = i;
                    break;
                }
            }
            d.dispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(cancel);
        btns.add(ok);

        JPanel content = (JPanel) d.getContentPane();
        content.add(opts, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);

        d.setVisible(true);
        return res[0];
    }

    private static JDialog baseDialog(JFrame parent, String title, String message, Theme theme) {
        Color accent = themeColor(theme);

        JDialog d = new JDialog(parent, true);
        d.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(12, 18, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 130));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel(title);
        t.setFont(EmojiHelper.getEmojiFont(16)); // ✅ FIXED! היה: new Font("Segoe UI", Font.BOLD, 16)
        t.setForeground(Color.WHITE);

        JLabel m = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        m.setFont(EmojiHelper.getEmojiFont(13)); // ✅ FIXED! היה: new Font("Segoe UI", Font.PLAIN, 13)
        m.setForeground(new Color(180, 200, 220));
        m.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(t, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(m, BorderLayout.NORTH);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        d.setContentPane(root);
        d.setSize(420, 220);
        d.setLocationRelativeTo(parent);

        // drag support
        final Point[] start = {null};
        root.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { start[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                Point p = d.getLocation();
                d.setLocation(p.x + e.getX() - start[0].x, p.y + e.getY() - start[0].y);
            }
        });

        return d;
    }

    private static JButton primaryButton(String text, Theme theme) {
        Color accent = themeColor(theme);

        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, accent, getWidth(), 0, accent.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 36));
        return b;
    }

    private static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(new Color(180, 200, 220));
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(90, 120, 160, 120), 2));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 36));
        return b;
    }
    
    public static void info(Window window, String title, String message, Theme theme) {
        JFrame owner = (window instanceof JFrame)
                ? (JFrame) window
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, window);
        info(owner, title, message, theme);
    }

    public static boolean confirm(Window window, String title, String message, Theme theme) {
        JFrame owner = (window instanceof JFrame)
                ? (JFrame) window
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, window);
        return confirm(owner, title, message, theme);
    }
}
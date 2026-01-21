package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "How to Play", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(680, 740);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 242));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(180, 80, 255));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                // subtle inner line
                g2.setColor(new Color(255, 255, 255, 22));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 26, 26);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 28, 20, 28));
        setContentPane(mainPanel);

        // =========================
        // HEADER
        // =========================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(
                "<html><font color='#00BFFF'>📘</font> <font color='#87CEFA'>How to Play</font></html>"
        );
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel closeBtn = new JLabel("✖");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        closeBtn.setForeground(new Color(150, 100, 200));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(new Color(200, 140, 255)); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(new Color(150, 100, 200)); }
        });
        headerPanel.add(closeBtn, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // =========================
        // CONTENT
        // =========================
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 0, 10, 10));

        // quick summary card
        content.add(createIntroCard());
        content.add(Box.createVerticalStrut(18));

        // sections
        content.add(createSectionHeader("1) Goal of the Game", "🎯"));
        content.add(Box.createVerticalStrut(8));
        content.add(createBullet("Reveal safe cells, find mines, and finish with the highest score."));
        content.add(createBullet("You win when all mines are handled (revealed or correctly flagged)."));
        content.add(createBullet("You lose if your lives reach 0."));
        content.add(Box.createVerticalStrut(18));

        content.add(createSectionHeader("2) Controls", "🖱️"));
        content.add(Box.createVerticalStrut(10));
        content.add(createControlRow("Left Click", new Color(0, 150, 150), "Reveal a cell (number / empty / special)."));
        content.add(Box.createVerticalStrut(8));
        content.add(createControlRow("Right Click", new Color(72, 118, 255), "Place a flag on a hidden cell."));
        content.add(Box.createVerticalStrut(8));
        content.add(createControlRow("Flag Mode", new Color(255, 165, 0), "If Flag Mode is ON, left click places flags instead of revealing."));
        content.add(Box.createVerticalStrut(18));

        content.add(createSectionHeader("3) Scoring & Lives (Practice Mode)", "🏆"));
        content.add(Box.createVerticalStrut(10));
        content.add(createRuleRow("Reveal a safe cell", "+1 point", new Color(0, 255, 127)));
        content.add(Box.createVerticalStrut(6));
        content.add(createRuleRow("Correctly flag a mine", "+1 point", new Color(0, 255, 127)));
        content.add(Box.createVerticalStrut(6));
        content.add(createRuleRow("Wrong flag", "−3 points", new Color(255, 99, 71)));
        content.add(Box.createVerticalStrut(6));
        content.add(createRuleRow("Hit a mine", "−1 life", new Color(255, 99, 71)));
        content.add(Box.createVerticalStrut(10));
        content.add(createHintBox(
                "Tip: In this mode, cascade reveal should NOT give extra points per cell.\n" +
                "Only the first click gives +1 (your board is fixed to behave like that)."
        ));
        content.add(Box.createVerticalStrut(18));

        content.add(createSectionHeader("4) Cell Types", "🧩"));
        content.add(Box.createVerticalStrut(12));

        content.add(createCellTypeCard("⬛ Hidden Cell", "Unknown cell. Click to reveal or flag it."));
        content.add(Box.createVerticalStrut(10));
        content.add(createCellTypeCard("🔢 Number Cell", "Shows how many mines are adjacent to it."));
        content.add(Box.createVerticalStrut(10));
        content.add(createCellTypeCard("⬜ Empty Cell", "No adjacent mines. Reveals nearby safe area (cascade)."));
        content.add(Box.createVerticalStrut(10));

        content.add(createCellTypeCard("🟡 Question Cell", ""
                + "Activating this cell costs points.\n"
                + "You answer a timed multiple-choice question.\n"
                + "Correct: gain points and lives.\n"
                + "Wrong: lose points and possibly lives.\n"
                + "If time runs out, it counts as skipped/failed (based on your dialog rules)."
        ));
        content.add(Box.createVerticalStrut(10));

        content.add(createCellTypeCard("🎁 Surprise Cell", ""
                + "Activating this cell costs points.\n"
                + "50% chance: bonus (points/lives) or penalty (points/lives)."
        ));
        content.add(Box.createVerticalStrut(18));

        content.add(createSectionHeader("5) Difficulty (Board Size & Mines)", "⚙️"));
        content.add(Box.createVerticalStrut(10));
        content.add(createDifficultyTable());
        content.add(Box.createVerticalStrut(18));

        content.add(createSectionHeader("6) Common Mistakes", "🚫"));
        content.add(Box.createVerticalStrut(10));
        content.add(createBullet("Wrong flags reduce your score, so don’t spam flags."));
        content.add(createBullet("If you don’t have enough points, you can’t activate Question/Surprise."));
        content.add(Box.createVerticalStrut(10));

        content.add(createFooterNote());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 120);
                this.trackColor = new Color(30, 30, 40, 0);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle tb) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 80, 110));
                g2.fillRoundRect(tb.x + 4, tb.y, 7, tb.height, 8, 8);
                g2.dispose();
            }
        });

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // =========================
    // UI BUILDERS
    // =========================
    private JPanel createIntroCard() {
        JPanel card = new JPanel(new BorderLayout(12, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(255, 255, 255, 26));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html><b>Practice Mode (Single Player)</b></html>");
        title.setFont(new Font("SansSerif", Font.PLAIN, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("<html><font color='#B0B0C8'>Learn the rules, improve your accuracy, and build high score.</font></html>");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));

        card.add(title, BorderLayout.NORTH);
        card.add(sub, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSectionHeader(String text, String iconEmoji) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(iconEmoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        icon.setForeground(Color.CYAN);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(new Color(135, 206, 250));

        p.add(icon);
        p.add(label);
        return p;
    }

    private JPanel createControlRow(String badgeText, Color badgeColor, String descText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(badgeText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2.setColor(new Color(255,255,255,55));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                super.paintComponent(g);
            }
        };
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setPreferredSize(new Dimension(110, 26));

        JTextArea desc = new JTextArea(descText);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(220, 220, 220));
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setPreferredSize(new Dimension(470, 36));

        panel.add(badge);
        panel.add(desc);
        return panel;
    }

    private JPanel createRuleRow(String action, String result, Color resultColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 14, 0, 10));

        JLabel left = new JLabel(action);
        left.setFont(new Font("SansSerif", Font.PLAIN, 15));
        left.setForeground(new Color(210, 210, 230));

        JLabel right = new JLabel(result);
        right.setFont(new Font("SansSerif", Font.BOLD, 15));
        right.setForeground(resultColor);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel createCellTypeCard(String title, String body) {
        JPanel card = new JPanel(new BorderLayout(8, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 50, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, 24));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(Color.WHITE);

        JTextArea txt = new JTextArea(body);
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setForeground(new Color(200, 200, 215));
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);

        card.add(t, BorderLayout.NORTH);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDifficultyTable() {
        JPanel table = new JPanel(new GridLayout(4, 3, 10, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                super.paintComponent(g);
            }
        };
        table.setOpaque(false);
        table.setAlignmentX(Component.LEFT_ALIGNMENT);
        table.setBorder(new EmptyBorder(12, 14, 12, 14));

        table.add(makeTableCell("<b>Difficulty</b>", new Color(135, 206, 250)));
        table.add(makeTableCell("<b>Board</b>", new Color(135, 206, 250)));
        table.add(makeTableCell("<b>Mines</b>", new Color(135, 206, 250)));

        table.add(makeTableCell("Easy", new Color(0, 255, 127)));
        table.add(makeTableCell("9 × 9", Color.WHITE));
        table.add(makeTableCell("10", Color.WHITE));

        table.add(makeTableCell("Medium", new Color(255, 165, 0)));
        table.add(makeTableCell("13 × 13", Color.WHITE));
        table.add(makeTableCell("26", Color.WHITE));

        table.add(makeTableCell("Hard", new Color(255, 99, 71)));
        table.add(makeTableCell("16 × 16", Color.WHITE));
        table.add(makeTableCell("44", Color.WHITE));

        return table;
    }

    private JLabel makeTableCell(String html, Color color) {
        JLabel l = new JLabel("<html>" + html + "</html>", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(color);
        l.setBorder(new EmptyBorder(6, 6, 6, 6));
        return l;
    }

    private JPanel createHintBox(String text) {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 191, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0, 191, 255, 60));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(10, 14, 10, 14));

        JTextArea area = new JTextArea(text);
        area.setOpaque(false);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(new Color(200, 220, 235));

        box.add(area, BorderLayout.CENTER);
        return box;
    }

    private JPanel createBullet(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(2, 18, 2, 0));

        JLabel dot = new JLabel("•");
        dot.setForeground(new Color(200, 200, 220));
        dot.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(210, 210, 225));

        p.add(dot);
        p.add(label);
        return p;
    }

    private JPanel createFooterNote() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(14, 10, 0, 10));

        JLabel l = new JLabel("<html><font color='#B0B0C8'>Good luck ⚡ Practice smart: fewer wrong flags = higher score.</font></html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        p.add(l, BorderLayout.WEST);
        return p;
    }
}

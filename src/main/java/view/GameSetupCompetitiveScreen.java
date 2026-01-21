package view;

import control.CompetitiveGameController;
import model.SysData;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GameSetupCompetitiveScreen extends JPanel {

    private final JFrame frame;

    // Inputs
    private JTextField player1NameField;
    private JTextField player2NameField;
    private JButton player1AvatarBtn;
    private JButton player2AvatarBtn;

    private JPanel easyPanel;
    private JPanel mediumPanel;
    private JPanel hardPanel;

    private String selectedDifficulty = "Easy";
    private String player1Avatar = "🦁";
    private String player2Avatar = "🐺";

    // Responsive sizing
    private int cardWidth = 280;
    private int cardHeight = 200;
    private int verticalGap = 12;

    public GameSetupCompetitiveScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setOpaque(false);

        calculateResponsiveCardSize();

        AnimatedBackgroundPanel bgPanel = new AnimatedBackgroundPanel();
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        bgPanel.add(createHeaderPanel(), gbc);

        // Players section
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        bgPanel.add(createPlayerInfoPanel(), gbc);

        // Difficulty section
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        bgPanel.add(createDifficultyPanel(), gbc);

        // Start button
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 20, 5, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        bgPanel.add(createStartButton(), gbc);

        // Back button
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 15, 20);
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.NORTH;
        bgPanel.add(createBackButton(), gbc);

        JScrollPane scrollPane = new JScrollPane(bgPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateResponsiveCardSize();
                revalidate();
                repaint();
            }
        });
    }

    private void calculateResponsiveCardSize() {
        int w = getWidth() > 0 ? getWidth() : 1400;
        int h = getHeight() > 0 ? getHeight() : 800;

        int availW = w - 150;
        int calcW = (availW / 3) - 30;
        cardWidth = Math.max(240, Math.min(320, calcW));

        if (h < 700) {
            cardHeight = 170;
            verticalGap = 6;
        } else {
            cardHeight = Math.max(185, Math.min(220, (int) (cardWidth * 0.7)));
            verticalGap = 12;
        }
    }

    // =========================
    // BACKGROUND (competitive colors + faint VS icons)
    // =========================
    class AnimatedBackgroundPanel extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Timer timer;

        // VS animation
        private float vsPhase = 0f;

        public AnimatedBackgroundPanel() {
            setOpaque(true);
            for (int i = 0; i < 45; i++) particles.add(new Particle());
            timer = new Timer(35, e -> {
                vsPhase += 0.06f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bg = new GradientPaint(
                    0, 0, new Color(10, 16, 28),
                    getWidth(), getHeight(), new Color(26, 18, 38)
            );
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            paintVsPattern(g2, vsPhase);

            for (Particle p : particles) {
                p.update(getWidth(), getHeight());
                p.draw(g2);
            }

            g2.setColor(new Color(255, 255, 255, 4));
            for (int i = 0; i < getWidth(); i += 80) g2.drawLine(i, 0, i, getHeight());
            for (int i = 0; i < getHeight(); i += 80) g2.drawLine(0, i, getWidth(), i);

            g2.setColor(new Color(255, 80, 80, 10));
            g2.drawOval(-200, getHeight() / 2 - 250, 500, 500);
            g2.setColor(new Color(80, 200, 255, 10));
            g2.drawOval(getWidth() - 350, getHeight() / 2 - 300, 600, 600);
        }

        private void paintVsPattern(Graphics2D g2, float phase) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(phase));
            int alphaBase = (int) (10 + 10 * pulse);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));

            int stepX = 220;
            int stepY = 160;

            for (int y = 60; y < getHeight(); y += stepY) {
                for (int x = 60; x < getWidth(); x += stepX) {
                    double rot = ((x / stepX + y / stepY) % 2 == 0) ? -0.08 : 0.08;

                    Graphics2D gg = (Graphics2D) g2.create();
                    gg.translate(x, y);
                    gg.rotate(rot);

                    gg.setColor(new Color(239, 68, 68, alphaBase));
                    gg.drawString("VS", 0, 0);

                    gg.setColor(new Color(56, 189, 248, alphaBase));
                    gg.drawString("VS", 2, 2);

                    gg.dispose();
                }
            }
        }
    }

    class Particle {
        float x, y, size, vx, vy, alpha;
        Color color;

        public Particle() { reset(); }

        void reset() {
            x = (float) (Math.random() * 2000);
            y = (float) (Math.random() * 1000);
            size = (float) (Math.random() * 2.8 + 1.1);
            vx = (float) (Math.random() * 0.35 - 0.175);
            vy = (float) (Math.random() * 0.55 + 0.08);
            alpha = (float) (Math.random() * 0.30 + 0.08);

            Color[] colors = {
                    new Color(239, 68, 68),
                    new Color(56, 189, 248),
                    new Color(168, 85, 247),
                    new Color(34, 211, 238)
            };
            color = colors[(int) (Math.random() * colors.length)];
        }

        void update(int w, int h) {
            x += vx; y += vy;
            if (y > h + 10) { y = -10; x = (float) (Math.random() * w); }
        }

        void draw(Graphics2D g) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g.fill(new Ellipse2D.Float(x, y, size, size));
        }
    }

    // =========================
    // HEADER
    // =========================
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Competitive Setup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("1v1 battle — separate score & lives — highest score wins");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(156, 163, 175));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        return panel;
    }

    // =========================
    // PLAYERS PANEL
    // =========================
    private JPanel createPlayerInfoPanel() {
        GlassPanel panel = new GlassPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("⚔ Fighters");
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setForeground(new Color(239, 68, 68));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.48;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(createPlayerPanel(1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.04;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(createVsBadgeAnimated(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.48;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(createPlayerPanel(2), gbc);

        return panel;
    }

    private JComponent createVsBadgeAnimated() {
        return new AnimatedVsBadge();
    }

    class AnimatedVsBadge extends JComponent {
        private float t = 0f;
        private final Timer timer;

        AnimatedVsBadge() {
            setOpaque(false);
            setPreferredSize(new Dimension(66, 44));
            setMinimumSize(new Dimension(66, 44));
            setMaximumSize(new Dimension(66, 44));

            timer = new Timer(35, e -> {
                t += 0.08f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            float pulse = (float) (0.5 + 0.5 * Math.sin(t));
            int glowA = (int) (80 + 70 * pulse);
            int outerA = (int) (50 + 40 * pulse);

            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(239, 68, 68, 230),
                    w, 0, new Color(56, 189, 248, 230)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            g2.setColor(new Color(255, 255, 255, outerA));
            g2.fill(new RoundRectangle2D.Float(3, 3, w - 6, (h - 6) / 2f, 16, 16));

            g2.setColor(new Color(255, 255, 255, glowA));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, 18, 18);

            int bounce = (int) (Math.sin(t * 1.3f) * 2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String s = "VS";
            int tx = (w - fm.stringWidth(s)) / 2;
            int ty = (h + fm.getAscent()) / 2 - 3 + bounce;

            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawString(s, tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(s, tx, ty);

            g2.dispose();
        }
    }

    class GlassPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(255, 255, 255, 28));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }
        @Override
        public Insets getInsets() { return new Insets(15, 20, 15, 20); }
    }

    private JPanel createPlayerPanel(int num) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        Color accent = (num == 1) ? new Color(239, 68, 68) : new Color(56, 189, 248);

        AnimatedAvatarButton avatarBtn = new AnimatedAvatarButton(
                (num == 1) ? player1Avatar : player2Avatar, accent
        );
        avatarBtn.addActionListener(e -> showAvatarSelector(num));
        if (num == 1) player1AvatarBtn = avatarBtn;
        else player2AvatarBtn = avatarBtn;

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(avatarBtn, gbc);

        ModernTextField inputPanel = new ModernTextField(accent);
        JTextField nameField = new PlaceholderTextField(num == 1 ? "Player 1 name" : "Player 2 name");

        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setForeground(Color.WHITE);
        nameField.setBackground(new Color(0, 0, 0, 0));
        nameField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        nameField.setCaretColor(Color.WHITE);

        nameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { inputPanel.setFocused(true); }
            public void focusLost(FocusEvent e) { inputPanel.setFocused(false); }
        });

        if (num == 1) player1NameField = nameField;
        else player2NameField = nameField;

        inputPanel.add(nameField, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(inputPanel, gbc);

        return panel;
    }

    class ModernTextField extends JPanel {
        private final Color accentColor;
        private boolean focused = false;

        public ModernTextField(Color accent) {
            this.accentColor = accent;
            setLayout(new BorderLayout());
            setOpaque(false);
        }

        public void setFocused(boolean f) { focused = f; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 41, 59, 180));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(focused ? accentColor : new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(focused ? 2.2f : 1.4f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        }

        @Override
        public Insets getInsets() { return new Insets(8, 10, 8, 10); }
    }

    class AnimatedAvatarButton extends JButton {
        private final Color bg;

        public AnimatedAvatarButton(String emoji, Color color) {
            super(emoji);
            this.bg = color;
            setPreferredSize(new Dimension(60, 60));
            setFont(new Font("Dialog", Font.PLAIN, 28));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 60));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(bg);
            g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 14, 14);

            super.paintComponent(g);
        }
    }

    // =========================
    // DIFFICULTY CARDS
    // =========================
    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Select Level");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0; gbc.gridwidth = 3; gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;

        gbc.gridx = 0;
        easyPanel = createDifficultyCard("Easy", new Color(34, 197, 94), 9, 10, "★");
        panel.add(easyPanel, gbc);

        gbc.gridx = 1;
        mediumPanel = createDifficultyCard("Medium", new Color(56, 189, 248), 13, 8, "★★");
        panel.add(mediumPanel, gbc);

        gbc.gridx = 2;
        hardPanel = createDifficultyCard("Hard", new Color(239, 68, 68), 16, 6, "★★★");
        panel.add(hardPanel, gbc);

        updateDifficultySelection();
        return panel;
    }

    private JPanel createDifficultyCard(String name, Color color, int grid, int lives, String stars) {
        DifficultyCard card = new DifficultyCard(color);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(cardWidth, cardHeight));

        JPanel circleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        circleContainer.setOpaque(false);

        JPanel circleIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 40;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                g2.fillOval(5, 5, s + 10, s + 10);
                g2.setColor(color);
                g2.fillOval(10, 10, s, s);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(60, 60); }
        };
        circleIcon.setOpaque(false);
        circleContainer.add(circleIcon);
        card.add(circleContainer);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(nameLabel);

        JLabel starsLabel = new JLabel(stars);
        starsLabel.setForeground(color);
        starsLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(starsLabel);

        card.add(Box.createVerticalStrut(verticalGap));

        card.add(createStatRow("📏 Grid", grid + "x" + grid));
        card.add(createStatRow("❤️ Lives", String.valueOf(lives)));

        JLabel selected = new JLabel("● SELECTED");
        selected.setFont(new Font("Segoe UI", Font.BOLD, 11));
        selected.setForeground(color);
        selected.setAlignmentX(CENTER_ALIGNMENT);
        card.add(Box.createVerticalGlue());
        card.add(selected);

        card.putClientProperty("selectedLabel", selected);
        card.putClientProperty("difficultyName", name);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedDifficulty = name;
                updateDifficultySelection();
            }
        });

        return card;
    }

    private JPanel createStatRow(String label, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(220, 20));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Dialog", Font.PLAIN, 12));
        l.setForeground(Color.LIGHT_GRAY);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Dialog", Font.BOLD, 12));
        v.setForeground(Color.WHITE);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    class DifficultyCard extends JPanel {
        private final Color cardColor;
        private boolean selected = false;

        public DifficultyCard(Color color) {
            this.cardColor = color;
            setOpaque(false);
        }

        public void setCardSelected(boolean s) { selected = s; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(selected ? cardColor : new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(selected ? 3f : 1.4f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
        }

        @Override public Insets getInsets() { return new Insets(10, 15, 10, 15); }
    }

    private void updateDifficultySelection() {
        updateCard(easyPanel, "Easy");
        updateCard(mediumPanel, "Medium");
        updateCard(hardPanel, "Hard");
    }

    private void updateCard(JPanel panel, String name) {
        if (panel == null) return;
        JLabel label = (JLabel) panel.getClientProperty("selectedLabel");
        boolean isSelected = selectedDifficulty.equals(name);
        if (label != null) label.setVisible(isSelected);
        if (panel instanceof DifficultyCard) ((DifficultyCard) panel).setCardSelected(isSelected);
    }

    // =========================
    // BUTTONS
    // =========================
    private JButton createStartButton() {
        GradientButton btn = new GradientButton("Start Battle", new Color(239, 68, 68), new Color(56, 189, 248));
        btn.setPreferredSize(new Dimension(220, 46));
        btn.addActionListener(e -> startGame(btn));
        return btn;
    }

    private void startGame(JButton btn) {
        String p1Name = player1NameField.getText().trim();
        String p2Name = player2NameField.getText().trim();

        if (p1Name.isEmpty() || p2Name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both player names.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btn.setEnabled(false);

        int gridSize = switch (selectedDifficulty) {
            case "Easy" -> 9;
            case "Medium" -> 13;
            case "Hard" -> 16;
            default -> 13;
        };

        // ✅ IMPORTANT: match YOUR controller constructor
        new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() {
                SysData sys = SysData.getInstance(); // assuming you have singleton
                User p1 = new User(p1Name, "");      // adjust if your User ctor differs
                User p2 = new User(p2Name, "");      // adjust if your User ctor differs

                CompetitiveGameController controller =
                        new CompetitiveGameController(sys, p1, p2, selectedDifficulty, gridSize);

                return new GameScreenCompetitive(frame, controller);
            }

            @Override
            protected void done() {
                try {
                    frame.setContentPane(get());
                    frame.revalidate();
                    frame.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Failed to start competitive game.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btn.setEnabled(true);
                }
            }
        }.execute();
    }

    private JButton createBackButton() {
        JButton btn = new JButton("Back to Menu");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.GRAY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });
        return btn;
    }

    class GradientButton extends JButton {
        private final Color c1, c2;

        public GradientButton(String text, Color c1, Color c2) {
            super(text);
            this.c1 = c1;
            this.c2 = c2;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
        }
    }

    // =========================
    // Avatar selector hook
    // =========================
    private void showAvatarSelector(int playerNum) {
        new AvatarSelectionDialog(frame, "Select Avatar", (idx) -> {
            String emoji = AvatarSelectionDialog.getEmojiByIndex(idx);
            if (playerNum == 1) {
                player1Avatar = emoji;
                if (player1AvatarBtn != null) player1AvatarBtn.setText(emoji);
            } else {
                player2Avatar = emoji;
                if (player2AvatarBtn != null) player2AvatarBtn.setText(emoji);
            }
        }).setVisible(true);
    }

    // =========================
    // Placeholder field
    // =========================
    static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(200, 210, 225, 120));
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = in.left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }
}

package view;

import control.GameSetupController;
import control.MultiPlayerGameController;
import model.SysData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class GameSetupScreen extends JPanel {

    private JFrame frame;
    private JTextField player1NameField;
    private JTextField player2NameField;
    private JButton player1AvatarBtn;
    private JButton player2AvatarBtn;
    private JPanel easyPanel;
    private JPanel mediumPanel;
    private JPanel hardPanel;
    private String selectedDifficulty = "Easy";
    private String player1Avatar = "👻";
    private String player2Avatar = "🐉";

    private int cardWidth = 280;
    private int cardHeight = 200;
    private int verticalGap = 15;

    public GameSetupScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        
        calculateResponsiveCardSize();

        AnimatedBackgroundPanel bgPanel = new AnimatedBackgroundPanel();
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 20, 10, 20); // Reduced top inset
        gbc.anchor = GridBagConstraints.CENTER;
        bgPanel.add(createHeaderPanel(), gbc);

        // Players Section
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        bgPanel.add(createPlayerInfoPanel(), gbc);

        // Difficulty Section
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        bgPanel.add(createDifficultyPanel(), gbc);

        // Start Button
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 20, 5, 20);
        bgPanel.add(createStartButton(), gbc);

        // Back Button
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 15, 20);
        gbc.weighty = 0.1; // Reduced weight
        gbc.anchor = GridBagConstraints.NORTH;
        bgPanel.add(createBackButton(), gbc);

        // ScrollPane as fallback only
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
        
        // Horizontal scaling
        int availW = w - 150;
        int calcW = (availW / 3) - 30;
        cardWidth = Math.max(240, Math.min(300, calcW));
        
        // Vertical scaling
        if (h < 700) {
            cardHeight = 170;
            verticalGap = 5;
        } else {
            cardHeight = Math.max(180, Math.min(210, (int)(cardWidth * 0.7)));
            verticalGap = 12;
        }
    }

    // ===== ANIMATED BACKGROUND =====
    class AnimatedBackgroundPanel extends JPanel {
        private List<Particle> particles = new ArrayList<>();
        private Timer timer;

        public AnimatedBackgroundPanel() {
            setOpaque(true);
            for (int i = 0; i < 40; i++) {
                particles.add(new Particle());
            }
            timer = new Timer(35, e -> repaint());
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bg = new GradientPaint(
                0, 0, new Color(15, 23, 42),
                getWidth(), getHeight(), new Color(30, 41, 59)
            );
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (Particle p : particles) {
                p.update(getWidth(), getHeight());
                p.draw(g2);
            }

            g2.setColor(new Color(255, 255, 255, 4));
            for (int i = 0; i < getWidth(); i += 80) g2.drawLine(i, 0, i, getHeight());
            for (int i = 0; i < getHeight(); i += 80) g2.drawLine(0, i, getWidth(), i);
        }
    }

    class Particle {
        float x, y, size, vx, vy, alpha;
        Color color;

        public Particle() { reset(); }

        void reset() {
            x = (float) (Math.random() * 2000);
            y = (float) (Math.random() * 1000);
            size = (float) (Math.random() * 2.5 + 1);
            vx = (float) (Math.random() * 0.3 - 0.15);
            vy = (float) (Math.random() * 0.4 + 0.1);
            alpha = (float) (Math.random() * 0.3 + 0.1);
            Color[] colors = { new Color(139, 92, 246), new Color(59, 130, 246), new Color(16, 185, 129) };
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

    // ===== HEADER =====
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Game Setup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Configure your MineSweeper battle");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(156, 163, 175));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        return panel;
    }

    // ===== PLAYERS PANEL =====
    private JPanel createPlayerInfoPanel() {
        GlassPanel panel = new GlassPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("👤 Players Information");
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setForeground(new Color(139, 92, 246));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(createPlayerPanel(1), gbc);

        gbc.gridx = 1; gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(createPlayerPanel(2), gbc);

        return panel;
    }

    class GlassPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }
        @Override
        public Insets getInsets() { return new Insets(15, 20, 15, 20); }
    }

    private JPanel createPlayerPanel(int num) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        Color btnColor = num == 1 ? new Color(139, 92, 246) : new Color(16, 185, 129);
        AnimatedAvatarButton avatarBtn = new AnimatedAvatarButton(num == 1 ? player1Avatar : player2Avatar, btnColor);
        avatarBtn.addActionListener(e -> showAvatarSelector(num));
        if (num == 1) player1AvatarBtn = avatarBtn; else player2AvatarBtn = avatarBtn;

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(avatarBtn, gbc);

        ModernTextField inputPanel = new ModernTextField(btnColor);
        JTextField nameField = new PlaceholderTextField(num == 1 ? "Player 1" : "Player 2");

        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setForeground(Color.WHITE);
        nameField.setBackground(new Color(0, 0, 0, 0));
        nameField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        nameField.setCaretColor(Color.WHITE);

        nameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { inputPanel.setFocused(true); }
            public void focusLost(FocusEvent e) { inputPanel.setFocused(false); }
        });

        if (num == 1) player1NameField = nameField; else player2NameField = nameField;
        inputPanel.add(nameField, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(inputPanel, gbc);

        return panel;
    }

    class ModernTextField extends JPanel {
        private Color accentColor;
        private boolean focused = false;
        public ModernTextField(Color accent) { this.accentColor = accent; setLayout(new BorderLayout()); setOpaque(false); }
        public void setFocused(boolean f) { focused = f; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 41, 59, 180));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(focused ? accentColor : new Color(71, 85, 105));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        }
        @Override public Insets getInsets() { return new Insets(8, 10, 8, 10); }
    }

    class AnimatedAvatarButton extends JButton {
        private float scale = 1.0f;
        private Color bg;
        public AnimatedAvatarButton(String emoji, Color color) {
            super(emoji); this.bg = color;
            setPreferredSize(new Dimension(60, 60));
            setFont(new Font("Dialog", Font.PLAIN, 28));
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }

    // ===== DIFFICULTY CARDS =====
    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Difficulty Level");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0; gbc.gridwidth = 3; gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0; easyPanel = createDifficultyCard("Easy", new Color(34, 197, 94), 9, 10, "★");
        panel.add(easyPanel, gbc);
        gbc.gridx = 1; mediumPanel = createDifficultyCard("Medium", new Color(251, 191, 36), 13, 8, "★★");
        panel.add(mediumPanel, gbc);
        gbc.gridx = 2; hardPanel = createDifficultyCard("Hard", new Color(239, 68, 68), 16, 6, "★★★");
        panel.add(hardPanel, gbc);

        updateDifficultySelection();
        return panel;
    }

    private JPanel createDifficultyCard(String name, Color color, int grid, int lives, String stars) {
        DifficultyCard card = new DifficultyCard(color);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(cardWidth, cardHeight));
        
        // Icon Panel with proper padding to prevent cutting
        JPanel circleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        circleContainer.setOpaque(false);
        JPanel circleIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 40;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                g2.fillOval(5, 5, s+10, s+10); // Glow
                g2.setColor(color);
                g2.fillOval(10, 10, s, s); // Circle
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

        // Stats rows - using common symbols for better compatibility
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
            public void mouseClicked(MouseEvent e) { selectedDifficulty = name; updateDifficultySelection(); }
        });

        return card;
    }

    private JPanel createStatRow(String label, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(200, 20));
        JLabel l = new JLabel(label); l.setFont(new Font("Dialog", Font.PLAIN, 12)); l.setForeground(Color.LIGHT_GRAY);
        JLabel v = new JLabel(val); v.setFont(new Font("Dialog", Font.BOLD, 12)); v.setForeground(Color.WHITE);
        row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        return row;
    }

    class DifficultyCard extends JPanel {
        private Color cardColor;
        private boolean selected = false;
        public DifficultyCard(Color color) { this.cardColor = color; setOpaque(false); }
        public void setCardSelected(boolean s) { selected = s; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(selected ? cardColor : new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(selected ? 3f : 1f));
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

    // ===== BUTTONS =====
    private JButton createStartButton() {
        GradientButton btn = new GradientButton("Start Game", new Color(139, 92, 246), new Color(59, 130, 246));
        btn.setPreferredSize(new Dimension(200, 45));
        btn.addActionListener(e -> startGame(btn));
        return btn;
    }

    private void startGame(JButton btn) {
        String p1 = player1NameField.getText();
        String p2 = player2NameField.getText();
        if (p1.isEmpty() || p2.isEmpty()) return;

        btn.setEnabled(false);
        new SwingWorker<GameScreenMultiPlayer, Void>() {
            @Override protected GameScreenMultiPlayer doInBackground() {
                GameSetupController setup = new GameSetupController(SysData.getInstance());
                setup.setDifficulty(selectedDifficulty);
                setup.createPlayers(p1, player1Avatar, p2, player2Avatar);
                GameSetupController.GameConfig config = setup.initializeGame();
                return new GameScreenMultiPlayer(frame, new MultiPlayerGameController(
                    config.sysData, config.player1, config.player2, config.difficulty, config.gridSize));
            }
            @Override protected void done() {
                try {
                    frame.setContentPane(get());
                    frame.revalidate();
                } catch (Exception ex) { ex.printStackTrace(); }
                btn.setEnabled(true);
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
        });
        return btn;
    }

    class GradientButton extends JButton {
        private Color c1, c2;
        public GradientButton(String text, Color c1, Color c2) {
            super(text); this.c1 = c1; this.c2 = c2;
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 16));
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
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

    private void showAvatarSelector(int playerNum) {
        new AvatarSelectionDialog(frame, "Select Avatar", (idx) -> {
            String emoji = AvatarSelectionDialog.getEmojiByIndex(idx);
            if (playerNum == 1) { player1Avatar = emoji; player1AvatarBtn.setText(emoji); }
            else { player2Avatar = emoji; player2AvatarBtn.setText(emoji); }
        }).setVisible(true);
    }
    
 // Placeholder text field (shows hint until user types)
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
                g2.setColor(new Color(200, 210, 225, 120)); // placeholder color
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
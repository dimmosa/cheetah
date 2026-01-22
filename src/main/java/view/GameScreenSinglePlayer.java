package view;

import control.SinglePlayerGameControl;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreenSinglePlayer extends JPanel {

    private final JFrame frame;

    private JButton flagButton;
    private JLabel livesValueLabel;
    private JLabel minesValueLabel;
    private JLabel scoreValueLabel;
    private JLabel timerLabel;

    private final SinglePlayerGameControl controller;
    private final MinesweeperBoardPanel board;

    // background animation
    private Timer bgTimer;
    private final List<BgParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private float wave = 0f;

    public GameScreenSinglePlayer(JFrame frame, SinglePlayerGameControl controller, MinesweeperBoardPanel board) {
        this.frame = frame;
        this.controller = controller;
        this.board = board;

        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(18, 18, 18, 18));
        setOpaque(false);

        initBackgroundAnimation();

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ✅ sidebar (smaller)
        gbc.gridx = 0;
        gbc.weightx = 0.24;
        centerPanel.add(createSidebarPanel(), gbc);

        // ✅ board (bigger)
        gbc.gridx = 1;
        gbc.weightx = 0.76;
        centerPanel.add(createBoardContainer(board), gbc);

        add(centerPanel, BorderLayout.CENTER);

        updateStatsDisplay();

        controller.startTimer(timeStr -> SwingUtilities.invokeLater(() -> {
            timerLabel.setText(timeStr); // keep plain text (cleaner)
        }));
    }

    // =========================================================
    // Animated background
    // =========================================================
    private void initBackgroundAnimation() {
        particles.clear();

        // more particles on hard looks cooler
        int count = controller.getDifficulty().equalsIgnoreCase("Hard") ? 55 : 40;
        for (int i = 0; i < count; i++) particles.add(new BgParticle(random));

        bgTimer = new Timer(30, e -> {
            wave += 0.04f;
            for (BgParticle p : particles) p.update(getWidth(), getHeight());
            repaint();
        });
        bgTimer.start();
    }

    private void stopBackgroundAnimation() {
        if (bgTimer != null) bgTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Full screen animated background
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // gradient base
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(8, 10, 18),
                getWidth(), getHeight(), new Color(20, 14, 28)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // soft pulse glow areas
        float pulse = (float) (0.5 + 0.5 * Math.sin(wave));
        int a1 = (int) (22 + 18 * pulse);
        int a2 = (int) (16 + 12 * pulse);

        g2.setColor(new Color(56, 189, 248, a1));
        g2.fillOval(-180, getHeight() / 2 - 260, 520, 520);

        g2.setColor(new Color(168, 85, 247, a2));
        g2.fillOval(getWidth() - 360, getHeight() / 2 - 280, 620, 620);

        // particles
        for (BgParticle p : particles) p.draw(g2);

        // subtle grid
        g2.setColor(new Color(255, 255, 255, 5));
        for (int x = 0; x < getWidth(); x += 90) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 90) g2.drawLine(0, y, getWidth(), y);

        g2.dispose();
        super.paintComponent(g);
    }

    static class BgParticle {
        float x, y, vx, vy, r, alpha;
        Color color;

        BgParticle(Random rand) {
            x = rand.nextFloat() * 1600;
            y = rand.nextFloat() * 900;
            vx = (rand.nextFloat() - 0.5f) * 0.5f;
            vy = (rand.nextFloat() * 0.6f) + 0.1f;
            r = rand.nextFloat() * 3.5f + 1.2f;
            alpha = rand.nextFloat() * 0.28f + 0.08f;

            Color[] colors = {
                    new Color(56, 189, 248),
                    new Color(168, 85, 247),
                    new Color(34, 211, 238),
                    new Color(135, 206, 250)
            };
            color = colors[rand.nextInt(colors.length)];
        }

        void update(int w, int h) {
            x += vx;
            y += vy;
            if (y > h + 15) { y = -15; x = (float) (Math.random() * Math.max(1, w)); }
            if (x < -20) x = w + 20;
            if (x > w + 20) x = -20;
        }

        void draw(Graphics2D g2) {
            int a = (int) (alpha * 255);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
            g2.fill(new Ellipse2D.Float(x, y, r, r));
        }
    }

    // =========================================================
    // Header
    // =========================================================
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        String difficulty = controller.getDifficulty().toUpperCase();

        JLabel titleLabel = new JLabel("SINGLE PLAYER • " + difficulty);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(new Color(135, 206, 250));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        timerLabel.setForeground(new Color(255, 165, 0));
        headerPanel.add(timerLabel, BorderLayout.EAST);

        return headerPanel;
    }

    // =========================================================
    // Sidebar
    // =========================================================
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        User user = controller.getCurrentUser();
        String userName = user.getUsername();

        sidebar.add(createInfoCard(userName));
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(createStatsCard());
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(createControlsCard());
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel createInfoCard(String name) {
        JPanel card = createBaseCard();
        card.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        nameLabel.setForeground(new Color(0, 200, 200));

        card.add(nameLabel);
        return card;
    }

    private JPanel createStatsCard() {
        JPanel card = createBaseCard();
        card.setLayout(new GridLayout(3, 1, 0, 10));

        JPanel scorePanel = createStatLine("Score", "0", new Color(255, 215, 0));
        scoreValueLabel = (JLabel) scorePanel.getComponent(1);
        card.add(scorePanel);

        int initialLives = controller.getMaxLives(); // ✅ עדיף מאשר getLivesForDifficulty
        JPanel livesPanel = createStatLine("Lives",
                controller.getLives() + " / " + initialLives,
                new Color(255, 100, 100));
        livesValueLabel = (JLabel) livesPanel.getComponent(1);
        card.add(livesPanel);

        JPanel minesPanel = createStatLine("Mines Left",
                String.valueOf(board.getMinesLeftCalculated()),  // ✅ מה-board
                new Color(135, 206, 250));
        minesValueLabel = (JLabel) minesPanel.getComponent(1);
        card.add(minesPanel);

        return card;
    }

    private JPanel createControlsCard() {
        JPanel card = createBaseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Controls");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(135, 206, 250));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        flagButton = createNeonButton("FLAG MODE: OFF", new Color(255, 165, 0), 220, 42);
        flagButton.addActionListener(e -> toggleFlagMode());
        card.add(flagButton);
        card.add(Box.createVerticalStrut(10));

        JButton exitButton = createNeonButton("GIVE UP", new Color(200, 50, 50), 220, 42);
        exitButton.addActionListener(e -> handleGiveUpModern());
        card.add(exitButton);

        return card;
    }

    // =========================================================
    // Modern dialogs (no JOptionPane)
    // =========================================================
    private void handleGiveUpModern() {
        boolean ok = ModernDialog.confirm(
                frame,
                "Give up?",
                "Are you sure you want to give up?\nYour score will be saved.",
                ModernDialog.Theme.WARNING
        );

        if (!ok) return;

        controller.endGame(false);

        // Use your existing dialog (looks nice already)
        GameEndedDialog gameEndedDialog = new GameEndedDialog(
                frame,
                controller.getCurrentUser().getUsername(),
                controller.getPoints(),
                timerLabel.getText(),
                controller.getDifficulty(),
                controller.getLives(),
                controller.getLivesForDifficulty(controller.getDifficulty()),
                "single",
                GameEndedDialog.EndReason.GIVE_UP,
                this::restartSingleGame,
                this::goToMainMenuSingle
        );

        gameEndedDialog.setVisible(true);
    }

    private void toggleFlagMode() {
        controller.toggleFlagMode();
        boolean isFlagMode = controller.isFlagMode();

        if (isFlagMode) {
            flagButton.setText("FLAG MODE: ON");
            flagButton.putClientProperty("baseColor", new Color(0, 200, 0));
        } else {
            flagButton.setText("FLAG MODE: OFF");
            flagButton.putClientProperty("baseColor", new Color(255, 165, 0));
        }
        flagButton.repaint();

        if (board != null) board.setFlagMode(isFlagMode);
    }

    // =========================================================
    // Update stats + end dialog
    // =========================================================
    public void updateStatsDisplay() {
        if (scoreValueLabel != null) {
            scoreValueLabel.setText(String.valueOf(controller.getPoints()));
        }

        if (livesValueLabel != null) {
            livesValueLabel.setText(controller.getLives() + " / " + controller.getMaxLives());
        }

        if (minesValueLabel != null && board != null) {
            minesValueLabel.setText(String.valueOf(board.getMinesLeftCalculated()) + " / " + board.getTotalMines());
            // או אם את רוצה רק מספר אחד:
            // minesValueLabel.setText(String.valueOf(board.getMinesLeftCalculated()));
        }

        revalidate();
        repaint();
    }


    private void showGameOverDialog() {
        GameEndedDialog.EndReason reason = controller.isGameWon()
                ? GameEndedDialog.EndReason.WIN
                : GameEndedDialog.EndReason.LOST_NO_LIVES;

        GameEndedDialog gameEndedDialog = new GameEndedDialog(
                frame,
                controller.getCurrentUser().getUsername(),
                controller.getPoints(),
                timerLabel.getText(),
                controller.getDifficulty(),
                controller.getLives(),
                controller.getLivesForDifficulty(controller.getDifficulty()),
                "single",
                reason,
                this::restartSingleGame,
                this::goToMainMenuSingle
        );

        gameEndedDialog.setVisible(true);
    }

    // =========================================================
    // UI blocks
    // =========================================================
    private JPanel createStatLine(String title, String initialValue, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        titleLabel.setForeground(new Color(180, 180, 200));

        JLabel valLabel = new JLabel(initialValue);
        valLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valLabel.setForeground(color);

        p.add(titleLabel);
        p.add(valLabel);
        return p;
    }

    /**
     * ✅ Fix for "Hard board too small":
     * - We DON'T shrink cell size.
     * - We wrap the board in a scroll pane, and the board area gets most space.
     */
    private JPanel createBoardContainer(MinesweeperBoardPanel board) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 185));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(56, 189, 248, 160));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
            }
        };
        glass.setOpaque(false);
        glass.setBorder(new EmptyBorder(14, 14, 14, 14));

        JScrollPane scroller = new JScrollPane(board);
        scroller.setBorder(null);
        scroller.getViewport().setOpaque(false);
        scroller.setOpaque(false);

        // only show scrollbars if needed
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // nicer scroll speed
        scroller.getVerticalScrollBar().setUnitIncrement(18);
        scroller.getHorizontalScrollBar().setUnitIncrement(18);

        glass.add(scroller, BorderLayout.CENTER);
        container.add(glass, BorderLayout.CENTER);
        return container;
    }

    private JPanel createBaseCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 185));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(320, 520));
        return panel;
    }

    private JButton createNeonButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.putClientProperty("baseColor", color);

        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });

        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();
                Color baseColor = (Color) c.getClientProperty("baseColor");
                boolean pressed = ((AbstractButton) c).getModel().isPressed();

                // glow
                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 90));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 14, 14);

                // fill
                Color fillColor = pressed ? baseColor.darker() : baseColor;
                g2.setColor(fillColor);
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                // border
                g2.setColor(new Color(255, 255, 255, 70));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return button;
    }

    // =========================================================
    // Navigation
    // =========================================================
    private void restartSingleGame() {
        controller.stopTimer();
        stopBackgroundAnimation();

        // Restart back to the single setup screen (practice)
        frame.setContentPane(new PrivateGameScreen(frame));
        frame.revalidate();
        frame.repaint();
    }

    private void goToMainMenuSingle() {
        controller.stopTimer();
        stopBackgroundAnimation();

        frame.setContentPane(new MainMenuPrivateScreen(frame));
        frame.revalidate();
        frame.repaint();
    }
    public void forceShowEndDialog() {
        controller.stopTimer();
        showGameOverDialog(); 
    }

}

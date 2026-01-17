package view;

import control.SinglePlayerGameControl;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameScreenSinglePlayer extends JPanel {

    JFrame frame;

    private boolean isFlagMode = false;
    private JButton flagButton;
    private JLabel livesValueLabel;
    private JLabel minesValueLabel;
    private JLabel scoreValueLabel;
    private JLabel timerLabel;

    private final SinglePlayerGameControl controller;
    private final MinesweeperBoardPanel board;

    public GameScreenSinglePlayer(JFrame frame, SinglePlayerGameControl controller, MinesweeperBoardPanel board) {
        this.frame = frame;
        this.controller = controller;
        this.board = board;

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(15, 15, 25));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);

        gbc.gridx = 0;
        gbc.weightx = 0.15;
        gbc.fill = GridBagConstraints.VERTICAL;
        centerPanel.add(createSidebarPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.85;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        centerPanel.add(createBoardContainer(this.board), gbc);

        add(centerPanel, BorderLayout.CENTER);

        updateStatsDisplay();
        controller.startTimer(timeStr ->
                timerLabel.setText("<html><font color='#FFA500'>" + timeStr + "</font></html>")
        );
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        String difficulty = controller.getDifficulty().toUpperCase();
        JLabel titleLabel = new JLabel("<html><font color='#00BFFF'>GAME</font> <font color='#87CEFA'>" + difficulty + " MODE</font></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        timerLabel = new JLabel("<html><font color='#FFA500'>00:00</font></html>");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        headerPanel.add(timerLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        User user = controller.getCurrentUser();
        String userName = user.getUsername();
        sidebar.add(createInfoCard(userName, true));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createStatsCard());
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createControlsCard());
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel createInfoCard(String name, boolean isActive) {
        JPanel card = createBaseCard();
        card.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        nameLabel.setForeground(isActive ? new Color(0, 200, 200) : new Color(150, 150, 150));

        card.add(nameLabel);
        return card;
    }

    private JPanel createStatsCard() {
        JPanel card = createBaseCard();
        card.setLayout(new GridLayout(3, 1, 0, 10));

        JPanel scorePanel = createStatLine("Score", "0", new Color(255, 215, 0), null);
        scoreValueLabel = (JLabel) scorePanel.getComponent(1);
        card.add(scorePanel);

        int initialLives = controller.getLivesForDifficulty(controller.getDifficulty());
        JPanel livesPanel = createStatLine("Lives",
                controller.getLives() + " / " + initialLives,
                new Color(255, 100, 100), null);
        livesValueLabel = (JLabel) livesPanel.getComponent(1);
        card.add(livesPanel);

        JPanel minesPanel = createStatLine("Mines Left",
                controller.getRemainingMines() + " / " + controller.getTotalMines(),
                new Color(135, 206, 250), null);
        minesValueLabel = (JLabel) minesPanel.getComponent(1);
        card.add(minesPanel);

        return card;
    }

    private JPanel createControlsCard() {
        JPanel card = createBaseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Controls");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(135, 206, 250));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        flagButton = createToggleButton("FLAG MODE: OFF", new Color(255, 165, 0));
        flagButton.addActionListener(e -> toggleFlagMode());
        card.add(flagButton);
        card.add(Box.createVerticalStrut(10));

        JButton exitButton = createNeonButton("GIVE UP", new Color(200, 50, 50), 200, 40);
        exitButton.addActionListener(e -> handleGiveUp());
        card.add(exitButton);

        return card;
    }

    private void handleGiveUp() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to give up?\nYour score will be saved.",
                "Give Up?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            controller.endGame(false);

            JOptionPane.showMessageDialog(
                    this,
                    "Game Over!\nFinal Score: " + controller.getPoints() + " points",
                    "Game Ended",
                    JOptionPane.INFORMATION_MESSAGE
            );

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
            // ✅ DO NOT force navigation here (dialog buttons already do it)
        }
    }

    private void toggleFlagMode() {
        controller.toggleFlagMode();
        boolean isFlagMode = controller.isFlagMode();

        if (isFlagMode) {
            flagButton.setText("FLAG MODE: ON");
            flagButton.setBackground(new Color(0, 200, 0));
            flagButton.setForeground(Color.WHITE);
        } else {
            flagButton.setText("FLAG MODE: OFF");
            flagButton.setBackground(new Color(255, 165, 0));
            flagButton.setForeground(Color.WHITE);
        }

        if (board != null) {
            board.setFlagMode(isFlagMode);
        }
    }

    public void updateStatsDisplay() {
        int currentLives = controller.getLives();
        int initialLives = controller.getLivesForDifficulty(controller.getDifficulty());
        int currentScore = controller.getPoints();
        int minesRemaining = controller.getRemainingMines();
        int totalMines = controller.getTotalMines();

        if (scoreValueLabel != null) {
            scoreValueLabel.setText(String.valueOf(currentScore));
            scoreValueLabel.setForeground(
                    currentScore >= 0 ? new Color(255, 215, 0) : new Color(255, 100, 100)
            );
        }

        if (livesValueLabel != null) {
            livesValueLabel.setText(currentLives + " / " + initialLives);
        }

        if (minesValueLabel != null) {
            minesValueLabel.setText(minesRemaining + " / " + totalMines);
        }

        if (controller.getLives() <= 0) {
            controller.stopTimer();
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        GameEndedDialog.EndReason reason = controller.isGameWon()
                ? GameEndedDialog.EndReason.WIN
                : GameEndedDialog.EndReason.LOST_NO_LIVES;

        GameEndedDialog gameEndedDialog = new GameEndedDialog(
                frame,
                controller.getCurrentUser().getUsername(),
                controller.getPoints(),
                timerLabel.getText(), // can be HTML, dialog strips it
                controller.getDifficulty(),
                controller.getLives(),
                controller.getLivesForDifficulty(controller.getDifficulty()),
                "single",
                reason,
                this::restartSingleGame,
                this::goToMainMenuSingle
        );

        gameEndedDialog.setVisible(true);
        // ✅ DO NOT force navigation here either
    }

    private JPanel createStatLine(String title, String initialValue, Color color, JLabel valueLabel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        titleLabel.setForeground(new Color(180, 180, 200));

        JLabel valLabel = new JLabel(initialValue);
        valLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valLabel.setForeground(color);

        if (title.equals("Lives")) {
            this.livesValueLabel = valLabel;
        } else if (title.equals("Mines Left")) {
            this.minesValueLabel = valLabel;
        }

        p.add(titleLabel);
        p.add(valLabel);
        return p;
    }

    private JPanel createBoardContainer(MinesweeperBoardPanel board) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(180, 80, 255));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(board, BorderLayout.CENTER);

        // ✅ FIX: add the PANEL (not the board directly)
        container.add(panel);
        return container;
    }

    private JPanel createBaseCard() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(300, 500));
        return panel;
    }

    private JButton createNeonButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.putClientProperty("baseColor", color);

        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(((Color) button.getClientProperty("baseColor")).brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
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

                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 15, 15);

                Color fillColor = button.getModel().isPressed() ? baseColor.darker() : baseColor;
                g2.setColor(fillColor);
                g2.fillRoundRect(0, 0, w, h, 15, 15);

                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return button;
    }

    private JButton createToggleButton(String text, Color color) {
        return createNeonButton(text, color, 200, 40);
    }

    // ✅ You must already have these methods in your class.
    // If your project has different constructors, paste them and I’ll match them exactly.
    private void restartSingleGame() {
        controller.stopTimer();

        // Create a new controller/board EXACTLY how your project expects.
        // If you already wrote this method and it has red errors, paste it and I’ll fix it.
    }

    private void goToMainMenuSingle() {
        controller.stopTimer();
        frame.getContentPane().removeAll();
        frame.add(new MainMenuPrivateScreen(frame));
        frame.revalidate();
        frame.repaint();
    }
}
package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import control.GameObserver;
import control.GameState;
import control.CompetitiveGameController;
import control.QuestionController;

import static view.CustomIconButton.createNeonButton;

public class GameScreenCompetitive extends JPanel implements GameObserver {

    private JFrame frame;
    private CompetitiveGameController gameController;

    private JButton audioBtn;

    private MinesweeperBoardPanelCompetitive board1;
    private MinesweeperBoardPanelCompetitive board2;

    private JPanel player1PanelContainer;
    private JPanel player2PanelContainer;

    // mini stats
    private JLabel player1CorrectFlagsLabel;
    private JLabel player1WrongFlagsLabel;
    private JLabel player1RevealedMinesLabel;
    private JLabel player1QuestionsLabel;
    private JLabel player1SurprisesLabel;

    private JLabel player2CorrectFlagsLabel;
    private JLabel player2WrongFlagsLabel;
    private JLabel player2RevealedMinesLabel;
    private JLabel player2QuestionsLabel;
    private JLabel player2SurprisesLabel;

    // score/lives per player
    private JLabel p1ScoreLabel;
    private JLabel p2ScoreLabel;
    private JLabel p1LivesLabel;
    private JLabel p2LivesLabel;

    private static final String FLAG_ICON = "🚩";
    private static final String FLAG_OFF_ICON = "❌";

    private boolean isFlagMode = false;
    private boolean isGiveUp = false;

    private JButton flagButton;
    private JLabel activePlayerIndicator;
    private JLabel timerLabel;

    private boolean endSequenceStarted = false;

    private final Color player1Color = new Color(0, 150, 255);
    private final Color player2Color = new Color(255, 0, 150);

    // ----------------------------
    // ✅ OBSERVER
    // ----------------------------
    @Override
    public void onGameStateChanged(GameState state) {
        SwingUtilities.invokeLater(() -> {

            setActivePlayer(state.currentPlayer());
            highlightActivePlayerPanel();

            int p1Score = gameController.getScore(1);
            int p2Score = gameController.getScore(2);
            int p1Lives = gameController.getLives(1);
            int p2Lives = gameController.getLives(2);

            p1ScoreLabel.setText("<html><font color='#FFD700'>P1: " + p1Score + "</font></html>");
            p2ScoreLabel.setText("<html><font color='#FFD700'>P2: " + p2Score + "</font></html>");

            int maxLives = gameController.getMaxLives();
            p1LivesLabel.setText("P1 " + p1Lives + "/" + maxLives + generateHearts(p1Lives, maxLives));
            p2LivesLabel.setText("P2 " + p2Lives + "/" + maxLives + generateHearts(p2Lives, maxLives));

            updatePlayerMiniStats(board1, player1CorrectFlagsLabel, player1WrongFlagsLabel,
                    player1RevealedMinesLabel, player1QuestionsLabel, player1SurprisesLabel);

            updatePlayerMiniStats(board2, player2CorrectFlagsLabel, player2WrongFlagsLabel,
                    player2RevealedMinesLabel, player2QuestionsLabel, player2SurprisesLabel);

            if (state.gameOver()) {
                onGameEnded();
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setMinimumSize(new Dimension(1200, 800));
                frame.setLocationRelativeTo(null);
            }
        });
    }

    // ----------------------------
    // ✅ CTOR
    // ----------------------------
    public GameScreenCompetitive(JFrame frame, CompetitiveGameController gameController) {
        this.frame = frame;
        this.gameController = gameController;
        gameController.addObserver(this);

        if (!AudioManager.isMuted()) {
            MusicManager.playLoop("/music/game_theme.wav");
        }

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 25));

        JPanel mainContent = new JPanel(new BorderLayout(2, 2));
        mainContent.setBackground(new Color(15, 15, 25));
        mainContent.setBorder(new EmptyBorder(2, 5, 2, 5));

        mainContent.add(createTopSection(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        centerPanel.setOpaque(false);

        board1 = new MinesweeperBoardPanelCompetitive(
                gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, true
        );

        board2 = new MinesweeperBoardPanelCompetitive(
                gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, false
        );

        board1.setMinimumSize(new Dimension(200, 200));
        board2.setMinimumSize(new Dimension(200, 200));

        player1PanelContainer = createPlayerPanel(
                "PLAYER 1",
                gameController.getPlayer1().getUsername(),
                gameController.getPlayer1().getAvatar(),
                player1Color,
                board1,
                true
        );
        centerPanel.add(player1PanelContainer);

        player2PanelContainer = createPlayerPanel(
                "PLAYER 2",
                gameController.getPlayer2().getUsername(),
                gameController.getPlayer2().getAvatar(),
                player2Color,
                board2,
                false
        );
        centerPanel.add(player2PanelContainer);

        mainContent.add(centerPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();

        gameController.startTimer(timeStr ->
                timerLabel.setText("<html><font color='#FFA500'>" + timeStr + "</font></html>")
        );
    }

    private JPanel createTopSection() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(createHeaderPanel());
        top.add(Box.createVerticalStrut(3));
        top.add(createGameInfoPanel());

        return top;
    }

    // ----------------------------
    // ✅ TOP GAME INFO
    // ----------------------------
    private JPanel createGameInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        panel.setBackground(new Color(30, 30, 50));
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));

        JLabel turnLabel = new JLabel("TURN:");
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        turnLabel.setForeground(new Color(135, 206, 250));
        panel.add(turnLabel);

        activePlayerIndicator = new JLabel();
        activePlayerIndicator.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(activePlayerIndicator);

        JLabel separator1 = new JLabel("|");
        separator1.setFont(new Font("SansSerif", Font.BOLD, 12));
        separator1.setForeground(new Color(60, 60, 100));
        panel.add(separator1);

        p1ScoreLabel = new JLabel();
        p1ScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(p1ScoreLabel);

        p2ScoreLabel = new JLabel();
        p2ScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(p2ScoreLabel);

        JLabel separator2 = new JLabel("|");
        separator2.setFont(new Font("SansSerif", Font.BOLD, 12));
        separator2.setForeground(new Color(60, 60, 100));
        panel.add(separator2);

        int maxLives = gameController.getMaxLives();
        p1LivesLabel = new JLabel("P1 0/" + maxLives);
        p1LivesLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        p1LivesLabel.setForeground(new Color(255, 100, 100));
        panel.add(p1LivesLabel);

        p2LivesLabel = new JLabel("P2 0/" + maxLives);
        p2LivesLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        p2LivesLabel.setForeground(new Color(255, 100, 100));
        panel.add(p2LivesLabel);

        flagButton = createToggleButton(FLAG_OFF_ICON, new Color(180, 0, 0));
        flagButton.addActionListener(e -> toggleFlagMode());
        panel.add(flagButton);

        JButton hintButton = createNeonButton("💡", new Color(0, 180, 255), 45, 26);
        hintButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        hintButton.setToolTipText("Hot/Cold mine hint - costs points");
        hintButton.addActionListener(e -> activateHotColdHintCompetitive());
        panel.add(hintButton);

        audioBtn = createNeonButton("🔊", new Color(120, 255, 120), 45, 26);
        audioBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        audioBtn.setToolTipText("Toggle music + sound effects");
        audioBtn.addActionListener(e -> toggleAudio());
        panel.add(audioBtn);
        refreshAudioButton();

        JButton exitButton = createNeonButton("Exit", new Color(200, 50, 50), 70, 26);
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        exitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to give up?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                isGiveUp = true;
                gameController.giveUp();
                onGameEnded();
            }
        });
        panel.add(exitButton);

        syncTopHudNow();

        return panel;
    }

    private void syncTopHudNow() {
        int p1Score = gameController.getScore(1);
        int p2Score = gameController.getScore(2);
        int p1Lives = gameController.getLives(1);
        int p2Lives = gameController.getLives(2);
        int maxLives = gameController.getMaxLives();

        p1ScoreLabel.setText("<html><font color='#FFD700'>P1: " + p1Score + "</font></html>");
        p2ScoreLabel.setText("<html><font color='#FFD700'>P2: " + p2Score + "</font></html>");

        p1LivesLabel.setText("P1 " + p1Lives + "/" + maxLives + generateHearts(p1Lives, maxLives));
        p2LivesLabel.setText("P2 " + p2Lives + "/" + maxLives + generateHearts(p2Lives, maxLives));
    }

    private void toggleAudio() {
        boolean newMuted = !AudioManager.isMuted();
        AudioManager.setMuted(newMuted);
        MusicManager.setMuted(newMuted);

        refreshAudioButton();

        if (!newMuted) {
            MusicManager.playLoop("/music/game_theme.wav");
        }
    }

    private void refreshAudioButton() {
        boolean muted = AudioManager.isMuted();

        if (muted) {
            Color red = new Color(200, 60, 60);
            audioBtn.setText("🔇");
            audioBtn.setForeground(Color.WHITE);
            audioBtn.putClientProperty("neonColor", red);
        } else {
            Color green = new Color(120, 255, 120);
            audioBtn.setText("🔊");
            audioBtn.setForeground(Color.BLACK);
            audioBtn.putClientProperty("neonColor", green);
        }

        audioBtn.repaint();
    }

    private static final int HINT_COST = 3;
    private static final int HINT_RADIUS = 3;

    private void activateHotColdHintCompetitive() {
        int currentPlayer = gameController.getCurrentPlayer();

        if (gameController.getScore(currentPlayer) < HINT_COST) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not enough points for HINT!\nNeed " + HINT_COST + " points.",
                    "Hint",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        gameController.addScore(currentPlayer, -HINT_COST);

        MinesweeperBoardPanelCompetitive board =
                (currentPlayer == 1) ? board1 : board2;

        if (board != null) board.showHotColdHint(HINT_RADIUS);

        AudioManager.play(AudioManager.Sfx.QUESTION_OPEN);

        syncTopHudNow();
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(2, 0, 3, 0));

        String difficulty = gameController.getDifficulty();

        JLabel titleLabel = new JLabel(
                "<html><font color='#FF2D95'>COMPETITIVE</font> " +
                        "<font color='#87CEFA'>MINESWEEPER</font> " +
                        "<font color='#FFA500'>" + difficulty + "</font></html>"
        );
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        timerLabel = new JLabel("<html><font color='#FFA500'>00:00</font></html>");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel timerIcon = new JLabel("⏱️");
        timerIcon.setForeground(Color.WHITE);
        timerIcon.setFont(new Font("SansSerif", Font.BOLD, 15));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(timerIcon);
        rightPanel.add(timerLabel);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void setActivePlayer(int playerNum) {
        String name = (playerNum == 1)
                ? gameController.getPlayer1().getUsername()
                : gameController.getPlayer2().getUsername();

        Color color = (playerNum == 1) ? player1Color : player2Color;

        activePlayerIndicator.setText(name);
        activePlayerIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);
        activePlayerIndicator.setForeground(color);
    }

    // ----------------------------
    // ✅ PLAYER PANEL
    // ----------------------------
    private JPanel createPlayerPanel(String title, String playerName, String avatar, Color color,
                                     MinesweeperBoardPanelCompetitive board,
                                     boolean isPlayer1) {

        JPanel container = new JPanel(new BorderLayout(0, 2));
        container.setOpaque(false);
        container.setName(title);

        // ---------- INFO CARD ----------
        JPanel infoCard = createBaseCard(color);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setPreferredSize(new Dimension(300, 55));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        infoCard.setBorder(new EmptyBorder(2, 4, 2, 4));

        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridy = 0;
        hgbc.anchor = GridBagConstraints.CENTER;
        hgbc.insets = new Insets(0, 0, 0, 2);

        JLabel avatarLabel = new JLabel(avatar, SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        avatarLabel.setPreferredSize(new Dimension(18, 18));

        hgbc.gridx = 0;
        headerPanel.add(avatarLabel, hgbc);

        JLabel nameLabel = new JLabel(title + " - " + playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
        nameLabel.setForeground(color);

        hgbc.gridx = 1;
        hgbc.insets = new Insets(0, 0, 0, 0);
        headerPanel.add(nameLabel, hgbc);

        headerPanel.setPreferredSize(new Dimension(1, 20));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        infoCard.add(headerPanel);
        infoCard.add(Box.createVerticalStrut(1));
        infoCard.add(createMiniStatsPanel(isPlayer1));

        // ---------- BOARD ----------
        JPanel boardContainer = createBoardContainer(board, color);

        container.add(infoCard, BorderLayout.NORTH);
        container.add(boardContainer, BorderLayout.CENTER);

        return container;
    }

    private JPanel createMiniStatsPanel(boolean isPlayer1) {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        int totalMines = getTotalMinesForDifficulty();

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row1.setOpaque(false);

        JLabel correctFlagsLabel = new JLabel("🚩 0/" + totalMines);
        correctFlagsLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        correctFlagsLabel.setForeground(new Color(100, 255, 100));
        row1.add(correctFlagsLabel);

        JLabel wrongFlagsLabel = new JLabel("🚩 0");
        wrongFlagsLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        wrongFlagsLabel.setForeground(new Color(255, 100, 100));
        row1.add(wrongFlagsLabel);

        JLabel revealedMinesLabel = new JLabel("💣 0/" + totalMines);
        revealedMinesLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        revealedMinesLabel.setForeground(new Color(255, 150, 0));
        row1.add(revealedMinesLabel);

        statsPanel.add(row1);
        statsPanel.add(Box.createVerticalStrut(1));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row2.setOpaque(false);

        JLabel questionsLabel = new JLabel("❓ 0/0");
        questionsLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        questionsLabel.setForeground(new Color(255, 200, 0));
        row2.add(questionsLabel);

        JLabel surprisesLabel = new JLabel("🎁 0/0");
        surprisesLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        surprisesLabel.setForeground(new Color(180, 100, 255));
        row2.add(surprisesLabel);

        statsPanel.add(row2);

        if (isPlayer1) {
            player1CorrectFlagsLabel = correctFlagsLabel;
            player1WrongFlagsLabel = wrongFlagsLabel;
            player1RevealedMinesLabel = revealedMinesLabel;
            player1QuestionsLabel = questionsLabel;
            player1SurprisesLabel = surprisesLabel;
        } else {
            player2CorrectFlagsLabel = correctFlagsLabel;
            player2WrongFlagsLabel = wrongFlagsLabel;
            player2RevealedMinesLabel = revealedMinesLabel;
            player2QuestionsLabel = questionsLabel;
            player2SurprisesLabel = surprisesLabel;
        }

        return statsPanel;
    }

    private int getTotalMinesForDifficulty() {
        switch (gameController.getDifficulty()) {
            case "Easy": return 10;
            case "Medium": return 26;
            case "Hard": return 44;
            default: return 26;
        }
    }

    private JPanel createBoardContainer(MinesweeperBoardPanelCompetitive board, Color glowColor) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JPanel glowPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(glowColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        glowPanel.setOpaque(false);
        glowPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        
        glowPanel.add(board, BorderLayout.CENTER);
        
        container.add(glowPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel createBaseCard(Color glowColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(glowColor.darker());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        panel.setOpaque(false);
        panel.setForeground(Color.WHITE);
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(500, 250));
        return panel;
    }

    private void toggleFlagMode() {
        isFlagMode = !isFlagMode;

        board1.setFlagMode(isFlagMode);
        board2.setFlagMode(isFlagMode);

        if (isFlagMode) {
            flagButton.setText(FLAG_ICON);
            flagButton.setBackground(new Color(0, 180, 0));
            flagButton.setForeground(Color.WHITE);
        } else {
            flagButton.setText(FLAG_OFF_ICON);
            flagButton.setBackground(new Color(180, 0, 0));
            flagButton.setForeground(Color.WHITE);
        }

        flagButton.repaint();
    }

    private JButton createToggleButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.putClientProperty("baseColor", baseColor);
        return button;
    }

    public void updateHUD() {
        syncTopHudNow();

        updatePlayerMiniStats(board1, player1CorrectFlagsLabel, player1WrongFlagsLabel,
                player1RevealedMinesLabel, player1QuestionsLabel, player1SurprisesLabel);

        updatePlayerMiniStats(board2, player2CorrectFlagsLabel, player2WrongFlagsLabel,
                player2RevealedMinesLabel, player2QuestionsLabel, player2SurprisesLabel);

        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
    }

    public void updateActivePlayer() {
        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
    }

    public void onGameEnded() {
        if (endSequenceStarted) return;
        endSequenceStarted = true;

        AudioManager.play(AudioManager.Sfx.BOOM);
        gameController.stopTimer();

        if (board1 != null) board1.revealAllCellsForEnd(true, false);
        if (board2 != null) board2.revealAllCellsForEnd(true, false);

        Timer t = new Timer(1200, e -> showGameOverDialog());
        t.setRepeats(false);
        t.start();
    }

    private String generateHearts(int lives, int maxLives) {
        int full = Math.max(0, Math.min(lives, maxLives));
        int empty = Math.max(0, maxLives - full);
        return " " + "❤️".repeat(full) + "🤍".repeat(empty);
    }

    private void updatePlayerMiniStats(MinesweeperBoardPanelCompetitive board,
                                       JLabel correctFlagLabel, JLabel wrongFlagLabel,
                                       JLabel revealedMinesLabel,
                                       JLabel questionsLabel, JLabel surprisesLabel) {

        int correctFlags = safe(board, "correctFlags");
        int wrongFlags = safe(board, "wrongFlags");
        int revealedMines = safe(board, "revealedMines");
        int totalMines = safe(board, "totalMines");
        int usedQuestions = safe(board, "usedQuestions");
        int totalQuestions = safe(board, "totalQuestions");
        int usedSurprises = safe(board, "usedSurprises");
        int totalSurprises = safe(board, "totalSurprises");

        correctFlagLabel.setText("🚩 " + correctFlags + "/" + totalMines);
        wrongFlagLabel.setText("🚩 " + wrongFlags);
        revealedMinesLabel.setText("💣 " + revealedMines + "/" + totalMines);
        questionsLabel.setText("❓ " + usedQuestions + "/" + totalQuestions);
        surprisesLabel.setText("🎁 " + usedSurprises + "/" + totalSurprises);
    }

    private int safe(MinesweeperBoardPanelCompetitive board, String what) {
        try {
            switch (what) {
                case "correctFlags": return board.getCorrectFlagsCount();
                case "wrongFlags": return board.getIncorrectFlagsCount();
                case "revealedMines": return board.getRevealedMinesCount();
                case "totalMines": return board.getTotalMines();
                case "usedQuestions": return board.getUsedQuestionsCount();
                case "totalQuestions": return board.getTotalQuestionsCount();
                case "usedSurprises": return board.getUsedSurprisesCount();
                case "totalSurprises": return board.getTotalSurprisesCount();
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    private void highlightActivePlayerPanel() {
        int currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer == 1) {
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(player1Color, 3));
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        } else {
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(player2Color, 3));
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        }
    }

    // ✅ FIXED: קריאה לקונסטרוקטור המלא עם הניקודים והחיים של שני השחקנים בנפרד
    private void showGameOverDialog() {
        GameEndedDialog.EndReason reason;
        if (isGiveUp) reason = GameEndedDialog.EndReason.GIVE_UP;
        else if (gameController.getLives(1) <= 0 && gameController.getLives(2) <= 0) reason = GameEndedDialog.EndReason.LOST_NO_LIVES;
        else reason = GameEndedDialog.EndReason.WIN;

        gameController.stopTimer();

        // ✅ קבלת הנתונים האמיתיים של כל שחקן
        String player1Name = gameController.getPlayer1().getUsername();
        String player2Name = gameController.getPlayer2().getUsername();
        
        int p1Score = gameController.getScore(1);
        int p2Score = gameController.getScore(2);
        
        int p1Lives = gameController.getLives(1);
        int p2Lives = gameController.getLives(2);
        int maxLives = gameController.getMaxLives();

        // ✅ קריאה לקונסטרוקטור המלא עם הנתונים של שני השחקנים בנפרד
        GameEndedDialogCompetitive dialog = new GameEndedDialogCompetitive(
                frame,
                player1Name,
                player2Name,
                p1Score,        // ✅ ניקוד אמיתי של שחקן 1
                p2Score,        // ✅ ניקוד אמיתי של שחקן 2
                stripHtml(timerLabel.getText()),
                gameController.getDifficulty(),
                p1Lives,        // ✅ חיים של שחקן 1
                p2Lives,        // ✅ חיים של שחקן 2
                maxLives,
                0,              // ✅ winnerPlayerNum - יחושב אוטומטית על ידי determineWinner
                mapReasonToCompetitive(reason),
                this::restartGame,
                this::goToMainMenu
        );

        dialog.setVisible(true);
        isGiveUp = false;
    }

    // ✅ פונקציה להמרת EndReason
    private GameEndedDialogCompetitive.EndReason mapReasonToCompetitive(GameEndedDialog.EndReason r) {
        if (r == null) return GameEndedDialogCompetitive.EndReason.GIVE_UP;
        return switch (r) {
            case WIN -> GameEndedDialogCompetitive.EndReason.WIN;
            case LOST_NO_LIVES -> GameEndedDialogCompetitive.EndReason.LOST_NO_LIVES;
            case GIVE_UP -> GameEndedDialogCompetitive.EndReason.GIVE_UP;
        };
    }

    private String stripHtml(String s) {
        return s == null ? "" : s.replaceAll("<[^>]*>", "");
    }

    public JFrame getFrame() { 
        return frame; 
    }

    public void goToMainMenu() {
        dispose();

        MusicManager.playLoop("/music/menu_theme.mp3");

        frame.getContentPane().removeAll();
        frame.add(new MainMenuTwoPlayerScreen(frame));
        keepFrameBig();
        frame.revalidate();
        frame.repaint();
    }

    private void restartGame() {
        dispose();
        gameController.stopTimer();

        CompetitiveGameController newController = new CompetitiveGameController(
                gameController.getSysData(),
                gameController.getPlayer1(),
                gameController.getPlayer2(),
                gameController.getDifficulty(),
                gameController.getGridSize()
        );

        frame.getContentPane().removeAll();
        frame.add(new GameScreenCompetitive(frame, newController));

        keepFrameBig();
        frame.revalidate();
        frame.repaint();
    }

    private void keepFrameBig() {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setLocationRelativeTo(null);
    }

    public void dispose() {
        gameController.removeObserver(this);
        gameController.stopTimer();
        System.out.println("✅ Disposed Competitive Screen");
    }
}
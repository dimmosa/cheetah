package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import control.MultiPlayerGameController;
import control.QuestionController;
import view.CellButton;
import view.GameEndedDialog;

import static view.CustomIconButton.createNeonButton;

public class GameScreenMultiPlayer extends JPanel {

    private JFrame frame;
    private control.MultiPlayerGameController gameController;

    private MinesweeperBoardPanelTwoPlayer board1;
    private MinesweeperBoardPanelTwoPlayer board2;

    private JPanel player1PanelContainer;
    private JPanel player2PanelContainer;

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

    private boolean isFlagMode = false;
    private JButton flagButton;
    private JLabel activePlayerIndicator;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;

    private Color player1Color = new Color(0, 150, 255);
    private Color player2Color = new Color(255, 0, 150);


    public GameScreenMultiPlayer(JFrame frame, MultiPlayerGameController gameController) {
        this.frame = frame;
        this.gameController = gameController;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 25));

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(new Color(15, 15, 25));
        mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

        mainContent.add(createTopSection(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        centerPanel.setOpaque(false);

        board1 = new MinesweeperBoardPanelTwoPlayer(gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, true);

        board2 = new MinesweeperBoardPanelTwoPlayer(gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, false);

        player1PanelContainer = createPlayerPanel("PLAYER 1", gameController.getPlayer1().getUsername(),
                gameController.getPlayer1().getAvatar(), player1Color, board1);
        centerPanel.add(player1PanelContainer);

        player2PanelContainer = createPlayerPanel("PLAYER 2", gameController.getPlayer2().getUsername(),
                gameController.getPlayer2().getAvatar(), player2Color, board2);
        centerPanel.add(player2PanelContainer);

        mainContent.add(centerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(15, 15, 25));
        scrollPane.getViewport().setBackground(new Color(15, 15, 25));

        add(scrollPane, BorderLayout.CENTER);

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
        top.add(Box.createVerticalStrut(8));

        top.add(createGameInfoPanel());
        top.add(Box.createVerticalStrut(10));

        top.add(createColorKeyPanel());

        return top;
    }

    private JPanel createGameInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(new Color(30, 30, 50));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel turnLabel = new JLabel("CURRENT TURN:");
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        turnLabel.setForeground(new Color(135, 206, 250));
        panel.add(turnLabel);

        activePlayerIndicator = new JLabel();
        activePlayerIndicator.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(activePlayerIndicator);

        JLabel separator1 = new JLabel("|");
        separator1.setFont(new Font("SansSerif", Font.BOLD, 18));
        separator1.setForeground(new Color(60, 60, 100));
        panel.add(separator1);

        JLabel statsLabel = new JLabel("SHARED LIVES:");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statsLabel.setForeground(new Color(135, 206, 250));
        panel.add(statsLabel);

        livesLabel = new JLabel("Lives: " + gameController.getSharedLives() + " / " + gameController.getMaxLives());
        livesLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        livesLabel.setForeground(new Color(255, 100, 100));

        int maxHearts = gameController.getMaxLives();
        int estimatedWidth = maxHearts * 48;
        livesLabel.setPreferredSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMinimumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMaximumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(livesLabel);

        flagButton = createToggleButton("FLAG MODE: OFF", new Color(255, 165, 0));
        flagButton.addActionListener(e -> toggleFlagMode());
        panel.add(flagButton);

        JButton exitButton = createNeonButton("Exit", new Color(200, 50, 50), 125, 32);
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        exitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to give up?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                gameController.giveUp();
                gameController.stopTimer();
                showGameOverDialog();
            }
        });
        panel.add(exitButton);

        return panel;
    }

    private JPanel createColorKeyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panel.setBackground(new Color(30, 30, 50));
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel header = new JLabel("Tile Status Key:");
        header.setFont(new Font("SansSerif", Font.PLAIN, 14));
        header.setForeground(new Color(200, 200, 220));
        panel.add(header);

        Map<CellButton.CellState, String> keyMap = new HashMap<>();
        keyMap.put(CellButton.CellState.HIDDEN, "Hidden");
        keyMap.put(CellButton.CellState.FLAGGED, "Flag");
        keyMap.put(CellButton.CellState.MINE, "Mine");
        keyMap.put(CellButton.CellState.NUMBER, "Number");
        keyMap.put(CellButton.CellState.EMPTY, "Empty");
        keyMap.put(CellButton.CellState.QUESTION, "Question");
        keyMap.put(CellButton.CellState.SURPRISE, "Surprise");

        for (Map.Entry<CellButton.CellState, String> entry : keyMap.entrySet()) {
            CellButton keyCell = new CellButton(28);
            keyCell.setState(entry.getKey());

            if (entry.getKey() == CellButton.CellState.NUMBER) {
                keyCell.setNumber(3);
            }

            JLabel description = new JLabel(entry.getValue());
            description.setFont(new Font("SansSerif", Font.PLAIN, 14));
            description.setForeground(new Color(200, 200, 220).darker());

            JPanel item = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            item.setOpaque(false);
            item.add(keyCell);
            item.add(description);

            panel.add(item);
        }

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        String difficulty = gameController.getDifficulty();

        JLabel titleLabel = new JLabel(
                "<html><font color='#00BFFF'>CO-OP</font> " +
                        "<font color='#87CEFA'>MINESWEEPER</font> " +
                        "<font color='#FFA500'>" + difficulty + " Mode</font></html>"
        );
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        scoreLabel = new JLabel("<html><font color='#FFD700'>Score: " + gameController.getSharedScore() + "</font></html>");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        timerLabel = new JLabel("<html><font color='#FFA500'>00:00</font></html>");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 32));

        JLabel timerIcon = new JLabel("⏱️");
        timerIcon.setForeground(Color.WHITE);
        timerIcon.setFont(new Font("SansSerif", Font.BOLD, 20));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(scoreLabel);
        rightPanel.add(timerIcon);
        rightPanel.add(timerLabel);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void setActivePlayer(int playerNum) {
        String name = (playerNum == 1) ? gameController.getPlayer1().getUsername()
                : gameController.getPlayer2().getUsername();
        Color color = (playerNum == 1) ? player1Color : player2Color;

        activePlayerIndicator.setText(name);
        activePlayerIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);
        activePlayerIndicator.setForeground(color);
    }

    private JPanel createPlayerPanel(String title, String playerName, String avatar, Color color,
                                     MinesweeperBoardPanelTwoPlayer board) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setName(title);
        container.setBorder(new EmptyBorder(0, 5, 0, 5));

        // INFO CARD - Made more compact
        JPanel infoCard = createBaseCard(color);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        infoCard.setBorder(new EmptyBorder(8, 12, 10, 12));

        // Header with avatar and name
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatarLabel = new JLabel(avatar);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel nameLabel = new JLabel(title + " - " + playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(color);

        headerPanel.add(avatarLabel);
        headerPanel.add(nameLabel);

        infoCard.add(headerPanel);
        infoCard.add(Box.createVerticalStrut(8));

        // Stats panel - now with revealed mines
        JPanel statsPanel = createMiniStatsPanel(title.equals("PLAYER 1"));
        infoCard.add(statsPanel);

        container.add(infoCard);
        container.add(Box.createVerticalStrut(10));

        // BOARD - Made MUCH bigger
        JPanel boardContainer = createBoardContainer(board, color);
        container.add(boardContainer);

        return container;
    }

    private JPanel createMiniStatsPanel(boolean isPlayer1) {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        // Get total mines based on difficulty
        int totalMines = getTotalMinesForDifficulty();

        // Row 1: Correct flags and Wrong flags
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        row1.setOpaque(false);

        JLabel correctFlagsLabel = new JLabel("🚩 0/" + totalMines);
        correctFlagsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        correctFlagsLabel.setForeground(new Color(100, 255, 100));
        correctFlagsLabel.setToolTipText("Correct Flags (on mines)");
        row1.add(correctFlagsLabel);

        JLabel wrongFlagsLabel = new JLabel("🚩 0");
        wrongFlagsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        wrongFlagsLabel.setForeground(new Color(255, 100, 100));
        wrongFlagsLabel.setToolTipText("Wrong Flags (on non-mines)");
        row1.add(wrongFlagsLabel);

        JLabel revealedMinesLabel = new JLabel("💣 0/" + totalMines);
        revealedMinesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        revealedMinesLabel.setForeground(new Color(255, 150, 0));
        revealedMinesLabel.setToolTipText("Revealed Mines (clicked)");
        row1.add(revealedMinesLabel);

        statsPanel.add(row1);
        statsPanel.add(Box.createVerticalStrut(4));

        // Row 2: Questions and Surprises
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        row2.setOpaque(false);

        JLabel questionsLabel = new JLabel("❓ 0/0");
        questionsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        questionsLabel.setForeground(new Color(255, 200, 0));
        questionsLabel.setToolTipText("Questions Used");
        row2.add(questionsLabel);

        JLabel surprisesLabel = new JLabel("🎁 0/0");
        surprisesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        surprisesLabel.setForeground(new Color(180, 100, 255));
        surprisesLabel.setToolTipText("Surprises Used");
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
            case "Easy":
                return 10;
            case "Medium":
                return 26;
            case "Hard":
                return 44;
            default:
                return 26;
        }
    }

    private JPanel createBoardContainer(MinesweeperBoardPanelTwoPlayer board, Color glowColor) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(glowColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(board, BorderLayout.CENTER);

        container.add(panel);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(glowColor.darker());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setForeground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(500, 300));
        return panel;
    }

    private void toggleFlagMode() {
        isFlagMode = !isFlagMode;
        board1.setFlagMode(isFlagMode);
        board2.setFlagMode(isFlagMode);

        if (isFlagMode) {
            flagButton.setText("FLAG MODE: ON");
            flagButton.putClientProperty("baseColor", new Color(0, 200, 0));
        } else {
            flagButton.setText("FLAG MODE: OFF");
            flagButton.putClientProperty("baseColor", new Color(255, 165, 0));
        }
        flagButton.repaint();
    }

    private JButton createToggleButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.putClientProperty("baseColor", baseColor);
        return button;
    }

    public void updateGameStateDisplay(control.MultiPlayerGameController.CellActionResult result) {
        scoreLabel.setText(
                "<html><font color='#FFD700'>Score: " + gameController.getSharedScore() + "</font></html>"
        );
        int lives = gameController.getSharedLives();
        int maxLives = gameController.getMaxLives();

        String hearts = generateHearts(lives);

        livesLabel.setText(lives + " / " + maxLives + hearts);

        livesLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        int estimatedWidth = maxLives * 44;
        livesLabel.setPreferredSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMinimumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMaximumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setHorizontalAlignment(SwingConstants.LEFT);

        updatePlayerMiniStats(board1, player1CorrectFlagsLabel, player1WrongFlagsLabel,
                player1RevealedMinesLabel, player1QuestionsLabel, player1SurprisesLabel);
        updatePlayerMiniStats(board2, player2CorrectFlagsLabel, player2WrongFlagsLabel,
                player2RevealedMinesLabel, player2QuestionsLabel, player2SurprisesLabel);

        if (gameController.getSharedLives() <= 0) {
            gameController.stopTimer();
            showGameOverDialog();
        }
    }

    private String generateHearts(int lives) {
        StringBuilder sb = new StringBuilder();
        sb.append("❤️".repeat(Math.max(0, lives)));
        return sb.toString();
    }

    private void updatePlayerMiniStats(MinesweeperBoardPanelTwoPlayer board,
            JLabel correctFlagLabel, JLabel wrongFlagLabel,
            JLabel revealedMinesLabel,
            JLabel questionsLabel, JLabel surprisesLabel) {
int correctFlags = board.getCorrectFlagsCount();
int wrongFlags = board.getIncorrectFlagsCount();
//int revealedMines = board.getRevealedMinesCount();
//int totalMines = board.getTotalMines();
int usedQuestions = board.getUsedQuestionsCount();
int totalQuestions = board.getTotalQuestionsCount();
int usedSurprises = board.getUsedSurprisesCount();
int totalSurprises = board.getTotalSurprisesCount();

// FIXED: Always show flag icon, even when totalMines is 0
correctFlagLabel.setText("🚩 " + correctFlags );
// FIXED: Always show flag icon for wrong flags too
wrongFlagLabel.setText("🚩 " + wrongFlags);
revealedMinesLabel.setText("💣 " );
questionsLabel.setText("❓ " + usedQuestions + "/" + totalQuestions);
surprisesLabel.setText("🎁 " + usedSurprises + "/" + totalSurprises);
}
    public void updateActivePlayer() {
        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
    }

    private void highlightActivePlayerPanel() {
        int currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer == 1) {
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(player1Color, 4));
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        } else {
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(player2Color, 4));
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        }
    }

    private void showGameOverDialog() {
        String message;
        if (gameController.isGameWon()) {
            message = "Congratulations! You won!\nFinal Score: " + gameController.getSharedScore();
        } else {
            message = "Game Over! You lost.\nFinal Score: " + gameController.getSharedScore();
        }

        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);

        GameEndedDialog gameEndedDialog = new GameEndedDialog(
                frame,
                gameController.getPlayer1().getUsername() + " & " + gameController.getPlayer2().getUsername(),
                gameController.getSharedScore(),
                timerLabel.getText(),
                gameController.getDifficulty(),
                gameController.getSharedLives(),
                gameController.getMaxLives(),
                "multi"
        );
        gameEndedDialog.setVisible(true);

        frame.getContentPane().removeAll();
        frame.add(new MainMenuTwoPlayerScreen(frame));
        frame.revalidate();
        frame.repaint();
    }
}
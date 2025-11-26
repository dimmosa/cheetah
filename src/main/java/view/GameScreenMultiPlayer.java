package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import control.MultiPlayerGameController;
import control.QuestionController;

import java.awt.*;

public class GameScreenMultiPlayer extends JPanel {

    private static final long serialVersionUID = 1L;

    private JFrame frame;
    private MultiPlayerGameController gameController;

    private MinesweeperBoardPanelTwoPlayer board1;
    private MinesweeperBoardPanelTwoPlayer board2;

    private JPanel player1PanelContainer;
    private JPanel player2PanelContainer;

    private JLabel player1MinesLabel;
    private JLabel player1RevealedLabel;
    private JLabel player1QuestionsLabel;
    private JLabel player1SurprisesLabel;

    private JLabel player2MinesLabel;
    private JLabel player2RevealedLabel;
    private JLabel player2QuestionsLabel;
    private JLabel player2SurprisesLabel;

    private boolean isFlagMode = false;
    private JButton flagButton;
    private JLabel activePlayerIndicator;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;

    public GameScreenMultiPlayer(JFrame frame, MultiPlayerGameController gameController) {
        this.frame = frame;
        this.gameController = gameController;

        setLayout(new BorderLayout());

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainContent.add(createTopSection(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        board1 = new MinesweeperBoardPanelTwoPlayer(
                gameController.getRows(),
                gameController.getCols(),
                gameController,
                new QuestionController(),
                this,
                true
        );

        board2 = new MinesweeperBoardPanelTwoPlayer(
                gameController.getRows(),
                gameController.getCols(),
                gameController,
                new QuestionController(),
                this,
                false
        );

        player1PanelContainer = createPlayerPanel(
                "PLAYER 1",
                gameController.getPlayer1().getUsername(),
                gameController.getPlayer1().getAvatar(),
                board1
        );
        centerPanel.add(player1PanelContainer);

        player2PanelContainer = createPlayerPanel(
                "PLAYER 2",
                gameController.getPlayer2().getUsername(),
                gameController.getPlayer2().getAvatar(),
                board2
        );
        centerPanel.add(player2PanelContainer);

        mainContent.add(centerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();

        gameController.startTimer(timeStr -> timerLabel.setText(timeStr));
    }

    // ───────────────────── Top Section ─────────────────────

    private JPanel createTopSection() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        top.add(createHeaderPanel());
        top.add(Box.createVerticalStrut(5));
        top.add(createGameInfoPanel());
        top.add(Box.createVerticalStrut(5));

        return top;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        String difficulty = gameController.getDifficulty();

        JLabel titleLabel = new JLabel("CO-OP MINESWEEPER - " + difficulty + " Mode");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        scoreLabel = new JLabel("Score: " + gameController.getSharedScore());
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 16f));

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 20f));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(scoreLabel);
        rightPanel.add(new JLabel("Time:"));
        rightPanel.add(timerLabel);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createGameInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel turnLabel = new JLabel("Current turn:");
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(turnLabel);

        activePlayerIndicator = new JLabel();
        activePlayerIndicator.setFont(activePlayerIndicator.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(activePlayerIndicator);

        panel.add(new JLabel("|"));

        JLabel livesTextLabel = new JLabel("Shared lives:");
        livesTextLabel.setFont(livesTextLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(livesTextLabel);

        livesLabel = new JLabel();
        livesLabel.setFont(livesLabel.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(livesLabel);
        updateLivesLabel();

        flagButton = new JButton("FLAG MODE: OFF");
        flagButton.setFont(flagButton.getFont().deriveFont(Font.BOLD, 12f));
        flagButton.addActionListener(e -> toggleFlagMode());
        panel.add(flagButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(exitButton.getFont().deriveFont(Font.BOLD, 12f));
        exitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to give up?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                gameController.giveUp();
                gameController.stopTimer();
                showGameOverDialog();
            }
        });
        panel.add(exitButton);

        return panel;
    }

    // ───────────────────── Player Panels ─────────────────────

    private JPanel createPlayerPanel(String title, String playerName, String avatar,
                                     MinesweeperBoardPanelTwoPlayer board) {

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setName(title);
        container.setBorder(new EmptyBorder(0, 5, 0, 5));

        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(new EmptyBorder(5, 5, 5, 5));
        infoCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        JLabel avatarLabel = new JLabel(avatar);
        JLabel nameLabel = new JLabel(title + " - " + playerName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        headerPanel.add(avatarLabel);
        headerPanel.add(nameLabel);

        infoCard.add(headerPanel);
        infoCard.add(Box.createVerticalStrut(5));

        JPanel statsPanel = createMiniStatsPanel(title.equals("PLAYER 1"));
        infoCard.add(statsPanel);

        container.add(infoCard);
        container.add(Box.createVerticalStrut(5));

        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        boardContainer.add(board, BorderLayout.CENTER);

        container.add(boardContainer);

        return container;
    }

    private JPanel createMiniStatsPanel(boolean isPlayer1) {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JLabel minesLabel = new JLabel("Correct flags: 0");
        minesLabel.setFont(minesLabel.getFont().deriveFont(12f));
        statsPanel.add(minesLabel);

        JLabel revealedLabel = new JLabel("Wrong flags: 0");
        revealedLabel.setFont(revealedLabel.getFont().deriveFont(12f));
        statsPanel.add(revealedLabel);

        JLabel questionsLabel = new JLabel("Questions: 0/0");
        questionsLabel.setFont(questionsLabel.getFont().deriveFont(12f));
        statsPanel.add(questionsLabel);

        JLabel surprisesLabel = new JLabel("Surprises: 0/0");
        surprisesLabel.setFont(surprisesLabel.getFont().deriveFont(12f));
        statsPanel.add(surprisesLabel);

        if (isPlayer1) {
            player1MinesLabel = minesLabel;
            player1RevealedLabel = revealedLabel;
            player1QuestionsLabel = questionsLabel;
            player1SurprisesLabel = surprisesLabel;
        } else {
            player2MinesLabel = minesLabel;
            player2RevealedLabel = revealedLabel;
            player2QuestionsLabel = questionsLabel;
            player2SurprisesLabel = surprisesLabel;
        }

        return statsPanel;
    }

    // ───────────────────── State Updates ─────────────────────

    private void toggleFlagMode() {
        isFlagMode = !isFlagMode;
        board1.setFlagMode(isFlagMode);
        board2.setFlagMode(isFlagMode);

        if (isFlagMode) {
            flagButton.setText("FLAG MODE: ON");
        } else {
            flagButton.setText("FLAG MODE: OFF");
        }
    }

    private void setActivePlayer(int playerNum) {
        String name = (playerNum == 1)
                ? gameController.getPlayer1().getUsername()
                : gameController.getPlayer2().getUsername();
        activePlayerIndicator.setText(name);
    }

    private void updateLivesLabel() {
        int lives = gameController.getSharedLives();
        int maxLives = gameController.getMaxLives();
        livesLabel.setText(lives + " / " + maxLives);
    }

    public void updateGameStateDisplay(MultiPlayerGameController.CellActionResult result) {
        scoreLabel.setText("Score: " + gameController.getSharedScore());
        updateLivesLabel();

        updatePlayerMiniStats(
                board1,
                player1MinesLabel,
                player1RevealedLabel,
                player1QuestionsLabel,
                player1SurprisesLabel
        );
        updatePlayerMiniStats(
                board2,
                player2MinesLabel,
                player2RevealedLabel,
                player2QuestionsLabel,
                player2SurprisesLabel
        );

        if (gameController.getSharedLives() <= 0) {
            gameController.stopTimer();
            showGameOverDialog();
        }
    }

    private void updatePlayerMiniStats(MinesweeperBoardPanelTwoPlayer board,
                                       JLabel correctFlagLabel, JLabel wrongFlagLabel,
                                       JLabel questionsLabel, JLabel surprisesLabel) {

        int correctFlags = board.getCorrectFlagsCount();
        int wrongFlags = board.getIncorrectFlagsCount();
        int usedQuestions = board.getUsedQuestionsCount();
        int totalQuestions = board.getTotalQuestionsCount();
        int usedSurprises = board.getUsedSurprisesCount();
        int totalSurprises = board.getTotalSurprisesCount();

        correctFlagLabel.setText("Correct flags: " + correctFlags);
        wrongFlagLabel.setText("Wrong flags: " + wrongFlags);
        questionsLabel.setText("Questions: " + usedQuestions + "/" + totalQuestions);
        surprisesLabel.setText("Surprises: " + usedSurprises + "/" + totalSurprises);
    }

    public void updateActivePlayer() {
        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
    }

    private void highlightActivePlayerPanel() {
        int currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer == 1) {
            player1PanelContainer.setBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2)
            );
            player2PanelContainer.setBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1)
            );
        } else {
            player2PanelContainer.setBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2)
            );
            player1PanelContainer.setBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1)
            );
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

        frame.dispose();
    }
}
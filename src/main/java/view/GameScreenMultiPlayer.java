package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import controller.MultiPlayerGameController;
import controller.QuestionController;
import view.CellButton;

public class GameScreenMultiPlayer extends JPanel {

    private JFrame frame;
    private MultiPlayerGameController gameController;

    private MinesweeperBoardPanelTwoPlayer board1;
    private MinesweeperBoardPanelTwoPlayer board2;

    private JPanel player1PanelContainer;
    private JPanel player2PanelContainer;

    private JLabel activePlayerIndicator;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;

    private Color player1Color = Color.DARK_GRAY;
    private Color player2Color = Color.GRAY;


    public GameScreenMultiPlayer(JFrame frame, MultiPlayerGameController gameController) {
        this.frame = frame;
        this.gameController = gameController;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        mainContent.add(createTopSection(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);

        board1 = new MinesweeperBoardPanelTwoPlayer(gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, true);

        board2 = new MinesweeperBoardPanelTwoPlayer(gameController.getRows(), gameController.getCols(),
                gameController, new QuestionController(), this, false);

        player1PanelContainer = createPlayerPanel("PLAYER 1", gameController.getPlayer1().getUsername(),
                player1Color, board1);
        centerPanel.add(player1PanelContainer);

        player2PanelContainer = createPlayerPanel("PLAYER 2", gameController.getPlayer2().getUsername(),
                player2Color, board2);
        centerPanel.add(player2PanelContainer);

        mainContent.add(centerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
        gameController.startTimer(timeStr ->
                timerLabel.setText(timeStr)
        );
    }

    private JPanel createTopSection() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(createHeaderPanel());
        top.add(Box.createVerticalStrut(15));

        top.add(createGameInfoPanel());

        return top;
    }

    private JPanel createGameInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel turnLabel = new JLabel("CURRENT TURN:");
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        turnLabel.setForeground(Color.BLACK);
        panel.add(turnLabel);

        activePlayerIndicator = new JLabel();
        activePlayerIndicator.setFont(new Font("SansSerif", Font.BOLD, 18));
        activePlayerIndicator.setForeground(Color.BLACK);
        panel.add(activePlayerIndicator);

        JLabel separator1 = new JLabel("|");
        separator1.setFont(new Font("SansSerif", Font.BOLD, 18));
        separator1.setForeground(Color.GRAY);
        panel.add(separator1);

        JLabel statsLabel = new JLabel("SHARED LIVES:");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statsLabel.setForeground(Color.BLACK);
        panel.add(statsLabel);

        livesLabel = new JLabel("Lives: " + gameController.getSharedLives() + " / " + gameController.getMaxLives());
        livesLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        livesLabel.setForeground(Color.BLACK);

        int maxHearts = gameController.getMaxLives();
        int estimatedWidth = maxHearts * 48;
        livesLabel.setPreferredSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMinimumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setMaximumSize(new Dimension(estimatedWidth, 25));
        livesLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(livesLabel);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        exitButton.setBackground(Color.LIGHT_GRAY);
        exitButton.setForeground(Color.BLACK);
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

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        String difficulty = gameController.getDifficulty();

        JLabel titleLabel = new JLabel("CO-OP MINESWEEPER - " + difficulty + " Mode");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.BLACK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        scoreLabel = new JLabel("Score: " + gameController.getSharedScore());
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        scoreLabel.setForeground(Color.BLACK);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        timerLabel.setForeground(Color.BLACK);

        JLabel timerIcon = new JLabel("⏱️");
        timerIcon.setForeground(Color.BLACK);
        timerIcon.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
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



    private JPanel createPlayerPanel(String title, String playerName, Color color,
                                     MinesweeperBoardPanelTwoPlayer board) {
        JPanel container = new JPanel();
        container.setBackground(Color.WHITE);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setName(title);
        container.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel infoCard = new JPanel();
        infoCard.setBackground(Color.LIGHT_GRAY);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        infoCard.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(title + " - " + playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(Color.BLACK);

        headerPanel.add(nameLabel);

        infoCard.add(headerPanel);

        container.add(infoCard);
        container.add(Box.createVerticalStrut(15));

        JPanel boardContainer = createBoardContainer(board);
        container.add(boardContainer);

        return container;
    }

    private JPanel createBoardContainer(MinesweeperBoardPanelTwoPlayer board) {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        panel.add(board, BorderLayout.CENTER);

        container.add(panel);
        return container;
    }


    public void updateGameStateDisplay(MultiPlayerGameController.CellActionResult result) {
        System.out.println(scoreLabel.getText());
        System.out.println(gameController.getSharedScore());
        scoreLabel.setText("Score: " + gameController.getSharedScore());
        
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


    public void updateActivePlayer() {
        setActivePlayer(gameController.getCurrentPlayer());
        highlightActivePlayerPanel();
    }

    private void highlightActivePlayerPanel() {
        int currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer == 1) {
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        } else {
            player2PanelContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
            player1PanelContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        }
    }

    private void showGameOverDialog() {
        String message;
        if (gameController.isGameWon()) {
            message = "🎉 Congratulations! You won! 🎉\nFinal Score: " + gameController.getSharedScore();
        } else {
            message = "💣 Game Over! You lost. 💣\nFinal Score: " + gameController.getSharedScore();
        }

        String[] options = {"Play Again", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // Play Again
            frame.setContentPane(new GameSetupScreen(frame));
            frame.revalidate();
            frame.repaint();
        } else { // Exit
            System.exit(0);
        }
    }
}
package view;

import controller.GameSetupController;
import controller.MultiPlayerGameController;
import model.SysData;

import javax.swing.*;
import java.awt.*;

public class GameSetupScreen extends JPanel {

    private JFrame frame;

    private JTextField player1NameField;
    private JTextField player2NameField;
    private JComboBox<String> difficultyComboBox;

    // נשמור אווטארים ברירת מחדל (לא מוצגים במסך)
    private String player1Avatar = "👻";
    private String player2Avatar = "🐉";

    public GameSetupScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());

        // פאנל ראשי באמצע
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // כותרת
        JLabel titleLabel = new JLabel("Game Setup", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Player 1 label
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Player 1 Name:"), gbc);

        // Player 1 text field
        player1NameField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(player1NameField, gbc);
        row++;

        // Player 2 label
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Player 2 Name:"), gbc);

        // Player 2 text field
        player2NameField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(player2NameField, gbc);
        row++;

        // Difficulty label
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Difficulty:"), gbc);

        // Difficulty combo box
        difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        gbc.gridx = 1;
        mainPanel.add(difficultyComboBox, gbc);
        row++;

        // Start button
        JButton startButton = new JButton("Start Game");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(startButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            String p1 = player1NameField.getText().trim();
            String p2 = player2NameField.getText().trim();

            if (p1.isEmpty() || p2.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter both player names",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();

            GameSetupController setupController = new GameSetupController(SysData.getInstance());
            setupController.setDifficulty(selectedDifficulty);
            setupController.createPlayers(p1, player1Avatar, p2, player2Avatar);
            GameSetupController.GameConfig config = setupController.initializeGame();

            MultiPlayerGameController gameController = new MultiPlayerGameController(
                    config.sysData, config.player1, config.player2,
                    config.difficulty, config.gridSize
            );

            frame.setContentPane(new GameScreenMultiPlayer(frame, gameController));
            frame.revalidate();
            frame.repaint();
        });
    }

    public static void main(String[] args) {
        JFrame frame1 = new JFrame("Minesweeper - Game Setup");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GameSetupScreen screen = new GameSetupScreen(frame1);
        frame1.setContentPane(screen);

        frame1.setSize(600, 500);
        frame1.setLocationRelativeTo(null);
        frame1.setVisible(true);
    }
}
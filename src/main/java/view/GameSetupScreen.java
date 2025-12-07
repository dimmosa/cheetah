package view;

import control.GameSetupController;
import control.MultiPlayerGameController;
import model.SysData;
import view.AvatarSelectionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameSetupScreen extends JPanel {

    JFrame frame;

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

    // FIXED: Increased card dimensions to fit all content
    private static final int CARD_WIDTH = 420;  // Increased width
    private static final int CARD_HEIGHT = 300;
    private static final int CARD_PAD_TOP = 30;
    private static final int CARD_PAD_SIDE = 35;
    private static final int CARD_PAD_BOTTOM = 25;

    public GameSetupScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 23, 35));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(15, 23, 35));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(createHeaderPanel(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 25, 8, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(createPlayerInfoPanel(), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(15, 25, 8, 25);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(createDifficultyPanel(), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(15, 20, 8, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(createStartButton(), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 15, 20);
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        contentPanel.add(createBackButton(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // -------------------------------------------------------
    // HEADER
    // -------------------------------------------------------

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 35));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel iconLabel = new JLabel("🎮");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(iconLabel, gbc);

        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Configure your MineSweeper battle");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(subtitleLabel, gbc);

        return panel;
    }

    // -------------------------------------------------------
    // PLAYERS PANEL
    // -------------------------------------------------------

    private JPanel createPlayerInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        headerPanel.add(iconLabel);

        JLabel headerLabel = new JLabel("Players Information");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(new Color(96, 165, 250));
        headerPanel.add(headerLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(headerPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 12);
        panel.add(createPlayerPanel(1), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 12, 0, 0);
        panel.add(createPlayerPanel(2), gbc);

        return panel;
    }

    private JPanel createPlayerPanel(int playerNum) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel playerLabel = new JLabel("Player " + playerNum);
        playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        playerLabel.setForeground(new Color(148, 163, 184));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(playerLabel, gbc);

        JButton avatarBtn = new JButton(playerNum == 1 ? player1Avatar : player2Avatar);
        avatarBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        avatarBtn.setPreferredSize(new Dimension(65, 65));
        avatarBtn.setBackground(playerNum == 1 ? new Color(139, 92, 246) : new Color(16, 185, 129));
        avatarBtn.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 2));
        avatarBtn.setFocusPainted(false);
        avatarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (playerNum == 1) {
            player1AvatarBtn = avatarBtn;
            avatarBtn.addActionListener(e -> showAvatarSelector(1));
        } else {
            player2AvatarBtn = avatarBtn;
            avatarBtn.addActionListener(e -> showAvatarSelector(2));
        }

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 0, 12);
        panel.add(avatarBtn, gbc);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(51, 65, 85));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconLabel.setForeground(new Color(148, 163, 184));
        inputPanel.add(iconLabel, BorderLayout.WEST);

        JTextField nameField = new JTextField("Enter name");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setForeground(new Color(148, 163, 184));
        nameField.setBackground(new Color(51, 65, 85));
        nameField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        nameField.setCaretColor(Color.WHITE);

        nameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (nameField.getText().equals("Enter name")) {
                    nameField.setText("");
                    nameField.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("Enter name");
                    nameField.setForeground(new Color(148, 163, 184));
                }
            }
        });

        if (playerNum == 1) {
            player1NameField = nameField;
        } else {
            player2NameField = nameField;
        }

        inputPanel.add(nameField, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(inputPanel, gbc);

        return panel;
    }

    // -------------------------------------------------------
    // DIFFICULTY PANEL
    // -------------------------------------------------------

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 35));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Difficulty Level");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Choose your challenge");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        panel.add(subtitleLabel, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // Easy
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 12);
        easyPanel = createDifficultyCard("Easy", "★",
                new Color(34, 197, 94), 9, 10);
        panel.add(easyPanel, gbc);

        // Medium
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 6, 0, 6);
        mediumPanel = createDifficultyCard("Medium", "★★",
                new Color(251, 191, 36), 13, 8);
        panel.add(mediumPanel, gbc);

        // Hard
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 12, 0, 0);
        hardPanel = createDifficultyCard("Hard", "★★★",
                new Color(239, 68, 68), 16, 6);
        panel.add(hardPanel, gbc);

        updateDifficultySelection();
        return panel;
    }

    private JPanel createDifficultyCard(String name, String stars,
                                        Color color, int gridSize, int lives) {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(23, 37, 63));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Dimension cardSize = new Dimension(CARD_WIDTH, CARD_HEIGHT);
        panel.setPreferredSize(cardSize);
        panel.setMinimumSize(cardSize);
        panel.setMaximumSize(cardSize);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 2),
                BorderFactory.createEmptyBorder(CARD_PAD_TOP, CARD_PAD_SIDE,
                        CARD_PAD_BOTTOM, CARD_PAD_SIDE)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Circle
        JLabel circleLabel = new JLabel("●");
        circleLabel.setFont(new Font("Dialog", Font.PLAIN, 44));
        circleLabel.setForeground(color);
        circleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stars
        JLabel starsLabel = new JLabel(stars);
        starsLabel.setFont(new Font("Dialog", Font.PLAIN, 18));
        starsLabel.setForeground(color);
        starsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(circleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(starsLabel);
        panel.add(Box.createVerticalStrut(20));

        // FIXED: Info box with proper sizing
        JPanel infoBox = new JPanel(new GridLayout(2, 1, 0, 10));
        infoBox.setBackground(new Color(15, 23, 42));
        infoBox.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        infoBox.setOpaque(true);
        infoBox.setMaximumSize(new Dimension(CARD_WIDTH - (CARD_PAD_SIDE * 2), 90));

        // Row 1 – Grid Size
        JPanel gridRow = new JPanel(new BorderLayout(10, 0));
        gridRow.setOpaque(false);

        JLabel gridLabel = new JLabel("Grid Size");
        gridLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gridLabel.setForeground(new Color(191, 197, 210));

        JLabel gridValueLabel = new JLabel(gridSize + "×" + gridSize);
        gridValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gridValueLabel.setForeground(Color.WHITE);

        gridRow.add(gridLabel, BorderLayout.WEST);
        gridRow.add(gridValueLabel, BorderLayout.EAST);

        // Row 2 – Lives
        JPanel livesRow = new JPanel(new BorderLayout(10, 0));
        livesRow.setOpaque(false);

        JPanel livesLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        livesLeft.setOpaque(false);

        JLabel heartIcon = new JLabel("♥");
        heartIcon.setFont(new Font("Dialog", Font.PLAIN, 14));
        heartIcon.setForeground(new Color(248, 113, 113));

        JLabel livesLabel = new JLabel("Lives");
        livesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        livesLabel.setForeground(new Color(191, 197, 210));

        livesLeft.add(heartIcon);
        livesLeft.add(livesLabel);

        JLabel livesValueLabel = new JLabel(String.valueOf(lives));
        livesValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        livesValueLabel.setForeground(Color.WHITE);

        livesRow.add(livesLeft, BorderLayout.WEST);
        livesRow.add(livesValueLabel, BorderLayout.EAST);

        infoBox.add(gridRow);
        infoBox.add(livesRow);

        panel.add(infoBox);
        panel.add(Box.createVerticalStrut(12));

        // FIXED: Selected label with visible styling - using HTML for emoji
        JLabel selectedLabel = new JLabel("<html>&#11088; SELECTED</html>");
        selectedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectedLabel.setForeground(color);
        selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedLabel.setVisible(false);
        panel.add(selectedLabel);
        panel.add(Box.createVerticalStrut(5));

        panel.putClientProperty("selectedLabel", selectedLabel);
        panel.putClientProperty("color", color);
        panel.putClientProperty("difficultyName", name);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDifficulty = name;
                updateDifficultySelection();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selectedDifficulty.equals(name)) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(color.brighter(), 3),
                            BorderFactory.createEmptyBorder(CARD_PAD_TOP - 1, CARD_PAD_SIDE - 1,
                                    CARD_PAD_BOTTOM - 1, CARD_PAD_SIDE - 1)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selectedDifficulty.equals(name)) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(51, 65, 85), 2),
                            BorderFactory.createEmptyBorder(CARD_PAD_TOP, CARD_PAD_SIDE,
                                    CARD_PAD_BOTTOM, CARD_PAD_SIDE)
                    ));
                }
            }
        });

        return panel;
    }

    private void updateDifficultySelection() {
        updateSingleDifficultyCard(easyPanel, "Easy");
        updateSingleDifficultyCard(mediumPanel, "Medium");
        updateSingleDifficultyCard(hardPanel, "Hard");
    }

    private void updateSingleDifficultyCard(JPanel panel, String name) {
        if (panel == null) return;

        JLabel selectedLabel = (JLabel) panel.getClientProperty("selectedLabel");
        Color color = (Color) panel.getClientProperty("color");

        boolean isSelected = selectedDifficulty.equals(name);

        if (isSelected) {
            // FIXED: Thicker border with proper padding adjustment
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 4),
                    BorderFactory.createEmptyBorder(CARD_PAD_TOP - 2, CARD_PAD_SIDE - 2,
                            CARD_PAD_BOTTOM - 2, CARD_PAD_SIDE - 2)
            ));
            if (selectedLabel != null) {
                selectedLabel.setVisible(true);
                selectedLabel.setForeground(color);
            }
        } else {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(51, 65, 85), 2),
                    BorderFactory.createEmptyBorder(CARD_PAD_TOP, CARD_PAD_SIDE,
                            CARD_PAD_BOTTOM, CARD_PAD_SIDE)
            ));
            if (selectedLabel != null) selectedLabel.setVisible(false);
        }
    }

    // -------------------------------------------------------
    // BUTTONS
    // -------------------------------------------------------

    private JButton createStartButton() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonPanel.setBackground(new Color(51, 65, 85));

        JLabel iconLabel = new JLabel("🎮 ");
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        buttonPanel.add(iconLabel);

        JLabel textLabel = new JLabel("Start Game");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        textLabel.setForeground(Color.WHITE);
        buttonPanel.add(textLabel);

        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.add(buttonPanel, BorderLayout.CENTER);
        btn.setPreferredSize(new Dimension(200, 48));
        btn.setBackground(new Color(51, 65, 85));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(71, 85, 105));
                buttonPanel.setBackground(new Color(71, 85, 105));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(51, 65, 85));
                buttonPanel.setBackground(new Color(51, 65, 85));
            }
        });

        btn.addActionListener(e -> {
            String p1 = player1NameField.getText();
            String p2 = player2NameField.getText();
            if (p1.isEmpty() || p1.equals("Enter name") || p2.isEmpty() || p2.equals("Enter name")) {
                JOptionPane.showMessageDialog(this, "Please enter both player names", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
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
            }
        });

        return btn;
    }

    private JButton createBackButton() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(new Color(15, 23, 35));

        JLabel iconLabel = new JLabel("⬅️");
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        buttonPanel.add(iconLabel);

        JLabel textLabel = new JLabel("Back to Menu");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLabel.setForeground(new Color(148, 163, 184));
        buttonPanel.add(textLabel);

        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.add(buttonPanel, BorderLayout.CENTER);
        btn.setBackground(new Color(15, 23, 35));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                textLabel.setForeground(Color.WHITE);
            }

            public void mouseExited(MouseEvent e) {
                textLabel.setForeground(new Color(148, 163, 184));
            }
        });

        btn.addActionListener(e -> {
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        return btn;
    }

    // -------------------------------------------------------
    // AVATAR DIALOG
    // -------------------------------------------------------

    private void showAvatarSelector(int playerNum) {
        AvatarSelectionDialog dialog = new AvatarSelectionDialog(
                frame,
                "Player " + playerNum,
                (selectedIndex) -> {
                    String selectedEmoji = AvatarSelectionDialog.getEmojiByIndex(selectedIndex);
                    if (playerNum == 1) {
                        player1Avatar = selectedEmoji;
                        player1AvatarBtn.setText(selectedEmoji);
                    } else {
                        player2Avatar = selectedEmoji;
                        player2AvatarBtn.setText(selectedEmoji);
                    }
                }
        );
        dialog.setVisible(true);
    }

    // -------------------------------------------------------
    // MAIN (for quick testing)
    // -------------------------------------------------------

    public static void main(String[] args) {
        JFrame frame1 = new JFrame("Minesweeper - Game");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setContentPane(new GameSetupScreen(frame1));
        frame1.setSize(1200, 900);
        frame1.setLocationRelativeTo(null);
        frame1.setVisible(true);
    }
}
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

    // Responsive card dimensions - very compact
    private int cardWidth = 280;
    private int cardHeight = 200;
    private static final int CARD_PAD_TOP = 15;
    private static final int CARD_PAD_SIDE = 20;
    private static final int CARD_PAD_BOTTOM = 10;

    public GameSetupScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 23, 35));

        // Calculate responsive card size
        calculateResponsiveCardSize();

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(15, 23, 35));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(createHeaderPanel(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(createPlayerInfoPanel(), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(createDifficultyPanel(), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        contentPanel.add(createStartButton(), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(3, 15, 5, 15);
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        contentPanel.add(createBackButton(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Add component listener to recalculate sizes on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateResponsiveCardSize();
                revalidate();
                repaint();
            }
        });
    }

    // Calculate card size based on window size
    private void calculateResponsiveCardSize() {
        int windowWidth = getWidth();
        int windowHeight = getHeight();

        if (windowWidth == 0) windowWidth = 1200; // Default
        if (windowHeight == 0) windowHeight = 800; // Default

        // Calculate card width to fit 3 cards with spacing
        int availableWidth = windowWidth - 150; // Reduced margins
        int calculatedWidth = (availableWidth / 3) - 20; // 3 cards with less spacing

        // Calculate height based on available screen height
        int availableHeight = windowHeight - 300; // Space for header, players, buttons
        int calculatedHeight = Math.min((int) (calculatedWidth * 0.75), availableHeight);

       
        cardWidth = Math.max(240, Math.min(320, calculatedWidth));
        cardHeight = Math.max(180, Math.min(240, calculatedHeight));
    }
    
    private void keepFrameBig() {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);     // keep maximized
        frame.setMinimumSize(new Dimension(1000, 700));    // prevent tiny window
        frame.setLocationRelativeTo(null);
    }


    // -------------------------------------------------------
    // HEADER
    // -------------------------------------------------------

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 35));
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel iconLabel = new JLabel("🎮");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 3, 0);
        panel.add(iconLabel, gbc);

        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 3, 0);
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Configure your MineSweeper battle");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("👥");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        headerPanel.add(iconLabel);

        JLabel headerLabel = new JLabel("Players Information");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerLabel.setForeground(new Color(96, 165, 250));
        headerPanel.add(headerLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
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
        playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        playerLabel.setForeground(new Color(148, 163, 184));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(playerLabel, gbc);

        
        JButton avatarBtn = new JButton(playerNum == 1 ? player1Avatar : player2Avatar);

   
        avatarBtn.setPreferredSize(new Dimension(62, 62));
        avatarBtn.setMinimumSize(new Dimension(62, 62));
        avatarBtn.setMaximumSize(new Dimension(62, 62));

        avatarBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        avatarBtn.setBackground(playerNum == 1 ? new Color(139, 92, 246) : new Color(16, 185, 129));
        avatarBtn.setOpaque(true);
        avatarBtn.setContentAreaFilled(true);

        
        avatarBtn.setMargin(new Insets(0, 0, 0, 0));
        avatarBtn.setHorizontalAlignment(SwingConstants.CENTER);
        avatarBtn.setVerticalAlignment(SwingConstants.CENTER);

       
        avatarBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 2),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

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
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel iconLabel = new JLabel("👤");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        iconLabel.setForeground(new Color(148, 163, 184));
        inputPanel.add(iconLabel, BorderLayout.WEST);

        JTextField nameField = new JTextField("Enter name");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 3, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Choose your challenge");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(subtitleLabel, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        // Easy
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        easyPanel = createDifficultyCard("Easy", "★",
                new Color(34, 197, 94), 9, 10);
        panel.add(easyPanel, gbc);

        // Medium
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 0, 4);
        mediumPanel = createDifficultyCard("Medium", "★★",
                new Color(251, 191, 36), 13, 8);
        panel.add(mediumPanel, gbc);

        // Hard
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 8, 0, 0);
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

        // Use responsive card size - very compact
        Dimension cardSize = new Dimension(cardWidth, cardHeight);
        panel.setPreferredSize(cardSize);
        panel.setMinimumSize(new Dimension(240, 180));
        panel.setMaximumSize(new Dimension(320, 240));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(CARD_PAD_TOP, CARD_PAD_SIDE,
                        CARD_PAD_BOTTOM, CARD_PAD_SIDE)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Circle
        JLabel circleLabel = new JLabel("●");
        circleLabel.setFont(new Font("Dialog", Font.PLAIN, 30));
        circleLabel.setForeground(color);
        circleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stars
        JLabel starsLabel = new JLabel(stars);
        starsLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        starsLabel.setForeground(color);
        starsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(circleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(starsLabel);
        panel.add(Box.createVerticalStrut(10));

        // Info box with responsive sizing - very compact
        JPanel infoBox = new JPanel(new GridLayout(2, 1, 0, 6));
        infoBox.setBackground(new Color(15, 23, 42));
        infoBox.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        infoBox.setOpaque(true);
        int infoBoxWidth = cardWidth - (CARD_PAD_SIDE * 2);
        infoBox.setMaximumSize(new Dimension(infoBoxWidth, 60));
        infoBox.setPreferredSize(new Dimension(infoBoxWidth, 55));

        // Row 1 – Grid Size
        JPanel gridRow = new JPanel(new BorderLayout(10, 0));
        gridRow.setOpaque(false);

        JLabel gridLabel = new JLabel("Grid Size");
        gridLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gridLabel.setForeground(new Color(191, 197, 210));

        JLabel gridValueLabel = new JLabel(gridSize + "×" + gridSize);
        gridValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gridValueLabel.setForeground(Color.WHITE);

        gridRow.add(gridLabel, BorderLayout.WEST);
        gridRow.add(gridValueLabel, BorderLayout.EAST);

        // Row 2 – Lives
        JPanel livesRow = new JPanel(new BorderLayout(10, 0));
        livesRow.setOpaque(false);

        JPanel livesLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        livesLeft.setOpaque(false);

        JLabel heartIcon = new JLabel("♥");
        heartIcon.setFont(new Font("Dialog", Font.PLAIN, 11));
        heartIcon.setForeground(new Color(248, 113, 113));

        JLabel livesLabel = new JLabel("Lives");
        livesLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        livesLabel.setForeground(new Color(191, 197, 210));

        livesLeft.add(heartIcon);
        livesLeft.add(livesLabel);

        JLabel livesValueLabel = new JLabel(String.valueOf(lives));
        livesValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        livesValueLabel.setForeground(Color.WHITE);

        livesRow.add(livesLeft, BorderLayout.WEST);
        livesRow.add(livesValueLabel, BorderLayout.EAST);

        infoBox.add(gridRow);
        infoBox.add(livesRow);

        panel.add(infoBox);
        panel.add(Box.createVerticalStrut(5));

        // Selected label
        JLabel selectedLabel = new JLabel("<html>&#11088; SELECTED</html>");
        selectedLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        selectedLabel.setForeground(color);
        selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedLabel.setVisible(false);
        panel.add(selectedLabel);
        panel.add(Box.createVerticalStrut(2));

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
                            BorderFactory.createLineBorder(color.brighter(), 2),
                            BorderFactory.createEmptyBorder(CARD_PAD_TOP - 1, CARD_PAD_SIDE - 1,
                                    CARD_PAD_BOTTOM - 1, CARD_PAD_SIDE - 1)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selectedDifficulty.equals(name)) {
                    panel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
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
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 3),
                    BorderFactory.createEmptyBorder(CARD_PAD_TOP - 2, CARD_PAD_SIDE - 2,
                            CARD_PAD_BOTTOM - 2, CARD_PAD_SIDE - 2)
            ));
            if (selectedLabel != null) {
                selectedLabel.setVisible(true);
                selectedLabel.setForeground(color);
            }
        } else {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
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
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        buttonPanel.add(iconLabel);

        JLabel textLabel = new JLabel("Start Game");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        textLabel.setForeground(Color.WHITE);
        buttonPanel.add(textLabel);

        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.add(buttonPanel, BorderLayout.CENTER);
        btn.setPreferredSize(new Dimension(180, 42));
        btn.setBackground(new Color(51, 65, 85));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
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
                return;
            }

            // ✅ SAVE frame size + state before switching screens
            final int oldState = frame.getExtendedState();
            final Dimension oldSize = frame.getSize();
            final boolean wasMaximized = (oldState & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;

            btn.setEnabled(false);

            new SwingWorker<GameScreenMultiPlayer, Void>() {
                @Override
                protected GameScreenMultiPlayer doInBackground() {
                    GameSetupController setupController = new GameSetupController(SysData.getInstance());
                    setupController.setDifficulty(selectedDifficulty);
                    setupController.createPlayers(p1, player1Avatar, p2, player2Avatar);
                    GameSetupController.GameConfig config = setupController.initializeGame();

                    MultiPlayerGameController gameController = new MultiPlayerGameController(
                            config.sysData, config.player1, config.player2,
                            config.difficulty, config.gridSize
                    );

                    return new GameScreenMultiPlayer(frame, gameController);
                }

                @Override
                protected void done() {
                    try {
                        GameScreenMultiPlayer screen = get();

                        frame.setContentPane(screen);

                        // ✅ RESTORE size/state AFTER setContentPane
                        if (wasMaximized) {
                            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        } else {
                            frame.setExtendedState(oldState);
                            frame.setSize(oldSize);
                        }

                        frame.revalidate();
                        frame.repaint();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame,
                                "Failed to start game: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btn.setEnabled(true);
                    }
                }
            }.execute();
        });

        return btn;
    }

    
    private void keepFrameSize() {
        // keep maximized if it was maximized
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            return;
        }

        // otherwise keep current size (don’t shrink)
        int w = frame.getWidth();
        int h = frame.getHeight();
        frame.setSize(w, h);
    }


    private JButton createBackButton() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(new Color(15, 23, 35));

        JLabel iconLabel = new JLabel("⬅️");
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        buttonPanel.add(iconLabel);

        JLabel textLabel = new JLabel("Back to Menu");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
            int state = frame.getExtendedState();
            int w = frame.getWidth();
            int h = frame.getHeight();

            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));

            // restore state/size AFTER swapping content
            frame.setExtendedState(state);
            if ((state & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                frame.setSize(w, h);
            }

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
}

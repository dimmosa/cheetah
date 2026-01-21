package view;

import control.SinglePlayerGameControl;
import model.SysData;
import model.User;
import model.SessionManager;
import view.CustomIconButton;
import view.HelpDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PrivateGameScreen extends JPanel {

    JFrame frame;

    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private String selectedDifficulty = "Easy";

    private final User user;

    public PrivateGameScreen(JFrame frame) {
        this.frame = frame;

        this.user=SessionManager.getInstance().getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 25));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("<html><font color='#00BFFF'>SINGLE</font> <font color='#87CEFA'>PLAYER</font></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setOpaque(false);
        JButton homeButton = createIconButton("⌂", new Color(100, 100, 200));
        JButton helpButton = createIconButton("?", new Color(100, 100, 200));
        navPanel.add(helpButton);
        navPanel.add(homeButton);
        headerPanel.add(navPanel, BorderLayout.EAST);

        homeButton.addActionListener(e -> {
            frame.setContentPane(new MainMenuPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        helpButton.addActionListener(e -> {
            HelpDialog helpDialog = new HelpDialog(frame);
            helpDialog.setVisible(true);
        });
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 10, 20, 10);
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        centerPanel.add(createProfilePanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        centerPanel.add(createDifficultyPanel(), gbc);
        
        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        footerPanel.setOpaque(false);
        
        JButton startButton = createNeonButton("START GAME", new Color(0, 200, 200), 200, 60);

        startButton.addActionListener(e -> {

            SysData sysData = new SysData();

            SinglePlayerGameControl gameController = new SinglePlayerGameControl(user, selectedDifficulty, sysData);

            MinesweeperBoardPanel boardPanel = gameController.createBoardPanel();
            GameScreenSinglePlayer gameScreen = new GameScreenSinglePlayer(frame, gameController, boardPanel);

            frame.setContentPane(gameScreen);
            frame.revalidate();
            frame.repaint();

            // JOptionPane.showMessageDialog(this, "Starting game with difficulty: " + selectedDifficulty);
        });

        footerPanel.add(startButton);
        add(footerPanel, BorderLayout.SOUTH);

        updateDifficultySelection();
    }
    

    private JPanel createProfilePanel() {
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
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profileHeader.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 40));
        avatarLabel.setForeground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        nameLabel.setForeground(new Color(255, 255, 255));
        
        profileHeader.add(avatarLabel);
        profileHeader.add(nameLabel);
        
        panel.add(profileHeader, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 10, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        statsPanel.add(createStatLabel("Games Won:", String.valueOf(user.getGamesWon()), new Color(0, 255, 127)));
        statsPanel.add(createStatLabel("Total Games:", String.valueOf(user.getGamesPlayed()), new Color(135, 206, 250)));
        statsPanel.add(createStatLabel("Total Points:", String.valueOf(user.getHighScore()), new Color(255, 215, 0)));
//        statsPanel.add(createStatLabel("Current Streak:", "", new Color(255, 100, 100)));
//        statsPanel.add(createStatLabel("Best Time (E):", "", new Color(152, 251, 152)));
//        statsPanel.add(createStatLabel("Best Time (H):", "", new Color(255, 165, 0)));

        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatLabel(String title, String value, Color valueColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(180, 180, 200));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(valueColor);
        valueLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        p.add(titleLabel);
        p.add(valueLabel);
        return p;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel diffTitle = new JLabel("Choose Difficulty");
        diffTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        diffTitle.setForeground(new Color(135, 206, 250));
        diffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(diffTitle);
        panel.add(Box.createVerticalStrut(30));

        JPanel buttonContainer = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonContainer.setOpaque(false);
        buttonContainer.setMaximumSize(new Dimension(600, 250));

        easyButton = createDifficultyButton("EASY", "9x9 Grid | 10 Mines", "Easy", new Color(0, 180, 0));
        mediumButton = createDifficultyButton("MEDIUM", "13x13 Grid | 26 Mines", "Medium", new Color(255, 165, 0));
        hardButton = createDifficultyButton("HARD", "16x16 Grid | 44 Mines", "Hard", new Color(255, 69, 0));

        buttonContainer.add(easyButton);
        buttonContainer.add(mediumButton);
        buttonContainer.add(hardButton);

        panel.add(buttonContainer);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton createDifficultyButton(String title, String desc, String difficulty, Color baseColor) {
        JButton button = new JButton() {
            private final Color hoverColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50);
            private final Color selectedBorderColor = new Color(255, 255, 255);
            private final Color defaultBorderColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 100);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(25, 25, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                if (getModel().isRollover() || selectedDifficulty.equals(difficulty)) {
                    g2.setColor(hoverColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                
                g2.setStroke(new BasicStroke(2f));
                if (selectedDifficulty.equals(difficulty)) {
                    g2.setColor(selectedBorderColor);
                } else {
                    g2.setColor(defaultBorderColor);
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g);
            }
        };

        button.setLayout(new BorderLayout());
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        JLabel descLabel = new JLabel(desc, SwingConstants.LEFT);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(180, 180, 180));
        
        button.add(titleLabel, BorderLayout.NORTH);
        button.add(descLabel, BorderLayout.CENTER);
        
        button.putClientProperty("difficulty", difficulty);
        
        button.addActionListener(e -> {
            selectedDifficulty = difficulty;
            if (difficulty.equals("Custom")) {
                 JOptionPane.showMessageDialog(this, "Custom difficulty setup not implemented yet.", "Custom Game", JOptionPane.INFORMATION_MESSAGE);
            }
            updateDifficultySelection();
        });
        
        return button;
    }
    
    private void updateDifficultySelection() {
        easyButton.repaint();
        mediumButton.repaint();
        hardButton.repaint();
    }

    private JButton createNeonButton(String text, Color color, int width, int height) {
       return CustomIconButton.createNeonButton(text, color, width, height);
    }
    
    private JButton createIconButton(String text, Color color) {
        return CustomIconButton.createIconButton(text,color);
    }
}
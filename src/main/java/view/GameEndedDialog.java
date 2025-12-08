package view;

import com.formdev.flatlaf.FlatClientProperties;

import view.MainMenuTwoPlayerScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameEndedDialog extends JDialog {

    String mode;

    public GameEndedDialog(JFrame owner, String playerNames, int score, String time, String difficulty, int livesLeft, int totalLives, String mode) {
        super(owner, "Game Ended", true);
        this.mode=mode;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(550, 720);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(5, 15, 30), getWidth(), getHeight(), new Color(10, 25, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(255, 255, 255, 10));
                for(int i=0; i<50; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    g2.fillRect(x, y, 2, 2);
                }
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 40, 55, 230));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");
        card.setPreferredSize(new Dimension(450, 600));

        JLabel headerIcon = new JLabel("🎉");
        headerIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        headerIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Game Ended");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(livesLeft>0 ?"All cells opened successfully!":"You Lost No lives left");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(150, 160, 190));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        statsPanel.add(createStatRow("👥", "Players", playerNames, Color.WHITE));
        statsPanel.add(createSeparator());

        statsPanel.add(createStatRow("🏆", "Total Score", score + " pts", new Color(255, 180, 0)));
        statsPanel.add(createSeparator());

        statsPanel.add(createStatRow("🕒", "Total Time", time, Color.WHITE));
        statsPanel.add(createSeparator());

        statsPanel.add(createStatRow("⭐", "Difficulty", difficulty, new Color(255, 160, 0)));
        statsPanel.add(createSeparator());

        statsPanel.add(createStatRow("❤️", "Lives Left", livesLeft + " / " + totalLives, new Color(255, 100, 100)));
        statsPanel.add(createSeparator());

        JButton playAgainBtn = new JButton("Play Again") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 120, 255), getWidth(), 0, new Color(0, 180, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        playAgainBtn.setForeground(Color.WHITE);
        playAgainBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        playAgainBtn.setContentAreaFilled(false);
        playAgainBtn.setBorderPainted(false);
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playAgainBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        playAgainBtn.addActionListener(e -> {

            dispose();
        });

        JButton mainMenuBtn = new JButton("Main Menu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 70, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainMenuBtn.setForeground(Color.WHITE);
        mainMenuBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        mainMenuBtn.setContentAreaFilled(false);
        mainMenuBtn.setBorderPainted(false);
        mainMenuBtn.setFocusPainted(false);
        mainMenuBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainMenuBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        mainMenuBtn.addActionListener(e -> {

            dispose();

        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playAgainBtn);
        buttonPanel.add(mainMenuBtn);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        card.add(headerIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(10));
        card.add(statsPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(buttonPanel);

        mainPanel.add(card);
    }

    private JComponent createSeparator() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setBackground(new Color(0,0,0,0));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        container.add(sep);

        container.add(Box.createVerticalStrut(8));

        return container;
    }

    private JPanel createStatRow(String icon, String labelText, String valueText, Color valueColor) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(new Color(150, 160, 190));
        gbc.gridx = 0;
        gbc.weightx = 0;
        row.add(iconLabel, gbc);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(200, 210, 230));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        row.add(label, gbc);

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("SansSerif", Font.BOLD, 16));
        value.setForeground(valueColor);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(value, gbc);

        return row;
    }
}
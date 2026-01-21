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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrivateGameScreen extends JPanel {

    JFrame frame;

    private JButton easyButton;
    private JButton mediumButton;
    private JButton hardButton;
    private String selectedDifficulty = "Easy";

    private final User user;
    
    // Animation fields
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private Random random;
    private float waveOffset = 0;

    public PrivateGameScreen(JFrame frame) {
        this.frame = frame;
        this.user = SessionManager.getInstance().getCurrentUser();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Initialize animation
        initializeAnimation();

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 10, 20, 10);
        
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        centerPanel.add(createProfilePanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        centerPanel.add(createDifficultyPanel(), gbc);
        
        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        footerPanel.setOpaque(false);
        
        JButton startButton = createNeonButton("🚀 START GAME", new Color(0, 200, 200), 250, 65);
     
        if ("Hard".equalsIgnoreCase(selectedDifficulty)) {
            if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                frame.setSize(Math.max(frame.getWidth(), 1400), Math.max(frame.getHeight(), 850));
                frame.setLocationRelativeTo(null);
            }
        }

        startButton.addActionListener(e -> {
            stopAnimation();
            SysData sysData = new SysData();
            SinglePlayerGameControl gameController = new SinglePlayerGameControl(user, selectedDifficulty, sysData);
            MinesweeperBoardPanel boardPanel = gameController.createBoardPanel();
            GameScreenSinglePlayer gameScreen = new GameScreenSinglePlayer(frame, gameController, boardPanel);
            frame.setContentPane(gameScreen);
            frame.revalidate();
            frame.repaint();
        });

        footerPanel.add(startButton);
        add(footerPanel, BorderLayout.SOUTH);

        updateDifficultySelection();
    }
    
    private void initializeAnimation() {
        random = new Random();
        particles = new ArrayList<>();
        
        for (int i = 0; i < 40; i++) {
            particles.add(new AnimatedParticle(random));
        }

        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (AnimatedParticle p : particles) {
                    p.update();
                }
                waveOffset += 0.04f;
                repaint();
            }
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private class AnimatedParticle {
        float x, y;
        float vx, vy;
        float size;
        float alpha;
        float pulseSpeed;
        float pulseOffset;
        Color color;

        AnimatedParticle(Random r) {
            x = r.nextFloat() * 1920;
            y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 0.8f;
            vy = (r.nextFloat() - 0.5f) * 0.8f;
            size = r.nextFloat() * 6 + 2;
            alpha = r.nextFloat() * 0.4f + 0.2f;
            pulseSpeed = r.nextFloat() * 0.04f + 0.02f;
            pulseOffset = r.nextFloat() * (float)Math.PI * 2;
            
            int colorChoice = r.nextInt(4);
            if (colorChoice == 0) {
                color = new Color(0, 191, 255);
            } else if (colorChoice == 1) {
                color = new Color(135, 206, 250);
            } else if (colorChoice == 2) {
                color = new Color(100, 149, 237);
            } else {
                color = new Color(0, 206, 209);
            }
        }

        void update() {
            x += vx;
            y += vy;

            if (x < 0) x = 1920;
            if (x > 1920) x = 0;
            if (y < 0) y = 1080;
            if (y > 1080) y = 0;
            
            pulseOffset += pulseSpeed;
        }

        void draw(Graphics2D g2, int panelWidth, int panelHeight) {
            float scaledX = (x / 1920f) * panelWidth;
            float scaledY = (y / 1080f) * panelHeight;

            float pulse = (float)Math.sin(pulseOffset) * 0.3f + 0.7f;
            float currentAlpha = alpha * pulse;
            float currentSize = size * pulse;

            for (int i = 2; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.15f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(glowAlpha * 255)));
                g2.fillOval((int)(scaledX - i*2), (int)(scaledY - i*2), (int)(currentSize + i*4), (int)(currentSize + i*4));
            }

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(currentAlpha * 255)));
            g2.fillOval((int)scaledX, (int)scaledY, (int)currentSize, (int)currentSize);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Animated title with gradient
        JLabel titleLabel = new JLabel("<html><font color='#00BFFF'>⚡ SINGLE</font> <font color='#87CEFA'>PLAYER</font></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle glow behind text
                float pulse = (float)Math.sin(waveOffset) * 0.3f + 0.7f;
                g2.setColor(new Color(0, 191, 255, (int)(30 * pulse)));
                g2.fillRoundRect(-10, -5, getWidth() + 20, getHeight() + 10, 15, 15);
                
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setOpaque(false);
        
        JButton helpButton = createIconButton("?", new Color(100, 149, 237));
        JButton homeButton = createIconButton("⌂", new Color(72, 118, 255));
        
        navPanel.add(helpButton);
        navPanel.add(homeButton);
        headerPanel.add(navPanel, BorderLayout.EAST);

        homeButton.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new MainMenuPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        helpButton.addActionListener(e -> {
            HelpDialog helpDialog = new HelpDialog(frame);
            helpDialog.setVisible(true);
        });
        
        return headerPanel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Animated gradient background
                float pulse = (float)Math.sin(waveOffset * 0.7) * 0.2f + 0.8f;
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(25, 25, 50, (int)(220 * pulse)),
                    getWidth(), getHeight(), new Color(40, 20, 60, (int)(200 * pulse))
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Glowing border
                g2.setColor(new Color(138, 43, 226, (int)(180 * pulse)));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
                
                // Inner glow
                g2.setColor(new Color(186, 85, 211, (int)(60 * pulse)));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 28, 28);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        profileHeader.setOpaque(false);
        
        // Avatar with glow
        JPanel avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                float pulse = (float)Math.sin(waveOffset * 1.5) * 0.3f + 0.7f;
                RadialGradientPaint rgp = new RadialGradientPaint(
                    getWidth()/2f, getHeight()/2f, 30f,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{
                        new Color(138, 43, 226, (int)(100 * pulse)),
                        new Color(138, 43, 226, (int)(50 * pulse)),
                        new Color(0, 0, 0, 0)
                    }
                );
                g2.setPaint(rgp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        avatarContainer.setOpaque(false);
        avatarContainer.setPreferredSize(new Dimension(60, 60));
        
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 40));
        avatarLabel.setForeground(Color.WHITE);
        avatarContainer.add(avatarLabel);
        
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        nameLabel.setForeground(new Color(255, 255, 255));
        
        profileHeader.add(avatarContainer);
        profileHeader.add(nameLabel);
        
        panel.add(profileHeader, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 10, 25));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(35, 10, 10, 10));

        statsPanel.add(createStatCard("🏆 Games Won", String.valueOf(user.getGamesWon()), 
            new Color(0, 255, 127), new Color(0, 200, 100)));
        statsPanel.add(createStatCard("🎮 Total Games", String.valueOf(user.getGamesPlayed()), 
            new Color(135, 206, 250), new Color(70, 130, 180)));
        statsPanel.add(createStatCard("⭐ Total Points", String.valueOf(user.getHighScore()), 
            new Color(255, 215, 0), new Color(218, 165, 32)));

        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color valueColor, Color accentColor) {
        JPanel card = new JPanel() {
            private float hoverProgress = 0f;
            private Timer hoverTimer = null;
            
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        animateHover(true);
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        animateHover(false);
                    }
                });
            }
            
            private void animateHover(boolean entering) {
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }
                
                hoverTimer = new Timer(20, null);
                hoverTimer.addActionListener(e -> {
                    if (entering) {
                        hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                    } else {
                        hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                    }
                    repaint();
                    if ((entering && hoverProgress >= 1f) || (!entering && hoverProgress <= 0f)) {
                        hoverTimer.stop();
                    }
                });
                hoverTimer.start();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(new Color(30, 30, 50, 180 + (int)(40 * hoverProgress)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Animated left accent bar
                int barWidth = 4 + (int)(2 * hoverProgress);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, barWidth, getHeight(), 15, 15);
                
                // Glow on hover
                if (hoverProgress > 0) {
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), 
                        accentColor.getBlue(), (int)(50 * hoverProgress)));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                }
                
                super.paintComponent(g);
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(new EmptyBorder(12, 18, 12, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(180, 180, 200));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Title with icon
        JLabel diffTitle = new JLabel("🎯 Choose Difficulty") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                float pulse = (float)Math.sin(waveOffset * 0.8) * 0.2f + 0.8f;
                g2.setColor(new Color(135, 206, 250, (int)(30 * pulse)));
                g2.fillRoundRect(-15, -8, getWidth() + 30, getHeight() + 16, 20, 20);
                
                super.paintComponent(g);
            }
        };
        diffTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        diffTitle.setForeground(new Color(135, 206, 250));
        diffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffTitle.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        panel.add(diffTitle);
        panel.add(Box.createVerticalStrut(35));

        JPanel buttonContainer = new JPanel(new GridLayout(2, 2, 25, 25));
        buttonContainer.setOpaque(false);
        buttonContainer.setMaximumSize(new Dimension(700, 280));

        easyButton = createDifficultyButton("🌱 EASY", "9x9 Grid | 10 Mines", "Easy", 
            new Color(0, 200, 83), new Color(0, 255, 127));
        mediumButton = createDifficultyButton("⚡ MEDIUM", "13x13 Grid | 26 Mines", "Medium", 
            new Color(255, 140, 0), new Color(255, 165, 0));
        hardButton = createDifficultyButton("🔥 HARD", "16x16 Grid | 44 Mines", "Hard", 
            new Color(220, 20, 60), new Color(255, 69, 0));

        buttonContainer.add(easyButton);
        buttonContainer.add(mediumButton);
        buttonContainer.add(hardButton);

        panel.add(buttonContainer);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton createDifficultyButton(String title, String desc, String difficulty, 
                                          Color baseColor, Color glowColor) {
        JButton button = new JButton() {
            private float hoverProgress = 0f;
            private float selectedProgress = selectedDifficulty.equals(difficulty) ? 1f : 0f;
            private Timer hoverTimer = null;
            private Timer selectTimer = null;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!selectedDifficulty.equals(difficulty)) {
                            animateHover(true);
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!selectedDifficulty.equals(difficulty)) {
                            animateHover(false);
                        }
                    }
                });
            }
            
            private void animateHover(boolean entering) {
                if (hoverTimer != null && hoverTimer.isRunning()) {
                    hoverTimer.stop();
                }
                
                hoverTimer = new Timer(20, null);
                hoverTimer.addActionListener(e -> {
                    if (entering) {
                        hoverProgress = Math.min(0.5f, hoverProgress + 0.05f);
                    } else {
                        hoverProgress = Math.max(0f, hoverProgress - 0.05f);
                    }
                    repaint();
                    if ((entering && hoverProgress >= 0.5f) || (!entering && hoverProgress <= 0f)) {
                        hoverTimer.stop();
                    }
                });
                hoverTimer.start();
            }
            
            public void animateSelection(boolean isSelected) {
                if (selectTimer != null && selectTimer.isRunning()) {
                    selectTimer.stop();
                }
                
                selectTimer = new Timer(20, null);
                selectTimer.addActionListener(e -> {
                    if (isSelected) {
                        selectedProgress = Math.min(1f, selectedProgress + 0.1f);
                    } else {
                        selectedProgress = Math.max(0f, selectedProgress - 0.1f);
                    }
                    repaint();
                    if ((isSelected && selectedProgress >= 1f) || (!isSelected && selectedProgress <= 0f)) {
                        selectTimer.stop();
                    }
                });
                selectTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark base
                g2.setColor(new Color(20, 25, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Color overlay
                float totalProgress = Math.max(hoverProgress, selectedProgress);
                if (totalProgress > 0) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), 
                        baseColor.getBlue(), (int)(60 * totalProgress)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                
                // Border
                g2.setStroke(new BasicStroke(2.5f));
                if (selectedProgress > 0) {
                    // Animated gradient border for selected
                    float pulse = (float)Math.sin(waveOffset) * 0.3f + 0.7f;
                    int alpha = (int)(255 * selectedProgress * pulse);
                    g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), 
                        glowColor.getBlue(), alpha));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                    
                    // Inner glow
                    g2.setColor(new Color(255, 255, 255, (int)(100 * selectedProgress * pulse)));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 18, 18);
                } else {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), 
                        baseColor.getBlue(), 100));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                }

                super.paintComponent(g);
            }
        };

        button.setLayout(new BorderLayout(8, 8));
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(18, 20, 18, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        JLabel descLabel = new JLabel(desc, SwingConstants.LEFT);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(160, 160, 180));
        
        button.add(titleLabel, BorderLayout.NORTH);
        button.add(descLabel, BorderLayout.CENTER);
        
        button.putClientProperty("difficulty", difficulty);
        
        button.addActionListener(e -> {
            selectedDifficulty = difficulty;
            if (difficulty.equals("Custom")) {
                JOptionPane.showMessageDialog(this, "Custom difficulty setup not implemented yet.", 
                    "Custom Game", JOptionPane.INFORMATION_MESSAGE);
            }
            updateDifficultySelection();
        });
        
        return button;
    }
    
    private void updateDifficultySelection() {
        // Animate selection state for each button
        java.lang.reflect.Method animateMethod;
        try {
            animateMethod = easyButton.getClass().getDeclaredMethod("animateSelection", boolean.class);
            animateMethod.invoke(easyButton, selectedDifficulty.equals("Easy"));
            animateMethod.invoke(mediumButton, selectedDifficulty.equals("Medium"));
            animateMethod.invoke(hardButton, selectedDifficulty.equals("Hard"));
        } catch (Exception e) {
            // Fallback to simple repaint
            easyButton.repaint();
            mediumButton.repaint();
            hardButton.repaint();
        }
    }

    private JButton createNeonButton(String text, Color color, int width, int height) {
        return CustomIconButton.createNeonButton(text, color, width, height);
    }
    
    private JButton createIconButton(String text, Color color) {
        return CustomIconButton.createIconButton(text, color);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Animated gradient background
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(10, 10, 20), 
            getWidth(), getHeight(), new Color(20, 15, 30)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw animated particles
        for (AnimatedParticle p : particles) {
            p.draw(g2, getWidth(), getHeight());
        }
    }
}
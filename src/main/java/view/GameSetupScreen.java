package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.GameSetupController;
import control.MultiPlayerGameController;
import model.SysData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSetupScreen extends JPanel {

    private final JFrame frame;
    private JTextField player1NameField;
    private JTextField player2NameField;
    private JButton player1AvatarBtn;
    private JButton player2AvatarBtn;
    private JPanel easyPanel, mediumPanel, hardPanel;

    private String selectedDifficulty = "Easy";
    private String player1Avatar = "👻";
    private String player2Avatar = "🐉";

    // card fade-in
    private float opacity = 0f;

    // ✅ Private-style animated background
    private Timer animationTimer;
    private final List<AnimatedParticle> particles = new ArrayList<>();
    private final List<FloatingIcon> floatingIcons = new ArrayList<>();
    private final Random random = new Random();
    private float waveOffset = 0f;

    // ✅ Responsive limits
    private static final int MIN_CARD_W = 820;
    private static final int MIN_CARD_H = 640;

    private static final int MAX_CARD_W = 1100;
    private static final int MAX_CARD_H = 780;

    private static final int MARGIN = 26;

    private final JPanel mainCard;

    public GameSetupScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ✅ init animated background (particles + floating icons)
        initializeBackground();

        // --- main responsive card ---
        mainCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

                g2.setColor(new Color(15, 23, 42, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                // subtle highlight border
                g2.setColor(new Color(255, 255, 255, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setOpaque(false);
        mainCard.setBorder(new EmptyBorder(45, 80, 45, 80));

        // --- Header Section ---
        JLabel iconHeader = new JLabel("🎮");
        iconHeader.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 45));
        iconHeader.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 38));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Configure your MineSweeper battle");
        subTitle.setFont(new Font("Inter", Font.PLAIN, 15));
        subTitle.setForeground(new Color(148, 163, 184));
        subTitle.setAlignmentX(CENTER_ALIGNMENT);

        // --- Players (2 columns) ---
        JPanel playersSection = new JPanel(new GridLayout(1, 2, 40, 0));
        playersSection.setOpaque(false);
        playersSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        playersSection.add(createPlayerBox(1));
        playersSection.add(createPlayerBox(2));

        // --- Difficulty ---
        JLabel diffTitle = new JLabel("Difficulty Level");
        diffTitle.setFont(new Font("Inter", Font.BOLD, 24));
        diffTitle.setForeground(Color.WHITE);
        diffTitle.setAlignmentX(CENTER_ALIGNMENT);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        easyPanel = createDifficultyCard("Easy", "9×9", 10, new Color(52, 211, 153));
        mediumPanel = createDifficultyCard("Medium", "13×13", 8, new Color(251, 191, 36));
        hardPanel = createDifficultyCard("Hard", "16×16", 6, new Color(248, 113, 113));

        cardsPanel.add(easyPanel);
        cardsPanel.add(mediumPanel);
        cardsPanel.add(hardPanel);

        // --- Buttons ---
        JButton btnStart = createStyledButton("Start Game", new Color(37, 99, 235));
        JButton btnBack = createStyledButton("Back to Menu", new Color(255, 255, 255, 10));
        btnBack.setFont(new Font("Inter", Font.PLAIN, 14));
        btnBack.setPreferredSize(new Dimension(350, 45));
        btnBack.setMaximumSize(new Dimension(350, 45));

        setupLogic(btnStart, btnBack);

        // Compose card
        mainCard.add(iconHeader);
        mainCard.add(Box.createVerticalStrut(10));
        mainCard.add(titleLabel);
        mainCard.add(subTitle);

        mainCard.add(Box.createVerticalStrut(35));
        mainCard.add(playersSection);

        mainCard.add(Box.createVerticalStrut(40));
        mainCard.add(diffTitle);
        mainCard.add(Box.createVerticalStrut(20));
        mainCard.add(cardsPanel);

        // push buttons down a bit (stable)
        mainCard.add(Box.createVerticalStrut(55));

        mainCard.add(btnStart);
        mainCard.add(Box.createVerticalStrut(12));
        mainCard.add(btnBack);

        mainCard.add(Box.createVerticalGlue());

        // Add card to screen (we size it ourselves in doLayout)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(mainCard, gbc);
    }

    // ✅ Responsive sizing – key
    @Override
    public void doLayout() {
        super.doLayout();

        int w = getWidth();
        int h = getHeight();

        int availW = Math.max(0, w - (MARGIN * 2));
        int availH = Math.max(0, h - (MARGIN * 2));

        int cardW = clamp(availW, MIN_CARD_W, MAX_CARD_W);
        int cardH = clamp(availH, MIN_CARD_H, MAX_CARD_H);

        int x = (w - cardW) / 2;
        int y = (h - cardH) / 2;

        mainCard.setBounds(x, y, cardW, cardH);
        mainCard.revalidate();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private JPanel createPlayerBox(int playerNum) {
        JPanel container = new JPanel(new BorderLayout(15, 10));
        container.setOpaque(false);

        JLabel title = new JLabel("Player " + playerNum);
        title.setForeground(new Color(148, 163, 184));
        title.setFont(new Font("Inter", Font.BOLD, 12));

        JButton avatarBtn = new JButton(playerNum == 1 ? player1Avatar : player2Avatar);
        avatarBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        avatarBtn.setPreferredSize(new Dimension(75, 75));
        avatarBtn.setBackground(new Color(30, 41, 59));
        avatarBtn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 1; borderColor: #ffffff15;");
        avatarBtn.addActionListener(e -> showAvatarSelector(playerNum));

        if (playerNum == 1) player1AvatarBtn = avatarBtn;
        else player2AvatarBtn = avatarBtn;

        JTextField nameField = new JTextField("Enter name");
        nameField.setForeground(new Color(100, 116, 139));
        nameField.putClientProperty(FlatClientProperties.STYLE,
                "arc: 15; background: #0f172a; foreground: #ffffff; margin: 5,15,5,15;");
        nameField.setFont(new Font("Inter", Font.PLAIN, 16));

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
                    nameField.setForeground(new Color(100, 116, 139));
                }
            }
        });

        if (playerNum == 1) player1NameField = nameField;
        else player2NameField = nameField;

        container.add(title, BorderLayout.NORTH);
        container.add(avatarBtn, BorderLayout.WEST);
        container.add(nameField, BorderLayout.CENTER);
        return container;
    }

    private JPanel createDifficultyCard(String name, String size, int lives, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isSel = selectedDifficulty.equals(name);

                g2.setColor(isSel
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35)
                        : new Color(30, 41, 59, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (isSel) {
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Dialog", Font.BOLD, 22));
        card.add(dot, c);

        c.gridy = 1;
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Inter", Font.BOLD, 19));
        nameLbl.setForeground(Color.WHITE);
        card.add(nameLbl, c);

        c.gridy = 2;
        JLabel stats = new JLabel("<html><center>Grid: " + size + "<br>❤️ Lives: " + lives + "</center></html>");
        stats.setFont(new Font("Inter", Font.PLAIN, 13));
        stats.setForeground(new Color(148, 163, 184));
        card.add(stats, c);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedDifficulty = name;
                repaint();
            }
        });

        return card;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(350, 50));
        btn.setMaximumSize(new Dimension(350, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 0; focusWidth: 0;");
        return btn;
    }

    private void setupLogic(JButton btnStart, JButton btnBack) {
        btnStart.addActionListener(e -> {
            String p1 = player1NameField.getText();
            String p2 = player2NameField.getText();

            if (p1.isEmpty() || p1.equals("Enter name") || p2.isEmpty() || p2.equals("Enter name")) {
                JOptionPane.showMessageDialog(this, "Please enter both player names", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

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
                        frame.setContentPane(get());
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnBack.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });
    }

    private void showAvatarSelector(int playerNum) {
        AvatarSelectionDialog dialog = new AvatarSelectionDialog(frame, "Player " + playerNum, (selectedIndex) -> {
            String selectedEmoji = AvatarSelectionDialog.getEmojiByIndex(selectedIndex);
            if (playerNum == 1) {
                player1Avatar = selectedEmoji;
                player1AvatarBtn.setText(selectedEmoji);
            } else {
                player2Avatar = selectedEmoji;
                player2AvatarBtn.setText(selectedEmoji);
            }
        });
        dialog.setVisible(true);
    }

    // ✅ Private-style background init
    private void initializeBackground() {
        // particles
        for (int i = 0; i < 60; i++) {
            particles.add(new AnimatedParticle(random));
        }

        // floating icons
        String[] icons = {"💣", "🚩", "💎", "⭐", "🎯"};
        for (int i = 0; i < 8; i++) {
            floatingIcons.add(new FloatingIcon(random, icons[random.nextInt(icons.length)]));
        }

        animationTimer = new Timer(30, e -> {
            if (opacity < 1f) {
                opacity = Math.min(1f, opacity + 0.05f);
            }

            for (AnimatedParticle p : particles) p.update();
            for (FloatingIcon ic : floatingIcons) ic.update();

            waveOffset += 0.05f;
            repaint();
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) animationTimer.stop();
    }

    // ✅ Animated particle class
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
            vx = (r.nextFloat() - 0.5f) * 1.2f;
            vy = (r.nextFloat() - 0.5f) * 1.2f;
            size = r.nextFloat() * 8 + 2;
            alpha = r.nextFloat() * 0.5f + 0.3f;
            pulseSpeed = r.nextFloat() * 0.05f + 0.02f;
            pulseOffset = r.nextFloat() * (float) Math.PI * 2;

            int colorChoice = r.nextInt(5);
            if (colorChoice == 0) color = new Color(0, 180, 255);
            else if (colorChoice == 1) color = new Color(0, 220, 100);
            else if (colorChoice == 2) color = new Color(100, 150, 255);
            else if (colorChoice == 3) color = new Color(0, 255, 200);
            else color = new Color(50, 200, 255);
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

            float pulse = (float) Math.sin(pulseOffset) * 0.3f + 0.7f;
            float currentAlpha = alpha * pulse;
            float currentSize = size * pulse;

            // Glow effect
            for (int i = 3; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.2f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (glowAlpha * 255)));
                g2.fillOval((int) (scaledX - i * 2), (int) (scaledY - i * 2),
                        (int) (currentSize + i * 4), (int) (currentSize + i * 4));
            }

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (currentAlpha * 255)));
            g2.fillOval((int) scaledX, (int) scaledY, (int) currentSize, (int) currentSize);
        }
    }

    // ✅ Floating icon class
    private class FloatingIcon {
        float x, y;
        float vx, vy;
        float rotation;
        float rotationSpeed;
        float alpha;
        float pulseOffset;
        String icon;

        FloatingIcon(Random r, String icon) {
            this.icon = icon;
            x = r.nextFloat() * 1920;
            y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 0.5f;
            vy = (r.nextFloat() - 0.5f) * 0.5f;
            rotation = r.nextFloat() * 360;
            rotationSpeed = (r.nextFloat() - 0.5f) * 2f;
            alpha = r.nextFloat() * 0.15f + 0.05f;
            pulseOffset = r.nextFloat() * (float) Math.PI * 2;
        }

        void update() {
            x += vx;
            y += vy;
            rotation += rotationSpeed;

            if (x < -50) x = 1920;
            if (x > 1920) x = -50;
            if (y < -50) y = 1080;
            if (y > 1080) y = -50;

            pulseOffset += 0.02f;
        }

        void draw(Graphics2D g2, int panelWidth, int panelHeight) {
            float scaledX = (x / 1920f) * panelWidth;
            float scaledY = (y / 1080f) * panelHeight;

            float pulse = (float) Math.sin(pulseOffset) * 0.3f + 0.7f;

            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            g2.setColor(new Color(255, 255, 255, (int) (alpha * pulse * 255)));

            Graphics2D g2d = (Graphics2D) g2.create();
            g2d.translate(scaledX, scaledY);
            g2d.rotate(Math.toRadians(rotation));
            g2d.drawString(icon, -12, 8);
            g2d.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ✅ Gradient background (Private-style)
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(2, 5, 15),
                getWidth(), getHeight(), new Color(10, 20, 40)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // draw particles + icons
        for (AnimatedParticle p : particles) p.draw(g2, getWidth(), getHeight());
        for (FloatingIcon ic : floatingIcons) ic.draw(g2, getWidth(), getHeight());

        g2.dispose();
    }
}

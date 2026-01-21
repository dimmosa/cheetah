package view;

import control.GameHistoryController;
import model.DetailedGameHistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenuTwoPlayerScreen extends JPanel {

    private final JFrame frame;
    private float cardOpacity = 0f;
    private Timer animationTimer;
    private final List<AnimatedParticle> particles = new ArrayList<>();
    private final List<FloatingIcon> floatingIcons = new ArrayList<>();
    private final Random random = new Random();
    private float waveOffset = 0f;

    public MainMenuTwoPlayerScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setOpaque(false);
        initializeAnimation();

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel topBadge = new JLabel("👥 Two-Player Mode") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float pulse = (float) Math.sin(waveOffset * 0.5f) * 0.2f + 0.8f;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, (int) (180 * pulse), (int) (220 * pulse)),
                        getWidth(), 0, new Color(0, (int) (220 * pulse), (int) (100 * pulse))
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0, 200, 255, 35));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        topBadge.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBadge.setForeground(Color.WHITE);
        topBadge.setBorder(new EmptyBorder(8, 20, 8, 20));
        topBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel badgeWrapper = new JPanel();
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(topBadge);

        JPanel card = createRoundedCard(new Color(15, 25, 40, 230), 30);
        card.setBorder(new EmptyBorder(45, 70, 55, 70));
        card.setPreferredSize(new Dimension(520, 650));

        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 58));
        iconLabel.setForeground(Color.WHITE);

        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float pulse = (float) Math.sin(waveOffset) * 0.3f + 0.7f;
                RadialGradientPaint rgp = new RadialGradientPaint(
                        getWidth() / 2f, getHeight() / 2f, 55f * pulse,
                        new float[]{0f, 0.55f, 1f},
                        new Color[]{
                                new Color(0, 200, 255, (int) (120 * pulse)),
                                new Color(0, 150, 255, (int) (55 * pulse)),
                                new Color(0, 0, 0, 0)
                        }
                );
                g2.setPaint(rgp);
                g2.fillOval(getWidth() / 2 - 55, getHeight() / 2 - 55, 110, 110);
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(0, 160, 255, 220),
                        0, getHeight(), new Color(0, 110, 200, 220)
                ));
                g2.fillRoundRect(12, 12, getWidth() - 24, getHeight() - 24, 22, 22);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawRoundRect(12, 12, getWidth() - 25, getHeight() - 25, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setMaximumSize(new Dimension(110, 110));
        iconContainer.setPreferredSize(new Dimension(110, 110));
        iconContainer.setLayout(new GridBagLayout());
        iconContainer.add(iconLabel);

        JLabel title = new JLabel("MineSweeper");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Two-Player Edition");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnNewGame = createMenuButton("🎮   New Game");
        JButton btnTraining = createMenuButton("⚔️   Training Simulation");
        JButton btnQuestions = createMenuButton("❓   Question Management");
        JButton btnHistory = createMenuButton("🕒   History");
        JButton btnCompetitive = createMenuButton("🏆   Competitive Mode");
        JButton btnLogout = createMenuButton("✕   Exit");

        btnNewGame.addActionListener(e -> {
            toggleButtons(false, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            stopAnimation();
            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    return new GameSetupScreen(frame);
                }
                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        keepFrameBig();
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        toggleButtons(true, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
                    }
                }
            }.execute();
        });

        btnTraining.addActionListener(e -> {
            toggleButtons(false, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            try {
                new TrainingSimulationDialog(frame).setVisible(true);
            } finally {
                toggleButtons(true, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            }
        });

        btnHistory.addActionListener(e -> {
            toggleButtons(false, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            stopAnimation();
            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    List<DetailedGameHistoryEntry> list = new GameHistoryController().getDetailedHistoryForLoggedUser();
                    return new DetailedGameHistoryScreen(frame, list);
                }
                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        toggleButtons(true, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
                    }
                }
            }.execute();
        });

        btnQuestions.addActionListener(e -> {
            toggleButtons(false, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            stopAnimation();
            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    return new QuestionManagementScreen(frame);
                }
                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        toggleButtons(true, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
                    }
                }
            }.execute();
        });

        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                stopAnimation();
                frame.setContentPane(new LoginTwoPlayerScreen(frame));
                frame.revalidate();
                frame.repaint();
            }
        });

        btnCompetitive.addActionListener(e -> {
            toggleButtons(false, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
            stopAnimation();
            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    return new GameSetupCompetitiveScreen(frame);
                }
                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        keepFrameBig();
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        toggleButtons(true, btnNewGame, btnTraining, btnQuestions, btnHistory, btnCompetitive, btnLogout);
                    }
                }
            }.execute();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(iconContainer);
        card.add(Box.createVerticalStrut(18));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(50));
        card.add(btnNewGame);
        card.add(Box.createVerticalStrut(15));
        card.add(btnCompetitive);
        card.add(Box.createVerticalStrut(15));
        card.add(btnTraining);
        card.add(Box.createVerticalStrut(15));
        card.add(btnQuestions);
        card.add(Box.createVerticalStrut(15));
        card.add(btnHistory);
        card.add(Box.createVerticalStrut(15));
        card.add(btnLogout);

        mainContainer.add(badgeWrapper);
        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(card);
        add(mainContainer);
    }

    private void toggleButtons(boolean enabled, JButton... buttons) {
        for (JButton b : buttons) b.setEnabled(enabled);
    }

    private void keepFrameBig() {
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
            frame.setSize(Math.max(frame.getWidth(), 1200), Math.max(frame.getHeight(), 760));
            frame.setLocationRelativeTo(null);
        }
    }

    private void initializeAnimation() {
        for (int i = 0; i < 60; i++) particles.add(new AnimatedParticle(random));
        String[] icons = {"💣", "🚩", "💎", "⭐", "🎯"};
        for (int i = 0; i < 8; i++) floatingIcons.add(new FloatingIcon(random, icons[random.nextInt(icons.length)]));
        animationTimer = new Timer(30, e -> {
            if (cardOpacity < 1f) cardOpacity = Math.min(1f, cardOpacity + 0.04f);
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

    private class AnimatedParticle {
        float x, y, vx, vy, size, alpha, pulseSpeed, pulseOffset;
        Color color;
        AnimatedParticle(Random r) {
            x = r.nextFloat() * 1920; y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 1.2f; vy = (r.nextFloat() - 0.5f) * 1.2f;
            size = r.nextFloat() * 8 + 2; alpha = r.nextFloat() * 0.5f + 0.3f;
            pulseSpeed = r.nextFloat() * 0.05f + 0.02f; pulseOffset = r.nextFloat() * (float) Math.PI * 2;
            int c = r.nextInt(5);
            if (c == 0) color = new Color(0, 180, 255);
            else if (c == 1) color = new Color(0, 220, 100);
            else if (c == 2) color = new Color(100, 150, 255);
            else if (c == 3) color = new Color(0, 255, 200);
            else color = new Color(50, 200, 255);
        }
        void update() {
            x += vx; y += vy;
            if (x < 0) x = 1920; if (x > 1920) x = 0;
            if (y < 0) y = 1080; if (y > 1080) y = 0;
            pulseOffset += pulseSpeed;
        }
        void draw(Graphics2D g2, int panelWidth, int panelHeight) {
            float scaledX = (x / 1920f) * panelWidth, scaledY = (y / 1080f) * panelHeight;
            float pulse = (float) Math.sin(pulseOffset) * 0.3f + 0.7f;
            float currentAlpha = alpha * pulse, currentSize = size * pulse;
            for (int i = 3; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.2f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (glowAlpha * 255)));
                g2.fillOval((int) (scaledX - i * 2), (int) (scaledY - i * 2), (int) (currentSize + i * 4), (int) (currentSize + i * 4));
            }
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (currentAlpha * 255)));
            g2.fillOval((int) scaledX, (int) scaledY, (int) currentSize, (int) currentSize);
        }
    }

    private class FloatingIcon {
        float x, y, vx, vy, rotation, rotationSpeed, alpha, pulseOffset;
        String icon;
        FloatingIcon(Random r, String icon) {
            this.icon = icon;
            x = r.nextFloat() * 1920; y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 0.5f; vy = (r.nextFloat() - 0.5f) * 0.5f;
            rotation = r.nextFloat() * 360; rotationSpeed = (r.nextFloat() - 0.5f) * 2f;
            alpha = r.nextFloat() * 0.15f + 0.05f; pulseOffset = r.nextFloat() * (float) Math.PI * 2;
        }
        void update() {
            x += vx; y += vy; rotation += rotationSpeed;
            if (x < -50) x = 1920; if (x > 1920) x = -50;
            if (y < -50) y = 1080; if (y > 1080) y = -50;
            pulseOffset += 0.02f;
        }
        void draw(Graphics2D g2, int panelWidth, int panelHeight) {
            float scaledX = (x / 1920f) * panelWidth, scaledY = (y / 1080f) * panelHeight;
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

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            private final Color normalColor = new Color(30, 40, 60, 180);
            private final Color hoverColor = new Color(50, 80, 120, 220);
            private float hoverProgress = 0f;
            private Timer hoverTimer = null;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { animateHover(true); }
                    @Override
                    public void mouseExited(MouseEvent e) { animateHover(false); }
                });
            }
            private void animateHover(boolean entering) {
                if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();
                hoverTimer = new Timer(20, e -> {
                    if (entering) hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                    else hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                    repaint();
                    if ((entering && hoverProgress >= 1f) || (!entering && hoverProgress <= 0f)) hoverTimer.stop();
                });
                hoverTimer.start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = (int) (normalColor.getRed() + (hoverColor.getRed() - normalColor.getRed()) * hoverProgress);
                int gr = (int) (normalColor.getGreen() + (hoverColor.getGreen() - normalColor.getGreen()) * hoverProgress);
                int b = (int) (normalColor.getBlue() + (hoverColor.getBlue() - normalColor.getBlue()) * hoverProgress);
                int a = (int) (normalColor.getAlpha() + (hoverColor.getAlpha() - normalColor.getAlpha()) * hoverProgress);
                g2.setColor(new Color(r, gr, b, a));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (hoverProgress > 0) {
                    g2.setColor(new Color(0, 200, 255, (int) (100 * hoverProgress)));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.setColor(new Color(0, 220, 100, (int) (50 * hoverProgress)));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setPreferredSize(new Dimension(400, 55));
        return btn;
    }

    private JPanel createRoundedCard(Color bg, int arc) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardOpacity));
                int w = getWidth(), h = getHeight();
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                        new Color[]{new Color(25, 33, 45, bg.getAlpha()), new Color(15, 20, 28, bg.getAlpha())}));
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, arc, arc));
                float pulse = (float) Math.sin(waveOffset * 0.3f) * 0.3f + 0.7f;
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(new Color(0, 180, 255, (int) (35 * pulse)));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawRoundRect(2, 2, w - 5, h - 5, arc - 2, arc - 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(2, 5, 15), getWidth(), getHeight(), new Color(10, 20, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        for (AnimatedParticle p : particles) p.draw(g2, getWidth(), getHeight());
        for (FloatingIcon ic : floatingIcons) ic.draw(g2, getWidth(), getHeight());
        g2.dispose();
    }
}
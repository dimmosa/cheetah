package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.GameHistoryController;
import model.GameHistoryEntry;

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

public class MainMenuPrivateScreen extends JPanel {

    JFrame frame;
    private String loggedInUsername; // ✅ FIXED: Store the logged-in user
    
    // Animation fields
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private List<FloatingIcon> floatingIcons;
    private Random random;
    private float waveOffset = 0;

    // ✅ FIXED: Constructor now accepts username parameter
    public MainMenuPrivateScreen(JFrame frame, String username) {
        this.frame = frame;
        this.loggedInUsername = username; // ✅ FIXED: Store the username

        setLayout(new GridBagLayout());
        setOpaque(false);
        
        // Initialize animation
        initializeAnimation();

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        // ✅ Display logged-in user
        JLabel topBadge = new JLabel("👤 " + (username != null ? username : "Private Account")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Animated gradient for badge
                float pulse = (float)Math.sin(waveOffset * 0.5) * 0.2f + 0.8f;
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color((int)(0 * pulse), (int)(180 * pulse), (int)(220 * pulse)),
                    getWidth(), 0, new Color((int)(0 * pulse), (int)(220 * pulse), (int)(100 * pulse))
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Glow effect
                g2.setColor(new Color(0, 200, 255, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);
                
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
        card.setBorder(new EmptyBorder(40, 60, 50, 60));
        card.setPreferredSize(new Dimension(450, 550));

        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Pulsing radial glow
                float pulse = (float)Math.sin(waveOffset) * 0.3f + 0.7f;
                RadialGradientPaint rgp = new RadialGradientPaint(
                    getWidth()/2f, getHeight()/2f, 50f * pulse,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{
                        new Color(0, 200, 255, (int)(120 * pulse)), 
                        new Color(0, 150, 255, (int)(60 * pulse)),
                        new Color(0, 0, 0, 0)
                    }
                );
                g2.setPaint(rgp);
                g2.fillOval(getWidth()/2 - 50, getHeight()/2 - 50, 100, 100);
                super.paintComponent(g);
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setMaximumSize(new Dimension(100, 100));
        iconContainer.add(iconLabel);

        JLabel title = new JLabel("MineSweeper");
        title.setFont(new Font("SansSerif", Font.PLAIN, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Private Account");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnPractice = createMenuButton("🎮   Practice Board");
        JButton btnHistory = createMenuButton("🕒   History");
        JButton btnExit = createMenuButton("❌   Exit");

        btnPractice.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new PrivateGameScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        btnHistory.addActionListener(e -> {
            stopAnimation();
            GameHistoryController controller = new GameHistoryController();
            List<GameHistoryEntry> list = controller.getSimpleHistoryForLoggedUser();
            
            // ✅ FIXED: Pass the logged-in username to GameHistoryScreen
            System.out.println("Opening history for user: " + loggedInUsername);
            frame.setContentPane(new GameHistoryScreen(frame, list, "single", loggedInUsername));
            frame.revalidate();
            frame.repaint();
        });

        btnExit.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new LoginPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(iconContainer);
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(50));
        
        card.add(btnPractice);
        card.add(Box.createVerticalStrut(15));
        card.add(btnHistory);
        card.add(Box.createVerticalStrut(15));
        card.add(btnExit);

        mainContainer.add(badgeWrapper);
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(card);

        add(mainContainer);
    }

    // ✅ DEPRECATED: Keep for backward compatibility but don't use
    @Deprecated
    public MainMenuPrivateScreen(JFrame frame) {
        this(frame, null);
    }

    // ✅ Initialize animated background
    private void initializeAnimation() {
        random = new Random();
        particles = new ArrayList<>();
        floatingIcons = new ArrayList<>();
        
        // Create floating particles
        for (int i = 0; i < 60; i++) {
            particles.add(new AnimatedParticle(random));
        }
        
        // Create floating game-themed icons
        String[] icons = {"💣", "🚩", "💎", "⭐", "🎯"};
        for (int i = 0; i < 8; i++) {
            floatingIcons.add(new FloatingIcon(random, icons[random.nextInt(icons.length)]));
        }

        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (AnimatedParticle p : particles) {
                    p.update();
                }
                for (FloatingIcon icon : floatingIcons) {
                    icon.update();
                }
                waveOffset += 0.05f;
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
            pulseOffset = r.nextFloat() * (float)Math.PI * 2;
            
            int colorChoice = r.nextInt(5);
            if (colorChoice == 0) {
                color = new Color(0, 180, 255);
            } else if (colorChoice == 1) {
                color = new Color(0, 220, 100);
            } else if (colorChoice == 2) {
                color = new Color(100, 150, 255);
            } else if (colorChoice == 3) {
                color = new Color(0, 255, 200);
            } else {
                color = new Color(50, 200, 255);
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

            // Glow effect
            for (int i = 3; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.2f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(glowAlpha * 255)));
                g2.fillOval((int)(scaledX - i*2), (int)(scaledY - i*2), (int)(currentSize + i*4), (int)(currentSize + i*4));
            }

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(currentAlpha * 255)));
            g2.fillOval((int)scaledX, (int)scaledY, (int)currentSize, (int)currentSize);
        }
    }
    
    // ✅ Floating icon class for extra visual interest
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
            pulseOffset = r.nextFloat() * (float)Math.PI * 2;
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
            
            float pulse = (float)Math.sin(pulseOffset) * 0.3f + 0.7f;
            
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            g2.setColor(new Color(255, 255, 255, (int)(alpha * pulse * 255)));
            
            Graphics2D g2d = (Graphics2D) g2.create();
            g2d.translate(scaledX, scaledY);
            g2d.rotate(Math.toRadians(rotation));
            g2d.drawString(icon, -12, 8);
            g2d.dispose();
        }
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            private Color normalColor = new Color(30, 40, 60, 180);
            private Color hoverColor = new Color(50, 80, 120, 220);
            private boolean hovering = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer = null;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { 
                        hovering = true;
                        animateHover(true);
                    }
                    public void mouseExited(MouseEvent e) { 
                        hovering = false;
                        animateHover(false);
                    }
                });
            }
            
            private void animateHover(boolean entering) {
                // Stop any existing timer to prevent conflicts
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

                // Interpolate colors
                int r = (int)(normalColor.getRed() + (hoverColor.getRed() - normalColor.getRed()) * hoverProgress);
                int gr = (int)(normalColor.getGreen() + (hoverColor.getGreen() - normalColor.getGreen()) * hoverProgress);
                int b = (int)(normalColor.getBlue() + (hoverColor.getBlue() - normalColor.getBlue()) * hoverProgress);
                int a = (int)(normalColor.getAlpha() + (hoverColor.getAlpha() - normalColor.getAlpha()) * hoverProgress);
                
                g2.setColor(new Color(r, gr, b, a));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Animated border glow on hover
                if (hoverProgress > 0) {
                    g2.setColor(new Color(0, 200, 255, (int)(100 * hoverProgress)));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                    g2.setColor(new Color(0, 220, 100, (int)(50 * hoverProgress)));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);
                }

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
        btn.setPreferredSize(new Dimension(350, 55));
        return btn;
    }
    
    private JPanel createRoundedCard(Color bg, int arc) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Card background
                g2.setColor(bg);
                g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
                
                // Subtle animated border glow
                float pulse = (float)Math.sin(waveOffset * 0.3) * 0.3f + 0.7f;
                g2.setColor(new Color(0, 180, 255, (int)(30 * pulse)));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

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
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(2, 5, 15), getWidth(), getHeight(), new Color(10, 20, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw animated particles
        for (AnimatedParticle p : particles) {
            p.draw(g2, getWidth(), getHeight());
        }
        
        // Draw floating icons
        for (FloatingIcon icon : floatingIcons) {
            icon.draw(g2, getWidth(), getHeight());
        }
    }
}
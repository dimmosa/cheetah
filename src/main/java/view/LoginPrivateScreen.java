package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.LoginControl;
import model.User;
import view.CustomPasswordField;
import view.CustomTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoginPrivateScreen extends JPanel {

    JFrame frame;

    // Animation fields
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private Random random;

    public LoginPrivateScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        // Initialize animation
        initializeAnimation();

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        togglePanel.setOpaque(false);
        JButton privateBtn = createToggleButton("👤 Private Account", true);
        JButton twoPlayerBtn = createToggleButton("👥 Two Players", false);
        togglePanel.add(privateBtn);
        togglePanel.add(twoPlayerBtn);

        twoPlayerBtn.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new LoginTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        // ✅ Rounded card matching two-player design
        JPanel card = createRoundedCard(new Color(15, 25, 40, 230), 30);
        card.setBorder(new EmptyBorder(30, 40, 40, 40));
        card.setPreferredSize(new Dimension(450, 550));

        JLabel iconLabel = new JLabel(" 💣 ");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconContainer = new JPanel();
        iconContainer.setOpaque(false);
        iconContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconContainer.add(iconLabel);
        iconContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel title = new JLabel("Minesweeper");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Private Account Login");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Player badge section
        JPanel playerSection = new JPanel();
        playerSection.setLayout(new BoxLayout(playerSection, BoxLayout.Y_AXIS));
        playerSection.setOpaque(false);
        playerSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        // Badge
        JLabel badge = new JLabel("👤 Player");
        badge.setForeground(new Color(0, 200, 255));
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(5, 18, 5, 18));

        JPanel badgeBg = createRoundedPanel(new Color(0, 200, 255, 55), 18);
        badgeBg.setOpaque(false);
        badgeBg.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeBg.add(badge);

        Dimension badgeSize = badgeBg.getPreferredSize();
        badgeBg.setMaximumSize(badgeSize);
        badgeBg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User icon
        JLabel userIcon = new JLabel("");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        userIcon.setForeground(new Color(220, 230, 255));
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        userIcon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        playerSection.add(badgeBg);
        playerSection.add(Box.createVerticalStrut(8));
        playerSection.add(userIcon);
        playerSection.add(Box.createVerticalStrut(18));

        JComponent userField = createDarkInput("Username", "👤");
        JComponent passField = createDarkPasswordInput("Password", "🔒");

        playerSection.add(userField);
        playerSection.add(Box.createVerticalStrut(12));
        playerSection.add(passField);

        JLabel forgotPass = new JLabel("Forgot Password?");
        forgotPass.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotPass.setForeground(new Color(0, 150, 255));
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        forgotPass.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ForgotPasswordDialog forgotDialog = new ForgotPasswordDialog(frame);
                forgotDialog.show();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                forgotPass.setForeground(new Color(0, 220, 255));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                forgotPass.setForeground(new Color(0, 150, 255));
            }
        });
        
        JPanel fpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fpPanel.setOpaque(false);
        fpPanel.add(forgotPass);
        fpPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fpPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton signInBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 180, 255), getWidth(), 0, new Color(0, 220, 100));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        signInBtn.setContentAreaFilled(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setFocusPainted(false);
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        LoginControl controller = new LoginControl();

        signInBtn.addActionListener(e -> {
            String username = ((JTextField) userField).getText().trim();
            String password = ((JTextField) passField).getText();

            // Check for empty fields
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = controller.login(username, password);

            if (user != null) {
                // ✅ FIXED: Pass username to MainMenuPrivateScreen
                SuccessDialog.show(frame, user.getUsername(), () -> {
                    stopAnimation();
                    frame.setContentPane(new MainMenuPrivateScreen(frame, user.getUsername()));
                    frame.revalidate();
                    frame.repaint();
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.\nPlease try again.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel footer = new JLabel("<html>Don't have an account? <font color='#00C6FF'><u>Sign Up</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                stopAnimation();
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(LoginPrivateScreen.this);
                f.setContentPane(new SignUpScreen(f));
                f.revalidate();
                f.repaint();
            }
        });

        // Layout
        card.add(iconContainer);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(playerSection);
        card.add(fpPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(footer);

        mainContainer.add(togglePanel);
        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(card);

        add(mainContainer);
    }

    // ✅ Initialize animated background
    private void initializeAnimation() {
        random = new Random();
        particles = new ArrayList<>();
        
        // Create more floating particles with varied sizes
        for (int i = 0; i < 60; i++) {
            particles.add(new AnimatedParticle(random));
        }

        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (AnimatedParticle p : particles) {
                    p.update();
                }
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
            
            // Theme colors: blue and cyan tones with more variety
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

            // Wrap around screen
            if (x < 0) x = 1920;
            if (x > 1920) x = 0;
            if (y < 0) y = 1080;
            if (y > 1080) y = 0;
            
            pulseOffset += pulseSpeed;
        }

        void draw(Graphics2D g2, int panelWidth, int panelHeight) {
            // Scale to actual panel size
            float scaledX = (x / 1920f) * panelWidth;
            float scaledY = (y / 1080f) * panelHeight;

            // Pulsing effect
            float pulse = (float)Math.sin(pulseOffset) * 0.3f + 0.7f;
            float currentAlpha = alpha * pulse;
            float currentSize = size * pulse;

            // Draw glow effect
            for (int i = 3; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.2f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(glowAlpha * 255)));
                g2.fillOval((int)(scaledX - i*2), (int)(scaledY - i*2), (int)(currentSize + i*4), (int)(currentSize + i*4));
            }

            // Draw main particle
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(currentAlpha * 255)));
            g2.fillOval((int)scaledX, (int)scaledY, (int)currentSize, (int)currentSize);
        }
    }

    private JButton createToggleButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        if (isActive) {
            btn.setForeground(new Color(0, 220, 100));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 220, 100)),
                    new EmptyBorder(5, 15, 5, 15)
            ));
        } else {
            btn.setForeground(Color.GRAY);
            btn.setBorder(new EmptyBorder(7, 15, 7, 15));
        }
        return btn;
    }

    private JComponent createDarkInput(String placeholder, String icon) {
        return CustomTextField.createDarkInput(placeholder, icon);
    }

    private JComponent createDarkPasswordInput(String placeholder, String icon) {
        return CustomPasswordField.createDarkPasswordInput(placeholder, icon);
    }

    private JPanel createRoundedCard(Color bg, int arc) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(bg);
                g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JPanel createRoundedPanel(Color bg, int arc) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
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

        // ✅ Draw animated particles
        for (AnimatedParticle p : particles) {
            p.draw(g2, getWidth(), getHeight());
        }
    }
}
package view;

import com.formdev.flatlaf.FlatClientProperties;
import model.User;
import model.SessionManager;
import model.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoginTwoPlayerScreen extends JPanel {

    JFrame frame;

    private JTextField player1User;
    private JTextField player1Pass;

    private JTextField player2User;
    private JTextField player2Pass;

    // Animation fields
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private Random random;

    public LoginTwoPlayerScreen(JFrame frame) {
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
        JButton privateBtn = createToggleButton("👤 Private Account", false);
        JButton twoPlayerBtn = createToggleButton("👥 Two Players", true);
        togglePanel.add(privateBtn);
        togglePanel.add(twoPlayerBtn);

        privateBtn.addActionListener(e -> {
            stopAnimation();
            frame.setContentPane(new LoginPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        // ✅ Rounded card (no corner artifacts, no clipping)
        JPanel card = createRoundedCard(new Color(15, 25, 40, 230), 30);
        card.setBorder(new EmptyBorder(30, 40, 40, 40));
        card.setPreferredSize(new Dimension(700, 550));

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

        JLabel subtitle = new JLabel("Two Player Login");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel playersGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        playersGrid.setOpaque(false);
        playersGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        JPanel p1Panel = createPlayerColumn("👤 Player 1", new Color(0, 150, 255), "");
        JPanel p2Panel = createPlayerColumn("👤 Player 2", new Color(0, 200, 100), "");

        playersGrid.add(p1Panel);
        playersGrid.add(p2Panel);

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

        signInBtn.addActionListener(e -> {
            String p1 = player1User.getText().trim();
            String p1Pass = player1Pass.getText();

            String p2 = player2User.getText().trim();
            String p2Pass = player2Pass.getText();

            // ✅ Check for duplicate usernames
            if (!p1.isEmpty() && p1.equalsIgnoreCase(p2)) {
                JOptionPane.showMessageDialog(this,
                        "Both players cannot use the same username!\nPlease enter different usernames.",
                        "Duplicate Username",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check for empty fields
            if (p1.isEmpty() || p1Pass.isEmpty() || p2.isEmpty() || p2Pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all fields for both players.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            UserService service = new UserService();

            User player1 = service.getUser(p1);
            User player2 = service.getUser(p2);

            if (player1 != null && player1.getPassword().equals(p1Pass) &&
                    player2 != null && player2.getPassword().equals(p2Pass)) {

                SessionManager.getInstance().loginTwoPlayers(player1, player2);

                // ✅ Show animated success dialog for two players
                SuccessDialog.show(frame, player1.getUsername() + " & " + player2.getUsername(), () -> {
                    stopAnimation();
                    frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
                    frame.revalidate();
                    frame.repaint();
                });

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid login credentials for one or both players.\nPlease check your username and password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel footer = new JLabel("<html>Don't have accounts? <font color='#00C6FF'><u>Sign Up</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                stopAnimation();
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(LoginTwoPlayerScreen.this);
                f.setContentPane(new SignUpScreen(f));
                f.revalidate();
                f.repaint();
            }
        });

        /* =========================
           ✅ NEW: Forgot Password link button (UI only)
           - Does NOT change login logic
           - Opens the same reset process dialog you already have
           ========================= */
        JButton forgotBtn = createLinkButton("Forgot password?");
        forgotBtn.addActionListener(e -> {
            // open your existing reset flow
            ForgotPasswordDialog fp = new ForgotPasswordDialog(frame);
            fp.show();
        });

        // Layout
        card.add(iconContainer);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(playersGrid);
        card.add(Box.createVerticalStrut(18));
        card.add(forgotBtn);              // ✅ added here (between grid and sign-in)
        card.add(Box.createVerticalStrut(10));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(footer);

        mainContainer.add(togglePanel);
        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(card);

        add(mainContainer);
    }

    /* =========================
       ✅ NEW helper: link-style button (UI only)
       ========================= */
    private JButton createLinkButton(String text) {
        JButton btn = new JButton("<html><u>" + text + "</u></html>");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(new Color(0, 200, 255));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return btn;
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
            pulseOffset = r.nextFloat() * (float) Math.PI * 2;

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
            float pulse = (float) Math.sin(pulseOffset) * 0.3f + 0.7f;
            float currentAlpha = alpha * pulse;
            float currentSize = size * pulse;

            // Draw glow effect
            for (int i = 3; i > 0; i--) {
                float glowAlpha = currentAlpha * 0.2f / i;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (glowAlpha * 255)));
                g2.fillOval((int) (scaledX - i * 2), (int) (scaledY - i * 2), (int) (currentSize + i * 4), (int) (currentSize + i * 4));
            }

            // Draw main particle
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (currentAlpha * 255)));
            g2.fillOval((int) scaledX, (int) scaledY, (int) currentSize, (int) currentSize);
        }
    }

    private JPanel createPlayerColumn(String titleText, Color badgeColor, String userIcon) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel badge = new JLabel(titleText);
        badge.setForeground(badgeColor);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(5, 18, 5, 18));

        JPanel badgeBg = createRoundedPanel(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 55), 18);
        badgeBg.setOpaque(false);
        badgeBg.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeBg.add(badge);

        Dimension badgeSize = badgeBg.getPreferredSize();
        badgeBg.setMaximumSize(badgeSize);
        badgeBg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel(userIcon);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        icon.setForeground(new Color(220, 230, 255));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        p.add(badgeBg);
        p.add(Box.createVerticalStrut(8));
        p.add(icon);
        p.add(Box.createVerticalStrut(18));

        JTextField userField = createDarkInput("Username", "👤");
        JTextField passField = createDarkPasswordInput("Password", "🔒");

        if (titleText.contains("Player 1")) {
            player1User = userField;
            player1Pass = passField;
        } else {
            player2User = userField;
            player2Pass = passField;
        }

        p.add(userField);
        p.add(Box.createVerticalStrut(12));
        p.add(passField);

        return p;
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

    private JTextField createDarkInput(String placeholder, String icon) {
        return (JTextField) CustomTextField.createDarkInput(placeholder, icon);
    }

    private JTextField createDarkPasswordInput(String placeholder, String icon) {
        return (JTextField) CustomPasswordField.createDarkPasswordInput(placeholder, icon);
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

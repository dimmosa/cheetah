package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern animated success dialog that auto-closes after 4 seconds
 */
public class SuccessDialog extends JDialog {
    
    private float opacity = 0.0f;
    private Timer fadeInTimer;
    private Timer autoCloseTimer;
    private JLabel messageLabel;
    private JLabel iconLabel;
    private int pulseCount = 0;
    
    public SuccessDialog(JFrame parent, String username) {
        super(parent, "Login Successful", false); // Non-modal
        
        setUndecorated(true);
        setSize(400, 180);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Make dialog rounded
        setShape(new RoundRectangle2D.Double(0, 0, 400, 180, 30, 30));
        
        // Main panel with gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(20, 30, 48),
                    getWidth(), getHeight(), new Color(30, 45, 70)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                // Glow border
                g2.setColor(new Color(0, 220, 100, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 28, 28);
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Icon with animation
        iconLabel = new JLabel("✓") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw glow circles
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                for (int i = 3; i > 0; i--) {
                    int size = 50 + (i * 10);
                    g2.setColor(new Color(0, 220, 100, 30 / i));
                    g2.fillOval(centerX - size/2, centerY - size/2, size, size);
                }
                
                // Draw checkmark circle background
                g2.setColor(new Color(0, 220, 100));
                g2.fillOval(centerX - 25, centerY - 25, 50, 50);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(80, 80));
        
        // Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("Login Successful!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 220, 100));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        messageLabel = new JLabel("<html>Welcome back, <b>" +username+ "</b>!</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(200, 210, 220));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel countdownLabel = new JLabel("Redirecting...");
        countdownLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countdownLabel.setForeground(new Color(150, 160, 170));
        countdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createVerticalStrut(8));
        messagePanel.add(messageLabel);
        messagePanel.add(Box.createVerticalStrut(8));
        messagePanel.add(countdownLabel);
        
        // Add components
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Start animations
        startFadeIn();
        startPulseAnimation();
        startAutoClose();
    }
    
    private void startFadeIn() {
        fadeInTimer = new Timer(20, e -> {
            opacity += 0.08f;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                fadeInTimer.stop();
            }
            setOpacity(opacity);
        });
        fadeInTimer.start();
    }
    
    private void startPulseAnimation() {
        Timer pulseTimer = new Timer(150, e -> {
            pulseCount++;
            iconLabel.repaint();
            
            if (pulseCount > 20) { // Stop after ~3 seconds
                ((Timer)e.getSource()).stop();
            }
        });
        pulseTimer.start();
    }
    
    private void startAutoClose() {
        autoCloseTimer = new Timer(1200, e -> {
            fadeOut();
        });
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
    }
    
    private void fadeOut() {
        Timer fadeOutTimer = new Timer(10, null);
        fadeOutTimer.addActionListener(e -> {
            opacity -= 0.1f;
            if (opacity <= 0.0f) {
                opacity = 0.0f;
                fadeOutTimer.stop();
                dispose();
            }
            setOpacity(opacity);
        });
        fadeOutTimer.start();
    }
    
    /**
     * Show the success dialog and execute callback after it closes
     */
    public static void show(JFrame parent, String username, Runnable onClose) {
        SuccessDialog dialog = new SuccessDialog(parent, username);
        dialog.setVisible(true);
        
        // Schedule callback after dialog closes (4.5 seconds total)
        Timer callbackTimer = new Timer(1400, e -> {
            if (onClose != null) {
                onClose.run();
            }
        });
        callbackTimer.setRepeats(false);
        callbackTimer.start();
    }
}
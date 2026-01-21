package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AvatarSelectionDialog extends JDialog {

    public static final String[] AVATAR_EMOJIS = {
            "😎", "🤓", "🥳", "🤖",
            "👻", "😺", "🐶", "🦊",
            "🐼", "🦄", "🐉", "🦁"
    };

    private static final Color[][] AVATAR_COLORS = {
            {new Color(255, 80, 60), new Color(255, 140, 60)},
            {new Color(100, 100, 255), new Color(180, 100, 255)},
            {new Color(255, 200, 0), new Color(255, 160, 0)},
            {new Color(0, 180, 255), new Color(0, 100, 200)},
            {new Color(180, 60, 220), new Color(220, 100, 200)},
            {new Color(255, 60, 100), new Color(255, 100, 100)},
            {new Color(255, 180, 0), new Color(220, 140, 0)},
            {new Color(255, 60, 0), new Color(220, 40, 0)},
            {new Color(100, 110, 130), new Color(140, 150, 170)},
            {new Color(240, 120, 240), new Color(200, 100, 255)},
            {new Color(0, 200, 100), new Color(50, 220, 150)},
            {new Color(240, 120, 20), new Color(255, 160, 40)}
    };

    private final Consumer<Integer> onAvatarSelected;
    private static final int ARC = 40;
    private final List<AnimatedAvatarButton> buttons = new ArrayList<>();
    private Timer globalAnimator;
    private long startTime;

    public AvatarSelectionDialog(Frame owner, String playerName, Consumer<Integer> onAvatarSelected) {
        super(owner, "Choose Avatar", true);
        this.onAvatarSelected = onAvatarSelected;
        this.startTime = System.currentTimeMillis();

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(550, 620);
        setLocationRelativeTo(owner);
        applyRoundedShape();

        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new LinearGradientPaint(0, 0, 0, getHeight(),
                        new float[]{0f, 1f},
                        new Color[]{new Color(20, 25, 45, 250), new Color(10, 12, 25, 255)}));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        JLabel title = new JLabel("Select Your Identity");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBounds(40, 35, 300, 35);
        mainPanel.add(title);

        JButton closeBtn = createHeaderButton("✖", 480, 35);
        closeBtn.addActionListener(e -> dispose());
        mainPanel.add(closeBtn);

        JPanel gridPanel = new JPanel(new GridLayout(3, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBounds(40, 120, 470, 450);

        for (int i = 0; i < AVATAR_EMOJIS.length; i++) {
            final int index = i;
            AnimatedAvatarButton btn = new AnimatedAvatarButton(index);
            btn.addActionListener(e -> {
                if (this.onAvatarSelected != null) this.onAvatarSelected.accept(index);
                dispose();
            });
            buttons.add(btn);
            gridPanel.add(btn);
        }
        mainPanel.add(gridPanel);

        startGlobalAnimation();
    }

    // --- HELPER METHOD ADDED BACK ---
    public static String getEmojiByIndex(int index) {
        if (index >= 0 && index < AVATAR_EMOJIS.length) {
            return AVATAR_EMOJIS[index];
        }
        return "❓";
    }

    private void startGlobalAnimation() {
        globalAnimator = new Timer(20, e -> repaint());
        globalAnimator.start();
    }

    private void applyRoundedShape() {
        setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC));
    }

    private class AnimatedAvatarButton extends JButton {
        private final int index;
        private float hoverAlpha = 0f;
        private float appearanceScale = 0f; // For the pop-in effect
        private final double phaseOffset;

        public AnimatedAvatarButton(int index) {
            this.index = index;
            this.phaseOffset = index * 0.5;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Entrance animation (Pop in)
            Timer entrance = new Timer(30, null);
            entrance.setInitialDelay(index * 50); // Staggered start
            entrance.addActionListener(e -> {
                appearanceScale += 0.1f;
                if (appearanceScale >= 1f) {
                    appearanceScale = 1f;
                    entrance.stop();
                }
                repaint();
            });
            entrance.start();

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animateHover(true); }
                @Override public void mouseExited(MouseEvent e) { animateHover(false); }
            });
        }

        private void animateHover(boolean in) {
            Timer t = new Timer(15, e -> {
                if (in) {
                    hoverAlpha += 0.1f;
                    if (hoverAlpha >= 1f) { hoverAlpha = 1f; ((Timer)e.getSource()).stop(); }
                } else {
                    hoverAlpha -= 0.1f;
                    if (hoverAlpha <= 0f) { hoverAlpha = 0f; ((Timer)e.getSource()).stop(); }
                }
            });
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (appearanceScale <= 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Apply Pop-in Scale
            g2.scale(appearanceScale, appearanceScale);
            // Re-center after scaling
            int xShift = (int)((getWidth() * (1 - appearanceScale)) / 2);
            int yShift = (int)((getHeight() * (1 - appearanceScale)) / 2);
            g2.translate(xShift, yShift);

            double time = (System.currentTimeMillis() - startTime) / 1000.0;
            int bobbingY = (int) (Math.sin(time * 2.5 + phaseOffset) * 5); 
            int totalY = bobbingY - (int)(hoverAlpha * 10);

            // Draw Shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(5, 5 + totalY, getWidth()-10, getHeight()-20, 30, 30);

            // Main Background
            GradientPaint gp = new GradientPaint(0, totalY, AVATAR_COLORS[index][0], 0, getHeight() + totalY, AVATAR_COLORS[index][1]);
            g2.setPaint(gp);
            g2.fillRoundRect(0, totalY, getWidth(), getHeight() - 10, 30, 30);

            // Border on Hover
            if (hoverAlpha > 0) {
                g2.setColor(new Color(255, 255, 255, (int)(hoverAlpha * 180)));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1 + totalY, getWidth()-3, getHeight()-13, 30, 30);
            }

            // Emoji with wiggle
            double wiggle = Math.sin(time * 4.0 + phaseOffset) * 2;
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
            FontMetrics fm = g2.getFontMetrics();
            String emoji = AVATAR_EMOJIS[index];
            int x = (getWidth() - fm.stringWidth(emoji)) / 2 + (int)wiggle;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + totalY - 5;

            g2.setColor(Color.WHITE);
            g2.drawString(emoji, x, y);

            g2.dispose();
        }
    }

    private JButton createHeaderButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 40, 40);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setForeground(new Color(150, 160, 190));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
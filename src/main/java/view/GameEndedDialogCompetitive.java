package view;

import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEndedDialogCompetitive extends JDialog {

    // =====================
    // API
    // =====================
    public enum EndReason { WIN, LOST_NO_LIVES, GIVE_UP }

    private final JFrame ownerFrame;
    private final String mode;

    // ✅ Private-style background layers
    private final List<AnimatedParticle> bgParticles = new ArrayList<>();
    private final List<FloatingIcon> floatingIcons = new ArrayList<>();

    // ✅ Celebration particles (only for WIN)
    private final List<CelebrationParticle> celebrationParticles = new ArrayList<>();

    private Timer animationTimer;

    // Animation vars
    private float cardScale = 0.6f;
    private float cardOpacity = 0.0f;
    private float floatOffset = 0.0f;

    @SuppressWarnings("unused")
    private float waveOffset = 0.0f;

    private final Random random = new Random();

    // ====== SIZE TUNING - SMALLER! ======
    private static final Dimension DIALOG_SIZE = new Dimension(420, 550); // ✅ קטן יותר!
    private static final Dimension CARD_SIZE   = new Dimension(360, 480); // ✅ קטן יותר!
    private static final int IMAGE_SIZE = 90;  // ✅ קטן יותר!
    private static final int BTN_W = 160;      // ✅ קטן יותר!
    private static final int BTN_H = 32;       // ✅ קטן יותר!

    // =========================================================
    // ✅ CONSTRUCTOR SHORT (what your GameScreenCompetitive calls)
    // =========================================================
    public GameEndedDialogCompetitive(
            JFrame owner,
            String playerNames,                 // "p1 vs p2"
            int score,                          // if you only show max for now
            String time,
            String difficulty,
            int livesLeft,                      // if you only show max for now
            int totalLives,
            String mode,
            GameEndedDialog.EndReason reason,   // reuse your existing enum from GameEndedDialog
            Runnable onPlayAgain,
            Runnable onMainMenu
    ) {
        super(owner, "Competitive Results", true);
        this.ownerFrame = owner;
        this.mode = mode;

        // Parse "p1 vs p2"
        String p1 = "Player 1";
        String p2 = "Player 2";
        if (playerNames != null && playerNames.contains("vs")) {
            String[] parts = playerNames.split("\\s*vs\\s*");
            if (parts.length >= 2) {
                p1 = parts[0].trim();
                p2 = parts[1].trim();
            }
        }

        // With this short ctor we don't have real winner/p1Score/p2Score -> show tie view
        int winnerPlayerNum = 0;

        initUI(
                owner,
                p1, p2,
                score, score,
                time,
                difficulty,
                livesLeft, livesLeft,
                totalLives,
                winnerPlayerNum,
                mapReason(reason),
                onPlayAgain,
                onMainMenu
        );
    }

    // =========================================================
    // ✅ OPTIONAL: FULL DATA CTOR (if later you want real results)
    // =========================================================
    public GameEndedDialogCompetitive(
            JFrame owner,
            String player1Name,
            String player2Name,
            int p1Score,
            int p2Score,
            String time,
            String difficulty,
            int p1Lives,
            int p2Lives,
            int totalLives,
            int winnerPlayerNum,        // 0=tie, 1 or 2 winner
            EndReason reason,
            Runnable onPlayAgain,
            Runnable onMainMenu
    ) {
        super(owner, "Competitive Results", true);
        this.ownerFrame = owner;
        this.mode = "competitive";

        initUI(
                owner,
                player1Name, player2Name,
                p1Score, p2Score,
                time,
                difficulty,
                p1Lives, p2Lives,
                totalLives,
                winnerPlayerNum,
                reason,
                onPlayAgain,
                onMainMenu
        );
    }

    // =========================================================
    // ✅ MAP old enum -> competitive enum
    // =========================================================
    private EndReason mapReason(GameEndedDialog.EndReason r) {
        if (r == null) return EndReason.GIVE_UP;
        return switch (r) {
            case WIN -> EndReason.WIN;
            case LOST_NO_LIVES -> EndReason.LOST_NO_LIVES;
            case GIVE_UP -> EndReason.GIVE_UP;
        };
    }

    // =========================================================
    // ✅ קובע את המנצח על סמך הניקוד והחיים
    // =========================================================
    private int determineWinner(int p1Score, int p2Score, int p1Lives, int p2Lives, EndReason reason) {
        // אם מישהו ויתר, לא קובעים מנצח
        if (reason == EndReason.GIVE_UP) {
            return 0; // תיקו/אין מנצח
        }
        
        // אם WIN - מישהו סיים את הלוח
        if (reason == EndReason.WIN) {
            // אם לשניהם יש חיים, מי שיש לו יותר ניקוד מנצח
            if (p1Lives > 0 && p2Lives > 0) {
                if (p1Score > p2Score) return 1;
                if (p2Score > p1Score) return 2;
                return 0; // תיקו
            }
            // אם רק לאחד יש חיים, הוא מנצח
            if (p1Lives > 0 && p2Lives == 0) return 1;
            if (p2Lives > 0 && p1Lives == 0) return 2;
            // אם לשניהם אין חיים, מי שיש לו יותר ניקוד מנצח
            if (p1Score > p2Score) return 1;
            if (p2Score > p1Score) return 2;
            return 0; // תיקו
        }
        
        // אם LOST_NO_LIVES - מישהו נשאר בלי חיים
        if (reason == EndReason.LOST_NO_LIVES) {
            // מי שיש לו חיים מנצח
            if (p1Lives > 0 && p2Lives == 0) return 1;
            if (p2Lives > 0 && p1Lives == 0) return 2;
            // אם לשניהם אין חיים, מי שיש לו יותר ניקוד מנצח
            if (p1Score > p2Score) return 1;
            if (p2Score > p1Score) return 2;
            return 0; // תיקו
        }
        
        return 0; // ברירת מחדל - תיקו
    }

    // =========================================================
    // ✅ REAL UI BUILDER
    // =========================================================
    private void initUI(
            JFrame owner,
            String player1Name,
            String player2Name,
            int p1Score,
            int p2Score,
            String time,
            String difficulty,
            int p1Lives,
            int p2Lives,
            int totalLives,
            int winnerPlayerNum,
            EndReason reason,
            Runnable onPlayAgain,
            Runnable onMainMenu
    ) {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(DIALOG_SIZE);
        setLocationRelativeTo(owner);

        initializeBackground();

        // --- LOGIC ---
        final Color accentColor;
        final String titleText;
        final String subText;

        Color p1Color = new Color(0, 150, 255);
        Color p2Color = new Color(255, 0, 150);

        // ✅ קובע את המנצח אוטומטית על סמך הניקוד והחיים
        int actualWinner = determineWinner(p1Score, p2Score, p1Lives, p2Lives, reason);

        String winnerText;
        Color winnerColor;

        if (actualWinner == 1) {
            winnerText = "🏆 WINNER: " + player1Name.toUpperCase();
            winnerColor = p1Color;
        } else if (actualWinner == 2) {
            winnerText = "🏆 WINNER: " + player2Name.toUpperCase();
            winnerColor = p2Color;
        } else {
            winnerText = "RESULT: TIE";
            winnerColor = new Color(200, 200, 200);
        }

        switch (reason) {
            case WIN -> {
                accentColor = winnerColor;
                titleText = (actualWinner == 0) ? "TIE" : "VICTORY";
                subText = "COMPETITIVE MATCH FINISHED";
                if (actualWinner != 0) {
                    initializeCelebrationParticles(accentColor);
                }
            }
            case LOST_NO_LIVES -> {
                accentColor = new Color(255, 50, 80);
                titleText = "GAME OVER";
                subText = "NO LIVES LEFT";
            }
            default -> {
                accentColor = new Color(140, 100, 255);
                titleText = "GIVE UP";
                subText = "MATCH ABANDONED";
            }
        }

        // --- Background panel ---
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(2, 5, 15),
                        getWidth(), getHeight(), new Color(10, 20, 40)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                for (AnimatedParticle p : bgParticles) p.draw(g2, getWidth(), getHeight());
                for (FloatingIcon ic : floatingIcons) ic.draw(g2, getWidth(), getHeight());

                for (CelebrationParticle p : celebrationParticles) {
                    int a = (int) (p.life * 255);
                    a = Math.max(0, Math.min(255, a));
                    g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), a));
                    g2.fillOval((int) p.x, (int) p.y, 4, 4);
                }

                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        setContentPane(contentPanel);

        // --- Card ---
        final Color darkCardBg = new Color(25, 30, 40, 240);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.translate(0, Math.sin(floatOffset) * 8);
                g2.scale(cardScale, cardScale);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardOpacity));

                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRoundRect(18, 24, getWidth() - 36, getHeight() - 36, 26, 26);

                g2.setColor(darkCardBg);
                g2.fillRoundRect(18, 18, getWidth() - 36, getHeight() - 36, 26, 26);

                GradientPaint glassGrad = new GradientPaint(
                        0, 18, new Color(255, 255, 255, 14),
                        0, 130, new Color(255, 255, 255, 0)
                );
                g2.setPaint(glassGrad);
                g2.fillRoundRect(18, 18, getWidth() - 36, 130, 26, 26);

                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(new Color(255, 255, 255, 35));
                g2.drawRoundRect(18, 18, getWidth() - 36, getHeight() - 36, 26, 26);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 26");
        card.setPreferredSize(CARD_SIZE);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // --- IMAGE ---
        JComponent imageHeader = createSquareEndImage(reason, accentColor, IMAGE_SIZE);
        imageHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGame = new JLabel("MineSweeper");
        lblGame.setFont(safeFont("SansSerif", Font.BOLD, 26));
        lblGame.setForeground(Color.WHITE);
        lblGame.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMode = new JLabel((mode == null ? "COMPETITIVE" : mode.toUpperCase()) + " EDITION");
        lblMode.setFont(safeFont("SansSerif", Font.BOLD, 10));
        lblMode.setForeground(new Color(0, 210, 255));
        lblMode.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblResult = new JLabel(titleText);
        lblResult.setFont(safeFont("SansSerif", Font.BOLD, 15));
        lblResult.setForeground(new Color(255, 255, 255, 220));
        lblResult.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(safeFont("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(180, 190, 200));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblWinner = new JLabel(winnerText);
        lblWinner.setFont(safeFont("SansSerif", Font.BOLD, 11));
        lblWinner.setForeground(winnerColor);
        lblWinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(240, 1));
        sep.setForeground(new Color(255, 255, 255, 40));

        JPanel statsContainer = new JPanel(new GridLayout(6, 1, 0, 6));
        statsContainer.setOpaque(false);
        statsContainer.setBorder(new EmptyBorder(10, 0, 8, 0));

        statsContainer.add(createRow("PLAYERS", player1Name + "  vs  " + player2Name, Color.WHITE));

        Color s1Col = (actualWinner == 1) ? p1Color : new Color(220, 220, 220);
        Color s2Col = (actualWinner == 2) ? p2Color : new Color(220, 220, 220);

        statsContainer.add(createRow("P1 SCORE", String.valueOf(p1Score), s1Col));
        statsContainer.add(createRow("P2 SCORE", String.valueOf(p2Score), s2Col));
        statsContainer.add(createRow("TIME", stripHtml(time), Color.WHITE));
        statsContainer.add(createRow("DIFFICULTY", difficulty, new Color(255, 200, 0)));
        statsContainer.add(createRow("LIVES",
                "P1 " + p1Lives + "/" + totalLives + "   |   P2 " + p2Lives + "/" + totalLives,
                new Color(255, 80, 80)));

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        btnPanel.setOpaque(false);

        JButton btnPlay = createStyledBtn("Play Again", accentColor);
        btnPlay.addActionListener(e -> runAndKeepFrameSize(onPlayAgain));

        JButton btnExit = createStyledBtn("Back to Menu", new Color(120, 130, 145));
        btnExit.addActionListener(e -> runAndKeepFrameSize(onMainMenu));

        btnPanel.add(btnPlay);
        btnPanel.add(btnExit);

        card.add(imageHeader);
        card.add(Box.createVerticalStrut(8));
        card.add(lblGame);
        card.add(Box.createVerticalStrut(3));
        card.add(lblMode);
        card.add(Box.createVerticalStrut(6));
        card.add(lblResult);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(6));
        card.add(lblWinner);
        card.add(Box.createVerticalStrut(8));
        card.add(sep);
        card.add(Box.createVerticalStrut(8));
        card.add(statsContainer);
        card.add(Box.createVerticalGlue());
        card.add(btnPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(card, gbc);

        startAnimations();
        playEndSfx(reason);
    }

    // ---------------- UI helpers ----------------

    private JPanel createRow(String label, String value, Color valCol) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(safeFont("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(120, 130, 145));

        JLabel v = new JLabel(value);
        v.setFont(safeFont("SansSerif", Font.BOLD, 11));
        v.setForeground(valCol);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JButton createStyledBtn(String text, Color baseColor) {
        class FancyButton extends JButton {
            private boolean hovering = false;
            private boolean pressed = false;

            FancyButton(String t) {
                super(t);
                setFont(safeFont("SansSerif", Font.PLAIN, 13));
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                setPreferredSize(new Dimension(BTN_W, BTN_H));
                setMaximumSize(new Dimension(BTN_W, BTN_H));
                setMinimumSize(new Dimension(BTN_W, BTN_H));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovering = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color bg = hovering ? new Color(55, 65, 80) : new Color(45, 50, 60);
                int inset = pressed ? 2 : 0;

                g2.setColor(bg);
                g2.fillRoundRect(inset, inset, w - inset * 2, h - inset * 2, 12, 12);

                g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), hovering ? 95 : 60));
                g2.fillRoundRect(inset + 10, inset + 8, w - inset * 2 - 20, 3, 10, 10);

                g2.setColor(new Color(255, 255, 255, 20));
                g2.drawRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, 12, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        }
        return new FancyButton(text);
    }

    // ✅ SQUARE IMAGE WITH ROUNDED CORNERS
    private JComponent createSquareEndImage(EndReason reason, Color accent, int size) {
        Image img = loadEndImage(reason, size, size);

        return new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 18;

                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRoundRect(4, 6, size, size, arc, arc);

                g2.setColor(new Color(30, 35, 45));
                g2.fillRoundRect(0, 0, size, size, arc, arc);

                Shape clip = new RoundRectangle2D.Float(0, 0, size, size, arc, arc);
                g2.setClip(clip);

                if (img != null) {
                    g2.drawImage(img, 0, 0, size, size, null);

                    g2.setPaint(new GradientPaint(
                            0, size * 0.6f, new Color(0, 0, 0, 0),
                            0, size, new Color(0, 0, 0, 120)
                    ));
                    g2.fillRect(0, 0, size, size);
                } else {
                    g2.setClip(null);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                    String icon = (reason == EndReason.WIN) ? "🏆" :
                            (reason == EndReason.GIVE_UP ? "🚩" : "💀");
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(Color.WHITE);
                    g2.drawString(icon, (size - fm.stringWidth(icon)) / 2, (size + fm.getAscent()) / 2 - 6);
                }

                g2.setClip(null);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 140));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, size - 1, size - 1, arc, arc);

                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
            @Override public Dimension getMaximumSize() { return getPreferredSize(); }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        };
    }

    private Image loadEndImage(EndReason reason, int w, int h) {
        String path = switch (reason) {
            case WIN -> "/images/win.jpeg";
            case LOST_NO_LIVES -> "/images/lose.jpeg";
            case GIVE_UP -> "/images/give_up.jpeg";
        };

        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Image not found: " + path);
                return null;
            }
            BufferedImage img = ImageIO.read(url);
            return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Font safeFont(String name, int style, int size) {
        Font f = new Font(name, style, size);
        if (!f.getFamily().equalsIgnoreCase(name)) return new Font("SansSerif", style, size);
        return f;
    }

    // ---------------- Animations ----------------

    private void startAnimations() {
        animationTimer = new Timer(16, e -> {
            if (cardScale < 1.0f) cardScale += (1.0f - cardScale) * 0.1f;
            if (cardOpacity < 1.0f) cardOpacity = Math.min(1f, cardOpacity + 0.06f);

            floatOffset += 0.04f;
            waveOffset += 0.05f;

            for (AnimatedParticle p : bgParticles) p.update();
            for (FloatingIcon ic : floatingIcons) ic.update();

            celebrationParticles.removeIf(CelebrationParticle::isDead);
            for (CelebrationParticle p : celebrationParticles) p.update();

            repaint();
        });
        animationTimer.start();
    }

    private void initializeBackground() {
        for (int i = 0; i < 60; i++) bgParticles.add(new AnimatedParticle(random));

        String[] icons = {"💣", "🚩", "💎", "⭐", "🎯"};
        for (int i = 0; i < 8; i++) {
            floatingIcons.add(new FloatingIcon(random, icons[random.nextInt(icons.length)]));
        }
    }

    private void initializeCelebrationParticles(Color color) {
        float cx = DIALOG_SIZE.width / 2f;
        float cy = DIALOG_SIZE.height / 2f - 80;

        for (int i = 0; i < 90; i++) {
            celebrationParticles.add(new CelebrationParticle(
                    cx, cy,
                    (random.nextFloat() - 0.5f) * 9,
                    (random.nextFloat() - 0.7f) * 12,
                    color
            ));
        }
    }

    private void playEndSfx(EndReason reason) {
        try {
            AudioManager.play(reason == EndReason.WIN ? AudioManager.Sfx.WIN : AudioManager.Sfx.GAME_OVER);
        } catch (Exception e) {
            System.out.println("Audio playback skipped");
        }
    }

    private void runAndKeepFrameSize(Runnable action) {
        if (animationTimer != null) animationTimer.stop();
        dispose();
        if (action != null) SwingUtilities.invokeLater(action);
    }

    private String stripHtml(String s) {
        return s == null ? "" : s.replaceAll("<[^>]*>", "");
    }

    @Override
    public void dispose() {
        if (animationTimer != null) animationTimer.stop();
        super.dispose();
    }

    // ---------------- Inner classes ----------------

    // ✅ Private-style particle
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

    // ✅ Floating icon
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

    // ✅ Celebration particle
    private static class CelebrationParticle {
        float x, y, vx, vy, life = 1.0f;
        Color color;

        CelebrationParticle(float x, float y, float vx, float vy, Color c) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = c;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.20f;
            life -= 0.012f;
        }

        boolean isDead() { return life <= 0; }
    }
}
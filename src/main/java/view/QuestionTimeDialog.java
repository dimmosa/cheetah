package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;


public class QuestionTimeDialog extends JDialog {

    private static final int ARC = 26;

    private int selectedAnswerIndex = -1;
    private JButton[] optionButtons;

    // Animations
    private float fade = 0f;
    private Timer fadeTimer;
    private Timer headerGlowTimer;
    private float headerGlow = 0f;
    private boolean headerGlowUp = true;

    // Toast
    private JPanel toast;
    private Timer toastTimer;

    // UI
    private JPanel root;
    private JPanel contentLayer;

    public QuestionTimeDialog(Frame owner,
                              String difficulty,
                              String question,
                              String[] options,
                              Consumer<Integer> onAnswerConfirmed) {

        super(owner, "Question Time", true);

        // Window setup
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // ✅ Critical: keep the dialog OPAQUE to avoid white rendering glitches on some systems
        setBackground(new Color(10, 14, 28));

        setSize(580, 720);
        setLocationRelativeTo(owner);

        // Root panel paints everything
        root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Backdrop (slightly darker so dialog edges look clean)
                g2.setColor(new Color(6, 9, 18));
                g2.fillRect(0, 0, w, h);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRoundRect(10, 10, w - 20, h - 20, ARC + 8, ARC + 8);

                // Main card gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(10, 18, 45),
                        w, h, new Color(18, 30, 70)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 12, h - 12, ARC, ARC);

                // Top neon line
                GradientPaint neon = new GradientPaint(
                        0, 0, new Color(0, 205, 255, 220),
                        w, 0, new Color(0, 230, 120, 220)
                );
                g2.setPaint(neon);
                g2.fillRoundRect(0, 0, w - 12, 7, ARC, ARC);

                // Border
                g2.setColor(new Color(90, 110, 150, 90));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 13, h - 13, ARC, ARC);

                // Fade overlay (for fade-in animation)
                if (fade < 1f) {
                    g2.setColor(new Color(0, 0, 0, (int) ((1f - fade) * 200)));
                    g2.fillRoundRect(0, 0, w - 12, h - 12, ARC, ARC);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        applyRoundedShape();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { applyRoundedShape(); layoutChildren(); }
            @Override public void componentShown(ComponentEvent e) { layoutChildren(); }
        });

        // Build content layer (so we can position toast over it)
        contentLayer = new JPanel();
        contentLayer.setOpaque(false);
        contentLayer.setLayout(new BorderLayout(0, 16));
        contentLayer.setBorder(new EmptyBorder(26, 26, 22, 26));
        root.add(contentLayer);

        // Header
        JPanel header = buildHeader(difficulty);

        // Question glass card (NO gray background)
        JPanel questionCard = buildQuestionCard(question);

        // Options area (scrollable if needed)
        JComponent optionsArea = buildOptionsArea(options);

        // Footer (confirm)
        JPanel footer = buildFooter(onAnswerConfirmed);

        // Center stack
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(questionCard);
        center.add(Box.createVerticalStrut(16));
        center.add(optionsArea);

        contentLayer.add(header, BorderLayout.NORTH);
        contentLayer.add(center, BorderLayout.CENTER);
        contentLayer.add(footer, BorderLayout.SOUTH);

        // Toast overlay
        toast = createToast();
        toast.setVisible(false);
        root.add(toast);

        // Block Alt+F4 / close: already DO_NOTHING
        // Optional: allow ESC to close? (you blocked close in original)
        // getRootPane().registerKeyboardAction(e -> dispose(),
        //         KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        //         JComponent.WHEN_IN_FOCUSED_WINDOW);

        layoutChildren();

        // Start animations
        startFadeIn();
        startHeaderGlow();
    }

    // ---------------- Layout helpers ----------------

    private void layoutChildren() {
        Insets in = root.getInsets();
        int w = getWidth() - in.left - in.right;
        int h = getHeight() - in.top - in.bottom;

        // Root uses null-layout only to position contentLayer + toast.
        contentLayer.setBounds(0, 0, w, h);

        // Toast stays top-center
        int tw = 360;
        int th = 54;
        toast.setSize(tw, th);
        toast.setLocation((w - tw) / 2, 16);
    }

    private void applyRoundedShape() {
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ARC, ARC));
    }

    // ---------------- UI builders ----------------

    private JPanel buildHeader(String difficulty) {
        Color diff = colorForDifficulty(difficulty);

        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle glow behind title
                int w = getWidth();
                int glowAlpha = (int) (50 + headerGlow * 70);
                g2.setColor(new Color(255, 190, 40, glowAlpha));
                g2.fillOval(w / 2 - 120, 6, 240, 60);

                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("❓  Question Time!");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(255, 195, 55));

        JLabel lvl = new JLabel(("LEVEL: " + (difficulty == null ? "EASY" : difficulty)).toUpperCase());
        lvl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lvl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lvl.setForeground(diff);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(lvl);

        return header;
    }

    private JPanel buildQuestionCard(String question) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Glass gradient (no gray block)
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 18),
                        w, h, new Color(255, 255, 255, 8)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                // Border
                g2.setColor(new Color(110, 130, 180, 70));
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JTextArea qText = new JTextArea(question);
        qText.setOpaque(false);
        qText.setEditable(false);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        qText.setFont(new Font("SansSerif", Font.PLAIN, 18));
        qText.setForeground(new Color(240, 245, 255));

        card.add(qText, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildOptionsArea(String[] options) {
        JPanel list = new JPanel(new GridLayout(options.length, 1, 0, 14));
        list.setOpaque(false);

        optionButtons = new JButton[options.length];
        String[] labels = {"A", "B", "C", "D"};

        for (int i = 0; i < options.length; i++) {
            String prefix = (i < labels.length ? labels[i] + ". " : (i + 1) + ". ");
            JButton b = createOptionButton(prefix + options[i], i);
            optionButtons[i] = b;
            list.add(b);
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(14);

        // Make the scrollbar nicer (simple)
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(120, 160, 220, 120);
                trackColor = new Color(0, 0, 0, 0);
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                b.setOpaque(false);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                return b;
            }
        });

        // Wrap scroll pane in a subtle transparent container so it breathes
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildFooter(Consumer<Integer> onAnswerConfirmed) {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JButton ok = createConfirmButton("CONFIRM ANSWER");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(e -> {
            if (selectedAnswerIndex == -1) {
                showToast("Please select an answer first!", new Color(255, 175, 70));
                shake(contentLayer);
                return;
            }
            int chosen = selectedAnswerIndex;
            dispose();
            if (onAnswerConfirmed != null) SwingUtilities.invokeLater(() -> onAnswerConfirmed.accept(chosen));
        });

        JLabel hint = new JLabel("Tip: Hover an option to highlight it ✨");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(new Color(165, 175, 195));

        footer.add(ok);
        footer.add(Box.createVerticalStrut(10));
        footer.add(hint);

        return footer;
    }

    // ---------------- Buttons ----------------

    private JButton createOptionButton(String text, int index) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                boolean hovered = Boolean.TRUE.equals(getClientProperty("hover"));
                boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));
                boolean pressed = getModel().isArmed();

                Color base = new Color(18, 26, 46);
                Color border = new Color(95, 110, 150, 90);

                if (hovered) {
                    base = new Color(22, 33, 58);
                    border = new Color(140, 175, 240, 140);
                }
                if (pressed) {
                    base = new Color(26, 40, 70);
                }
                if (selected) {
                    base = new Color(0, 185, 255, 55);
                    border = new Color(0, 195, 255);
                }

                // Background
                g2.setColor(base);
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                // Glow when selected / hovered
                if (selected || hovered) {
                    Color glow = selected ? new Color(0, 200, 255, 60) : new Color(0, 200, 255, 28);
                    g2.setColor(glow);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);
                }

                // Border
                g2.setStroke(new BasicStroke(selected ? 2.3f : 1.2f));
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(14, 16, 14, 16));
        btn.setPreferredSize(new Dimension(10, 56));

        // Hover
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.putClientProperty("hover", true); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e) { btn.putClientProperty("hover", false); btn.repaint(); }
        });

        btn.addActionListener(e -> selectOption(index));
        return btn;
    }

    private JButton createConfirmButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                boolean hovered = Boolean.TRUE.equals(getClientProperty("hover"));
                boolean pressed = getModel().isArmed();

                Color a = new Color(0, 230, 120);
                Color b = new Color(0, 195, 255);

                if (hovered) { a = new Color(0, 245, 140); b = new Color(30, 210, 255); }
                if (pressed) { a = new Color(0, 200, 105); b = new Color(0, 170, 230); }

                GradientPaint gp = new GradientPaint(0, 0, a, w, 0, b);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                // Shine
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, w, h / 2, 18, 18);

                // Border
                g2.setColor(new Color(255, 255, 255, 45));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setPreferredSize(new Dimension(10, 52));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.putClientProperty("hover", true); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e) { btn.putClientProperty("hover", false); btn.repaint(); }
        });

        return btn;
    }

    private void selectOption(int idx) {
        selectedAnswerIndex = idx;
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].putClientProperty("selected", i == idx);
            optionButtons[i].repaint();
        }
    }

    // ---------------- Toast + animations ----------------

    private JPanel createToast() {
        JPanel t = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color accent = (Color) getClientProperty("accent");
                if (accent == null) accent = new Color(255, 175, 70);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRoundRect(6, 6, w - 6, h - 6, 18, 18);

                // Body
                g2.setColor(new Color(15, 22, 40, 245));
                g2.fillRoundRect(0, 0, w - 8, h - 8, 18, 18);

                // Accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, w - 8, 5, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        t.setOpaque(false);
        t.setLayout(new BorderLayout());
        t.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lbl = new JLabel("");
        lbl.setName("toastLabel");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.add(lbl, BorderLayout.CENTER);

        t.setSize(360, 54);
        return t;
    }

    private void showToast(String msg, Color accent) {
        if (toastTimer != null) toastTimer.stop();

        JLabel lbl = findToastLabel(toast);
        if (lbl != null) lbl.setText(msg);
        toast.putClientProperty("accent", accent);

        toast.setVisible(true);
        toast.repaint();

        toastTimer = new Timer(1400, e -> toast.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private JLabel findToastLabel(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JLabel lbl && "toastLabel".equals(lbl.getName())) return lbl;
            if (child instanceof Container cc) {
                JLabel found = findToastLabel(cc);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void shake(JComponent comp) {
        Point p = comp.getLocation();
        final int dx = 6;
        final int[] n = {0};

        Timer t = new Timer(18, null);
        t.addActionListener(e -> {
            int i = n[0]++;
            int offset = (i % 2 == 0) ? dx : -dx;
            comp.setLocation(p.x + offset, p.y);

            if (i >= 10) {
                comp.setLocation(p);
                t.stop();
            }
        });
        t.start();
    }

    private void startFadeIn() {
        fade = 0f;
        try { setOpacity(0f); } catch (Exception ignored) { /* Some systems may not support; safe */ }

        fadeTimer = new Timer(16, e -> {
            fade += 0.08f;
            if (fade >= 1f) {
                fade = 1f;
                fadeTimer.stop();
            }
            try { setOpacity(Math.min(1f, fade)); } catch (Exception ignored) { }
            root.repaint();
        });
        fadeTimer.start();
    }

    private void startHeaderGlow() {
        headerGlowTimer = new Timer(30, e -> {
            if (headerGlowUp) headerGlow += 0.03f;
            else headerGlow -= 0.03f;

            if (headerGlow >= 1f) { headerGlow = 1f; headerGlowUp = false; }
            if (headerGlow <= 0f) { headerGlow = 0f; headerGlowUp = true; }

            // Only repaint top region lightly
            root.repaint(0, 0, getWidth(), 120);
        });
        headerGlowTimer.start();

        // stop timers when dialog closes
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { stopTimers(); }
        });
    }

    private void stopTimers() {
        if (fadeTimer != null) fadeTimer.stop();
        if (headerGlowTimer != null) headerGlowTimer.stop();
        if (toastTimer != null) toastTimer.stop();
    }

    // ---------------- Colors ----------------

    private Color colorForDifficulty(String difficulty) {
        if (difficulty == null) return new Color(0, 230, 120);
        String d = difficulty.trim().toUpperCase();
        return switch (d) {
            case "MEDIUM" -> new Color(255, 175, 70);
            case "HARD", "EXPERT" -> new Color(255, 85, 95);
            default -> new Color(0, 230, 120);
        };
    }
}

package view;

import model.GameHistoryEntry;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class GameHistoryScreen extends JPanel {

    private final JFrame frame;
    private final List<GameHistoryEntry> allRecords;
    private final String mode;
    private final String loggedInUser;

    private JPanel historyContainer;
    private JTextField searchField;
    private JComboBox<String> typeFilter, sortFilter;

    // Background animation (same vibe as LoginPrivateScreen)
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private final Random random = new Random();

    // Colors (modern, no orange)
    private static final Color BG_DARK = new Color(2, 5, 15);
    private static final Color BG_GRAD = new Color(10, 20, 40);

    private static final Color CARD_BG = new Color(15, 25, 40, 230);
    private static final Color CARD_HOVER = new Color(20, 35, 55, 245);

    private static final Color ACCENT_BLUE = new Color(0, 180, 255);
    private static final Color ACCENT_CYAN = new Color(0, 220, 100);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);   // replaces orange

    private static final Color WIN_GREEN = new Color(34, 197, 94);
    private static final Color LOSE_RED = new Color(239, 68, 68);

    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(203, 213, 225);
    private static final Color TEXT_GRAY = new Color(148, 163, 184);

    private static final DateTimeFormatter DISPLAY_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public GameHistoryScreen(JFrame frame, List<GameHistoryEntry> records, String mode, String loggedInUser) {
        this.frame = frame;
        this.allRecords = records == null ? new ArrayList<>() : new ArrayList<>(records);
        this.mode = mode == null ? "single" : mode;
        this.loggedInUser = loggedInUser;

        setLayout(new BorderLayout());
        setOpaque(false);

        add(createTopBar(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);

        initAnimationBackground();
        refreshList();
    }

    public GameHistoryScreen(JFrame frame, List<GameHistoryEntry> records, String mode) {
        this(frame, records, mode, null);
    }

    // =========================
    // UI
    // =========================
    private JPanel createTopBar() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(24, 40, 16, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("💣 Game History");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(TEXT_WHITE);

        JButton backBtn = createButton("← Back");
        backBtn.addActionListener(e -> {
            stopAnimation();
            if ("multi".equalsIgnoreCase(mode)) frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            else frame.setContentPane(new MainMenuPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        wrap.add(header);
        wrap.add(Box.createVerticalStrut(16));
        wrap.add(createFilterRow());

        return wrap;
    }

    private JPanel createFilterRow() {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        searchField = createSearchField("🔍 Search by player / difficulty / date / win...");
        typeFilter = createCombo(new String[]{"All Types", "Practice", "Multiplayer"}, 190);
        sortFilter = createCombo(new String[]{"Newest First", "Oldest First", "Highest Score"}, 220);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(typeFilter);
        right.add(sortFilter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
        });

        typeFilter.addActionListener(e -> refreshList());
        sortFilter.addActionListener(e -> refreshList());

        row.add(searchField, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent createCenter() {
        historyContainer = new JPanel();
        historyContainer.setOpaque(false);
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setBorder(new EmptyBorder(12, 40, 28, 40));

        JScrollPane scrollPane = new JScrollPane(historyContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void refreshList() {
        historyContainer.removeAll();

        List<GameHistoryEntry> filtered = filterByUser(allRecords);

        // Type filter
        String tf = (String) typeFilter.getSelectedItem();
        if ("Practice".equals(tf)) filtered.removeIf(r -> !isPractice(r));
        else if ("Multiplayer".equals(tf)) filtered.removeIf(this::isPractice);

        // Search
        String q = searchField.getText();
        if (q != null && !q.trim().isEmpty()) {
            String qq = q.trim().toLowerCase();
            filtered.removeIf(r -> !matchesSearch(r, qq));
        }

        // Sort
        sortRecords(filtered);

        if (filtered.isEmpty()) {
            showEmpty();
            historyContainer.revalidate();
            historyContainer.repaint();
            return;
        }

        for (int i = 0; i < filtered.size(); i++) {
            JPanel card = createCard(filtered.get(i));
            card.setVisible(false);
            historyContainer.add(card);
            historyContainer.add(Box.createVerticalStrut(14));

            Timer t = new Timer(Math.min(45 * i, 450), e -> {
                card.setVisible(true);
                historyContainer.revalidate();
                historyContainer.repaint();
            });
            t.setRepeats(false);
            t.start();
        }

        historyContainer.revalidate();
        historyContainer.repaint();
    }

    private JPanel createCard(GameHistoryEntry r) {
        boolean practice = isPractice(r);
        boolean won = safeIsWon(r);

        Color accent = practice ? ACCENT_PURPLE : (won ? WIN_GREEN : LOSE_RED);

        JPanel card = new JPanel(new BorderLayout(18, 0)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                // subtle border
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(CARD_HOVER); }
            public void mouseExited(MouseEvent e) { card.setBackground(CARD_BG); }
        });

        card.add(createLeftBadge(practice, won, accent), BorderLayout.WEST);
        card.add(createCardCenter(r, practice, won, accent), BorderLayout.CENTER);
        card.add(createRightScore(r, accent), BorderLayout.EAST);

        return card;
    }

    private JPanel createLeftBadge(boolean practice, boolean won, Color accent) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(110, 170));

        // Big status
        JLabel status = new JLabel(practice ? "PRACTICE" : (won ? "WIN" : "LOSE"));
        status.setFont(new Font("SansSerif", Font.BOLD, 18));
        status.setForeground(accent);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Icon bubble
        JPanel bubble = new JPanel() {
            float pulse = 0f;
            Timer t = new Timer(30, e -> { pulse += 0.06f; repaint(); });
            { t.start(); }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                float s = (float) (Math.sin(pulse) * 0.15 + 0.85);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(65 * s)));
                g2.fillOval(0, 0, w, h);

                g2.setPaint(new GradientPaint(0, 0, accent, w, h, accent.darker()));
                g2.fillOval(8, 8, w - 16, h - 16);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
                String icon = practice ? "⚙" : (won ? "🏆" : "💥");
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, w / 2 - fm.stringWidth(icon) / 2, h / 2 + fm.getAscent() / 2 - 2);

                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setPreferredSize(new Dimension(64, 64));
        bubble.setMaximumSize(new Dimension(64, 64));
        bubble.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(bubble);
        p.add(Box.createVerticalStrut(10));
        p.add(status);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel createCardCenter(GameHistoryEntry r, boolean practice, boolean won, Color accent) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Players row
        String p1 = safeStr(getPlayer1(r));
        String p2 = safeStr(getPlayer2(r));
        if (practice) p2 = "Practice Mode";

        JLabel players = new JLabel(practice ? ("👤 " + p1) : ("👤 " + p1 + "   vs   👤 " + p2));
        players.setFont(new Font("SansSerif", Font.BOLD, 18));
        players.setForeground(TEXT_WHITE);

        // Date row
        String ts = formatTimestampForDisplay(r);
        JLabel date = new JLabel(ts.isEmpty() ? "📅 Recent" : "📅 " + ts);
        date.setFont(new Font("SansSerif", Font.PLAIN, 14));
        date.setForeground(TEXT_GRAY);

        // Mode + Difficulty
        JLabel type = new JLabel("🎮 " + (practice ? "Practice" : "Multiplayer"));
        type.setFont(new Font("SansSerif", Font.BOLD, 16));
        type.setForeground(accent);

        JLabel diff = new JLabel("🎯 Difficulty: " + safeStr(getDifficulty(r)));
        diff.setFont(new Font("SansSerif", Font.PLAIN, 14));
        diff.setForeground(TEXT_MUTED);

        // Stats grid (bigger, clearer)
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(520, 78));

        grid.add(createStatBig("Score", safeInt(r.getFinalScore()) + " pts", ACCENT_CYAN));
        grid.add(createStatBig("Time", formatTime(safeInt(r.getDurationSeconds())), ACCENT_BLUE));

        // Questions + Flags (robust reflection)
        String qStats = getQuestionStatsRobust(r);
        String fStats = getFlagStatsRobust(r);

        grid.add(createStatBig("Questions", qStats, ACCENT_PURPLE));
        grid.add(createStatBig("Flags", fStats, ACCENT_BLUE));

        p.add(players);
        p.add(Box.createVerticalStrut(6));
        p.add(date);
        p.add(Box.createVerticalStrut(10));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.add(type);
        row.add(diff);

        p.add(row);
        p.add(Box.createVerticalStrut(12));
        p.add(grid);

        return p;
    }

    private JPanel createRightScore(GameHistoryEntry r, Color accent) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(190, 170));

        JLabel lbl = new JLabel("FINAL SCORE");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(TEXT_GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel val = new JLabel(String.valueOf(safeInt(r.getFinalScore())));
        val.setFont(new Font("SansSerif", Font.BOLD, 40));
        val.setForeground(accent);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pts = new JLabel("POINTS");
        pts.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pts.setForeground(TEXT_GRAY);
        pts.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(val);
        p.add(Box.createVerticalStrut(4));
        p.add(pts);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel createStatBig(String label, String value, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(TEXT_GRAY);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel v = new JLabel(value);
        v.setForeground(valueColor);
        v.setFont(new Font("SansSerif", Font.BOLD, 14));
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JTextField createSearchField(String ph) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 25, 40), getWidth(), 0, new Color(20, 35, 55)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                g2.setColor(isFocusOwner() ? ACCENT_BLUE : new Color(40, 50, 70));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, arc - 2, arc - 2);
                g2.dispose();

                super.paintComponent(g);

                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D gg = (Graphics2D) g.create();
                    gg.setColor(TEXT_GRAY);
                    gg.setFont(getFont());
                    FontMetrics fm = gg.getFontMetrics();
                    gg.drawString(ph, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    gg.dispose();
                }
            }
        };

        f.setOpaque(false);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(TEXT_WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(new EmptyBorder(12, 20, 12, 20));
        f.setPreferredSize(new Dimension(0, 48));
        return f;
    }

    private JComboBox<String> createCombo(String[] items, int w) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setOpaque(false);
        c.setForeground(TEXT_WHITE);
        c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.setPreferredSize(new Dimension(w, 48));
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));

        c.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object val, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, val, i, sel, foc);
                setBorder(new EmptyBorder(10, 16, 10, 16));
                setForeground(TEXT_WHITE);
                setBackground(sel ? ACCENT_BLUE : new Color(15, 25, 40));
                return this;
            }
        });
        return c;
    }

    private JButton createButton(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 42));

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 25, 40, 210));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 28));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 18, 18);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return b;
    }

    private void showEmpty() {
        historyContainer.add(Box.createVerticalStrut(90));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel i = new JLabel("🗂️");
        i.setFont(new Font("SansSerif", Font.PLAIN, 62));
        i.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel m = new JLabel("No games found");
        m.setForeground(TEXT_GRAY);
        m.setFont(new Font("SansSerif", Font.BOLD, 18));
        m.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tip = new JLabel("Try changing filters or searching by player name.");
        tip.setForeground(new Color(100, 120, 150));
        tip.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(i);
        p.add(Box.createVerticalStrut(14));
        p.add(m);
        p.add(Box.createVerticalStrut(8));
        p.add(tip);

        historyContainer.add(p);
    }

    // =========================
    // Background animation (like LoginPrivateScreen)
    // =========================
    private void initAnimationBackground() {
        particles = new ArrayList<>();
        for (int i = 0; i < 60; i++) particles.add(new AnimatedParticle(random));

        animationTimer = new Timer(30, e -> {
            for (AnimatedParticle p : particles) p.update();
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
            x = r.nextFloat() * 1920;
            y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 1.2f;
            vy = (r.nextFloat() - 0.5f) * 1.2f;
            size = r.nextFloat() * 8 + 2;
            alpha = r.nextFloat() * 0.5f + 0.3f;
            pulseSpeed = r.nextFloat() * 0.05f + 0.02f;
            pulseOffset = r.nextFloat() * (float) Math.PI * 2;

            int c = r.nextInt(5);
            if (c == 0) color = new Color(0, 180, 255);
            else if (c == 1) color = new Color(0, 220, 100);
            else if (c == 2) color = new Color(100, 150, 255);
            else if (c == 3) color = new Color(0, 255, 200);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), BG_GRAD);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (particles != null) {
            for (AnimatedParticle p : particles) p.draw(g2, getWidth(), getHeight());
        }

        g2.dispose();
    }

    // =========================
    // Filters / search / sort
    // =========================
    private List<GameHistoryEntry> filterByUser(List<GameHistoryEntry> recs) {
        if (loggedInUser == null || loggedInUser.trim().isEmpty()) return new ArrayList<>(recs);
        String u = loggedInUser.trim().toLowerCase();
        List<GameHistoryEntry> res = new ArrayList<>();
        for (GameHistoryEntry r : recs) {
            String p1 = safeLower(getPlayer1(r));
            String p2 = safeLower(getPlayer2(r));
            if (u.equals(p1) || u.equals(p2)) res.add(r);
        }
        return res;
    }

    private boolean matchesSearch(GameHistoryEntry r, String q) {
        String diff = safeLower(getDifficulty(r));
        String ts = safeLower(extractTimestamp(r));
        String p1 = safeLower(getPlayer1(r));
        String p2 = safeLower(getPlayer2(r));

        String wl = safeIsWon(r) ? "win won victory" : "lose lost defeat";
        String type = isPractice(r) ? "practice" : "multiplayer vs";

        return diff.contains(q) || ts.contains(q) || p1.contains(q) || p2.contains(q) || wl.contains(q) || type.contains(q);
    }

    private void sortRecords(List<GameHistoryEntry> recs) {
        String s = (String) sortFilter.getSelectedItem();
        if ("Oldest First".equals(s)) {
            recs.sort(Comparator.comparingLong(this::extractTimestampMillis));
        } else if ("Highest Score".equals(s)) {
            recs.sort((a, b) -> Integer.compare(safeInt(b.getFinalScore()), safeInt(a.getFinalScore())));
        } else { // Newest
            recs.sort((a, b) -> Long.compare(extractTimestampMillis(b), extractTimestampMillis(a)));
        }
    }

    // =========================
    // Practice / Win / Lose
    // =========================
    private boolean safeIsWon(GameHistoryEntry r) {
        try { return r != null && r.isWon(); }
        catch (Exception e) { return false; }
    }

    private boolean isPractice(GameHistoryEntry r) {
        String p2 = safeStr(getPlayer2(r)).trim();
        return p2.isEmpty()
                || p2.equalsIgnoreCase("Practice")
                || p2.equalsIgnoreCase("Practice Mode");
    }

    // =========================
    // Timestamp parsing
    // =========================
    private long extractTimestampMillis(GameHistoryEntry r) {
        String ts = extractTimestamp(r);
        if (ts == null) return 0L;
        ts = ts.trim();
        if (ts.isEmpty()) return 0L;

        // epoch numeric
        if (ts.matches("\\d+")) {
            try {
                long n = Long.parseLong(ts);
                return (ts.length() <= 10) ? n * 1000L : n;
            } catch (Exception ignored) {}
        }

        // ISO Instant
        try {
            return Instant.parse(ts).toEpochMilli();
        } catch (Exception ignored) {}

        // common patterns
        DateTimeFormatter[] fmts = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        };

        for (DateTimeFormatter f : fmts) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(ts, f);
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
        }

        // date-only
        DateTimeFormatter[] dateOnly = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };

        for (DateTimeFormatter f : dateOnly) {
            try {
                LocalDate d = LocalDate.parse(ts, f);
                return d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception ignored) {}
        }

        return 0L;
    }

    private String formatTimestampForDisplay(GameHistoryEntry r) {
        long ms = extractTimestampMillis(r);
        if (ms <= 0) return "";
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
        return DISPLAY_TS.format(ldt);
    }

    private String extractTimestamp(GameHistoryEntry r) {
        if (r == null) return "";

        // try methods
        for (String m : new String[]{"getTimestamp", "getDate", "getTime", "getCreatedAt"}) {
            Object v = tryInvoke(r, m);
            if (v != null) return String.valueOf(v);
        }

        // try fields
        for (String f : new String[]{"timestamp", "date", "time", "createdAt"}) {
            Object v = tryField(r, f);
            if (v != null) return String.valueOf(v);
        }

        return "";
    }

    // =========================
    // Robust Questions/Flags (fix your N/A)
    // =========================
    private String getQuestionStatsRobust(GameHistoryEntry r) {
        Integer total = getIntByNames(r,
                new String[]{"getQuestionsAnswered", "getTotalQuestions", "getQuestionsTotal", "getTotalAnswered"},
                new String[]{"questionsAnswered", "totalQuestions", "questionsTotal", "questions"}
        );
        Integer correct = getIntByNames(r,
                new String[]{"getQuestionsCorrect", "getCorrectAnswers", "getCorrectQuestions"},
                new String[]{"questionsCorrect", "correctAnswers", "correctQuestions"}
        );

        if (total == null || correct == null) return "N/A";
        return correct + "/" + total;
    }

    private String getFlagStatsRobust(GameHistoryEntry r) {
        Integer total = getIntByNames(r,
                new String[]{"getMinesFlagged", "getFlagsPlaced", "getTotalFlags"},
                new String[]{"minesFlagged", "flagsPlaced", "totalFlags"}
        );
        Integer correct = getIntByNames(r,
                new String[]{"getCorrectFlags", "getCorrectFlagCount"},
                new String[]{"correctFlags", "correctFlagCount"}
        );

        if (total == null || correct == null) return "N/A";
        return correct + "/" + total;
    }

    private Integer getIntByNames(Object obj, String[] methodNames, String[] fieldNames) {
        if (obj == null) return null;

        for (String m : methodNames) {
            Object v = tryInvoke(obj, m);
            Integer n = toInt(v);
            if (n != null) return n;
        }

        for (String f : fieldNames) {
            Object v = tryField(obj, f);
            Integer n = toInt(v);
            if (n != null) return n;
        }

        return null;
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v).trim()); }
        catch (Exception e) { return null; }
    }

    private Object tryInvoke(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            return m.invoke(obj);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object tryField(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ignored) {
            return null;
        }
    }

    // =========================
    // Safe getters
    // =========================
    private String getPlayer1(GameHistoryEntry r) { try { return r.getPlayer1(); } catch (Exception e) { return ""; } }
    private String getPlayer2(GameHistoryEntry r) { try { return r.getPlayer2(); } catch (Exception e) { return ""; } }
    private String getDifficulty(GameHistoryEntry r) { try { return r.getDifficulty(); } catch (Exception e) { return ""; } }

    private String formatTime(int sec) {
        sec = Math.max(sec, 0);
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    private int safeInt(int x) { return x; }
    private String safeStr(String s) { return s == null ? "" : s; }
    private String safeLower(String s) { return safeStr(s).toLowerCase(); }
}

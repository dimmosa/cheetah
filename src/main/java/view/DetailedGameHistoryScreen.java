package view;

import model.DetailedGameHistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

import javax.swing.Timer;


public class DetailedGameHistoryScreen extends JPanel {

    private JFrame frame;
   
    private javax.swing.Timer bgTimer1;

    private void startBackgroundAnimation1() {
        if (bgTimer1 != null) bgTimer1.stop();
        bgTimer1 = new javax.swing.Timer(40, e -> {
            for (Particle p : particles) p.update(getWidth(), getHeight());
            repaint();
        });
        bgTimer1.start();
    }



    // Keep original data (never modified)
    private final List<DetailedGameHistoryEntry> allRecords;

    // What we actually display (filtered + sorted)
    private List<DetailedGameHistoryEntry> records;

    private JPanel historyContainer;

    // --- Modern Theme Colors ---
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color BG_GRAD_2 = new Color(30, 41, 59);
    private static final Color CARD_BG = new Color(30, 41, 59, 235);
    private static final Color CARD_HOVER = new Color(51, 65, 85, 245);

    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_CYAN = new Color(34, 211, 238);

    private static final Color WIN_GREEN = new Color(34, 197, 94);
    private static final Color LOSE_RED = new Color(239, 68, 68);

    private static final Color TEXT_DIM = new Color(148, 163, 184);
    private static final Color TEXT_PRIMARY = new Color(235, 242, 255);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font SMALL_CAPS = new Font("Segoe UI", Font.BOLD, 11);

    // Sort UI
    private JComboBox<String> sortCombo;
    private String currentSort = "Newest First";

    // Date filter UI
    private JSpinner dateSpinner;
    private JCheckBox enableDateFilter;

    // Background animation
    private final java.util.List<Particle> particles = new ArrayList<>();
    private final Random rnd = new Random();
    private Timer bgTimer;

    public DetailedGameHistoryScreen(JFrame frame, List<DetailedGameHistoryEntry> records) {
        this.frame = frame;

        this.allRecords = new ArrayList<>(records);
        this.records = new ArrayList<>(records);

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(BG_DARK);

        initParticles();
        startBackgroundAnimation1();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        topPanel.add(createHeaderPanel());
        topPanel.add(createFilterPanel());

        add(topPanel, BorderLayout.NORTH);
        add(createHistoryScrollPane(), BorderLayout.CENTER);

        // default sort
        applySort(currentSort);
        refreshUI();
    }

    // =============================
    // TOP: Header
    // =============================

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 40, 10, 40));

        JButton backBtn = createSmallBackButton();

        JLabel title = new JLabel("GAME HISTORY", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);

        panel.add(backBtn, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(120), BorderLayout.EAST);
        return panel;
    }

    private JButton createSmallBackButton() {
        JButton backBtn = new JButton("← Back") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(17, 27, 43, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 28));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setForeground(new Color(241, 245, 249));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 38));

        backBtn.addActionListener(e -> {
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            frame.revalidate();
        });

        return backBtn;
    }
    
    

    // =============================
    // TOP: Filter panel (Sort + Date)
    // =============================

    private JPanel createFilterPanel() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 40, 16, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 12);
        gc.anchor = GridBagConstraints.WEST;

        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setForeground(TEXT_DIM);
        sortLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String[] options = {"Newest First", "Oldest First", "Highest Score", "Difficulty"};
        sortCombo = createPillCombo(options);
        sortCombo.setSelectedItem(currentSort);
        sortCombo.addActionListener(e -> {
            currentSort = (String) sortCombo.getSelectedItem();
            applySort(currentSort);
            refreshUI();
        });

        // Date filter
        enableDateFilter = new JCheckBox("Filter by date");
        enableDateFilter.setOpaque(false);
        enableDateFilter.setForeground(TEXT_DIM);
        enableDateFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        enableDateFilter.setFocusPainted(false);

        dateSpinner = createDateSpinner();
        dateSpinner.setEnabled(false);

        enableDateFilter.addActionListener(e -> {
            dateSpinner.setEnabled(enableDateFilter.isSelected());
            applyDateFilterAndSort();
        });

        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setForeground(TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setCaretColor(ACCENT_BLUE);

        dateSpinner.addChangeListener(e -> applyDateFilterAndSort());

        JButton showAllBtn = createGhostButton("Show All");
        showAllBtn.addActionListener(e -> {
            enableDateFilter.setSelected(false);
            dateSpinner.setEnabled(false);
            records = new ArrayList<>(allRecords);
            applySort(currentSort);
            refreshUI();
        });

        // Layout
        gc.gridx = 0; bar.add(sortLabel, gc);
        gc.gridx = 1; bar.add(sortCombo, gc);

        gc.gridx = 2; bar.add(Box.createHorizontalStrut(18), gc);

        gc.gridx = 3; bar.add(enableDateFilter, gc);
        gc.gridx = 4; bar.add(dateSpinner, gc);
        gc.gridx = 5; bar.add(showAllBtn, gc);

        gc.weightx = 1;
        gc.gridx = 6;
        bar.add(Box.createHorizontalGlue(), gc);

        return bar;
    }

    private void applyDateFilterAndSort() {
        if (!enableDateFilter.isSelected()) {
            records = new ArrayList<>(allRecords);
        } else {
            Date selected = (Date) dateSpinner.getValue();
            LocalDate sel = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            List<DetailedGameHistoryEntry> filtered = new ArrayList<>();
            for (DetailedGameHistoryEntry r : allRecords) {
                // r.getTimestamp() is String, so we match by prefix "YYYY-MM-DD"
                // Example: "2025-11-25 22:46:20"
                String ts = r.getTimestamp();
                if (ts != null && ts.length() >= 10) {
                    String datePart = ts.substring(0, 10);
                    LocalDate rowDate;
                    try {
                        rowDate = LocalDate.parse(datePart);
                        if (rowDate.equals(sel)) filtered.add(r);
                    } catch (Exception ignored) {}
                }
            }
            records = filtered;
        }

        applySort(currentSort);
        refreshUI();
    }

    // =============================
    // Scroll + cards
    // =============================

    private JScrollPane createHistoryScrollPane() {
        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setOpaque(false);
        historyContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        fillHistoryContainer();

        JScrollPane scrollPane = new JScrollPane(historyContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        return scrollPane;
    }

    private void fillHistoryContainer() {
        historyContainer.removeAll();

        if (records.isEmpty()) {
            showEmptyState();
            return;
        }
        
        for (int i = 0; i < records.size(); i++) {
            JPanel card = createDetailedGameCard(records.get(i));
            card.setVisible(false);

            historyContainer.add(card);
            historyContainer.add(Box.createVerticalStrut(18));

            final int index = i;
            Timer animTimer = new Timer(index * 70, e -> {
                card.setVisible(true);
                historyContainer.revalidate();
                historyContainer.repaint();
            });
            animTimer.setRepeats(false);
            animTimer.start();
        }
        }

    private void refreshUI() {
        fillHistoryContainer();
        historyContainer.revalidate();
        historyContainer.repaint();
    }

    private JPanel createDetailedGameCard(DetailedGameHistoryEntry r) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));

                g2.setColor(new Color(255, 255, 255, 18));
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 22, 22));

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setMaximumSize(new Dimension(1000, 440));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.repaint();
            }
        });

        card.add(createGameOverview(r), BorderLayout.NORTH);
        card.add(createPlayerStatsPanel(r), BorderLayout.CENTER);

        return card;
    }

    // =============================
    // Game overview row (organized)
    // =============================

    private JPanel createGameOverview(DetailedGameHistoryEntry r) {
        JPanel panel = new JPanel(new BorderLayout(18, 0));
        panel.setOpaque(false);

        // LEFT: icon + result + bold date
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel();
        icon.setIcon(r.isWon() ? new FlagIcon(40, 40, WIN_GREEN) : new MineIcon(40, 40, LOSE_RED));
        icon.setBorder(new EmptyBorder(2, 0, 0, 0));

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));

        JLabel resLbl = new JLabel(r.isWon() ? "VICTORY" : "DEFEAT");
        resLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resLbl.setForeground(r.isWon() ? WIN_GREEN : LOSE_RED);

        JLabel timeLbl = new JLabel(r.getTimestamp());
        timeLbl.setForeground(TEXT_PRIMARY);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13)); // ✅ bold date/time

        titleGroup.add(resLbl);
        titleGroup.add(Box.createVerticalStrut(2));
        titleGroup.add(timeLbl);

        left.add(icon);
        left.add(titleGroup);

        // RIGHT: meta info
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 22, 0));
        right.setOpaque(false);

        right.add(createInfoBox("DIFFICULTY", r.getDifficulty(), ACCENT_PURPLE));
        right.add(createInfoBox("SCORE", r.getFinalScore() + " pts", ACCENT_CYAN));
        right.add(createInfoBox("DURATION", formatTime(r.getDurationSeconds()), ACCENT_BLUE));

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // =============================
    // Player section
    // =============================

    private JPanel createPlayerStatsPanel(DetailedGameHistoryEntry r) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        panel.add(createPlayerSubCard(
                r.getPlayer1(),
                new Color(56, 189, 248),
                r.getPlayer1QuestionsAnswered(),
                r.getPlayer1QuestionsCorrect(),
                r.getPlayer1MinesFlagged(),
                r.getPlayer1CorrectFlags(),
                r.getPlayer1GoodSurprises(),
                r.getPlayer1BadSurprises()
        ));

        panel.add(createPlayerSubCard(
                r.getPlayer2(),
                new Color(244, 114, 182),
                r.getPlayer2QuestionsAnswered(),
                r.getPlayer2QuestionsCorrect(),
                r.getPlayer2MinesFlagged(),
                r.getPlayer2CorrectFlags(),
                r.getPlayer2GoodSurprises(),
                r.getPlayer2BadSurprises()
        ));

        return panel;
    }

    private JPanel createPlayerSubCard(String name, Color accent, int qTotal, int qCorr, int flags, int fCorr, int gs, int bs) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 23, 42, 135));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);

                g2.dispose();
            }
        };

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel n = new JLabel(name);
        n.setForeground(accent);
        n.setFont(new Font("Segoe UI", Font.BOLD, 18));

        p.add(n);
        p.add(Box.createVerticalStrut(10));

        p.add(createStatRow("Questions", String.valueOf(qTotal), TEXT_PRIMARY));
        p.add(createProgressRow("Correct", qCorr, qTotal, accent));

        p.add(Box.createVerticalStrut(8));

        p.add(createStatRow("Mines Flagged", String.valueOf(flags), TEXT_PRIMARY));
        p.add(createProgressRow("Flag Accuracy", fCorr, flags, accent));

        p.add(Box.createVerticalStrut(10));

        // Surprises row (no emojis -> always consistent)
        JPanel surprises = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        surprises.setOpaque(false);

        JLabel good = new JLabel("Good surprises: " + gs);
        good.setForeground(new Color(226, 232, 240));
        good.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel bad = new JLabel("Bad surprises: " + bs);
        bad.setForeground(new Color(226, 232, 240));
        bad.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        surprises.add(good);
        surprises.add(bad);

        p.add(surprises);

        return p;
    }

    // =============================
    // Helper UI components
    // =============================

    private JPanel createStatRow(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setForeground(TEXT_DIM);
        l.setFont(LABEL_FONT);

        JLabel v = new JLabel(value);
        v.setForeground(valueColor);
        v.setFont(VALUE_FONT);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel createProgressRow(String label, int part, int total, Color accent) {
        int pct = (total > 0) ? (part * 100 / total) : 0;

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel l = new JLabel("   " + label);
        l.setForeground(TEXT_DIM);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel v = new JLabel(part + " (" + pct + "%)");
        v.setForeground(new Color(253, 224, 71));
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));

        top.add(l, BorderLayout.WEST);
        top.add(v, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(pct);
        bar.setBorderPainted(false);
        bar.setStringPainted(false);
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(10, 9));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();

                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(0, 0, w, h, 10, 10);

                int fill = (int) (w * bar.getPercentComplete());
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 190));
                g2.fillRoundRect(0, 0, fill, h, 10, 10);

                g2.dispose();
            }
        });

        wrap.add(top);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(bar);
        return wrap;
    }

    private JPanel createInfoBox(String label, String value, Color accent) {
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setOpaque(false);

        JLabel l = new JLabel(label, SwingConstants.RIGHT);
        l.setFont(SMALL_CAPS);
        l.setForeground(TEXT_DIM);

        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(Color.WHITE);

        box.add(l);
        box.add(v);
        return box;
    }

    private void showEmptyState() {
        JLabel msg = new JLabel("No records available for this date.");
        msg.setForeground(TEXT_DIM);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        historyContainer.add(Box.createVerticalStrut(100));
        historyContainer.add(msg);
    }

    // =============================
    // Sort logic (unchanged)
    // =============================

    private void applySort(String criteria) {
        switch (criteria) {
            case "Highest Score" -> records.sort((a, b) -> b.getFinalScore() - a.getFinalScore());
            case "Oldest First" -> records.sort(Comparator.comparing(DetailedGameHistoryEntry::getTimestamp));
            case "Difficulty" -> records.sort(Comparator.comparing(DetailedGameHistoryEntry::getDifficulty));
            default -> records.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        }
    }

    private String formatTime(int sec) {
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    // =============================
    // Modern UI: Pill Combo + Buttons + Date Spinner
    // =============================

    private JComboBox<String> createPillCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setOpaque(false);
        combo.setForeground(new Color(241, 245, 249));
        combo.setFont(new Font("SansSerif", Font.BOLD, 13));
        combo.setBorder(new EmptyBorder(8, 16, 8, 16));
        combo.setPreferredSize(new Dimension(190, 42));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setFocusable(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(10, 14, 10, 14));
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(new Color(241, 245, 249));

                if (index == -1) {
                    lbl.setOpaque(false); // remove gray box
                    lbl.setBackground(new Color(0, 0, 0, 0));
                } else {
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? new Color(59, 130, 246) : new Color(17, 27, 43));
                }
                return lbl;
            }
        });

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("▾");
                btn.setFont(new Font("SansSerif", Font.BOLD, 13));
                btn.setForeground(new Color(226, 232, 240));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // prevent default box
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Object value = combo.getSelectedItem();
                if (value == null) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.setFont(combo.getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = bounds.x + 16;
                int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(value.toString(), x, y);

                g2.dispose();
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(17, 27, 43, 210));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 28));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 18, 18);

                g2.dispose();
                super.paint(g, c);
            }
        });

        return combo;
    }

    private JButton createGhostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(17, 27, 43, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setColor(new Color(255, 255, 255, 22));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 38));
        return btn;
    }

    private JSpinner createDateSpinner() {
        // Calendar-like: you can click arrows or type date
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(165, 38));

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        editor.getTextField().setBorder(new EmptyBorder(8, 12, 8, 12));
        editor.getTextField().setOpaque(false);
        editor.getTextField().setBackground(new Color(0, 0, 0, 0));
        editor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 12));

        spinner.setBorder(new EmptyBorder(0, 0, 0, 0));
        spinner.setOpaque(false);

        spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            @Override protected Component createNextButton() {
                JButton b = (JButton) super.createNextButton();
                styleSpinnerButton(b, "▲");
                return b;
            }

            @Override protected Component createPreviousButton() {
                JButton b = (JButton) super.createPreviousButton();
                styleSpinnerButton(b, "▼");
                return b;
            }
        });

        // paint pill background
        spinner.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        spinner.setOpaque(false);

        spinner = new JSpinner(model) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(17, 27, 43, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(255, 255, 255, 28));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        spinner.setModel(model);
        spinner.setPreferredSize(new Dimension(165, 38));
        spinner.setEditor(editor);
        spinner.setOpaque(false);

        return spinner;
    }

    private void styleSpinnerButton(AbstractButton b, String txt) {
        b.setText(txt);
        b.setForeground(TEXT_PRIMARY);
        b.setFont(new Font("Segoe UI", Font.BOLD, 9));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =============================
    // Background animation
    // =============================

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < 55; i++) {
            particles.add(new Particle(rnd.nextInt(1400), rnd.nextInt(900),
                    (rnd.nextFloat() - 0.5f) * 0.6f,
                    (rnd.nextFloat() - 0.5f) * 0.6f,
                    1 + rnd.nextFloat() * 2.5f,
                    0.06f + rnd.nextFloat() * 0.10f));
        }
    }

  
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), BG_GRAD_2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // particles
        for (Particle p : particles) p.draw(g2);

        g2.dispose();
    }

    private static class Particle {
        float x, y, vx, vy, size, alpha;

        Particle(float x, float y, float vx, float vy, float size, float alpha) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.size = size; this.alpha = alpha;
        }

        void update(int w, int h) {
            x += vx;
            y += vy;

            if (w <= 0 || h <= 0) return;

            if (x < 0) x = w;
            if (x > w) x = 0;
            if (y < 0) y = h;
            if (y > h) y = 0;
        }

        void draw(Graphics2D g2) {
            g2.setColor(new Color(255, 255, 255, Math.min(255, (int) (alpha * 255))));
            g2.fillOval((int) x, (int) y, (int) size, (int) size);
        }
    }

    // =============================
    // Game-themed icons (no emoji)
    // =============================

    private static class MineIcon implements Icon {
        private final int w, h;
        private final Color color;

        MineIcon(int w, int h, Color color) {
            this.w = w; this.h = h; this.color = color;
        }

        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = x + w / 2;
            int cy = y + h / 2;
            int r = Math.min(w, h) / 3;

            // spikes
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
            for (int i = 0; i < 8; i++) {
                double ang = i * (Math.PI / 4);
                int x1 = (int) (cx + Math.cos(ang) * (r + 2));
                int y1 = (int) (cy + Math.sin(ang) * (r + 2));
                int x2 = (int) (cx + Math.cos(ang) * (r + 10));
                int y2 = (int) (cy + Math.sin(ang) * (r + 10));
                g2.drawLine(x1, y1, x2, y2);
            }

            // body
            g2.setColor(new Color(20, 30, 48, 220));
            g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.4f));
            g2.drawOval(cx - r, cy - r, 2 * r, 2 * r);

            // highlight
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillOval(cx - r/2, cy - r/2, r, r);

            g2.dispose();
        }
    }

    private static class FlagIcon implements Icon {
        private final int w, h;
        private final Color color;

        FlagIcon(int w, int h, Color color) {
            this.w = w; this.h = h; this.color = color;
        }

        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int poleX = x + w / 3;
            int topY = y + h / 6;
            int bottomY = y + h - h / 6;

            // pole
            g2.setColor(new Color(226, 232, 240, 200));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(poleX, topY, poleX, bottomY);

            // flag
            Polygon flag = new Polygon();
            flag.addPoint(poleX, topY + 2);
            flag.addPoint(poleX + w/2, topY + h/6);
            flag.addPoint(poleX, topY + h/3);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 220));
            g2.fillPolygon(flag);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(flag);

            // base
            g2.setColor(new Color(226, 232, 240, 160));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(poleX - 10, bottomY, poleX + 10, bottomY);

            g2.dispose();
        }
    }
}

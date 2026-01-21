package view;

import model.Question;
import model.SysData;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class QuestionManagementScreen extends JPanel {
    private JFrame frame;
    private DefaultTableModel model;
    private JTable questionTable;
    private JTextField searchField;
    private JComboBox<String> filterDropdown;
    private TableRowSorter<DefaultTableModel> sorter;

    // Background animation
    private Timer animationTimer;
    private List<AnimatedParticle> particles;
    private Random random;
    
    // Pulse effect
    private int pulseRow = -1;
    private float pulseAlpha = 0f;
    private Timer pulseTimer;
    
    // UI Constants
    private final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private final Color PARTICLE_COLOR = new Color(255, 255, 255, 40);

    public QuestionManagementScreen(JFrame frame) {
        this.frame = frame;

        // Anti-Flash setup - CRITICAL
        
        setBackground(BACKGROUND_DARK);
        setOpaque(true);
        setDoubleBuffered(true);
        
        // Disable ALL tooltips globally
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(Integer.MAX_VALUE);
        UIManager.put("ToolTip.hideAccelerator", Boolean.TRUE);
        
        setLayout(new BorderLayout(0, 30));
        setBorder(new EmptyBorder(40, 50, 40, 50));

        initializeAnimation();
        
        // Build UI
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        
        loadQuestions();
        killAllTooltips();
        
    }

    
    private void killAllTooltips() {
        unregisterTooltipsRecursively(this);
        if (frame != null) {
            frame.getContentPane().setBackground(BACKGROUND_DARK);
            unregisterTooltipsRecursively(frame.getRootPane());
        }
    }

    private void unregisterTooltipsRecursively(Component c) {
        if (c instanceof JComponent jc) {
            jc.setToolTipText(null);
            jc.setVerifyInputWhenFocusTarget(false);
            ToolTipManager.sharedInstance().unregisterComponent(jc);
        }
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                unregisterTooltipsRecursively(child);
            }
        }
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Question Management Interface");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Manage quiz questions for MineSweeper game");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(148, 163, 184));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalStrut(18));

        JButton backButton = createBackButton();
        backButton.addActionListener(e -> {
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame)); // change if needed
            frame.revalidate();
            frame.repaint();
        });

        titlePanel.add(backButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);

        JPanel buttonPane2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);

//        // BACK BUTTON (define it first)
//        JButton backButton1 = createTopActionButton("Back to Menu", new Color(51, 65, 85), "⬅");
//        backButton1.addActionListener(e -> {
//            frame.setContentPane(new MainMenuTwoPlayerScreen(frame)); // 🔁 change if your menu class name is different
//            frame.revalidate();
//            frame.repaint();
//        });

        // EXISTING BUTTONS
        JButton addButton = createTopActionButton("Add Question", new Color(37, 99, 235), "➕");
        JButton editButton = createTopActionButton("Edit Selected", new Color(71, 85, 105), "✎");
        JButton deleteButton = createTopActionButton("Delete Selected", new Color(220, 38, 38), "🗑");



        addButton.addActionListener(e -> showQuestionDialog("Add New Question", null));
        editButton.addActionListener(e -> {
            int viewRow = questionTable.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = questionTable.convertRowIndexToModel(viewRow);
                showQuestionDialog("Edit Question", modelRow);
            } else {
                MessageDialog.show(frame, "Please select a question to edit.", MessageDialog.Type.WARNING);
            }
        });
        deleteButton.addActionListener(e -> performDelete());
        
    
       // buttonPanel.add(backButton1);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);


        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);

        searchField = createPillSearchField("Search questions by keyword...");
        filterDropdown = createPillFilter();

        JPanel searchRow = new JPanel(new BorderLayout(15, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(filterDropdown, BorderLayout.EAST);

        model = new DefaultTableModel(
                new Object[]{"", "Question", "Answer 1", "Answer 2", "Answer 3", "Answer 4", "Difficulty"}, 0) {
            /**
					 * 
					 */
					private static final long serialVersionUID = 1L;
			@Override public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int row, int col) { return col == 0; }
        };

        questionTable = new JTable(model);
        questionTable.setTableHeader(new JTableHeader(questionTable.getColumnModel()));
        styleModernTable();

        JPanel tableWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(questionTable);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        tableWrapper.add(sp);

        mainPanel.add(searchRow, BorderLayout.NORTH);
        mainPanel.add(tableWrapper, BorderLayout.CENTER);
        return mainPanel;
        
    }

    private void styleModernTable() {
        questionTable.setRowHeight(65);
        questionTable.setShowGrid(false);
        questionTable.setOpaque(false);
        questionTable.setBackground(new Color(0, 0, 0, 0));
        questionTable.setSelectionBackground(new Color(59, 130, 246, 40));
        questionTable.setForeground(new Color(226, 232, 240));
        questionTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        JTableHeader header = questionTable.getTableHeader();
        header.setBackground(new Color(30, 41, 59));
        header.setForeground(new Color(148, 163, 184));
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 50));
        header.setReorderingAllowed(false);
        
        // Disable tooltips on header
        ToolTipManager.sharedInstance().unregisterComponent(header);
        header.setToolTipText(null);
        
        // Custom renderer for answer columns with checkmark
        questionTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                if (c == 6) return createBadge(v.toString(), isS);
                
                // Get the correct answer index for this row
                int correctIndex = getCorrectAnswerIndex(r);
                
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, false, r, c);
                l.setOpaque(false);
                l.setBorder(new EmptyBorder(0, 15, 0, 15));
                
                // Check if this is an answer column (2-5) and if it's the correct one
                if (c >= 2 && c <= 5) {
                    int answerIndex = c - 2; // 0, 1, 2, 3 for answers A, B, C, D
                    if (answerIndex == correctIndex) {
                        // This is the correct answer - add checkmark and green color
                        l.setText("✓  " + v.toString());
                        l.setForeground(new Color(34, 197, 94)); // Green
                        l.setFont(new Font("SansSerif", Font.BOLD, 13));
                    } else {
                        l.setForeground(new Color(226, 232, 240));
                        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    }
                } else if (c == 1) {
                    // Question column - make bold if row is selected
                    if (isS) {
                        l.setFont(new Font("SansSerif", Font.BOLD, 14));
                    } else {
                        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    }
                    l.setForeground(new Color(226, 232, 240));
                } else {
                    l.setForeground(new Color(226, 232, 240));
                }
                
                return l;
            }
            
        });
        
        // Custom renderer for the entire row to show pulse effect
        questionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                
                // Add pulse effect
                if (r == pulseRow && pulseAlpha > 0) {
                    comp.setBackground(new Color(59, 130, 246, (int)(pulseAlpha * 80)));
                } else if (isS) {
                    comp.setBackground(new Color(59, 130, 246, 40));
                } else {
                    comp.setBackground(new Color(0, 0, 0, 0));
                }
                
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setOpaque(pulseAlpha > 0 || isS);
                }
                
                return comp;
            }
        });
        
        JButton backButton = createTopActionButton("Back to Menu", new Color(51, 65, 85), "⬅");
        backButton.addActionListener(e -> {
            // TODO: replace this with your real navigation code
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame)); // example class name
            frame.revalidate();
            frame.repaint();
        });


        // Add mouse listener to make rows clickable with pulse effect
        questionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = questionTable.rowAtPoint(e.getPoint());
                int col = questionTable.columnAtPoint(e.getPoint());
                
                // Don't select if clicking checkbox column
                if (col == 0) return;
                
                if (row >= 0) {
                    questionTable.setRowSelectionInterval(row, row);
                    triggerPulseEffect(row);
                    questionTable.repaint();
                }
            }
        });

        // Column widths
        questionTable.getColumnModel().getColumn(0).setMaxWidth(40);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        questionTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(6).setMaxWidth(150);

        sorter = new TableRowSorter<>(model);
        questionTable.setRowSorter(sorter);
    }
    
    private int getCorrectAnswerIndex(int viewRow) {
        try {
            int modelRow = questionTable.convertRowIndexToModel(viewRow);
            List<Question> questions = SysData.getInstance().getQuestions();
            if (modelRow >= 0 && modelRow < questions.size()) {
                return questions.get(modelRow).getCorrectIndex();
            }
        } catch (Exception e) {
            // Ignore
        }
        return -1;
    }
    
    private void triggerPulseEffect(int row) {
        // Stop existing pulse timer if any
        if (pulseTimer != null && pulseTimer.isRunning()) {
            pulseTimer.stop();
        }
        
        pulseRow = row;
        pulseAlpha = 1.0f;
        
        // Pulse animation - fade out
        pulseTimer = new Timer(30, e -> {
            pulseAlpha -= 0.05f;
            if (pulseAlpha <= 0f) {
                pulseAlpha = 0f;
                pulseRow = -1;
                ((Timer) e.getSource()).stop();
            }
            questionTable.repaint();
        });
        pulseTimer.start();
    }

    
    private JPanel createBadge(String text, boolean isSelected) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        p.setOpaque(false);
        Color baseColor = switch(text) {
            case "Easy" -> new Color(34, 197, 94);
            case "Hard" -> new Color(239, 68, 68);
            case "Expert" -> new Color(168, 85, 247);
            default -> new Color(245, 158, 11);
        };
        JLabel b = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(baseColor);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(4, 15, 4, 15));
        p.add(b);
        return p;
    }

    private JTextField createPillSearchField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(148, 163, 184));
                    g.drawString(placeholder, 25, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 2);
                }
            }
        };
        f.setOpaque(false);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(new EmptyBorder(0, 25, 0, 25));
        f.setPreferredSize(new Dimension(0, 45));
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        return f;
    }

    private JComboBox<String> createPillFilter() {
        JComboBox<String> combo = new JComboBox<>(
                new String[]{"All Difficulties", "Easy", "Medium", "Hard", "Expert"}
        );

        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setForeground(new Color(241, 245, 249));
        combo.setFont(new Font("SansSerif", Font.BOLD, 13));
        combo.setBorder(new EmptyBorder(8, 16, 8, 16));
        combo.setPreferredSize(new Dimension(220, 52));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setFocusable(false);

        // ✅ Renderer: popup items opaque, BUT selected-value renderer (index=-1) NOT opaque
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                lbl.setBorder(new EmptyBorder(10, 14, 10, 14));
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(new Color(241, 245, 249));

                if (index == -1) {
                    // Selected item (inside the closed combobox)
                    lbl.setOpaque(false);                 // ✅ prevents gray box
                    lbl.setBackground(new Color(0,0,0,0));
                } else {
                    // Popup list items
                    lbl.setOpaque(true);
                    lbl.setBackground(isSelected ? new Color(59, 130, 246) : new Color(17, 27, 43));
                }
                return lbl;
            }
        });

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
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
                // ✅ do nothing => prevents Swing default background box
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                // ✅ draw text ourselves (no renderer background)
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

                // pill background
                g2.setColor(new Color(51, 65, 85, 220));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);

                // border
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 18, 18);

                g2.dispose();
                super.paint(g, c); // arrow still painted
            }
        });

        combo.addActionListener(e -> applyFilters());
        return combo;
    }

    

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(BACKGROUND_DARK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(new GradientPaint(0, 0, BACKGROUND_DARK, getWidth(), getHeight(), new Color(30, 41, 59)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        for (AnimatedParticle p : particles) p.draw(g2, getWidth(), getHeight());
        g2.dispose();
    }

    private void applyFilters() {
        String text = searchField.getText();
        String diff = filterDropdown.getSelectedItem().toString();
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (text != null && !text.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        if (!diff.equals("All Difficulties")) filters.add(RowFilter.regexFilter("^" + diff + "$", 6));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    public void loadQuestions() {
        model.setRowCount(0);
        SysData.getInstance().loadQuestions();
        for (Question q : SysData.getInstance().getQuestions()) {
            String[] ans = q.getAnswers();
            model.addRow(new Object[]{false, q.getText(), ans[0], ans[1], ans[2], ans[3], getDifficultyString(q.getDifficulty())});
        }
    }

    private String getDifficultyString(int d) {
        return switch (d) { case 1 -> "Easy"; case 3 -> "Hard"; case 4 -> "Expert"; default -> "Medium"; };
    }

    private int getDifficultyInt(String s) {
        return switch (s) { case "Easy" -> 1; case "Hard" -> 3; case "Expert" -> 4; default -> 2; };
    }

    private void initializeAnimation() {
        random = new Random();
        particles = new ArrayList<>();
        for (int i = 0; i < 40; i++) particles.add(new AnimatedParticle(random));
        animationTimer = new Timer(40, e -> repaint());
        animationTimer.start();
    }

    private class AnimatedParticle {
        float x, y, vx, vy, size;
        AnimatedParticle(Random r) {
            x = r.nextFloat() * 1920; y = r.nextFloat() * 1080;
            vx = (r.nextFloat() - 0.5f) * 0.4f; vy = (r.nextFloat() - 0.5f) * 0.4f;
            size = r.nextFloat() * 3 + 1;
        }
        void draw(Graphics2D g2, int w, int h) {
            x += vx; y += vy;
            if (x < 0) x = w; if (x > w) x = 0;
            if (y < 0) y = h; if (y > h) y = 0;
            g2.setColor(PARTICLE_COLOR);
            g2.fillOval((int) x, (int) y, (int) size, (int) size);
        }
    }

    private JButton createTopActionButton(String text, Color baseColor, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);
                g2.dispose();
                super.paint(g, c);
            }
        });
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showQuestionDialog(String title, Integer modelRow) {
        Question existingQuestion = null;
        
        if (modelRow != null) {
            // Get the actual question from SysData
            List<Question> questions = SysData.getInstance().getQuestions();
            existingQuestion = questions.get(modelRow);
        }
        
        // Show dialog
        Question result = EditAddQuestionDialog.show(frame, title, existingQuestion);
        
        if (result != null) {
            if (modelRow != null) {
                // Update existing question - pass the Question object, not the index
                SysData.getInstance().updateQuestion(result);
                MessageDialog.show(frame, "Question updated successfully!", MessageDialog.Type.SUCCESS);
            } else {
                // Add new question
                SysData.getInstance().addQuestion(result);
                MessageDialog.show(frame, "Question added successfully!", MessageDialog.Type.SUCCESS);
            }
            loadQuestions();
        }
    }
    private JButton createBackButton() {
        JButton btn = new JButton("⬅ Back");
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(203, 213, 225)); // soft gray
        btn.setBackground(new Color(30, 41, 59));   // dark muted
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 36));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(30, 41, 59, 220));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);

                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 12, 12);

                super.paint(g2, c);
                g2.dispose();
            }
        });

        return btn;
    }


    private void performDelete() {
        // Collect all selected rows
        List<Integer> selectedModelRows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                selectedModelRows.add(i);
            }
        }
        
        if (selectedModelRows.isEmpty()) {
            MessageDialog.show(frame, "Please select at least one question to delete.", MessageDialog.Type.WARNING);
            return;
        }
        
        // Confirm deletion
        String message = selectedModelRows.size() == 1 
            ? "Are you sure you want to delete this question?" 
            : "Are you sure you want to delete " + selectedModelRows.size() + " questions?";
        
        boolean confirmed = ConfirmDialog.confirm(frame, message, "Confirm Deletion");
        
        if (confirmed) {
            // Get the actual Question IDs before deleting
            List<Integer> questionIds = new ArrayList<>();
            List<Question> questions = SysData.getInstance().getQuestions();
            
            for (int rowIndex : selectedModelRows) {
                questionIds.add(questions.get(rowIndex).getId());
            }
            
            // Delete by ID (not index, since indices shift after each deletion)
            for (int questionId : questionIds) {
                SysData.getInstance().deleteQuestion(questionId);
            }
            
            MessageDialog.show(frame, 
                selectedModelRows.size() + " question(s) deleted successfully!", 
                MessageDialog.Type.SUCCESS);
            
            loadQuestions();
        }
    }
}
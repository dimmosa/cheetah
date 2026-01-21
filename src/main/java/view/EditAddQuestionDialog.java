package view;

import model.Question;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class EditAddQuestionDialog extends JDialog {

    private boolean confirmed = false;
    private float opacity = 0f;
    private float slideOffset = 50f;

    private JTextField questionField;
    private JTextField[] answerFields = new JTextField[4];
    private JComboBox<String> correctAnswerCombo;
    private JComboBox<String> difficultyCombo;
    private Question resultQuestion;
    private Integer existingQuestionId;

    private final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private final Color SIDE_PANEL_BG = new Color(30, 41, 59);
    private final Color ACCENT_BLUE = new Color(59, 130, 246);
    private final Color TEXT_PRIMARY = new Color(235, 242, 255);
    private final Color TEXT_SECONDARY = new Color(148, 163, 184);

    public EditAddQuestionDialog(JFrame parent, String title, Question existingQuestion) {
        super(parent, title, true);
        setUndecorated(true);
        setSize(900, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setShape(new RoundRectangle2D.Double(0, 0, 900, 650, 26, 26));
        setBackground(new Color(0, 0, 0, 0));

        // Set this before building UI so side panel icon color is correct
        existingQuestionId = (existingQuestion != null) ? existingQuestion.getId() : null;

        JPanel mainPanel = createMainPanel(title);
        add(mainPanel);

        // Pre-fill if editing
        if (existingQuestion != null) {
            questionField.setText(existingQuestion.getText());
            String[] answers = existingQuestion.getAnswers();
            for (int i = 0; i < 4; i++) answerFields[i].setText(answers[i]);
            correctAnswerCombo.setSelectedIndex(existingQuestion.getCorrectIndex());
            difficultyCombo.setSelectedIndex(existingQuestion.getDifficulty() - 1);
        }

        fadeInAndSlide();
    }

    private JPanel createMainPanel(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, BACKGROUND_DARK,
                        getWidth(), getHeight(), new Color(20, 30, 48)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

                g2.setColor(new Color(59, 130, 246, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 24, 24);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 0));

        panel.add(createSidePanel(title), BorderLayout.WEST);
        panel.add(createContentPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSidePanel(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, SIDE_PANEL_BG,
                        0, getHeight(), new Color(20, 30, 48)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(ACCENT_BLUE);
                g2.fillRect(getWidth() - 3, 0, 3, getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 30, 40, 30));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = 60;

                Color iconColor = existingQuestionId != null ? new Color(168, 85, 247) : new Color(34, 197, 94);

                for (int i = 3; i > 0; i--) {
                    int size = 70 + (i * 15);
                    int a = 15 * (4 - i);
                    g2.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), a));
                    g2.fillOval(centerX - size / 2, centerY - size / 2, size, size);
                }

                g2.setColor(iconColor);
                g2.fillOval(centerX - 35, centerY - 35, 70, 70);

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(220, 140));
        iconPanel.setMaximumSize(new Dimension(220, 140));

        String iconEmoji = existingQuestionId != null ? "✏️" : "➕";
        JLabel iconLabel = new JLabel(iconEmoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.add(iconLabel);

        content.add(iconPanel);
        content.add(Box.createVerticalStrut(30));

        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>" + title + "</div></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);

        content.add(Box.createVerticalStrut(15));

        JLabel subtitle = new JLabel("<html><div style='text-align: center;'>Fill in all fields to create a quiz question</div></html>");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(subtitle);

        content.add(Box.createVerticalStrut(40));

        content.add(createInfoBadge("📝", "Question text required"));
        content.add(Box.createVerticalStrut(12));
        content.add(createInfoBadge("✅", "4 answer options"));
        content.add(Box.createVerticalStrut(12));
        content.add(createInfoBadge("🎯", "Select correct answer"));
        content.add(Box.createVerticalStrut(12));
        content.add(createInfoBadge("⚡", "Choose difficulty level"));

        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createInfoBadge(String icon, String text) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        badge.setOpaque(false);
        badge.setMaximumSize(new Dimension(220, 35));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        textLabel.setForeground(TEXT_SECONDARY);

        badge.add(iconLabel);
        badge.add(textLabel);
        return badge;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 35, 35, 35));
        panel.add(createForm(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Question
        form.add(createColoredLabel("📝  Question Text", new Color(59, 130, 246)));
        questionField = createTextField("Enter the question...", 55);
        form.add(questionField);
        form.add(Box.createVerticalStrut(20));

        // Answers: label LEFT + field RIGHT
        String[] letters = {"A", "B", "C", "D"};
        Color[] answerColors = {
                new Color(239, 68, 68),    // A red
                new Color(59, 130, 246),   // B blue
                new Color(245, 158, 11),   // C orange
                new Color(34, 197, 94)     // D green
        };

        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(14, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

            JLabel leftLabel = new JLabel("●  Answer " + letters[i]);
            leftLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            leftLabel.setForeground(answerColors[i]);
            leftLabel.setPreferredSize(new Dimension(120, 28));
            leftLabel.setBorder(new EmptyBorder(0, 6, 0, 0));

            // A is red-strong, others normal
            answerFields[i] = createTextField(
                    "Enter answer " + letters[i] + "...",
                    48,
                    answerColors[i],
                    (i == 0)
            );

            row.add(leftLabel, BorderLayout.WEST);
            row.add(answerFields[i], BorderLayout.CENTER);

            form.add(row);
            form.add(Box.createVerticalStrut(14));
        }

        // Dropdown row
        JPanel dropdownRow = new JPanel(new GridLayout(1, 2, 25, 0));
        dropdownRow.setOpaque(false);
        dropdownRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JPanel correctPanel = new JPanel();
        correctPanel.setOpaque(false);
        correctPanel.setLayout(new BoxLayout(correctPanel, BoxLayout.Y_AXIS));
        correctPanel.add(createColoredLabel("✅  Correct Answer", new Color(34, 197, 94)));
        correctPanel.add(Box.createVerticalStrut(5));
        correctAnswerCombo = createComboBox(new String[]{"Answer A", "Answer B", "Answer C", "Answer D"});
        correctPanel.add(correctAnswerCombo);

        JPanel diffPanel = new JPanel();
        diffPanel.setOpaque(false);
        diffPanel.setLayout(new BoxLayout(diffPanel, BoxLayout.Y_AXIS));
        diffPanel.add(createColoredLabel("⚡  Difficulty", new Color(168, 85, 247)));
        diffPanel.add(Box.createVerticalStrut(5));
        difficultyCombo = createComboBox(new String[]{"Easy", "Medium", "Hard", "Expert"});
        diffPanel.add(difficultyCombo);

        dropdownRow.add(correctPanel);
        dropdownRow.add(diffPanel);

        form.add(dropdownRow);
        return form;
    }

    private JLabel createColoredLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 5, 8, 0));
        return label;
    }

    // ✅ keep this so your QUESTION field compiles and looks the same
    private JTextField createTextField(String placeholder, int height) {
        return createTextField(placeholder, height, ACCENT_BLUE, false);
    }

    private JTextField createTextField(String placeholder, int height, Color accent, boolean strongAccent) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(30, 41, 59, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                boolean focus = isFocusOwner();

                Color border = focus
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), strongAccent ? 185 : 130)
                        : (strongAccent
                            ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110) // A stays red even unfocused
                            : new Color(255, 255, 255, 20));

                g2.setColor(border);
                g2.setStroke(new BasicStroke(focus ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);

                if (focus) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), strongAccent ? 40 : 24));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);

                if (getText().isEmpty() && !focus) {
                    g.setColor(TEXT_SECONDARY);
                    g.setFont(getFont());
                    g.drawString(placeholder, 18, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 2);
                }
            }
        };

        field.setOpaque(false);

        if (strongAccent) {
            field.setForeground(new Color(255, 210, 210));
            field.setCaretColor(accent);
        } else {
            field.setForeground(TEXT_PRIMARY);
            field.setCaretColor(ACCENT_BLUE);
        }

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(0, 18, 0, 18));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        field.setPreferredSize(new Dimension(0, height));
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setOpaque(false);
        combo.setForeground(new Color(241, 245, 249));
        combo.setFont(new Font("SansSerif", Font.BOLD, 14));
        combo.setBorder(new EmptyBorder(10, 18, 10, 18));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        combo.setPreferredSize(new Dimension(0, 54));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setFocusable(false);
        combo.setOpaque(false);
        combo.setBackground(new Color(0,0,0,0));

        // Important: remove the default painted current-value background
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // ✅ remove the “gray rectangle behind”
                lbl.setOpaque(false);

                lbl.setBorder(new EmptyBorder(10, 16, 10, 16));
                lbl.setForeground(new Color(241, 245, 249));

                // dropdown list background (only when the popup is open)
                if (index >= 0) {
                    lbl.setOpaque(true); // list items can be opaque
                    lbl.setBackground(isSelected ? new Color(59, 130, 246) : new Color(17, 27, 43));
                }
                return lbl;
            }
        });


        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(10, 14, 10, 14));
                setForeground(new Color(241, 245, 249));
                setBackground(isSelected ? new Color(59, 130, 246) : new Color(17, 27, 43));
                return this;
            }
        });

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {

            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▾");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setForeground(new Color(226, 232, 240));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background color
                g2.setColor(new Color(51, 65, 85, 220)); // modern dark slate
                g2.fillRoundRect(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height,
                        16,
                        16
                );

                // Border
                g2.setColor(hasFocus
                        ? new Color(59, 130, 246, 160)
                        : new Color(255, 255, 255, 35)
                );
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(
                        bounds.x + 1,
                        bounds.y + 1,
                        bounds.width - 3,
                        bounds.height - 3,
                        16,
                        16
                );

                g2.dispose();
            }

        });


        return combo;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton cancelBtn = createSecondaryButton("Cancel");
        JButton saveBtn = createPrimaryButton("💾  Save Question");

        cancelBtn.addActionListener(e -> {
            confirmed = false;
            fadeOutAndDispose();
        });

        saveBtn.addActionListener(e -> {
            if (validateFields()) {
                saveQuestion();
                confirmed = true;
                fadeOutAndDispose();
            }
        });

        panel.add(cancelBtn);
        panel.add(saveBtn);
        return panel;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, ACCENT_BLUE,
                        getWidth(), getHeight(), new Color(37, 99, 235)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 80, 120, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 45));
        return btn;
    }

    private boolean validateFields() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        if (questionField.getText().trim().isEmpty()) {
            MessageDialog.show(parentFrame, "Please enter a question text.", MessageDialog.Type.ERROR);
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (answerFields[i].getText().trim().isEmpty()) {
                MessageDialog.show(parentFrame, "Please fill in all answer fields.", MessageDialog.Type.ERROR);
                return false;
            }
        }
        return true;
    }

    private void saveQuestion() {
        String[] answers = new String[4];
        for (int i = 0; i < 4; i++) answers[i] = answerFields[i].getText().trim();

        int questionId = (existingQuestionId != null) ? existingQuestionId : -1;

        resultQuestion = new Question(
                questionId,
                questionField.getText().trim(),
                answers,
                correctAnswerCombo.getSelectedIndex(),
                difficultyCombo.getSelectedIndex() + 1
        );
    }

    private void fadeInAndSlide() {
        setOpacity(0f);
        Timer timer = new Timer(16, e -> {
            opacity = Math.min(1f, opacity + 0.08f);
            slideOffset = Math.max(0f, slideOffset - 4f);
            setOpacity(opacity);
            setLocation(getX(), getY() - (int) (slideOffset * 0.08f));
            if (opacity >= 1f && slideOffset <= 0f) ((Timer) e.getSource()).stop();
        });
        timer.start();
    }

    private void fadeOutAndDispose() {
        Timer timer = new Timer(16, e -> {
            opacity = Math.max(0f, opacity - 0.12f);
            setOpacity(opacity);
            if (opacity <= 0f) {
                ((Timer) e.getSource()).stop();
                dispose();
            }
        });
        timer.start();
    }

    public static Question show(JFrame parent, String title, Question existingQuestion) {
        EditAddQuestionDialog dialog = new EditAddQuestionDialog(parent, title, existingQuestion);
        dialog.setVisible(true);
        return dialog.confirmed ? dialog.resultQuestion : null;
    }
}

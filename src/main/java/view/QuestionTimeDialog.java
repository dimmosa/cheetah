package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class QuestionTimeDialog extends JDialog {

    private int selectedAnswerIndex = -1;
    private JButton[] optionButtons;

    public QuestionTimeDialog(Frame owner,
                              String difficulty,
                              String question,
                              String[] options,
                              Consumer<Integer> onAnswerConfirmed) {
        super(owner, "Question Time", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(550, 650);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(10, 15, 40),
                        getWidth(), getHeight(), new Color(20, 30, 60));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(29, 41, 61));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        JLabel closeBtn = new JLabel("✖");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeBtn.setForeground(new Color(100, 110, 140));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBounds(510, 15, 30, 30);
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
            }
        });
        mainPanel.add(closeBtn);

        JLabel titleLabel = new JLabel("❓ Question Time!");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 180, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 30, 550, 30);
        mainPanel.add(titleLabel);

        Color diffColor = Color.GREEN;
        if (difficulty.equalsIgnoreCase("MEDIUM")) diffColor = new Color(255, 160, 0);
        if (difficulty.equalsIgnoreCase("HARD")) diffColor = new Color(255, 60, 60);

        JLabel levelLabel = new JLabel("LEVEL: " + difficulty.toUpperCase());
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        levelLabel.setForeground(diffColor);
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        levelLabel.setBounds(0, 65, 550, 20);
        mainPanel.add(levelLabel);

        JTextArea qText = new JTextArea(question);
        qText.setFont(new Font("SansSerif", Font.PLAIN, 18));
        qText.setForeground(Color.WHITE);
        qText.setOpaque(false);
        qText.setEditable(false);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);

        JPanel qPanel = new JPanel(new BorderLayout());
        qPanel.setOpaque(false);
        qPanel.setBounds(40, 110, 470, 150);
        qPanel.add(qText, BorderLayout.CENTER);
        mainPanel.add(qPanel);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 18));
        optionsPanel.setOpaque(false);
        optionsPanel.setBounds(50, 280, 450, 300);

        optionButtons = new JButton[options.length];
        ActionListener optionListener = e -> {
            JButton source = (JButton) e.getSource();
            for (int i = 0; i < optionButtons.length; i++) {
                if (optionButtons[i] == source) {
                    selectedAnswerIndex = i;
                    optionButtons[i].setBorder(
                            BorderFactory.createLineBorder(new Color(0, 180, 255), 3));
                    optionButtons[i].setBackground(new Color(0, 180, 255, 50));
                } else {
                    optionButtons[i].setBorder(
                            BorderFactory.createLineBorder(new Color(80, 90, 120), 1));
                    optionButtons[i].setBackground(new Color(30, 40, 60));
                }
            }
        };

        String[] labels = {"A", "B", "C", "D"};
        for (int i = 0; i < options.length; i++) {
            JButton btn = new JButton(labels[i] + ". " + options[i]);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setBackground(new Color(30, 40, 60));
            btn.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 120), 1));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(optionListener);

            optionButtons[i] = btn;
            optionsPanel.add(btn);
        }
        mainPanel.add(optionsPanel);

        JButton okBtn = new JButton("CONFIRM ANSWER") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 180, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        okBtn.setContentAreaFilled(false);
        okBtn.setBorderPainted(false);
        okBtn.setFocusPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.setBounds(175, 590, 200, 45);

        okBtn.addActionListener(e -> {
            if (selectedAnswerIndex == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select an answer first!",
                        "No Answer Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int chosen = selectedAnswerIndex;

            // ✅ close dialog first
            dispose();

            // ✅ then run callback, which may open a JOptionPane
            if (onAnswerConfirmed != null) {
                SwingUtilities.invokeLater(() -> onAnswerConfirmed.accept(chosen));
            }
        });

        mainPanel.add(okBtn);
    }
}
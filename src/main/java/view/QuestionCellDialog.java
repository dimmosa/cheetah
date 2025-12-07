package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class QuestionCellDialog extends JDialog {

    private boolean shouldProceed = false;

    public QuestionCellDialog(Frame owner) {
        super(owner, "Question", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(450, 500);
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(10, 15, 35));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

                g2.setColor(new Color(255, 200, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            }
        };
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        JLabel titleLabel = new JLabel("❓ Question Cell Found!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 180, 0));
        titleLabel.setBounds(0, 30, 450, 40);
        contentPanel.add(titleLabel);

        JTextArea descLabel = new JTextArea("Do you want to answer this question now or skip it for later?");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descLabel.setForeground(Color.WHITE);
        descLabel.setOpaque(false);
        descLabel.setEditable(false);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setBounds(50, 80, 350, 60);

        JLabel htmlDesc = new JLabel("<html><div style='text-align: center;'>Do you want to answer this question now or skip it for later?</div></html>", SwingConstants.CENTER);
        htmlDesc.setFont(new Font("SansSerif", Font.PLAIN, 16));
        htmlDesc.setForeground(Color.WHITE);
        htmlDesc.setBounds(40, 70, 370, 60);
        contentPanel.add(htmlDesc);

        JButton answerNowBtn = createCustomButton(
                "✅", "Answer Now", "Try to earn points and lives",
                new Color(0, 180, 80), new Color(0, 140, 60)
        );
        answerNowBtn.setBounds(45, 150, 360, 100);
        answerNowBtn.addActionListener(e -> {
            shouldProceed = true;
            dispose();
        });
        contentPanel.add(answerNowBtn);

        JButton answerLaterBtn = createCustomButton(
                "⏭", "Answer Later", "Skip and answer on your next turn",
                new Color(255, 100, 0), new Color(200, 60, 0)
        );
        answerLaterBtn.setBounds(45, 270, 360, 100);
        answerLaterBtn.addActionListener(e -> {
            shouldProceed = false;
            dispose();
        });
        contentPanel.add(answerLaterBtn);
    }

    private JButton createCustomButton(String iconEmoji, String mainText, String subText, Color topColor, Color botColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), botColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
                
                super.paintComponent(g);
            }
        };

        String html = "<html><center>" +
                "<font size='5'>" + iconEmoji + "</font><br>" +
                "<font size='5'><b>" + mainText + "</b></font><br>" +
                "<font size='3'>" + subText + "</font>" +
                "</center></html>";

        btn.setText(html);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean shouldProceed() {
        return shouldProceed;
    }
}
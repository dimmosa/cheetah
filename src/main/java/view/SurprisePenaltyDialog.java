package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class SurprisePenaltyDialog extends JDialog {

    JLabel statsLabel;

    public SurprisePenaltyDialog(Frame owner, String difficulty) {
        super(owner, "Penalty", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(400, 320);
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(40, 10, 10),
                        0, getHeight(), new Color(120, 20, 20)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2.setColor(new Color(255, 100, 100));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        JLabel closeBtn = new JLabel("✖");
        closeBtn.setForeground(new Color(255, 200, 200));
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setBounds(360, 15, 30, 30);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        contentPanel.add(closeBtn);

        JLabel iconLabel = new JLabel("💀", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLabel.setForeground(new Color(230, 230, 230));
        iconLabel.setBounds(0, 40, 400, 80);
        contentPanel.add(iconLabel);

        JLabel titleLabel = new JLabel("Surprise Penalty!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 130, 400, 30);
        contentPanel.add(titleLabel);

        JLabel subLabel = new JLabel("Hole in the ground! Watch your step!", SwingConstants.CENTER);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLabel.setForeground(new Color(220, 220, 220));
        subLabel.setBounds(0, 170, 400, 20);
        
        contentPanel.add(subLabel);

        statsLabel = new JLabel("-12 points   -1 ❤️", SwingConstants.CENTER);
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        statsLabel.setForeground(new Color(255, 100, 100));
        statsLabel.setBounds(0, 220, 400, 30);

        setStatsLabelText(difficulty);

        contentPanel.add(statsLabel);
    }

    private void setStatsLabelText(String Difficulty) {
        String stats = switch (Difficulty.toLowerCase()) {
            case "easy" -> "- 8 points";
            case "medium" -> "- 12 points";
            case "hard" -> "- 16 points";
            default -> "";
        };

        statsLabel.setText(stats+"  -1 ❤️");
        statsLabel.setText("<html><font color='#FFD700'>" +stats+ "</font>  <font color='#FF4444'>-1 ❤️</font></html>");

    }
}
package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "How to Play", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(600, 700);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 20, 35, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(new Color(180, 80, 255));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("<html><font color='#00BFFF'>?</font> <font color='#87CEFA'>How to Play</font></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel closeBtn = new JLabel("✖");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        closeBtn.setForeground(new Color(150, 100, 200));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        headerPanel.add(closeBtn, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);
        contentContainer.setBorder(new EmptyBorder(10, 0, 10, 10));

        contentContainer.add(createSectionHeader("Basic Controls"));
        contentContainer.add(Box.createVerticalStrut(15));
        
        contentContainer.add(createControlRow("Click", new Color(0, 150, 150), "Click on a cell to reveal it"));
        contentContainer.add(Box.createVerticalStrut(10));
        contentContainer.add(createControlRow("Flag Mode", new Color(60, 80, 180), "Toggle 'Flag Mode ON' button, then click cells to mark suspected mines"));
        contentContainer.add(Box.createVerticalStrut(10));
        contentContainer.add(createControlRow("Re-click", new Color(200, 100, 0), "Orange (skipped) question cells can be clicked again to answer"));

        contentContainer.add(Box.createVerticalStrut(30));

        contentContainer.add(createSectionHeader("Cell Types"));
        contentContainer.add(Box.createVerticalStrut(15));

        JPanel qCellHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qCellHeader.setOpaque(false);
        qCellHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel yellowDot = new JLabel("🟡");
        yellowDot.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel qTitle = new JLabel("Question Cells");
        qTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        qTitle.setForeground(Color.WHITE);
        qCellHeader.add(yellowDot);
        qCellHeader.add(qTitle);
        contentContainer.add(qCellHeader);

        contentContainer.add(Box.createVerticalStrut(5));
        contentContainer.add(createBulletPoint("Trigger a multiple-choice trivia question"));
        contentContainer.add(createBulletPoint("<html><font color='#00FF7F'><b>Correct answer:</b></font> Gain points and +1 life</html>"));
        contentContainer.add(createBulletPoint("<html><font color='#FF6347'><b>Wrong answer:</b></font> Lose points and -1 life</html>"));
        contentContainer.add(createBulletPoint("<html><font color='#FFA500'><b>Skip/Answer Later:</b></font> Cell turns orange, can re-click anytime</html>"));
        contentContainer.add(createBulletPoint("<html><font color='#00FF7F'><b>Answered:</b></font> Cell turns green</html>"));
        contentContainer.add(createBulletPoint("30-second timer (auto-skips if time runs out)"));

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 120);
                this.trackColor = new Color(30, 30, 40, 0);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            
            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                g.setColor(new Color(80, 80, 100));
                g.fillRoundRect(thumbBounds.x + 4, thumbBounds.y, 6, thumbBounds.height, 6, 6);
            }
        });

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionHeader(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel icon = new JLabel("✨");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        icon.setForeground(Color.CYAN);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(new Color(135, 206, 250));

        p.add(icon);
        p.add(label);
        return p;
    }

    private JPanel createControlRow(String badgeText, Color badgeColor, String descText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(badgeText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.setColor(new Color(255,255,255,50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                super.paintComponent(g);
            }
        };
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setPreferredSize(new Dimension(90, 26));

        JTextArea desc = new JTextArea(descText);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(220, 220, 220));
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setPreferredSize(new Dimension(400, 40));

        panel.add(badge);
        panel.add(desc);
        return panel;
    }

    private JPanel createBulletPoint(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(2, 40, 2, 0));

        JLabel dot = new JLabel("•");
        dot.setForeground(Color.LIGHT_GRAY);
        dot.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(200, 200, 200));

        p.add(dot);
        p.add(label);
        return p;
    }
}
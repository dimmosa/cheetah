package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.GameHistoryController;
import model.GameHistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.List;

public class MainMenuPrivateScreen extends JPanel {

    JFrame frame;

    public MainMenuPrivateScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JLabel topBadge = new JLabel("👤 Private Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 180, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        topBadge.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBadge.setForeground(Color.WHITE);
        topBadge.setBorder(new EmptyBorder(8, 20, 8, 20));
        topBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel badgeWrapper = new JPanel();
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(topBadge);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(15, 25, 40, 240));
        card.setBorder(new EmptyBorder(40, 60, 50, 60));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");
        card.setPreferredSize(new Dimension(450, 500));

        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RadialGradientPaint rgp = new RadialGradientPaint(
                    getWidth()/2f, getHeight()/2f, 40f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 150, 255, 100), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(rgp);
                g2.fillOval(getWidth()/2 - 40, getHeight()/2 - 40, 80, 80);
                super.paintComponent(g);
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setMaximumSize(new Dimension(100, 100));
        iconContainer.add(iconLabel);

        JLabel title = new JLabel("MineSweeper");
        title.setFont(new Font("SansSerif", Font.PLAIN, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Private Account");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setForeground(new Color(0, 160, 220));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnPractice = createMenuButton("🎮   Practice Board");
        JButton btnHistory = createMenuButton("🕒   History");
        JButton btnExit = createMenuButton("❌   Exit");

        btnPractice.addActionListener(e ->
        {
            frame.setContentPane(new PrivateGameScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        btnHistory.addActionListener(e ->
        {
            GameHistoryController controller = new GameHistoryController();
            List<GameHistoryEntry> list = controller.getSimpleHistoryForLoggedUser();
            System.out.println(list);
            frame.setContentPane(new GameHistoryScreen(frame,list,"single"));
            frame.revalidate();
            frame.repaint();
        });

        btnExit.addActionListener(e ->{
            frame.setContentPane(new LoginPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(iconContainer);
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(50));
        
        card.add(btnPractice);
        card.add(Box.createVerticalStrut(15));
        card.add(btnHistory);
        card.add(Box.createVerticalStrut(15));
        card.add(btnExit);

        mainContainer.add(badgeWrapper);
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(card);

        add(mainContainer);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            private Color normalColor = new Color(30, 40, 60);
            private Color hoverColor = new Color(50, 70, 100);
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(hovering ? hoverColor : normalColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setPreferredSize(new Dimension(350, 55));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(5, 15, 30), getWidth(), getHeight(), new Color(10, 25, 50));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        for(int i=0; i<20; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            g2.fillRect(x, y, 2, 2);
        }
    }
}
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class AvatarSelectionDialog extends JDialog {


    public static final String[] AVATAR_EMOJIS = {
        "😎", "🤓", "🥳", "🤖", 
        "👻", "😺", "🐶", "🦊", 
        "🐼", "🦄", "🐉", "🦁"
    };

    private static final Color[][] AVATAR_COLORS = {
        {new Color(255, 80, 60), new Color(255, 140, 60)},
        {new Color(100, 100, 255), new Color(180, 100, 255)},
        {new Color(255, 200, 0), new Color(255, 160, 0)},
        {new Color(0, 180, 255), new Color(0, 100, 200)},
        
        {new Color(180, 60, 220), new Color(220, 100, 200)},
        {new Color(255, 60, 100), new Color(255, 100, 100)},
        {new Color(255, 180, 0), new Color(220, 140, 0)},
        {new Color(255, 60, 0), new Color(220, 40, 0)},

        {new Color(100, 110, 130), new Color(140, 150, 170)},
        {new Color(240, 120, 240), new Color(200, 100, 255)},
        {new Color(0, 200, 100), new Color(50, 220, 150)},
        {new Color(240, 120, 20), new Color(255, 160, 40)}
    };

    private Consumer<Integer> onAvatarSelected;

    public AvatarSelectionDialog(Frame owner, String playerName, Consumer<Integer> onAvatarSelected) {
        super(owner, "Choose Avatar", true);
        this.onAvatarSelected = onAvatarSelected;
        
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setSize(500, 550);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(30, 35, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.setColor(new Color(60, 70, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        JLabel title = new JLabel("Choose Avatar");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBounds(40, 30, 200, 30);
        mainPanel.add(title);

        JLabel subtitle = new JLabel(playerName);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(150, 160, 190));
        subtitle.setBounds(40, 60, 200, 20);
        mainPanel.add(subtitle);

        JLabel closeBtn = new JLabel("✖");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setForeground(new Color(150, 160, 190));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBounds(450, 30, 30, 30);
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
        });
        mainPanel.add(closeBtn);

        JPanel gridPanel = new JPanel(new GridLayout(3, 4, 15, 15));
        gridPanel.setOpaque(false);
        gridPanel.setBounds(40, 100, 420, 400);

        for (int i = 0; i < AVATAR_EMOJIS.length; i++) {
            final int index = i;
            JButton btn = createAvatarButton(i);
            btn.addActionListener(e -> {
                if (onAvatarSelected != null) {
                    onAvatarSelected.accept(index);
                }
                dispose();
            });
            gridPanel.add(btn);
        }

        mainPanel.add(gridPanel);
    }

    private JButton createAvatarButton(int index) {
        String emoji = AVATAR_EMOJIS[index];
        Color c1 = AVATAR_COLORS[index][0];
        Color c2 = AVATAR_COLORS[index][1];

        JButton btn = new JButton(emoji) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }
    
    public static String getEmojiByIndex(int index) {
        if (index >= 0 && index < AVATAR_EMOJIS.length) {
            return AVATAR_EMOJIS[index];
        }
        return "❓";
    }
}
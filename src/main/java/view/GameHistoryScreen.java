package view;

import model.GameHistoryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameHistoryScreen extends JPanel {

    private JFrame frame;
    private List<GameHistoryEntry> records;
    private JPanel historyContainer;
    String mode;


    public GameHistoryScreen(JFrame frame, List<GameHistoryEntry> records, String mode) {
        this.frame = frame;
        this.records = records;
        this.mode = mode;

        setLayout(new GridBagLayout());
        setBackground(new Color(15, 23, 35));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(20, 30, 15, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createHeaderPanel(), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 30, 20, 30);
        gbc.fill = GridBagConstraints.BOTH;
        add(createHistoryScrollPane(), gbc);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 35));

        GridBagConstraints gbc = new GridBagConstraints();

        JButton backBtn = new JButton("←");
        backBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(30, 41, 59));
        backBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backBtn.addActionListener(e -> {
            if(mode.equals("multi")){
                frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            }else{
            frame.setContentPane(new MainMenuPrivateScreen(frame));
            }
            frame.revalidate();
            frame.repaint();
        });

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(backBtn, gbc);

        JLabel title = new JLabel("Game History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(title, gbc);

        return panel;
    }

    private JScrollPane createHistoryScrollPane() {
        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setBackground(new Color(15, 23, 35));

        for (GameHistoryEntry record : records) {
            historyContainer.add(createGameCard(record));
            historyContainer.add(Box.createVerticalStrut(15));
        }

        JScrollPane scrollPane = new JScrollPane(historyContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getVerticalScrollBar().setBackground(new Color(30, 41, 59));

        return scrollPane;
    }

    private JPanel createGameCard(GameHistoryEntry r) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 20);
        card.add(createTrophyIcon(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(createCardHeader(r), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(12, 0, 0, 0);
        card.add(createStatsRow(r), gbc);

        return card;
    }

    private JPanel createTrophyIcon() {
        JPanel p = new JPanel();
        p.setBackground(new Color(45, 60, 85));
        p.setPreferredSize(new Dimension(80, 80));

        p.add(emojiLabel("🏆", 40, false));
        return p;
    }

    private JPanel createCardHeader(GameHistoryEntry r) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(30, 41, 59));

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel user = emojiLabel("👤 " + r.getPlayer1(), 18, true);

        String p1 = r.getPlayer1();
        String p2 = r.getPlayer2();

        String modeText;
        if (!p2.equalsIgnoreCase("Practice Mode")) {
            modeText = "🤝 " + p1 + " & " + p2;
        } else {
            modeText = "🎮 Practice Mode";
        }

        JLabel mode = emojiLabel(modeText, 14, false);
        mode.setForeground(new Color(148, 163, 184));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        p.add(user, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0);
        p.add(mode, gbc);

        JLabel difficultyBadge = emojiLabel("", 14, true);
        difficultyBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        difficultyBadge.setOpaque(true);
        difficultyBadge.setForeground(Color.WHITE);
        difficultyBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        switch (r.getDifficulty()) {
            case "Hard" -> {
                difficultyBadge.setBackground(new Color(239, 68, 68));
                difficultyBadge.setText("⭐⭐⭐");
            }
            case "Medium" -> {
                difficultyBadge.setBackground(new Color(251, 191, 36));
                difficultyBadge.setText("⭐⭐");
            }
            default -> {
                difficultyBadge.setBackground(new Color(34, 197, 94));
                difficultyBadge.setText("⭐");
            }
        }

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 15, 0, 0);
        gbc.anchor = GridBagConstraints.EAST;
        p.add(difficultyBadge, gbc);

        return p;
    }


    private JPanel createStatsRow(GameHistoryEntry r) {
        JPanel p = new JPanel(new GridLayout(1, 3, 20, 0));
        p.setBackground(new Color(30, 41, 59));

        p.add(stat("⚡ Score", r.getFinalScore() + " pts"));
        p.add(stat("⏱ Duration", formatTime(r.getDurationSeconds())));
        p.add(stat("🏁 Result", r.isWon() ? "Won 🎉" : "Lost ❌"));

        return p;
    }

    private JPanel stat(String title, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(new Color(30, 41, 59));

        JLabel t = emojiLabel(title, 12, false);
        t.setForeground(new Color(148, 163, 184));

        JLabel v = emojiLabel(value, 16, true);

        p.add(t);
        p.add(v);

        return p;
    }

    private String formatTime(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    private JLabel emojiLabel(String text, int size, boolean bold) {
        return new JLabel(text) {{
            setFont(new Font("Segoe UI Emoji", bold ? Font.BOLD : Font.PLAIN, size));
            setForeground(Color.WHITE);
        }};
    }
}
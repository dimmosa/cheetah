package view;

import model.DetailedGameHistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DetailedGameHistoryScreen extends JPanel {

    private JFrame frame;
    private List<DetailedGameHistoryEntry> records;
    private JPanel historyContainer;

    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 24);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public DetailedGameHistoryScreen(JFrame frame, List<DetailedGameHistoryEntry> records) {
        this.frame = frame;
        this.records = records;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 23, 35));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createHistoryScrollPane(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 23, 35));
        panel.setBorder(new EmptyBorder(20, 30, 15, 30));

        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(30, 41, 59));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setFocusPainted(false);

        backBtn.addActionListener(e -> {
            frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });

        JLabel title = new JLabel("Detailed Game History", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel icon = new JLabel("📊 ");
        icon.setFont(EMOJI_FONT);
        icon.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(15, 23, 35));
        titlePanel.add(icon);
        titlePanel.add(title);

        panel.add(backBtn, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(100), BorderLayout.EAST);

        return panel;
    }

    private JScrollPane createHistoryScrollPane() {
        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        historyContainer.setBackground(new Color(15, 23, 35));
        historyContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        if (records.isEmpty()) {
            JLabel emptyLabel = new JLabel("No game history yet. Start playing to see your records!");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            emptyLabel.setForeground(new Color(148, 163, 184));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            historyContainer.add(Box.createVerticalStrut(50));
            historyContainer.add(emptyLabel);
        } else {
            for (DetailedGameHistoryEntry record : records) {
                historyContainer.add(createDetailedGameCard(record));
                historyContainer.add(Box.createVerticalStrut(25));
            }
        }

        JScrollPane scrollPane = new JScrollPane(historyContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(15, 23, 35));

        scrollPane.getVerticalScrollBar().setBackground(new Color(15, 23, 35));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        return scrollPane;
    }

    private JPanel createDetailedGameCard(DetailedGameHistoryEntry r) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        card.add(createGameOverview(r), BorderLayout.NORTH);
        card.add(createPlayerStatsPanel(r), BorderLayout.CENTER);

        return card;
    }

    private JPanel createGameOverview(DetailedGameHistoryEntry r) {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftSection.setBackground(new Color(30, 41, 59));

        JLabel trophy = new JLabel(r.isWon() ? "🏆" : "💀");
        trophy.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));

        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        resultPanel.setBackground(new Color(30, 41, 59));

        JLabel resultLabel = new JLabel(r.isWon() ? "VICTORY" : "DEFEAT");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        resultLabel.setForeground(r.isWon() ? new Color(34, 197, 94) : new Color(239, 68, 68));

        JLabel timestampLabel = new JLabel(r.getTimestamp());
        timestampLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timestampLabel.setForeground(new Color(148, 163, 184));

        resultPanel.add(resultLabel);
        resultPanel.add(timestampLabel);

        leftSection.add(trophy);
        leftSection.add(resultPanel);

        JPanel rightSection = new JPanel(new GridLayout(1, 3, 30, 0));
        rightSection.setBackground(new Color(30, 41, 59));

        rightSection.add(createInfoBox("Difficulty", getDifficultyBadge(r.getDifficulty())));
        rightSection.add(createInfoBox("Final Score", r.getFinalScore() + " pts"));
        rightSection.add(createInfoBox("Duration", formatTime(r.getDurationSeconds())));

        panel.add(leftSection, BorderLayout.WEST);
        panel.add(rightSection, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPlayerStatsPanel(DetailedGameHistoryEntry r) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(30, 41, 59));

        panel.add(createPlayerCard(
                r.getPlayer1(),
                new Color(56, 189, 248),
                r.getPlayer1QuestionsAnswered(),
                r.getPlayer1QuestionsCorrect(),
                r.getPlayer1SurprisesActivated(),
                r.getPlayer1GoodSurprises(),
                r.getPlayer1BadSurprises(),
                r.getPlayer1MinesFlagged(),
                r.getPlayer1CorrectFlags(),
                r.getPlayer1WrongFlags(),
                r.getPlayer1CellsRevealed(),
                r.getPlayer1MinesRevealed()
        ));

        panel.add(createPlayerCard(
                r.getPlayer2(),
                new Color(244, 114, 182),
                r.getPlayer2QuestionsAnswered(),
                r.getPlayer2QuestionsCorrect(),
                r.getPlayer2SurprisesActivated(),
                r.getPlayer2GoodSurprises(),
                r.getPlayer2BadSurprises(),
                r.getPlayer2MinesFlagged(),
                r.getPlayer2CorrectFlags(),
                r.getPlayer2WrongFlags(),
                r.getPlayer2CellsRevealed(),
                r.getPlayer2MinesRevealed()
        ));

        return panel;
    }

    private JPanel createPlayerCard(String name, Color accentColor,
                                    int questions, int correct, int surprises,
                                    int goodSurp, int badSurp, int flagged,
                                    int correctFlags, int wrongFlags,
                                    int revealed, int minesHit) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(40, 50, 65));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setBackground(new Color(40, 50, 65));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel playerIcon = new JLabel("👤");
        playerIcon.setFont(EMOJI_FONT);
        playerIcon.setForeground(Color.WHITE);

        JLabel playerName = new JLabel(name);
        playerName.setFont(HEADER_FONT);
        playerName.setForeground(accentColor);

        header.add(playerIcon);
        header.add(playerName);
        card.add(header);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(71, 85, 105));
        sep.setBackground(new Color(71, 85, 105));
        card.add(Box.createVerticalStrut(10));
        card.add(sep);
        card.add(Box.createVerticalStrut(10));

        card.add(createStatRow("Total Questions", String.valueOf(questions), false));
        double qAccuracy = questions > 0 ? (correct * 100.0 / questions) : 0;
        card.add(createStatRow("   Correct", correct + " (" + String.format("%.0f%%", qAccuracy) + ")", true));

        card.add(Box.createVerticalStrut(8));

        card.add(createStatRow("Mines Flagged", String.valueOf(flagged), false));
        double fAccuracy = flagged > 0 ? (correctFlags * 100.0 / flagged) : 0;
        card.add(createStatRow("   Correct", correctFlags + " (" + String.format("%.0f%%", fAccuracy) + ")", true));
        card.add(createStatRow("   Wrong", String.valueOf(wrongFlags), true));

        card.add(Box.createVerticalStrut(8));

        card.add(createStatRow("Surprises", String.valueOf(surprises), false));
        JPanel surpriseDetail = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        surpriseDetail.setBackground(new Color(40, 50, 65));
        surpriseDetail.add(createInlineStat("😊 " + goodSurp));
        surpriseDetail.add(Box.createHorizontalStrut(15));
        surpriseDetail.add(createInlineStat("😢 " + badSurp));
        surpriseDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
        surpriseDetail.setBorder(new EmptyBorder(2, 10, 2, 0));
        card.add(surpriseDetail);

        card.add(Box.createVerticalStrut(4));

        card.add(createStatRow("Cells Revealed", String.valueOf(revealed), false));
        card.add(createStatRow("Mines Hit", String.valueOf(minesHit), false));

        return card;
    }

    private JPanel createStatRow(String label, String value, boolean isSubStat) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(40, 50, 65));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        int leftPad = isSubStat ? 15 : 0;
        row.setBorder(new EmptyBorder(1, leftPad, 1, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(isSubStat ? new Font("Segoe UI", Font.PLAIN, 13) : LABEL_FONT);
        labelComp.setForeground(isSubStat ? new Color(148, 163, 184) : new Color(226, 232, 240));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(VALUE_FONT);
        valueComp.setForeground(isSubStat ? new Color(203, 213, 225) : new Color(253, 224, 71));

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);

        return row;
    }

    private JLabel createInlineStat(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lbl.setForeground(new Color(203, 213, 225));
        return lbl;
    }

    private JPanel createInfoBox(String label, String value) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(new Color(30, 41, 59));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelComp.setForeground(new Color(148, 163, 184));
        labelComp.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        if(label.equals("Difficulty")) {
            valueComp.setForeground(Color.YELLOW);
        }else{
        valueComp.setForeground(Color.WHITE);
        }
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(labelComp);
        panel.add(valueComp);

        return panel;
    }

    private String getDifficultyBadge(String difficulty) {
        return switch (difficulty) {
            case "Hard" -> "⭐⭐⭐";
            case "Medium" -> "⭐⭐";
            default -> "⭐";
        };
    }

    private String formatTime(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}
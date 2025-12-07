package Test;

import model.DetailedGameHistoryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.DetailedGameHistoryScreen;
import view.MainMenuTwoPlayerScreen;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DetailedGameHistoryScreenTest {

    private JFrame frame;

    // ───────────────────── Helpers ─────────────────────

    private JLabel findLabelExact(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel label) {
                if (text.equals(label.getText())) {
                    return label;
                }
            } else if (c instanceof Container container) {
                JLabel result = findLabelExact(container, text);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JButton findButtonExact(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton btn) {
                if (text.equals(btn.getText())) {
                    return btn;
                }
            } else if (c instanceof Container container) {
                JButton result = findButtonExact(container, text);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JLabel findLabelContaining(Container root, String substring) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel label) {
                if (label.getText() != null && label.getText().contains(substring)) {
                    return label;
                }
            } else if (c instanceof Container container) {
                JLabel result = findLabelContaining(container, substring);
                if (result != null) return result;
            }
        }
        return null;
    }

    // ───────────────────── Tests ─────────────────────

    @BeforeEach
    void setUp() {
        frame = new JFrame();
    }

    @Test
    void emptyHistoryShowsEmptyMessage() {
        List<DetailedGameHistoryEntry> records = new ArrayList<>();
        DetailedGameHistoryScreen screen = new DetailedGameHistoryScreen(frame, records);
        frame.setContentPane(screen);

        JLabel empty = findLabelContaining(
                screen,
                "No game history yet. Start playing to see your records!"
        );
        assertNotNull(empty, "Empty-history info label should be shown when there are no records");
    }

    @Test
    void backButtonReturnsToMainMenu() {
        List<DetailedGameHistoryEntry> records = new ArrayList<>();
        DetailedGameHistoryScreen screen = new DetailedGameHistoryScreen(frame, records);
        frame.setContentPane(screen);

        JButton backBtn = findButtonExact(screen, "← Back");
        assertNotNull(backBtn, "Back button should exist");

        backBtn.doClick();

        assertTrue(frame.getContentPane() instanceof MainMenuTwoPlayerScreen,
                "After clicking Back, content pane should be MainMenuTwoPlayerScreen");
    }

    @Test
    void victoryRecordShowsVictoryLabelAndCorrectInfo() {
        // Create one sample record: player1, player2, difficulty, score, duration, won
        DetailedGameHistoryEntry record =
                new DetailedGameHistoryEntry("P1", "P2", "Medium", 123, 90, true);

        List<DetailedGameHistoryEntry> records = new ArrayList<>();
        records.add(record);

        DetailedGameHistoryScreen screen = new DetailedGameHistoryScreen(frame, records);
        frame.setContentPane(screen);

        // Victory / defeat label
        JLabel victoryLabel = findLabelExact(screen, "VICTORY");
        assertNotNull(victoryLabel, "Record should display 'VICTORY' for a won game");

        // Difficulty badge (Medium → ⭐⭐)
        JLabel difficultyBadge = findLabelExact(screen, "⭐⭐");
        assertNotNull(difficultyBadge, "Medium difficulty should be displayed as '⭐⭐'");

        // Final score
        JLabel scoreLabel = findLabelExact(screen, "123 pts");
        assertNotNull(scoreLabel, "Final score '123 pts' should be displayed");

        // Duration 90 sec → 01:30
        JLabel durationLabel = findLabelExact(screen, "01:30");
        assertNotNull(durationLabel, "Duration 90 seconds should be formatted as 01:30");

        // Player names appear somewhere
        JLabel p1Label = findLabelExact(screen, "P1");
        JLabel p2Label = findLabelExact(screen, "P2");
        assertNotNull(p1Label, "Player 1 name should be displayed");
        assertNotNull(p2Label, "Player 2 name should be displayed");
    }

    @Test
    void defeatRecordShowsDefeatLabel() {
        DetailedGameHistoryEntry record =
                new DetailedGameHistoryEntry("P1", "P2", "Easy", 50, 40, false);

        List<DetailedGameHistoryEntry> records = new ArrayList<>();
        records.add(record);

        DetailedGameHistoryScreen screen = new DetailedGameHistoryScreen(frame, records);
        frame.setContentPane(screen);

        JLabel defeatLabel = findLabelExact(screen, "DEFEAT");
        assertNotNull(defeatLabel, "Record should display 'DEFEAT' for a lost game");
    }
}
package Test;

import control.MultiPlayerGameController;
import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.GameScreenMultiPlayer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class GameScreenMultiPlayerTest {

    private JFrame frame;
    private FakeMultiPlayerGameController controller;
    private GameScreenMultiPlayer gameScreen;

    @BeforeEach
    void setUp() throws Exception {
        // Create a real JFrame (not headless)
        frame = new JFrame();

        SysData sysData = SysData.getInstance();
        User p1 = new User("JUnitP1", "123");
        User p2 = new User("JUnitP2", "123");

        controller = new FakeMultiPlayerGameController(sysData, p1, p2, "Medium", 8);

        // Build the screen (not showing the frame)
        gameScreen = new GameScreenMultiPlayer(frame, controller);
    }

    /**
     * Test-double controller to avoid running a real timer.
     */
    private static class FakeMultiPlayerGameController extends MultiPlayerGameController {

        public FakeMultiPlayerGameController(SysData sysData, User p1, User p2,
                                             String difficulty, int gridSize) {
            super(sysData, p1, p2, difficulty, gridSize);
        }

        @Override
        public void startTimer(java.util.function.Consumer<String> onTick) {
            // Override: no timer for tests
        }

        @Override
        public void stopTimer() {
            // Override: do nothing for tests
        }

        @Override
        public void giveUp() {
            // Override for test; no real logic
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TESTS
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void toggleFlagModeChangesButtonText() throws Exception {
        Field flagButtonField = GameScreenMultiPlayer.class.getDeclaredField("flagButton");
        flagButtonField.setAccessible(true);
        JButton flagButton = (JButton) flagButtonField.get(gameScreen);

        assertEquals("FLAG MODE: OFF", flagButton.getText());

        flagButton.doClick();
        assertEquals("FLAG MODE: ON", flagButton.getText());

        flagButton.doClick();
        assertEquals("FLAG MODE: OFF", flagButton.getText());
    }

    @Test
    void livesLabelShowsSharedLivesAndMaxLives() throws Exception {
        Field livesField = GameScreenMultiPlayer.class.getDeclaredField("livesLabel");
        livesField.setAccessible(true);

        JLabel livesLabel = (JLabel) livesField.get(gameScreen);

        assertNotNull(livesLabel);
        assertTrue(livesLabel.getText().contains("/"),
                "Lives label should be in format 'current / max'");
    }

    @Test
    void highlightActivePlayerPanelUpdatesBorders() throws Exception {
        Field p1Field = GameScreenMultiPlayer.class.getDeclaredField("player1PanelContainer");
        Field p2Field = GameScreenMultiPlayer.class.getDeclaredField("player2PanelContainer");
        p1Field.setAccessible(true);
        p2Field.setAccessible(true);

        JPanel p1Panel = (JPanel) p1Field.get(gameScreen);
        JPanel p2Panel = (JPanel) p2Field.get(gameScreen);

        assertNotNull(p1Panel);
        assertNotNull(p2Panel);

        // Set active player = 1
        setCurrentPlayer(controller, 1);
        gameScreen.updateActivePlayer();

        assertTrue(p1Panel.getBorder() instanceof LineBorder);
        assertTrue(p2Panel.getBorder() instanceof LineBorder);

        LineBorder p1Border = (LineBorder) p1Panel.getBorder();
        LineBorder p2Border = (LineBorder) p2Panel.getBorder();

        assertEquals(2, p1Border.getThickness());
        assertEquals(1, p2Border.getThickness());

        // Switch to player 2
        setCurrentPlayer(controller, 2);
        gameScreen.updateActivePlayer();

        p1Border = (LineBorder) p1Panel.getBorder();
        p2Border = (LineBorder) p2Panel.getBorder();

        assertEquals(1, p1Border.getThickness());
        assertEquals(2, p2Border.getThickness());
    }

    @Test
    void headerTitleContainsDifficulty() {
        JLabel title = findLabelStartsWith(gameScreen, "CO-OP MINESWEEPER -");
        assertNotNull(title, "Header title label must exist");
        assertTrue(title.getText().contains("Medium"),
                "Header must include the difficulty text");
    }

    // ─────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────

    private void setCurrentPlayer(MultiPlayerGameController controller, int player) throws Exception {
        Field f = MultiPlayerGameController.class.getDeclaredField("currentPlayer");
        f.setAccessible(true);
        f.setInt(controller, player);
    }

    /** Recursively find a JLabel whose text begins with a given prefix */
    private JLabel findLabelStartsWith(Container root, String prefix) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel label) {
                if (label.getText() != null && label.getText().startsWith(prefix)) {
                    return label;
                }
            } else if (c instanceof Container cont) {
                JLabel result = findLabelStartsWith(cont, prefix);
                if (result != null) return result;
            }
        }
        return null;
    }
}
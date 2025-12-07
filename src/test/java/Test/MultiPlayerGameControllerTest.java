package Test;

import control.MultiPlayerGameController;
import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerGameControllerTest {

    private SysData sysData;
    private MultiPlayerGameController controller;

    @BeforeEach
    void setUp() throws Exception {
        sysData = SysData.getInstance();
        sysData.clearDetailedHistory();

        User p1 = new User("JUnitP1", "123");
        User p2 = new User("JUnitP2", "123");

        controller = new MultiPlayerGameController(sysData, p1, p2, "Medium", 13);

        // Force current player = 1 (constructor chooses randomly)
        setIntField(controller, "currentPlayer", 1);
    }

    // Helper to set private int fields by reflection
    private void setIntField(Object target, String fieldName, int value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(target, value);
    }

    @Test
    void revealMineReducesLifeAndEndsTurn() {
        int livesBefore = controller.getSharedLives();
        int playerBefore = controller.getCurrentPlayer();

        MultiPlayerGameController.CellActionResult result = controller.revealMine();

        assertEquals(livesBefore - 1, controller.getSharedLives());
        assertTrue(result.turnEnded);
        assertEquals(-1, result.livesChanged);
        assertNotEquals(playerBefore, controller.getCurrentPlayer());
        assertFalse(controller.isGameOver());
    }

    @Test
    void flagMineCorrectlyIncreasesScoreButDoesNotEndTurn() {
        int scoreBefore = controller.getSharedScore();
        int playerBefore = controller.getCurrentPlayer();

        MultiPlayerGameController.CellActionResult result = controller.flagMineCorrectly();

        int scoreDelta = controller.getSharedScore() - scoreBefore;

        assertFalse(result.turnEnded);
        assertEquals(scoreDelta, result.pointsChanged);
        assertTrue(scoreDelta > 0);
        assertEquals(playerBefore, controller.getCurrentPlayer());
    }

    @Test
    void flagIncorrectlyDecreasesScoreButDoesNotEndTurn() {
        int scoreBefore = controller.getSharedScore();
        int playerBefore = controller.getCurrentPlayer();

        MultiPlayerGameController.CellActionResult result = controller.flagIncorrectly();

        int scoreDelta = controller.getSharedScore() - scoreBefore;

        assertFalse(result.turnEnded);
        assertEquals(scoreDelta, result.pointsChanged);
        assertTrue(scoreDelta < 0);
        assertEquals(playerBefore, controller.getCurrentPlayer());
    }

    @Test
    void activateQuestionCorrectMediumDifficultyQ1GivesExpectedReward() throws Exception {
        // Make sure we are NOT at max lives to avoid extra-life bonus logic
        int targetLives = controller.getMaxLives() - 1;
        setIntField(controller, "sharedLives", targetLives);

        int scoreBefore = controller.getSharedScore();
        int livesBefore = controller.getSharedLives();
        int playerBefore = controller.getCurrentPlayer();

        MultiPlayerGameController.CellActionResult result =
                controller.activateQuestion(1, true);

        // For Medium + q=1 + correct → +8 points, +1 life (no overflow now)
        assertTrue(result.turnEnded);
        assertEquals(8, result.pointsChanged,
                "For Medium, q=1, correct => +8 points");
        assertEquals(1, result.livesChanged,
                "For Medium, q=1, correct => +1 life");

        assertEquals(scoreBefore + 8, controller.getSharedScore());
        assertEquals(livesBefore + 1, controller.getSharedLives());
        assertNotEquals(playerBefore, controller.getCurrentPlayer());
    }

    @Test
    void setPlayerBoardCompleteTriggersVictoryWhenBothComplete() {
        assertFalse(controller.isGameOver());
        assertFalse(controller.isGameWon());

        controller.setPlayerBoardComplete(1, true);
        assertFalse(controller.isGameOver());

        controller.setPlayerBoardComplete(2, true);

        assertTrue(controller.isGameOver());
        assertTrue(controller.isGameWon());
    }

    @Test
    void giveUpEndsGameAndMarksAsLost() {
        assertFalse(controller.isGameOver());
        assertFalse(controller.isGameWon());

        controller.giveUp();

        assertTrue(controller.isGameOver());
        assertFalse(controller.isGameWon());
    }

    @Test
    void handleAllMinesRevealedEndsGameAndMarksAsLost() {
        assertFalse(controller.isGameOver());
        assertFalse(controller.isGameWon());

        controller.handleAllMinesRevealed(1);

        assertTrue(controller.isGameOver());
        assertFalse(controller.isGameWon());
    }
}
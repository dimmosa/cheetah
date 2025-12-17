package Test;

import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import control.MultiPlayerGameController;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerGameController_SetPlayerBoardComplete_Bonus_Test {

    private MultiPlayerGameController ctrl;

    @BeforeEach
    void setup() {
        SysData sysData = null;
        User p1 = new User("p1", "pass");
        User p2 = new User("p2", "pass");
        ctrl = new MultiPlayerGameController(sysData, p1, p2, "Easy", 9);
    }

    private void setInt(String name, int v) {
        try {
            Field f = ctrl.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(ctrl, v);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void setPlayerBoardComplete_setsWinAndAddsBonus() {
        // Easy: getSurprisePoints() = 8
        setInt("sharedScore", 10);
        setInt("sharedLives", 4);

        int beforeScore = ctrl.getSharedScore();
        int lives = ctrl.getSharedLives();

        ctrl.setPlayerBoardComplete(1, true);

        int afterScore = ctrl.getSharedScore();
        int expectedBonus = lives * 8;

        System.out.println("[setPlayerBoardComplete] beforeScore=" + beforeScore +
                ", lives=" + lives +
                " | expectedBonus=" + expectedBonus +
                " -> afterScore=" + afterScore +
                " | gameOver=" + ctrl.isGameOver() +
                ", gameWon=" + ctrl.isGameWon());

        assertTrue(ctrl.isGameOver());
        assertTrue(ctrl.isGameWon());
        assertEquals(beforeScore + expectedBonus, afterScore);
    }
}
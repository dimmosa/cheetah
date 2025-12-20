package Test;

import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import control.MultiPlayerGameController;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerGameController_RevealMine_GameOver_Test {

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

    private void setObj(String name, Object v) {
        try {
            Field f = ctrl.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(ctrl, v);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void revealMine_whenLastLife_reachesGameOver() {
        setObj("currentPlayer", 1);
        setInt("sharedLives", 1);

        int beforeLives = ctrl.getSharedLives();
        int beforePlayer = ctrl.getCurrentPlayer();

        var r = ctrl.revealMine();

        System.out.println("[revealMine gameOver] beforeLives=" + beforeLives +
                ", beforePlayer=" + beforePlayer +
                " | livesDelta=" + r.livesChanged +
                " -> afterLives=" + ctrl.getSharedLives() +
                " | gameOver=" + ctrl.isGameOver() +
                ", gameWon=" + ctrl.isGameWon());

        assertEquals(0, ctrl.getSharedLives());
        assertTrue(ctrl.isGameOver());
        assertFalse(ctrl.isGameWon());
        // כשהמשחק נגמר, endTurn לא אמור להחליף שחקן (כי checkGameOver קובע gameOver ואז endTurn לא מחליף)
        assertEquals(1, ctrl.getCurrentPlayer());
    }
}
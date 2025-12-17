package Test;

import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import control.MultiPlayerGameController;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerGameController_ActivateQuestion_Easy_Q4_Wrong_Test {

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
    void activateQuestion_easy_q4_wrong_appliesCostAndPenaltyAndLifeLoss() {
        // Easy: cost=5, Q4 wrong => points=-15, lives=-1
        // Δpoints = -5 + (-15) = -20, Δlives = -1
        setInt("sharedScore", 50);
        setInt("sharedLives", 5);

        int beforeScore = ctrl.getSharedScore();
        int beforeLives = ctrl.getSharedLives();

        var r = ctrl.activateQuestion(4, false);

        System.out.println("[activateQuestion easy Q4 wrong] beforeScore=" + beforeScore +
                ", beforeLives=" + beforeLives +
                " | pointsDelta=" + r.pointsChanged +
                ", livesDelta=" + r.livesChanged +
                " -> afterScore=" + ctrl.getSharedScore() +
                ", afterLives=" + ctrl.getSharedLives());

        assertFalse(r.turnEnded);
        assertEquals(-20, r.pointsChanged);
        assertEquals(-1, r.livesChanged);
        assertEquals(30, ctrl.getSharedScore());
        assertEquals(4, ctrl.getSharedLives());
    }
}
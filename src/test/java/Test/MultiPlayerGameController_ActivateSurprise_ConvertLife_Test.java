package Test;

import model.SysData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import control.MultiPlayerGameController;

import java.lang.reflect.Field;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPlayerGameController_ActivateSurprise_ConvertLife_Test {

    private MultiPlayerGameController ctrl;

    static class FixedRandomGood extends Random {
        @Override public boolean nextBoolean() { return true; } // isGood = true
    }

    @BeforeEach
    void setup() {
        SysData sysData = null;
        User p1 = new User("p1", "pass");
        User p2 = new User("p2", "pass");
        ctrl = new MultiPlayerGameController(sysData, p1, p2, "Easy", 9);
        injectRandom(new FixedRandomGood());
    }

    private void injectRandom(Random r) {
        try {
            Field f = ctrl.getClass().getDeclaredField("random");
            f.setAccessible(true);
            f.set(ctrl, r);
        } catch (Exception e) {
            fail(e.getMessage());
        }
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
    void activateSurprise_good_whenLivesFull_convertsLifeToPoints() {
        // Easy: cost=5, surprisePoints=8, lifeConversion=5
        setInt("sharedScore", 10);
        setInt("sharedLives", ctrl.getMaxLives()); // already full

        int beforeScore = ctrl.getSharedScore();
        int beforeLives = ctrl.getSharedLives();

        var r = ctrl.activateSurprise();

        System.out.println("[activateSurprise convert] beforeScore=" + beforeScore +
                ", beforeLives=" + beforeLives +
                " | pointsDelta=" + r.pointsChanged +
                ", livesDelta=" + r.livesChanged +
                " -> afterScore=" + ctrl.getSharedScore() +
                ", afterLives=" + ctrl.getSharedLives());

        // Δpoints = -5 + 8 + 5 = +8, Δlives = 0
        assertEquals(8, r.pointsChanged);
        assertEquals(0, r.livesChanged);
        assertEquals(18, ctrl.getSharedScore());
        assertEquals(ctrl.getMaxLives(), ctrl.getSharedLives());
        assertFalse(r.turnEnded);
    }
}

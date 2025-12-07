package Test;

import model.GameHistoryEntry;
import model.Question;
import model.SysData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SysDataTest {

    private static final String APP_DIR =
            System.getProperty("user.home") + File.separator + ".minesweeper";

    @BeforeEach
    void setUp() throws Exception {
        // Clear the .minesweeper directory before each test
        cleanAppDir();
        resetSysDataSingleton();
    }

    /** Deletes the .minesweeper directory and all files inside it (if exists) */
    private void cleanAppDir() {
        File dir = new File(APP_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    /** Resets the SysData singleton using reflection */
    private void resetSysDataSingleton() throws Exception {
        Field instanceField = SysData.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void testSingletonReturnsSameInstance() {
        SysData s1 = SysData.getInstance();
        SysData s2 = SysData.getInstance();

        assertNotNull(s1);
        assertSame(s1, s2, "SysData.getInstance() should always return the same instance");
    }

    @Test
    void testAddQuestionPersistsToFile() throws Exception {
        SysData sysData = SysData.getInstance();

        String[] answers = {"1", "2", "4", "5"};
        Question q = new Question(0, "What is 2+2?", answers, 2, 1);

        boolean saved = sysData.addQuestion(q);
        assertTrue(saved, "addQuestion should return true when the save operation succeeds");

        int sizeBeforeReload = sysData.getQuestions().size();
        assertTrue(sizeBeforeReload > 0, "There should be at least one question after adding");

        // Restart SysData to verify data persistence
        resetSysDataSingleton();
        SysData reloaded = SysData.getInstance();

        List<Question> reloadedQuestions = reloaded.getQuestions();
        assertEquals(sizeBeforeReload, reloadedQuestions.size(),
                "After reload, the number of questions should remain unchanged");

        boolean found = reloadedQuestions.stream()
                .anyMatch(qq -> "What is 2+2?".equals(qq.getText()));
        assertTrue(found, "The added question must be reloaded from CSV");
    }

    @Test
    void testUpdateQuestionUpdatesFields() {
        SysData sysData = SysData.getInstance();

        String[] answers = {"A", "B", "C", "D"};
        Question original = new Question(0, "Original?", answers, 0, 1);
        sysData.addQuestion(original);

        Question added = sysData.getQuestions().get(0);
        int id = added.getId();

        Question updated = new Question(id, "Updated text", answers, 1, 2);
        boolean updatedOk = sysData.updateQuestion(updated);
        assertTrue(updatedOk, "updateQuestion should return true if the question was found and updated");

        Question fromList = sysData.getQuestions().stream()
                .filter(q -> q.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(fromList);
        assertEquals("Updated text", fromList.getText());
        assertEquals(1, fromList.getCorrectIndex());
        assertEquals(2, fromList.getDifficulty());
    }

    @Test
    void testDeleteQuestionRemovesQuestion() {
        SysData sysData = SysData.getInstance();

        String[] answers = {"A", "B", "C", "D"};
        Question q = new Question(0, "To be deleted", answers, 0, 1);
        sysData.addQuestion(q);

        int id = sysData.getQuestions().get(0).getId();
        boolean removed = sysData.deleteQuestion(id);
        assertTrue(removed, "deleteQuestion should return true if the question was successfully removed");

        boolean stillThere = sysData.getQuestions().stream()
                .anyMatch(qq -> qq.getId() == id);
        assertFalse(stillThere, "The deleted question must not remain in the question list");
    }

    @Test
    void testAddGameHistoryPersistsToFile() throws Exception {
        SysData sysData = SysData.getInstance();

        GameHistoryEntry entry =
                new GameHistoryEntry("P1", "P2", "Easy", 100, 60, true);
        sysData.addGameHistory(entry);

        int sizeBeforeReload = sysData.getHistory().size();
        assertEquals(1, sizeBeforeReload, "There should be exactly one history entry after adding");

        // Reload to verify persistence
        resetSysDataSingleton();
        SysData reloaded = SysData.getInstance();
        List<GameHistoryEntry> reloadedHistory = reloaded.getHistory();

        assertEquals(sizeBeforeReload, reloadedHistory.size(),
                "History size after reload should remain unchanged");

        GameHistoryEntry h = reloadedHistory.get(0);
        assertEquals("P1", h.getPlayer1());
        assertEquals("P2", h.getPlayer2());
        assertEquals("Easy", h.getDifficulty());
        assertEquals(100, h.getFinalScore());
        assertEquals(60, h.getDurationSeconds());
        assertTrue(h.isWon());
    }

    @Test
    void testClearDetailedHistoryClearsList() throws Exception {
        SysData sysData = SysData.getInstance();

        // Access detailedGameHistory using reflection
        Field f = SysData.class.getDeclaredField("detailedGameHistory");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<?> detailedList = (List<?>) f.get(sysData);

        detailedList.add(null); // Add dummy data to ensure list is not empty
        assertFalse(detailedList.isEmpty(), "Before clearDetailedHistory the list should not be empty");

        sysData.clearDetailedHistory();

        @SuppressWarnings("unchecked")
        List<?> afterClear = (List<?>) f.get(sysData);
        assertTrue(afterClear.isEmpty(), "After clearDetailedHistory the list must be empty");
    }
}

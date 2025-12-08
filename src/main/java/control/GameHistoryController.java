package control;

import java.util.List;

import model.DetailedGameHistoryEntry;
import model.GameHistoryEntry;
import model.GameHistoryRepository;
import model.GameRecord;

public class GameHistoryController {

    private final GameHistoryRepository repo = new GameHistoryRepository();

    /**
     * Iteration 2:
     * אין לוגין / משתמשים → מחזירים את כל ההיסטוריה (כל המשחקים).
     */
    public List<GameRecord> getHistoryForLoggedUser() {
        return repo.loadHistoryCombined();
    }

    /**
     * מחזיר היסטוריה פשוטה (simple) לכל המשחקים.
     */
    public List<GameHistoryEntry> getSimpleHistoryForLoggedUser() {
        return repo.loadSimpleHistoryCombined();
    }

    /**
     * מחזיר detailed history לכל המשחקים.
     */
    public List<DetailedGameHistoryEntry> getDetailedHistoryForLoggedUser() {
        return repo.loadDetailedHistoryCombined();
    }
}

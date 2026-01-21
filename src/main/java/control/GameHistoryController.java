package control;

import model.DetailedGameHistoryEntry;
import model.GameHistoryEntry;
import model.GameRecord;
import model.SysData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GameHistoryController {

    private final SysData repo;

    public GameHistoryController() {
        this.repo = SysData.getInstance();
    }

    public List<GameRecord> getHistoryForLoggedUser() {
        return new ArrayList<>();
    }

    public List<GameHistoryEntry> getSimpleHistoryForLoggedUser() {
        // Try to reload from file if SysData has loadHistory()
        tryCall(repo, "loadHistory");

        List<GameHistoryEntry> all = repo.getHistory();
        return (all == null) ? new ArrayList<>() : new ArrayList<>(all);
    }

    public List<DetailedGameHistoryEntry> getDetailedHistoryForLoggedUser() {
        List<DetailedGameHistoryEntry> all = repo.getDetailedGameHistory();
        return (all == null) ? new ArrayList<>() : new ArrayList<>(all);
    }

    public List<GameHistoryEntry> getHistoryForUser(String username) {
        List<GameHistoryEntry> all = getSimpleHistoryForLoggedUser();

        if (username == null || username.trim().isEmpty()) return new ArrayList<>(all);

        String u = username.trim().toLowerCase();
        List<GameHistoryEntry> res = new ArrayList<>();

        for (GameHistoryEntry r : all) {
            if (r == null) continue;
            String p1 = safeLower(r.getPlayer1());
            if (u.equals(p1)) res.add(r);
        }
        return res;
    }

    // ✅ ONLY THIS ONE REALLY SAVES
    public void savePracticeResult(String username, String difficulty, int finalScore, int durationSeconds, boolean won) {
        if (username == null) username = "";
        if (difficulty == null) difficulty = "Unknown";

        GameHistoryEntry entry = new GameHistoryEntry(
                username, "Practice Mode", difficulty, finalScore, durationSeconds, won
        );

        repo.addGameHistory(entry);

        // Refresh memory if method exists
        tryCall(repo, "loadHistory");
    }

   
    public void saveMultiplayerResult(String player1, String player2, String difficulty,
                                      int finalScore, int durationSeconds, boolean won) {
        // intentionally disabled
    }

    
    public void saveMultiplayerGame(GameHistoryEntry entry) {
      
    }

    // ------------------ helpers ------------------

    private String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private void tryCall(Object target, String methodName) {
        if (target == null || methodName == null) return;
        try {
            Method m = target.getClass().getMethod(methodName);
            m.invoke(target);
        } catch (Exception ignored) {
        }
    }
}

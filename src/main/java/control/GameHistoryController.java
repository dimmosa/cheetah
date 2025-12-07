package control;

import java.util.List;

import model.DetailedGameHistoryEntry;
import model.GameHistoryEntry;
import model.GameHistoryRepository;
import model.GameRecord;
import model.SessionManager;

public class GameHistoryController {

    private GameHistoryRepository repo = new GameHistoryRepository();

    public List<GameRecord> getHistoryForLoggedUser() {
        if(SessionManager.getInstance().isTwoPlayerMode()){
            String username = SessionManager.getInstance().getPlayer1().getUsername();
            return repo.loadHistoryForUser(username);
        }else{
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            return repo.loadHistoryForUser(username);
        }
    }

    public List<GameHistoryEntry> getSimpleHistoryForLoggedUser() {
        if(SessionManager.getInstance().isTwoPlayerMode()){
            String username = SessionManager.getInstance().getPlayer1().getUsername();
            return repo.loadSimpleHistoryForUser(username);
        }else{
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            return repo.loadSimpleHistoryForUser(username);
        }
    }

    public List<DetailedGameHistoryEntry> getDetailedHistoryForLoggedUser() {
        if(SessionManager.getInstance().isTwoPlayerMode()){
            String username = SessionManager.getInstance().getPlayer1().getUsername();
            return repo.loadDetailedHistoryForUser(username);
        }else{
            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            return repo.loadDetailedHistoryForUser(username);
        }
    }

}
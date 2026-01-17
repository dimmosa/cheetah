package control;

import model.GameHistoryEntry;
import model.SysData;
import model.User;
import model.UserService;

import java.util.List;

public class LoginControl {

    private final UserService userService;
    private final SysData sysData;

    public LoginControl() {
        this.userService = new UserService();
        this.sysData = SysData.getInstance();
    }


    public User login(String username, String password) {
        User user = userService.login(username, password);
        if (user != null) {

            GameHistoryController controller = new GameHistoryController();
            List<GameHistoryEntry>  userHistory=  controller.getSimpleHistoryForLoggedUser();

            int gamesPlayed = userHistory.size();
            int gamesWon = (int) userHistory.stream()
                    .filter(h -> h.isWon() && h.getPlayer1().equals(user.getUsername()))
                    .count();
            int highScore = userHistory.stream()
                    .filter(h -> h.getPlayer1().equals(user.getUsername()))
                    .mapToInt(GameHistoryEntry::getFinalScore)
                    .max()
                    .orElse(0);

            user.setGamesPlayed(gamesPlayed);
            user.setGamesWon(gamesWon);
            user.setHighScore(highScore);

            System.out.println(gamesPlayed);
            System.out.println(gamesWon);
            System.out.println(highScore);
            System.out.println(userHistory.size());

            return user;
        }
        return null;
    }

    public SysData getSysData() {
        return sysData;
    }
}
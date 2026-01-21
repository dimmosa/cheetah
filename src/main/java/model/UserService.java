package model;



import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String APP_DIR =
            System.getProperty("user.home") + File.separator + ".minesweeper";

    private static final String USERS_CSV =
            APP_DIR + File.separator + "users.csv";

    public UserService() {
        ensureAppDirectory();
        ensureUsersFile();
    }

    private void ensureAppDirectory() {
        File dir = new File(APP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void ensureUsersFile() {
        try {
            File file = new File(USERS_CSV);
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("username,password,gamesPlayed,gamesWon,highScore\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean signup(String username, String password) {
        if (getUser(username) != null) return false;

        try (FileWriter fw = new FileWriter(USERS_CSV, true)) {
            fw.write(username + "," + password + ",0,0,0\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public User login(String username, String password) {
        User u = getUser(username);
        if (u != null && u.getPassword().equals(password)) {
            SessionManager.getInstance().login(u);
            return u;
        }
        return null;
    }

    public boolean loginTwoPlayers(String user1, String pass1, String user2, String pass2) {
        User p1 = getUser(user1);
        User p2 = getUser(user2);

        if (p1 != null && p1.getPassword().equals(pass1)
                && p2 != null && p2.getPassword().equals(pass2)) {
            SessionManager.getInstance().loginTwoPlayers(p1, p2);
            return true;
        }
        return false;
    }

    public User getUser(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_CSV))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length >= 2 && parts[0].equals(username)) {
                    return new User(
                            parts[0],
                            parts[1],
                            parts.length > 2 ? Integer.parseInt(parts[2]) : 0,
                            parts.length > 3 ? Integer.parseInt(parts[3]) : 0,
                            parts.length > 4 ? Integer.parseInt(parts[4]) : 0
                    );
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUserStats(String username, int gamesPlayed, int gamesWon, int highScore) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(USERS_CSV))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    lines.add(line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    lines.add(username + "," + parts[1] + "," + gamesPlayed + "," + gamesWon + "," + highScore);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter fw = new FileWriter(USERS_CSV)) {
            for (String l : lines) fw.write(l + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void incrementGamesPlayed(String username) {
        User user = getUser(username);
        if (user != null) {
            updateUserStats(username, user.getGamesPlayed() + 1, user.getGamesWon(), user.getHighScore());
        }
    }

    public void incrementGamesWon(String username) {
        User user = getUser(username);
        if (user != null) {
            updateUserStats(username, user.getGamesPlayed() + 1, user.getGamesWon() + 1, user.getHighScore());
        }
    }

    public void updateHighScore(String username, int score) {
        User user = getUser(username);
        if (user != null && score > user.getHighScore()) {
            updateUserStats(username, user.getGamesPlayed(), user.getGamesWon(), score);
        }
    }
}
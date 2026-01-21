package model;

public class User {
    private String username;
    private String password;
    private int gamesPlayed;
    private int gamesWon;
    private int highScore;

    private String avatar;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.highScore = 0;
    }

    public User(String username, String password, String avatar) {
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.highScore = 0;
    }

    public User(String username, String password, int gamesPlayed, int gamesWon, int highScore) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.highScore = highScore;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
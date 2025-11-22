package model;

public class GameHistoryEntry {
    private String player1;
    private String player2;
    private String difficulty;
    private int finalScore;
    private int durationSeconds;
    private boolean won;

    public GameHistoryEntry() {
    }

    public GameHistoryEntry(String p1, String p2, String difficulty, int finalScore) {
        this(p1, p2, difficulty, finalScore, 0, false);
    }

    public GameHistoryEntry(String p1, String p2, String difficulty, int finalScore, int durationSeconds, boolean won) {
        this.player1 = p1;
        this.player2 = p2;
        this.difficulty = difficulty;
        this.finalScore = finalScore;
        this.durationSeconds = durationSeconds;
        this.won = won;
    }

    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getDifficulty() { return difficulty; }
    public int getFinalScore() { return finalScore; }
    public int getDurationSeconds() { return durationSeconds; }
    public boolean isWon() { return won; }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
}

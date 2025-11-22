package model;

public class PlayerStatistics {
    private String playerName;
    private int gamesPlayed;
    private int gamesWon;
    private int totalQuestions;
    private int totalCorrect;
    private int totalSurprises;
    private int totalGoodSurprises;
    private int totalCorrectFlags;
    private int totalWrongFlags;
    private int totalCellsRevealed;
    private int totalMinesRevealed;

    public PlayerStatistics(String playerName) {
        this.playerName = playerName;
    }

    public void addGame(boolean won, int questions, int correct, int surprises, int goodSurprises,
                        int correctFlags, int wrongFlags, int cellsRevealed, int minesRevealed) {
        gamesPlayed++;
        if (won) gamesWon++;
        totalQuestions += questions;
        totalCorrect += correct;
        totalSurprises += surprises;
        totalGoodSurprises += goodSurprises;
        totalCorrectFlags += correctFlags;
        totalWrongFlags += wrongFlags;
        totalCellsRevealed += cellsRevealed;
        totalMinesRevealed += minesRevealed;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public int getTotalSurprises() {
        return totalSurprises;
    }

    public int getTotalGoodSurprises() {
        return totalGoodSurprises;
    }

    public int getTotalCorrectFlags() {
        return totalCorrectFlags;
    }

    public int getTotalWrongFlags() {
        return totalWrongFlags;
    }

    public int getTotalCellsRevealed() {
        return totalCellsRevealed;
    }

    public int getTotalMinesRevealed() {
        return totalMinesRevealed;
    }

    public double getWinRate() {
        return gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0;
    }

    public double getQuestionAccuracy() {
        return totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0;
    }

    public double getFlagAccuracy() {
        int totalFlags = totalCorrectFlags + totalWrongFlags;
        return totalFlags > 0 ? (double) totalCorrectFlags / totalFlags * 100 : 0;
    }
}
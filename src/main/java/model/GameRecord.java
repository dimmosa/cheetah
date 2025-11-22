package model;

import java.util.ArrayList;
import java.util.List;

public class GameRecord {
    private String date;
    private String difficulty;
    private String players;
    private int totalScore;
    private String duration;
    private int totalQuestions;
    private int opened;
    private int notOpened;
    private int correctOverall;
    private int wrongOverall;
    private int surprises;
    private boolean livesLost;
    private int livesRemaining;
    private boolean multiplayer;

    private List<PlayerStats> playerStats = new ArrayList<>();

    public List<PlayerStats> getPlayerStats() { return playerStats; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getOpened() {
        return opened;
    }

    public void setOpened(int opened) {
        this.opened = opened;
    }

    public int getNotOpened() {
        return notOpened;
    }

    public void setNotOpened(int notOpened) {
        this.notOpened = notOpened;
    }

    public int getCorrectOverall() {
        return correctOverall;
    }

    public void setCorrectOverall(int correctOverall) {
        this.correctOverall = correctOverall;
    }

    public int getWrongOverall() {
        return wrongOverall;
    }

    public void setWrongOverall(int wrongOverall) {
        this.wrongOverall = wrongOverall;
    }

    public int getSurprises() {
        return surprises;
    }

    public void setSurprises(int surprises) {
        this.surprises = surprises;
    }

    public boolean isLivesLost() {
        return livesLost;
    }

    public void setLivesLost(boolean livesLost) {
        this.livesLost = livesLost;
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    public void setLivesRemaining(int livesRemaining) {
        this.livesRemaining = livesRemaining;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    public void setMultiplayer(boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    public void setPlayerStats(List<PlayerStats> playerStats) {
        this.playerStats = playerStats;
    }
}

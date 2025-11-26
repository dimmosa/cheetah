package controller;

import model.SysData;
import model.User;

public class GameSetupController {
    
    private SysData sysData;
    private String selectedDifficulty;
    private User player1;
    private User player2;
    
    private int gridSize;
    private int totalLives;
    private int mineCount;

    public GameSetupController() {
        this.sysData = SysData.getInstance();
        this.selectedDifficulty = "Easy";
    }


    public GameSetupController(SysData sysData) {
        this.sysData = sysData;
        this.selectedDifficulty = "Medium";
    }
    

    public boolean validatePlayerNames(String name1, String name2) {
        if (name1 == null || name1.trim().isEmpty() || name1.equals("Enter name")) {
            return false;
        }
        if (name2 == null || name2.trim().isEmpty() || name2.equals("Enter name")) {
            return false;
        }
        if (name1.trim().equalsIgnoreCase(name2.trim())) {
            return false;
        }
        return true;
    }
    

    public void setDifficulty(String difficulty) {
        this.selectedDifficulty = difficulty;
        
        switch (difficulty) {
            case "Easy":
                gridSize = 9;
                totalLives = 10;
                mineCount = calculateMineCount(9);
                break;
            case "Medium":
                gridSize = 13;
                totalLives = 8;
                mineCount = calculateMineCount(13);
                break;
            case "Hard":
                gridSize = 16;
                totalLives = 6;
                mineCount = calculateMineCount(16);
                break;
            default:
                gridSize = 13;
                totalLives = 8;
                mineCount = calculateMineCount(13);
        }
    }
    

    private int calculateMineCount(int size) {
        int totalCells = size * size;
        return (int) Math.ceil(totalCells * 0.15);
    }
    

    public void createPlayers(String name1, String avatar1, String name2, String avatar2) {
        this.player1 = new User(name1,"", avatar1);
        this.player2 = new User(name2, "",avatar2);

    }
    

    public GameConfig initializeGame() {
        if (player1 == null || player2 == null) {
            throw new IllegalStateException("Players must be created before initializing game");
        }
        
        return new GameConfig(
            player1,
            player2,
            selectedDifficulty,
            gridSize,
            totalLives,
            mineCount,
            sysData
        );
    }
    
    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }
    
    public int getGridSize() {
        return gridSize;
    }
    
    public int getTotalLives() {
        return totalLives;
    }
    
    public int getMineCount() {
        return mineCount;
    }
    
    public User getPlayer1() {
        return player1;
    }
    
    public User getPlayer2() {
        return player2;
    }
    

    public int getGoodSurprisePoints() {
        switch (selectedDifficulty) {
            case "Easy": return 8;
            case "Medium": return 12;
            case "Hard": return 16;
            default: return 12;
        }
    }
    

    public int getBadSurprisePoints() {
        return -getGoodSurprisePoints();
    }
    

    public static class GameConfig {
        public final User player1;
        public final User player2;
        public final String difficulty;
        public final int gridSize;
        public final int totalLives;
        public final int mineCount;
        public final SysData sysData;
        
        public GameConfig(User player1, User player2, String difficulty, 
                         int gridSize, int totalLives, int mineCount, SysData sysData) {
            this.player1 = player1;
            this.player2 = player2;
            this.difficulty = difficulty;
            this.gridSize = gridSize;
            this.totalLives = totalLives;
            this.mineCount = mineCount;
            this.sysData = sysData;
        }
    }
}
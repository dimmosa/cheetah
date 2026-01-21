package control;

import model.User;
import model.SysData;
import model.GameHistoryEntry;
import model.CellType;
import model.FlagResult;

import model.SessionManager;
import model.UserService;
import view.MinesweeperBoardPanel;
import view.GameEndedDialog;

import javax.swing.*;
import java.util.Random;
import java.util.function.Consumer;

public class SinglePlayerGameControl {

    private User currentUser;
    private int rows;
    private int cols;
    private int totalMines;
    private int remainingMines;
    private int lives;
    private int points;
    private String difficulty;
    private boolean gameOver;
    private boolean gameWon;

    private int questionCells;
    private int surpriseCells;
    private int activationCost;
    private int goodSurprisePoints;
    private int badSurprisePoints;

    private boolean instantFlagFeedback = true;

    private Random random;
    private SysData sysData;

    private Timer timer;
    private int elapsedSeconds = 0;

    public SinglePlayerGameControl(User user, String difficulty, SysData sysData) {
        this.currentUser = user;
        this.difficulty = difficulty;
        this.sysData = sysData;
        this.random = new Random();
        this.gameOver = false;
        this.points = 0;

        initializeDifficultySettings();
    }

    private void initializeDifficultySettings() {
        switch (difficulty) {
            case "Easy":
                rows = 9;
                cols = 9;
                totalMines = 10;
                lives = 10;
                questionCells = 6;
                surpriseCells = 2;
                activationCost = 8;
                goodSurprisePoints = 8;
                badSurprisePoints = 8;
                break;

            case "Medium":
                rows = 13;
                cols = 13;
                totalMines = 26;
                lives = 8;
                questionCells = 7;
                surpriseCells = 3;
                activationCost = 8;
                goodSurprisePoints = 12;
                badSurprisePoints = 12;
                break;

            case "Hard":
                rows = 16;
                cols = 16;
                totalMines = 44;
                lives = 6;
                questionCells = 11;
                surpriseCells = 4;
                activationCost = 12;
                goodSurprisePoints = 16;
                badSurprisePoints = 16;
                break;

            default:
                rows = 16;
                cols = 16;
                totalMines = 44;
                lives = 6;
                questionCells = 11;
                surpriseCells = 4;
                activationCost = 12;
                goodSurprisePoints = 16;
                badSurprisePoints = 16;
        }
        remainingMines = totalMines;
    }

    public MinesweeperBoardPanel createBoardPanel() {
        return new MinesweeperBoardPanel(rows, cols, this, new QuestionController());
    }


    public boolean revealCell(int row, int col, CellType cellType) {
        if (gameOver) return false;

        switch (cellType) {
            case MINE:
                decrementLife();
                remainingMines--;
                if (remainingMines == 0) {
                    endGame(true);
                }
                return true;

            case NUMBER:
            case EMPTY:
                addPoints(1);
                return true;

            case QUESTION:
            case SURPRISE:
                addPoints(1);
                return true;

            default:
                return false;
        }
    }


    public FlagResult placeFlag(int row, int col, CellType actualCellType) {
        if (gameOver) return FlagResult.INVALID;

        if (actualCellType == CellType.MINE) {
            addPoints(1);
            remainingMines--;

            if (remainingMines == 0) {
                endGame(true);
            }

            return FlagResult.CORRECT_MINE;

        } else if (actualCellType == CellType.NUMBER || actualCellType == CellType.EMPTY) {
            addPoints(-3);
            return FlagResult.INCORRECT;

        } else {
            addPoints(-3);
            return FlagResult.INCORRECT;
        }
    }


    public void removeFlag(int row, int col, CellType actualCellType) {
        if (actualCellType == CellType.MINE) {
            remainingMines++;
        }
    }


    public boolean activateSurpriseCell() {
        if (gameOver) return false;

        if (points >= activationCost) {
            addPoints(-activationCost);

            boolean goodSurprise = random.nextBoolean();

            if (goodSurprise) {
                incrementLife();
                addPoints(goodSurprisePoints);
                return true;
            } else {
                decrementLife();
                addPoints(-badSurprisePoints);
                return false;
            }
        }
        return false;
    }

    public boolean activateQuestionCell() {
        if (gameOver) return false;

        if (points >= activationCost) {
            addPoints(-activationCost);

            return true;
        }
        return false;
    }

    public void handleQuestionAnswer(String questionDifficulty, boolean correct) {
        if (gameOver) return;

        switch (difficulty) {
            case "Easy":
                handleEasyModeQuestion(questionDifficulty, correct);
                break;
            case "Medium":
                handleMediumModeQuestion(questionDifficulty, correct);
                break;
            case "Hard":
                handleHardModeQuestion(questionDifficulty, correct);
                break;
        }
    }

    private void handleEasyModeQuestion(String qDiff, boolean correct) {
        switch (qDiff) {
            case "Easy":
                if (correct) {
                    addPoints(3);
                    incrementLife();
                } else {
                    if (random.nextBoolean()) addPoints(-3);
                }
                break;
            case "Medium":
                if (correct) {
                    addPoints(6);
                } else {
                    if (random.nextBoolean()) addPoints(-6);
                }
                break;
            case "Hard":
                if (correct) {
                    addPoints(10);
                } else {
                    addPoints(-10);
                }
                break;
            case "Expert":
                if (correct) {
                    addPoints(15);
                    addLives(2);
                } else {
                    addPoints(-15);
                    decrementLife();
                }
                break;
        }
    }

    private void handleMediumModeQuestion(String qDiff, boolean correct) {
        switch (qDiff) {
            case "Easy":
                if (correct) {
                    addPoints(8);
                    incrementLife();
                } else {
                    addPoints(-8);
                }
                break;
            case "Medium":
                if (correct) {
                    addPoints(10);
                    incrementLife();
                } else {
                    if (random.nextBoolean()) {
                        addPoints(-10);
                        decrementLife();
                    }
                }
                break;
            case "Hard":
                if (correct) {
                    addPoints(20);
                    addLives(2);
                } else {
                    addPoints(-20);
                    decrementLife();
                }
                break;
            case "Expert":
                if (correct) {
                    addPoints(20);
                    addLives(3);
                } else {
                    addPoints(-20);
                    addLives(-2);
                }
                break;
        }
    }

    private void handleHardModeQuestion(String qDiff, boolean correct) {
        switch (qDiff) {
            case "Easy":
                if (correct) {
                    addPoints(10);
                    incrementLife();
                } else {
                    addPoints(-10);
                    decrementLife();
                }
                break;
            case "Medium":
                if (correct) {
                    addPoints(15);
                    incrementLife();
                } else {
                    if (random.nextBoolean()) {
                        addPoints(-15);
                        decrementLife();
                    }
                }
                break;
            case "Hard":
                if (correct) {
                    addPoints(15);
                    addLives(2);
                } else {
                    if (random.nextBoolean()) {
                        addPoints(-15);
                        addLives(-2);
                    }
                }
                break;
            case "Expert":
                if (correct) {
                    addPoints(40);
                    addLives(3);
                } else {
                    addPoints(-40);
                    addLives(-3);
                }
                break;
        }
    }


    public void decrementLife() {
        if (lives > 0) {
            lives--;
            if (lives == 0) {
                endGame(false);
            }
        }
    }

    public void decrementRemainingMines() {
        remainingMines--;
    }

    public void incrementLife() {
        if (lives < 10) {
            lives++;
        } else {
            addPoints(activationCost);
        }
    }

    private void addLives(int amount) {
        for (int i = 0; i < Math.abs(amount); i++) {
            if (amount > 0) {
                incrementLife();
            } else {
                decrementLife();
            }
        }
    }


    public void addPoints(int amount) {
        points += amount;
    }


    public void endGame(boolean allMinesRevealed) {
        if (gameOver) return;

        gameOver = true;

        if (timer != null) {
            timer.stop();
        }

        int lifeBonus = lives * activationCost;
        addPoints(lifeBonus);

        boolean won = allMinesRevealed || remainingMines == 0 || lives > 0;

        if (sysData != null) {
            GameHistoryEntry history = new GameHistoryEntry(
                    SessionManager.getInstance().getCurrentUser().getUsername(),
                    "Practice Mode",
                    difficulty,
                    points,
                    elapsedSeconds,
                    won
            );
            System.out.println(history);
            sysData.addGameHistory(history);
        }
    }

    public boolean isGameWon() {
        return remainingMines == 0 || lives > 0;
    }

    private boolean isFlagMode = false;

    public boolean isFlagMode() {
        return isFlagMode;
    }

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    public void toggleFlagMode() {
        this.isFlagMode = !this.isFlagMode;
    }

    public void startTimer(Consumer<String> onTick) {
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            String timeStr = String.format("%02d:%02d", minutes, seconds);
            onTick.accept(timeStr);
        });
        timer.start();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void resetTimer() {
        elapsedSeconds = 0;
    }

    public int getLives() { return lives; }
    public int getPoints() { return points; }
    public int getRemainingMines() { return remainingMines; }
    public boolean isGameOver() { return gameOver; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalMines() { return totalMines; }
    public int getQuestionCells() { return questionCells; }
    public int getSurpriseCells() { return surpriseCells; }
    public int getActivationCost() { return activationCost; }
    public User getCurrentUser() { return currentUser; }
    public boolean hasInstantFlagFeedback() { return instantFlagFeedback; }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getLivesForDifficulty(String difficulty) {
        switch (difficulty) {
            case "Easy": return 10;
            case "Medium": return 8;
            case "Hard": return 6;
            default: return 6;
        }
    }
}
package control;

import model.GameHistoryEntry;
import model.SysData;
import model.User;
import view.MinesweeperBoardPanel;

import javax.swing.*;
import java.util.Random;
import java.util.function.Consumer;


public class SinglePlayerGameControl {

    
    private static final int TOTAL_LIVES_CAP = 10;

    private static final int REVEAL_POINTS = 1;
    private static final int FLAG_MINE_POINTS = 1;
    private static final int WRONG_FLAG_PENALTY = -3;

   
    private final User currentUser;
    private final SysData sysData;
    private final Random random = new Random();

    private String difficulty;

    private int rows;
    private int cols;

    private int totalMines;
    private int remainingMines;

    private int lives;
    private int maxLives;
    private int points;

    private boolean gameOver;
    private boolean gameWon;

    private boolean flagMode;

    // Special cells config (if your board uses them)
    private int questionCells;
    private int surpriseCells;
    private int activationCost;

    // Timer
    private Timer timer;
    private int elapsedSeconds = 0;

    // =======================
    // ctor
    // =======================
    public SinglePlayerGameControl(User currentUser, String difficulty, SysData sysData) {
        this.currentUser = currentUser;
        this.sysData = sysData;
        initDifficulty(difficulty);
    }

    // =======================
    // Difficulty setup
    // =======================
    private void initDifficulty(String difficulty) {
        this.difficulty = difficulty;

        // Match your MultiPlayer DifficultyFactory idea
        // Adjust if your exact values differ.
        switch (difficulty) {
            case "Easy" -> {
                rows = 9; cols = 9;
                maxLives = 10;
                activationCost = 3;
                questionCells = 6;
                surpriseCells = 2;
                totalMines = 10;
            }
            case "Medium" -> {
                rows = 13; cols = 13;
                maxLives = 8;
                activationCost = 5;
                questionCells = 7;
                surpriseCells = 3;
                totalMines = 26;
            }
            case "Hard" -> {
                rows = 16; cols = 16;
                maxLives = 6;
                activationCost = 7;
                questionCells = 11;
                surpriseCells = 4;
                totalMines = 44;
            }
            default -> {
                rows = 13; cols = 13;
                maxLives = 8;
                activationCost = 5;
                questionCells = 7;
                surpriseCells = 3;
                totalMines = 26;
            }
        }

        lives = maxLives;
        points = 0;
        remainingMines = totalMines;

        gameOver = false;
        gameWon = false;
        flagMode = false;

        resetTimer();
    }

    // If your board generates mines dynamically and you want controller to reflect it:
    public void onBoardGenerated(int totalMinesFromBoard) {
        this.totalMines = totalMinesFromBoard;
        this.remainingMines = totalMinesFromBoard;
    }

    // =======================
    // Getters used by UI
    // =======================
    public User getCurrentUser() { return currentUser; }

    public String getDifficulty() { return difficulty; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getTotalMines() { return totalMines; }
    public int getRemainingMines() { return remainingMines; }

    public int getLives() { return lives; }
    public int getMaxLives() { return maxLives; }

    public int getPoints() { return points; }

    public int getActivationCost() { return activationCost; }

    public int getQuestionCells() { return questionCells; }
    public int getSurpriseCells() { return surpriseCells; }

    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }

    public boolean isFlagMode() { return flagMode; }

    public int getLivesForDifficulty(String diff) {
        return switch (diff) {
            case "Easy" -> 10;
            case "Medium" -> 8;
            case "Hard" -> 6;
            default -> 8;
        };
    }

    // =======================
    // Flag mode
    // =======================
    public void toggleFlagMode() {
        flagMode = !flagMode;
    }

    public void setFlagMode(boolean on) {
        flagMode = on;
    }

    // =======================
    // Board actions (match multi)
    // =======================

    /** Reveal NUMBER or EMPTY cell -> +1 point (single player does NOT "end turn") */
    public void onRevealNumberOrEmpty() {
        if (gameOver) return;
        points += REVEAL_POINTS;
    }

    /** Mine revealed (clicked) -> -1 life */
    public void onMineHit() {
        if (gameOver) return;
        lives--;
        if (lives < 0) lives = 0;
        checkGameOver();
    }

    /**
     * Place a flag on a cell.
     * correct=true if the actual cell is a mine.
     */
    public void onFlagPlaced(boolean correct) {
        if (gameOver) return;

        if (correct) {
            points += FLAG_MINE_POINTS;
            // optional: if you treat flagged mine as "handled", decrease remaining mines
            if (remainingMines > 0) remainingMines--;
        } else {
            points += WRONG_FLAG_PENALTY;
        }
    }

    /**
     * Remove a flag.
     * If you decreased remainingMines when placing a correct flag, restore it here.
     */
    public void onFlagRemoved(boolean wasCorrectMine) {
        if (gameOver) return;
        if (wasCorrectMine) {
            remainingMines++;
            if (remainingMines > totalMines) remainingMines = totalMines;
        }
    }

    public CellActionResult activateSurpriseSingle() {
        if (gameOver) return new CellActionResult(false, 0, 0, "Game is over.");
        if (points < activationCost) return new CellActionResult(false, 0, 0, "Not enough points!");

        int pointsBefore = points;
        int livesBefore = lives;

        points -= activationCost;

        boolean isGood = random.nextBoolean();
        int p = getSurprisePoints();

        if (isGood) {
            points += p;
            lives += 1;

            if (lives > TOTAL_LIVES_CAP) {
                int excess = lives - TOTAL_LIVES_CAP;
                int lifePoints = getLifeConversionValue();
                points += excess * lifePoints;
                lives = TOTAL_LIVES_CAP;
            }
        } else {
            points -= p;
            lives -= 1;
            if (lives < 0) lives = 0;
        }

        checkGameOver();

        int pointsDelta = points - pointsBefore;
        int livesDelta = lives - livesBefore;

        String message = String.format(
                "Surprise %s! %+d points, %+d lives.",
                isGood ? "good" : "bad",
                pointsDelta,
                livesDelta
        );

        return new CellActionResult(false, pointsDelta, livesDelta, message);
    }

    private int getSurprisePoints() {
        return switch (difficulty) {
            case "Easy" -> 8;
            case "Medium" -> 12;
            case "Hard" -> 16;
            default -> 12;
        };
    }

    private int getLifeConversionValue() {
        return switch (difficulty) {
            case "Easy" -> 5;
            case "Medium" -> 8;
            case "Hard" -> 12;
            default -> 8;
        };
    }


    public CellActionResult activateQuestionSingle(int questionDifficulty, boolean answeredCorrectly) {
        if (gameOver) return new CellActionResult(false, 0, 0, "Game is over.");
        if (points < activationCost) return new CellActionResult(false, 0, 0, "Not enough points!");

        int pointsBefore = points;
        int livesBefore = lives;

        // pay cost first
        points -= activationCost;

        QuestionResult qr = calculateQuestionResult(questionDifficulty, answeredCorrectly);
        points += qr.points;
        lives += qr.lives;

        // convert extra lives above TOTAL cap to points
        if (lives > TOTAL_LIVES_CAP) {
            int excess = lives - TOTAL_LIVES_CAP;
            int lifePoints = getLifeConversionValue();
            points += excess * lifePoints;
            lives = TOTAL_LIVES_CAP;
        }

        if (lives < 0) lives = 0;

        checkGameOver();

        int pointsDelta = points - pointsBefore;
        int livesDelta = lives - livesBefore;

        String message = String.format(
                "Question %s! %+d points, %+d lives.",
                answeredCorrectly ? "correct" : "wrong",
                pointsDelta,
                livesDelta
        );

        return new CellActionResult(false, pointsDelta, livesDelta, message);
    }

    private QuestionResult calculateQuestionResult(int questionDifficulty, boolean correct) {
        int p = 0;
        int l = 0;
        boolean random50 = random.nextBoolean();

        switch (difficulty) {
            case "Easy" -> {
                if (correct) {
                    switch (questionDifficulty) {
                        case 1 -> { p = 3;  l = 1; }
                        case 2 -> { p = 6;  l = 0; } // bonuses handled in VIEW if you have them
                        case 3 -> { p = 10; l = 0; } // 3x3 reveal handled in VIEW
                        case 4 -> { p = 15; l = 2; }
                        default -> { p = 0; l = 0; }
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1 -> { p = random50 ? -3 : 0; l = 0; }
                        case 2 -> { p = random50 ? -6 : 0; l = 0; }
                        case 3 -> { p = -10; l = 0; }
                        case 4 -> { p = -15; l = -1; }
                        default -> { p = 0; l = 0; }
                    }
                }
            }
            case "Medium" -> {
                if (correct) {
                    switch (questionDifficulty) {
                        case 1 -> { p = 8;  l = 1; }
                        case 2 -> { p = 10; l = 1; }
                        case 3 -> { p = 15; l = 1; }
                        case 4 -> { p = 20; l = 2; }
                        default -> { p = 0; l = 0; }
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1 -> { p = -8;  l = 0; }
                        case 2 -> { if (random50) { p = -10; l = -1; } else { p = 0; l = 0; } }
                        case 3 -> { p = -15; l = -1; }
                        case 4 -> { p = -20; l = random50 ? -1 : -2; }
                        default -> { p = 0; l = 0; }
                    }
                }
            }
            case "Hard" -> {
                if (correct) {
                    switch (questionDifficulty) {
                        case 1 -> { p = 10; l = 1; }
                        case 2 -> { p = 15; l = random50 ? 1 : 2; }
                        case 3 -> { p = 20; l = 2; }
                        case 4 -> { p = 40; l = 3; }
                        default -> { p = 0; l = 0; }
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1 -> { p = -10; l = -1; }
                        case 2 -> { p = -15; l = random50 ? -1 : -2; }
                        case 3 -> { p = -20; l = -2; }
                        case 4 -> { p = -40; l = -3; }
                        default -> { p = 0; l = 0; }
                    }
                }
            }
            default -> {
                // fallback behaves like Medium
                if (correct) { p = 10; l = 1; }
                else { p = -10; l = -1; }
            }
        }

        return new QuestionResult(p, l);
    }

    // Helpers: use same "bonus trigger" rules as multi (if you want)
    public boolean shouldRevealMineBonus(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 2 && correct;
    }

    public boolean shouldTrigger3x3Reveal(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 3 && correct;
    }

    // =======================
    // Timer
    // =======================
    public void startTimer(Consumer<String> onTick) {
        stopTimer();
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            onTick.accept(String.format("%02d:%02d", minutes, seconds));
        });
        timer.start();
    }

    public void stopTimer() {
        if (timer != null) timer.stop();
    }

    public void resetTimer() {
        elapsedSeconds = 0;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    // =======================
    // End game + history
    // =======================
    public void endGame(boolean won) {
        if (gameOver) return;

        gameOver = true;
        gameWon = won;
        stopTimer();


        if (sysData != null) {
            GameHistoryEntry entry = new GameHistoryEntry(
                    currentUser.getUsername(), // p1
                    "SINGLE",                  // p2 (or "" if you prefer)
                    difficulty,
                    points,
                    elapsedSeconds,
                    won
            );
            sysData.addGameHistory(entry);
        }

    }

    private void checkGameOver() {
        if (lives <= 0 && !gameOver) {
            lives = 0;
            endGame(false);
        }
    }

    public MinesweeperBoardPanel createBoardPanel() {
        // If your MinesweeperBoardPanel constructor is different, edit here.
    	return new MinesweeperBoardPanel(rows, cols, this, new QuestionController());

    }

    public static class CellActionResult {
        public final boolean turnEnded; // kept for compatibility with your old UI flow
        public final int pointsChanged;
        public final int livesChanged;
        public final String message;

        public CellActionResult(boolean turnEnded, int pointsChanged, int livesChanged, String message) {
            this.turnEnded = turnEnded;
            this.pointsChanged = pointsChanged;
            this.livesChanged = livesChanged;
            this.message = message;
        }
    }

    private static class QuestionResult {
        public final int points;
        public final int lives;

        private QuestionResult(int points, int lives) {
            this.points = points;
            this.lives = lives;
        }
    }
}

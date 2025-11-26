package controller;

import model.DetailedGameHistoryEntry;
import model.SysData;
import model.User;

import javax.swing.*;
import java.util.Random;
import java.util.function.Consumer;

public class MultiPlayerGameController {

    private SysData sysData;
    private User player1;
    private User player2;
    private String difficulty;
    private int gridSize;

    private int sharedScore;
    private int sharedLives;
    private int maxLives;

    private int currentPlayer;
    private boolean gameOver;
    private boolean gameWon;

    private static final int REVEAL_POINTS = 1;
    private static final int FLAG_MINE_POINTS = 1;
    private static final int WRONG_FLAG_PENALTY = -3;

    private int rows;
    private int cols;

    private Random random;

    private Timer timer;
    private int elapsedSeconds = 0;

    private boolean player1BoardComplete = false;
    private boolean player2BoardComplete = false;

    private int activationCost;

    private DetailedGameHistoryEntry detailedHistory;

    public MultiPlayerGameController(SysData sysData, User player1, User player2,
                                     String difficulty, int gridSize) {
        this.sysData = sysData;
        this.player1 = player1;
        this.player2 = player2;
        this.difficulty = difficulty;
        this.gridSize = gridSize;
        this.random = new Random();

        initializeSharedResources();

        // תור התחלתי רנדומלי
        this.currentPlayer = random.nextBoolean() ? 1 : 2;
        this.gameOver = false;
        this.gameWon = false;

        this.detailedHistory = new DetailedGameHistoryEntry(
                player1.getUsername(),
                player2.getUsername(),
                difficulty,
                0,
                0,
                false
        );
    }

    private void initializeSharedResources() {
        switch (difficulty) {
            case "Easy":
                maxLives = 10;
                rows = 9;
                cols = 9;
                activationCost = 8;
                break;
            case "Medium":
                maxLives = 8;
                rows = 13;
                cols = 13;
                activationCost = 8;
                break;
            case "Hard":
                maxLives = 6;
                rows = 16;
                cols = 16;
                activationCost = 12;
                break;
            default:
                maxLives = 8;
                rows = 16;
                cols = 16;
                activationCost = 8;
        }
        sharedLives = maxLives;
        sharedScore = 0;
    }

    // ======== פעולות על תאים ========

    public CellActionResult revealMine() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1MineRevealed();
        } else {
            detailedHistory.incrementPlayer2MineRevealed();
        }
        sharedLives--;
        checkGameOver();
        endTurn();
        return new CellActionResult(true, 0, -1, "Mine revealed! Lost 1 life. Turn ends.");
    }

    public CellActionResult flagMineCorrectly() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Flag(true);
        } else {
            detailedHistory.incrementPlayer2Flag(true);
        }
        sharedScore += FLAG_MINE_POINTS;
        return new CellActionResult(false, FLAG_MINE_POINTS, 0,
                "Correct flag! +1 point. Continue your turn.");
    }

    public CellActionResult flagIncorrectly() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Flag(false);
        } else {
            detailedHistory.incrementPlayer2Flag(false);
        }
        sharedScore += WRONG_FLAG_PENALTY;
        return new CellActionResult(false, WRONG_FLAG_PENALTY, 0,
                "Wrong flag! -3 points. Continue your turn.");
    }

    public CellActionResult revealNumberCell() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1CellRevealed();
        } else {
            detailedHistory.incrementPlayer2CellRevealed();
        }
        sharedScore += REVEAL_POINTS;
        endTurn();
        return new CellActionResult(true, REVEAL_POINTS, 0,
                "Number cell revealed! +1 point. Turn ends.");
    }

    public CellActionResult revealEmptyCell() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1CellRevealed();
        } else {
            detailedHistory.incrementPlayer2CellRevealed();
        }
        sharedScore += REVEAL_POINTS;
        endTurn();
        return new CellActionResult(true, REVEAL_POINTS, 0,
                "Empty cell revealed! +1 point. Cascading... Turn ends.");
    }

    // ======== הפתעות ========

    public CellActionResult activateSurprise() {

        boolean isGood = random.nextBoolean();
        int surprisePoints = getSurprisePoints();
        int pointsChange = -activationCost;   // עלות הפעלה
        int livesChange = 0;
        String message;

        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Surprise(isGood);
        } else {
            detailedHistory.incrementPlayer2Surprise(isGood);
        }

        if (isGood) {
            // הפתעה טובה
            pointsChange += surprisePoints;

            if (sharedLives < maxLives) {
                sharedLives++;
                livesChange = 1;
            }

            message = String.format("Good surprise! %+d points, %+d lives. Your turn ends.",
                    pointsChange, livesChange);
        } else {
            // הפתעה רעה
            pointsChange -= surprisePoints;
            sharedLives--;
            livesChange = -1;
            checkGameOver();

            message = String.format("Bad surprise! %+d points, %+d lives. Your turn ends.",
                    pointsChange, livesChange);
        }

        sharedScore += pointsChange;

        endTurn();

        return new CellActionResult(true, pointsChange, livesChange, message);
    }

    // ======== שאלות ========

    public CellActionResult activateQuestion(int questionDifficulty, boolean answeredCorrectly) {

        QuestionResult result = calculateQuestionResult(questionDifficulty, answeredCorrectly);

        // היסטוריה
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Questions(answeredCorrectly);
        } else {
            detailedHistory.incrementPlayer2Questions(answeredCorrectly);
        }

        int pointsChange = result.points;
        int livesChange = result.lives;

        sharedScore += pointsChange;
        sharedLives += livesChange;

        // אם עברנו את מקסימום החיים – העודף נהפך לנקודות
        if (sharedLives > maxLives) {
            int excess = sharedLives - maxLives;
            sharedLives = maxLives;

            int extraPoints = excess * getSurprisePoints();
            sharedScore += extraPoints;
            pointsChange += extraPoints;
            livesChange -= excess; // בפועל קיבל פחות חיים
        }

        checkGameOver();

        String message = String.format(
                "Question %s! %+d points, %+d lives. Your turn ends.",
                answeredCorrectly ? "correct" : "wrong",
                pointsChange,
                livesChange
        );

        endTurn();

        return new CellActionResult(true, pointsChange, livesChange, message);
    }

    /**
     * חישוב נקודות/חיים לשאלה לפי רמת משחק ורמת שאלה
     * q = 1..4 (קלה / בינונית / קשה / מומחה)
     */
    private QuestionResult calculateQuestionResult(int q, boolean correct) {
        int pts = 0;
        int lives = 0;
        boolean coin = random.nextBoolean(); // ל-OR

        switch (difficulty) {

            case "Easy":
                if (correct) {
                    switch (q) {
                        case 1: pts = 3;  lives = 1; break;  // שאלה קלה
                        case 2: pts = 6;  lives = 0; break;  // בינונית
                        case 3: pts = 10; lives = 0; break;  // קשה
                        case 4: pts = 15; lives = 2; break;  // מומחה
                    }
                } else {
                    switch (q) {
                        case 1: pts = coin ? -3 : 0;  lives = 0;  break;
                        case 2: pts = coin ? -6 : 0;  lives = 0;  break;
                        case 3: pts = -10;            lives = 0;  break;
                        case 4: pts = -15;            lives = -1; break;
                    }
                }
                break;

            case "Medium":
                if (correct) {
                    switch (q) {
                        case 1: pts = 8;  lives = 1; break;
                        case 2: pts = 10; lives = 1; break;
                        case 3: pts = 20; lives = 2; break;
                        case 4: pts = 20; lives = 3; break;
                    }
                } else {
                    switch (q) {
                        case 1: pts = -8;  lives = 0;  break;
                        case 2: pts = coin ? -10 : 0;
                                lives = coin ? -1 : 0; break;
                        case 3: pts = -20; lives = -1; break;
                        case 4: pts = -20; lives = -2; break;
                    }
                }
                break;

            case "Hard":
                if (correct) {
                    switch (q) {
                        case 1: pts = 10; lives = 1; break;
                        case 2: pts = 15; lives = 1; break;
                        case 3: pts = 15; lives = 2; break;
                        case 4: pts = 40; lives = 3; break;
                    }
                } else {
                    switch (q) {
                        case 1: pts = -10; lives = -1; break;
                        case 2: pts = coin ? -15 : 0;
                                lives = coin ? -1 : 0; break;
                        case 3: pts = coin ? -15 : 0;
                                lives = coin ? -2 : 0; break;
                        case 4: pts = -40; lives = -3; break;
                    }
                }
                break;
        }

        return new QuestionResult(pts, lives);
    }

    private int getSurprisePoints() {
        switch (difficulty) {
            case "Easy":   return 8;
            case "Medium": return 12;
            case "Hard":   return 16;
            default:       return 12;
        }
    }

    // ======== תורות / סוף משחק ========

    public void endTurn() {
        if (!gameOver) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
    }

    public void switchTurn() {
        endTurn();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public User getCurrentPlayerUser() {
        return currentPlayer == 1 ? player1 : player2;
    }

    private void checkGameOver() {
        if (sharedLives <= 0) {
            gameOver = true;
            gameWon = false;
            stopTimer();
            saveGameHistory();
        }
    }

    /** ממיר חיים שנשארו לנקודות – רק אם ניצחנו. */
    private void applyLivesBonusIfNeeded() {
        if (gameWon && sharedLives > 0) {
            int bonus = sharedLives * getSurprisePoints();
            sharedScore += bonus;
            // אם תרצי לאפס חיים בסוף:
            // sharedLives = 0;
        }
    }

    private void saveGameHistory() {
        if (sysData != null) {
            applyLivesBonusIfNeeded();

            detailedHistory.setPlayer1(this.player1.getUsername());
            detailedHistory.setPlayer2(this.player2.getUsername());
            detailedHistory.setFinalScore(sharedScore);
            detailedHistory.setDurationSeconds(elapsedSeconds);
            detailedHistory.setWon(gameWon);

            sysData.addDetailedGameHistory(detailedHistory);
        }
    }

    public void setPlayerBoardComplete(int playerNum, boolean complete) {
        if (playerNum == 1) {
            player1BoardComplete = complete;
        } else {
            player2BoardComplete = complete;
        }
        checkVictory(player1BoardComplete, player2BoardComplete);
    }

    public void handleAllMinesRevealed(int playerNum) {
        gameOver = true;
        gameWon = false;
        stopTimer();
        saveGameHistory();
    }

    public void checkVictory(boolean player1Complete, boolean player2Complete) {
        if (player1Complete && player2Complete) {
            gameOver = true;
            gameWon = true;
            stopTimer();
            saveGameHistory();
        }
    }

    // ======== טיימר ========

    public void startTimer(Consumer<String> onTick) {
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            String timeStr = String.format("%02d:%02d", minutes, seconds);
            onTick.accept(timeStr);
        });
        timer.start();

        if (isGameOver()) {
            stopTimer();
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void resetTimer() {
        elapsedSeconds = 0;
    }

    public void giveUp() {
        gameOver = true;
        gameWon = false;
        stopTimer();
        saveGameHistory();
    }

    // ======== getters ========

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public int getSharedScore() {
        return sharedScore;
    }

    public int getSharedLives() {
        return sharedLives;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public User getPlayer1() {
        return player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getActivationCost() {
        return activationCost;
    }

    public DetailedGameHistoryEntry getDetailedHistory() {
        return detailedHistory;
    }

    // ======== עזר פנימי ========

    public static class CellActionResult {
        public final boolean turnEnded;
        public final int pointsChanged;
        public final int livesChanged;
        public final String message;

        public CellActionResult(boolean turnEnded, int pointsChanged,
                                int livesChanged, String message) {
            this.turnEnded = turnEnded;
            this.pointsChanged = pointsChanged;
            this.livesChanged = livesChanged;
            this.message = message;
        }
    }

    private static class QuestionResult {
        public final int points;
        public final int lives;

        public QuestionResult(int points, int lives) {
            this.points = points;
            this.lives = lives;
        }
    }
}
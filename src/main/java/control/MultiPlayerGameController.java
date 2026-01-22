package control;

import model.DetailedGameHistoryEntry;
import model.GameHistoryEntry;
import model.SysData;
import model.User;
import java.util.ArrayList;
import java.util.List;


import javax.swing.*;
import java.util.Random;
import java.util.function.Consumer;


public class MultiPlayerGameController {
	private final List<GameObserver> observers = new ArrayList<>();


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

    // Track first move
    private boolean player1FirstMove = true;
    private boolean player2FirstMove = true;
    private boolean gameStarted = false;


    // Track total mines and flagged cells per board
    private int player1TotalMines = 0;
    private int player2TotalMines = 0;
    private int player1TotalCells = 0;
    private int player2TotalCells = 0;
    private int player1FlaggedCells = 0;
    private int player2FlaggedCells = 0;

    private int activationCost;

    private DetailedGameHistoryEntry detailedHistory;
    public void addObserver(GameObserver o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    public void removeObserver(GameObserver o) {
        observers.remove(o);
    }

    private void notifyObservers() {
        GameState s = new GameState(sharedScore, sharedLives, currentPlayer, gameOver);
        for (GameObserver o : observers) {
            o.onGameStateChanged(s);
        }
    }


    public MultiPlayerGameController(SysData sysData, User player1, User player2, String difficulty, int gridSize) {
        this.sysData = sysData;
        this.player1 = player1;
        this.player2 = player2;
        this.difficulty = difficulty;
        this.gridSize = gridSize;
        this.random = new Random();

        initializeSharedResources();

        // FIXED: Random starting player (50/50 chance)
        this.currentPlayer = random.nextBoolean() ? 1 : 2;
        System.out.println("Starting player: Player " + currentPlayer);

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
    public int getGridSize() {
        return gridSize;
    }

    public SysData getSysData() {
        return sysData;
    }

    private static final int TOTAL_LIVES = 10;

    private void initializeSharedResources() {
        DifficultyFactory.Config cfg = DifficultyFactory.create(difficulty);

        this.rows = cfg.rows();
        this.cols = cfg.cols();
        this.activationCost = cfg.activationCost();

        // מספר הלבבות הפעילים לפי רמת קושי
        this.maxLives = cfg.maxLives();   // Easy=10, Medium=8, Hard=6

        // מתחילים עם כל הלבבות הפעילים מלאים
        this.sharedLives = maxLives;

        this.sharedScore = 0;

        this.player1TotalCells = rows * cols;
        this.player2TotalCells = rows * cols;
    }

    // Track mine counts
    public void setPlayerTotalMines(int playerNum, int totalMines) {
        if (playerNum == 1) {
            player1TotalMines = totalMines;
        } else {
            player2TotalMines = totalMines;
        }
    }

    // Check if first move for player
    public boolean isPlayerFirstMove(int playerNum) {
        return playerNum == 1 ? player1FirstMove : player2FirstMove;
    }

    // Mark first move as done
    public void markFirstMoveDone(int playerNum) {
        if (playerNum == 1) {
            player1FirstMove = false;
        } else {
            player2FirstMove = false;
        }
    }

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
            player1FlaggedCells++;
        } else {
            detailedHistory.incrementPlayer2Flag(true);
            player2FlaggedCells++;
        }
        

        sharedScore += FLAG_MINE_POINTS;

        // Win/lose decision when all unrevealed cells are flagged is now handled
        // from MinesweeperBoardPanelTwoPlayer.checkBoardComplete()
        notifyObservers();

        return new CellActionResult(false, FLAG_MINE_POINTS, 0, "Correct flag! +1 point. Continue your turn.");
    }

    public CellActionResult flagIncorrectly() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Flag(false);
            player1FlaggedCells++;
        } else {
            detailedHistory.incrementPlayer2Flag(false);
            player2FlaggedCells++;
        }
        sharedScore += WRONG_FLAG_PENALTY;
        notifyObservers();


        return new CellActionResult(false, WRONG_FLAG_PENALTY, 0, "Wrong flag! -3 points. Continue your turn.");
    }

    @SuppressWarnings("unused")
    private void checkAllCellsFlagged() {
        // Deprecated: logic moved to MinesweeperBoardPanelTwoPlayer.checkBoardComplete()
    }

    public CellActionResult revealNumberCell() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1CellRevealed();
        } else {
            detailedHistory.incrementPlayer2CellRevealed();
        }
        sharedScore += REVEAL_POINTS;
        endTurn();
        notifyObservers();

        return new CellActionResult(true, REVEAL_POINTS, 0, "Number cell revealed! +1 point. Turn ends.");
    }

    public CellActionResult revealEmptyCell() {
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1CellRevealed();
        } else {
            detailedHistory.incrementPlayer2CellRevealed();
        }
        sharedScore += REVEAL_POINTS;
        endTurn();
        notifyObservers();

        return new CellActionResult(true, REVEAL_POINTS, 0, "Empty cell revealed! +1 point. Cascading... Turn ends.");
    }

    public CellActionResult activateSurprise() {
        if (sharedScore < activationCost) {
            return new CellActionResult(false, 0, 0, "Not enough points!");
        }

        int scoreBefore = sharedScore;
        int livesBefore = sharedLives;

        // Deduct activation cost
        sharedScore -= activationCost;

        boolean isGood = random.nextBoolean();
        int points = getSurprisePoints();

        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Surprise(isGood);
        } else {
            detailedHistory.incrementPlayer2Surprise(isGood);
        }

        if (isGood) {
            // base points
            sharedScore += points;

            // give life (could be >1 in future)
            sharedLives += 1;

            // 🔥 המרה אם עברנו את 10
            if (sharedLives > TOTAL_LIVES) {
                int excess = sharedLives - TOTAL_LIVES;
                int lifePoints = getLifeConversionValue();
                sharedScore += excess * lifePoints;
                sharedLives = TOTAL_LIVES;
            }

        } else {
            // bad surprise
            sharedScore -= points;
            sharedLives--;

            if (sharedLives < 0) sharedLives = 0;
            checkGameOver();
        }

        int pointsDelta = sharedScore - scoreBefore;
        int livesDelta = sharedLives - livesBefore;

        String message = String.format(
                "Surprise %s! %+d points, %+d lives. Continue your turn.",
                isGood ? "good" : "bad",
                pointsDelta,
                livesDelta
        );

        notifyObservers();
        return new CellActionResult(false, pointsDelta, livesDelta, message);
    }


    private int getSurprisePoints() {
        switch (difficulty) {
            case "Easy":
                return 8;
            case "Medium":
                return 12;
            case "Hard":
                return 16;
            default:
                return 12;
        }
    }
    
 // =========================
 // SCORE UPDATE (for HINT etc.)
 // =========================
 public void addScore(int delta) {
     sharedScore += delta;

     if (sharedScore < 0) {
         sharedScore = 0;
     }

     notifyObservers();
 }

    private int getLifeConversionValue() {
        switch (difficulty) {
            case "Easy":
                return 5;
            case "Medium":
                return 8;
            case "Hard":
                return 12;
            default:
                return 8;
        }
    }


 // Question no longer ends turn
    public CellActionResult activateQuestion(int questionDifficulty, boolean answeredCorrectly) {
        // Check affordability FIRST, before deducting
        if (sharedScore < activationCost) {
            return new CellActionResult(false, 0, 0, "Not enough points!");
        }

        int scoreBefore = sharedScore;
        int livesBefore = sharedLives;

        // Step 1: Deduct activation cost
        sharedScore -= activationCost;

        System.out.println("Question activated. Cost: -" + activationCost +
                ". Score after cost: " + sharedScore);

        // Step 2: Calculate question result (from the table)
        QuestionResult result = calculateQuestionResult(questionDifficulty, answeredCorrectly);

        // Step 3: Track statistics
        if (currentPlayer == 1) {
            detailedHistory.incrementPlayer1Questions(answeredCorrectly);
        } else {
            detailedHistory.incrementPlayer2Questions(answeredCorrectly);
        }

        // Step 4: Apply points & lives from question
        sharedScore += result.points;
        sharedLives += result.lives;

        System.out.println("Question base result: " + result.points + " points, " +
                result.lives + " lives. After base -> score=" + sharedScore +
                ", lives=" + sharedLives);

        // Step 5: If we exceeded max lives -> convert extra lives to points
        if (sharedLives > TOTAL_LIVES) {
            int excess = sharedLives - TOTAL_LIVES;      // ✅ עודף מעל 10
            int lifePoints = getLifeConversionValue();
            int bonusPoints = excess * lifePoints;

            sharedScore += bonusPoints;
            sharedLives = TOTAL_LIVES;                   // ✅ נצמד ל-10

            System.out.println("Excess lives converted: " + excess +
                    " lives → +" + bonusPoints + " points. Final score=" + sharedScore +
                    ", lives capped to TOTAL=" + TOTAL_LIVES);
        }


        // Step 6: Check game over
        checkGameOver();

        int pointsDelta = sharedScore - scoreBefore;
        int livesDelta = sharedLives - livesBefore;

        String message = String.format(
                "Question %s! %+d points, %+d lives. Continue your turn.",
                answeredCorrectly ? "correct" : "wrong",
                pointsDelta,
                livesDelta
        );
        notifyObservers();


        // Question does NOT end turn
        return new CellActionResult(false, pointsDelta, livesDelta, message);
    }


    // Exact implementation from requirements table with proper 50% random logic
    private QuestionResult calculateQuestionResult(int questionDifficulty, boolean correct) {
        int points = 0;
        int lives = 0;
        boolean random50 = random.nextBoolean(); // True = first option (50%), False = second option (50%)

        switch (difficulty) {
            case "Easy":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Correct
                            points = 3;
                            lives = 1;
                            break;

                        case 2: // Medium Question - Correct
                           
                                points = 6; // Base 6 + bonus 6 = 12 total
                                lives = 0;
                            
                            break;

                        case 3: // Hard Question - Correct
                            points = 10;
                            lives = 0;
                            // 3x3 reveal handled separately in view
                            break;

                        case 4: // Very Hard Question - Correct
                            points = 15;
                            lives = 2;
                            break;
                    }
                } else { // Wrong answer
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Wrong
                            // 50% chance: -3 points OR nothing
                            points = random50 ? -3 : 0;
                            lives = 0;
                            break;

                        case 2: // Medium Question - Wrong
                            // 50% chance: -6 points OR nothing
                            points = random50 ? -6 : 0;
                            lives = 0;
                            break;

                        case 3: // Hard Question - Wrong
                            points = -10;
                            lives = 0;
                            break;

                        case 4: // Very Hard Question - Wrong
                            points = -15;
                            lives = -1;
                            break;
                    }
                }
                break;

            case "Medium":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Correct
                            points = 8;
                            lives = 1;
                            break;

                        case 2: // Medium Question - Correct
                            points = 10;
                            lives = 1;
                            break;

                        case 3: // Hard Question - Correct
                            points = 15;
                            lives = 1;
                            break;

                        case 4: // Very Hard Question - Correct
                            points = 20;
                            lives = 2;
                            break;
                    }
                } else { // Wrong answer
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Wrong
                            points = -8;
                            lives = 0;
                            break;

                        case 2: // Medium Question - Wrong
                            // 50% chance: (-10pts & -1 life) OR nothing
                            if (random50) {
                                points = -10;
                                lives = -1;
                            } else {
                                points = 0;
                                lives = 0;
                            }
                            break;

                        case 3: // Hard Question - Wrong
                            points = -15;
                            lives = -1;
                            break;

                        case 4: // Very Hard Question - Wrong
                            // 50% chance: (-20pts & -1 life) OR (-20pts & -2 lives)
                            points = -20;
                            lives = random50 ? -1 : -2;
                            break;
                    }
                }
                break;

            case "Hard":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Correct
                            points = 10;
                            lives = 1;
                            break;

                        case 2: // Medium Question - Correct
                            // 50% chance: (+15pts & +1 life) OR (+15pts & +2 lives)
                            points = 15;
                            lives = random50 ? 1 : 2;
                            break;

                        case 3: // Hard Question - Correct
                            points = 20;
                            lives = 2;
                            break;

                        case 4: // Very Hard Question - Correct
                            points = 40;
                            lives = 3;
                            break;
                    }
                } else { // Wrong answer
                    switch (questionDifficulty) {
                        case 1: // Easy Question - Wrong
                            points = -10;
                            lives = -1;
                            break;

                        case 2: // Medium Question - Wrong
                            // 50% chance: (-15pts & -1 life) OR (-15pts & -2 lives)
                            points = -15;
                            lives = random50 ? -1 : -2;
                            break;

                        case 3: // Hard Question - Wrong
                                                
                                points = -20;
                                lives = -2;
                           
                            break;

                        case 4: // Very Hard Question - Wrong
                            points = -40;
                            lives = -3;
                            break;
                    }
                }
                break;
        }

        return new QuestionResult(points, lives);
    }
    

    // Medium question bonus in Easy mode – reveal one random mine safely
    public boolean shouldRevealMineBonus(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 2 && correct;
    }

    // Hard question bonus in Easy mode – reveal 3x3 safely
    public boolean shouldTrigger3x3Reveal(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 3 && correct;
    }

    public void endTurn() {
        if (!gameOver) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        notifyObservers();

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
            detailedHistory.setEndReason("LOST");
            saveGameHistory();
            notifyObservers();

        }
    }

    private void saveGameHistory() {
        if (sysData != null) {
            detailedHistory.setFinalScore(sharedScore);
            detailedHistory.setDurationSeconds(elapsedSeconds);
            detailedHistory.setWon(gameWon);

            // ✅ NEW: mark as COOP (no winner)
            detailedHistory.setMode(model.TwoPlayerMode.COOP);
            detailedHistory.setWinner("");

            sysData.addDetailedGameHistory(detailedHistory);
        }
    }


    // Win Condition #1 - ONE player clears their board
    public void setPlayerBoardComplete(int playerNum, boolean complete) {
        if (playerNum == 1) {
            player1BoardComplete = complete;
        } else {
            player2BoardComplete = complete;
        }

        if (complete) {
            gameOver = true;
            gameWon = true;

            int bonus = sharedLives * getSurprisePoints();
            sharedScore += bonus;

            stopTimer();
            detailedHistory.setEndReason("WIN");
            saveGameHistory();
            notifyObservers();

        }
    }

    // Loss Condition #2: All mines revealed
 // WIN: all mines are handled (revealed/flagged) and lives > 0
    public void handleAllMinesRevealed(int playerNum) {
        if (gameOver) return;

        if (sharedLives > 0) {
            gameOver = true;
            gameWon = true;

            int bonus = sharedLives * getSurprisePoints();
            sharedScore += bonus;

            stopTimer();
            saveGameHistory();
        } else {
            // אם בדיוק נגמרו חיים – זה LOST
            gameOver = true;
            gameWon = false;
            stopTimer();
            detailedHistory.setEndReason("WIN");
            saveGameHistory();
            notifyObservers();

        }
    }


    // Loss Condition #3: All flags used, some wrong
    public void handleAllFlagsUsed(int playerNum) {
        System.out.println("handleAllFlagsUsed called for Player " + playerNum);
        if (!gameOver) {
            gameOver = true;
            gameWon = false;
            stopTimer();
            saveGameHistory();
            notifyObservers();

        }
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
        detailedHistory.setEndReason("GIVE_UP");
        saveGameHistory();
        notifyObservers();
        

    }

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

    public static class CellActionResult {
        public final boolean turnEnded;
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

        public QuestionResult(int points, int lives) {
            this.points = points;
            this.lives = lives;
        }
    }
    public void markGameStarted() {
        gameStarted = true;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

}

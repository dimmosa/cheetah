package control;

import model.DetailedGameHistoryEntry;
import model.SysData;
import model.User;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CompetitiveGameController {

    private final List<GameObserver> observers = new ArrayList<>();

    private final SysData sysData;
    private final User player1;
    private final User player2;
    private final String difficulty;
    private final int gridSize;

    private int player1Score;
    private int player2Score;

    private int player1Lives;
    private int player2Lives;

    private int maxLives;           // active hearts by difficulty (10/8/6)
    private static final int TOTAL_LIVES_CAP = 10; // cap for conversion (same idea as your Multi)

    private int currentPlayer;
    private boolean gameOver;
    private boolean gameWon;

    private int winnerPlayerNum = 0; // 0 = none, 1 or 2 = winner

    private static final int REVEAL_POINTS = 1;
    private static final int FLAG_MINE_POINTS = 1;
    private static final int WRONG_FLAG_PENALTY = -3;

    private int rows;
    private int cols;

    private final Random random;

    private Timer timer;
    private int elapsedSeconds = 0;

    private boolean player1BoardComplete = false;
    private boolean player2BoardComplete = false;

    private boolean player1FirstMove = true;
    private boolean player2FirstMove = true;
    private boolean gameStarted = false;

    private int player1TotalMines = 0;
    private int player2TotalMines = 0;

    private int player1FlaggedCells = 0;
    private int player2FlaggedCells = 0;

    private int activationCost;

    private final DetailedGameHistoryEntry detailedHistory;

    // ----------------------------
    // ✅ OBSERVER
    // ----------------------------
    public void addObserver(GameObserver o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    public void removeObserver(GameObserver o) {
        observers.remove(o);
    }

    private void notifyObservers() {
        // אם את רוצה CompetitiveGameState נפרד – תחליפי כאן.
        GameState s = new GameState(
                /* sharedScore */ 0,
                /* sharedLives */ 0,
                currentPlayer,
                gameOver
        );
        for (GameObserver o : observers) {
            o.onGameStateChanged(s);
        }
    }

    // ----------------------------
    // ✅ CTOR
    // ----------------------------
    public CompetitiveGameController(SysData sysData, User player1, User player2, String difficulty, int gridSize) {
        this.sysData = sysData;
        this.player1 = player1;
        this.player2 = player2;
        this.difficulty = difficulty;
        this.gridSize = gridSize;
        this.random = new Random();

        initializeResources();

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

        notifyObservers();
    }

    public int getGridSize() { return gridSize; }
    public SysData getSysData() { return sysData; }

    private void initializeResources() {
        DifficultyFactory.Config cfg = DifficultyFactory.create(difficulty);

        this.rows = cfg.rows();
        this.cols = cfg.cols();
        this.activationCost = cfg.activationCost();

        this.maxLives = cfg.maxLives();     // Easy=10, Medium=8, Hard=6

        // start each player with their own lives
        this.player1Lives = maxLives;
        this.player2Lives = maxLives;

        this.player1Score = 0;
        this.player2Score = 0;
    }

    // ----------------------------
    // ✅ Mines count per board
    // ----------------------------
    public void setPlayerTotalMines(int playerNum, int totalMines) {
        if (playerNum == 1) player1TotalMines = totalMines;
        else player2TotalMines = totalMines;
    }

    // ----------------------------
    // ✅ First move tracking
    // ----------------------------
    public boolean isPlayerFirstMove(int playerNum) {
        return playerNum == 1 ? player1FirstMove : player2FirstMove;
    }

    public void markFirstMoveDone(int playerNum) {
        if (playerNum == 1) player1FirstMove = false;
        else player2FirstMove = false;
    }

    public void markGameStarted() { gameStarted = true; }
    public boolean isGameStarted() { return gameStarted; }

    // ----------------------------
    // ✅ Core getters
    // ----------------------------
    public int getScore(int playerNum) {
        return playerNum == 1 ? player1Score : player2Score;
    }

    public int getLives(int playerNum) {
        return playerNum == 1 ? player1Lives : player2Lives;
    }

    public int getMaxLives() { return maxLives; }
    public String getDifficulty() { return difficulty; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getActivationCost() { return activationCost; }

    public int getCurrentPlayer() { return currentPlayer; }

    public User getCurrentPlayerUser() { return currentPlayer == 1 ? player1 : player2; }

    public User getPlayer1() { return player1; }
    public User getPlayer2() { return player2; }

    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }

    public int getWinnerPlayerNum() { return winnerPlayerNum; }

    public DetailedGameHistoryEntry getDetailedHistory() { return detailedHistory; }

    // ----------------------------
    // ✅ Score update (for HINT etc.)
    // ----------------------------
    public void addScore(int playerNum, int delta) {
        if (playerNum == 1) {
            player1Score += delta;
            if (player1Score < 0) player1Score = 0;
        } else {
            player2Score += delta;
            if (player2Score < 0) player2Score = 0;
        }
        notifyObservers();
    }

    // ----------------------------
    // ✅ Actions (same idea as Multi, but per-player)
    // ----------------------------
    public CellActionResult revealMine(int playerNum) {
        if (playerNum == 1) detailedHistory.incrementPlayer1MineRevealed();
        else detailedHistory.incrementPlayer2MineRevealed();

        decLives(playerNum, 1);

        checkGameOverAfterLives();

        endTurn(); // mine ends turn (like your Multi)
        return new CellActionResult(true, 0, -1, "Mine revealed! Lost 1 life. Turn ends.", gameOver);
    }

    public CellActionResult flagMineCorrectly(int playerNum) {
        if (playerNum == 1) {
            detailedHistory.incrementPlayer1Flag(true);
            player1FlaggedCells++;
            player1Score += FLAG_MINE_POINTS;
        } else {
            detailedHistory.incrementPlayer2Flag(true);
            player2FlaggedCells++;
            player2Score += FLAG_MINE_POINTS;
        }

        notifyObservers();
        return new CellActionResult(false, FLAG_MINE_POINTS, 0, "Correct flag! +1 point. Continue your turn.", false);
    }

    public CellActionResult flagIncorrectly(int playerNum) {
        if (playerNum == 1) {
            detailedHistory.incrementPlayer1Flag(false);
            player1FlaggedCells++;
            player1Score += WRONG_FLAG_PENALTY;
            if (player1Score < 0) player1Score = 0;
        } else {
            detailedHistory.incrementPlayer2Flag(false);
            player2FlaggedCells++;
            player2Score += WRONG_FLAG_PENALTY;
            if (player2Score < 0) player2Score = 0;
        }

        notifyObservers();
        return new CellActionResult(false, WRONG_FLAG_PENALTY, 0, "Wrong flag! -3 points. Continue your turn.", false);
    }

    public CellActionResult revealNumberCell(int playerNum) {
        if (playerNum == 1) detailedHistory.incrementPlayer1CellRevealed();
        else detailedHistory.incrementPlayer2CellRevealed();

        addScoreInternal(playerNum, REVEAL_POINTS);

        endTurn();
        notifyObservers();

        return new CellActionResult(true, REVEAL_POINTS, 0, "Number cell revealed! +1 point. Turn ends.", false);
    }

    public CellActionResult revealEmptyCell(int playerNum) {
        if (playerNum == 1) detailedHistory.incrementPlayer1CellRevealed();
        else detailedHistory.incrementPlayer2CellRevealed();

        addScoreInternal(playerNum, REVEAL_POINTS);

        endTurn();
        notifyObservers();

        return new CellActionResult(true, REVEAL_POINTS, 0, "Empty cell revealed! +1 point. Cascading... Turn ends.", false);
    }

    public CellActionResult activateSurprise(int playerNum) {
        if (getScore(playerNum) < activationCost) {
            return new CellActionResult(false, 0, 0, "Not enough points!", false);
        }

        int scoreBefore = getScore(playerNum);
        int livesBefore = getLives(playerNum);

        // deduct cost
        addScoreInternal(playerNum, -activationCost);

        boolean isGood = random.nextBoolean();
        int points = getSurprisePoints();

        if (playerNum == 1) detailedHistory.incrementPlayer1Surprise(isGood);
        else detailedHistory.incrementPlayer2Surprise(isGood);

        if (isGood) {
            addScoreInternal(playerNum, points);
            incLives(playerNum, 1);
            convertExcessLivesToPoints(playerNum);
        } else {
            addScoreInternal(playerNum, -points);
            decLives(playerNum, 1);
            checkGameOverAfterLives();
        }

        int pointsDelta = getScore(playerNum) - scoreBefore;
        int livesDelta = getLives(playerNum) - livesBefore;

        String msg = String.format(
                "Surprise %s! %+d points, %+d lives. Continue your turn.",
                isGood ? "good" : "bad",
                pointsDelta,
                livesDelta
        );

        notifyObservers();
        return new CellActionResult(false, pointsDelta, livesDelta, msg, gameOver);
    }

    public CellActionResult activateQuestion(int playerNum, int questionDifficulty, boolean answeredCorrectly) {
        if (getScore(playerNum) < activationCost) {
            return new CellActionResult(false, 0, 0, "Not enough points!", false);
        }

        int scoreBefore = getScore(playerNum);
        int livesBefore = getLives(playerNum);

        addScoreInternal(playerNum, -activationCost);

        QuestionResult qr = calculateQuestionResult(questionDifficulty, answeredCorrectly);

        if (playerNum == 1) detailedHistory.incrementPlayer1Questions(answeredCorrectly);
        else detailedHistory.incrementPlayer2Questions(answeredCorrectly);

        addScoreInternal(playerNum, qr.points);
        if (qr.lives > 0) incLives(playerNum, qr.lives);
        else if (qr.lives < 0) decLives(playerNum, -qr.lives);

        convertExcessLivesToPoints(playerNum);
        checkGameOverAfterLives();

        int pointsDelta = getScore(playerNum) - scoreBefore;
        int livesDelta = getLives(playerNum) - livesBefore;

        String msg = String.format(
                "Question %s! %+d points, %+d lives. Continue your turn.",
                answeredCorrectly ? "correct" : "wrong",
                pointsDelta,
                livesDelta
        );

        notifyObservers();

        // like your Multi: question does NOT end turn
        return new CellActionResult(false, pointsDelta, livesDelta, msg, gameOver);
    }

    // ----------------------------
    // ✅ turn handling
    // ----------------------------
    public void endTurn() {
        if (!gameOver) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        notifyObservers();
    }

    public void switchTurn() { endTurn(); }

    // ----------------------------
    // ✅ win/lose rules (competitive)
    // ----------------------------
    private void checkGameOverAfterLives() {
        if (player1Lives <= 0 || player2Lives <= 0) {
            gameOver = true;
            stopTimer();

            // winner is the one still alive (or tie)
            if (player1Lives > 0 && player2Lives <= 0) {
                winnerPlayerNum = 1;
                gameWon = true;
            } else if (player2Lives > 0 && player1Lives <= 0) {
                winnerPlayerNum = 2;
                gameWon = true;
            } else {
                // both <=0 -> tie/lose
                winnerPlayerNum = 0;
                gameWon = false;
            }

            saveGameHistory();
        }
    }

    public void setPlayerBoardComplete(int playerNum, boolean complete) {
        if (playerNum == 1) player1BoardComplete = complete;
        else player2BoardComplete = complete;

        if (complete && !gameOver) {
            gameOver = true;
            gameWon = true;
            winnerPlayerNum = playerNum;

            // bonus: remaining lives * surprise points -> add to winner
            int bonus = getLives(playerNum) * getSurprisePoints();
            addScoreInternal(playerNum, bonus);

            stopTimer();
            saveGameHistory();
            notifyObservers();
        }
    }

    public void handleAllMinesRevealed(int playerNum) {
        if (gameOver) return;

        // In competitive: if a player handled all mines on their board -> they win
        gameOver = true;
        gameWon = true;
        winnerPlayerNum = playerNum;

        int bonus = getLives(playerNum) * getSurprisePoints();
        addScoreInternal(playerNum, bonus);

        stopTimer();
        saveGameHistory();
        notifyObservers();
    }

    public void handleAllFlagsUsed(int playerNum) {
        if (gameOver) return;

        // If flags exhausted and wrong -> you decide rule:
        // common: current player loses.
        gameOver = true;
        gameWon = true;
        winnerPlayerNum = (playerNum == 1) ? 2 : 1;

        stopTimer();
        saveGameHistory();
        notifyObservers();
    }

    public void giveUp() {
        if (gameOver) return;

        gameOver = true;
        gameWon = true;

        // giveUp -> other player wins
        winnerPlayerNum = (currentPlayer == 1) ? 2 : 1;

        stopTimer();
        saveGameHistory();
        notifyObservers();
    }

    // ----------------------------
    // ✅ Timer
    // ----------------------------
    public void startTimer(Consumer<String> onTick) {
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            int minutes = elapsedSeconds / 60;
            int seconds = elapsedSeconds % 60;
            String timeStr = String.format("%02d:%02d", minutes, seconds);
            onTick.accept(timeStr);

            if (isGameOver()) stopTimer();
        });
        timer.start();
    }

    public void stopTimer() {
        if (timer != null) timer.stop();
    }

    public void resetTimer() { elapsedSeconds = 0; }

    // ----------------------------
    // ✅ helpers
    // ----------------------------
    private void addScoreInternal(int playerNum, int delta) {
        if (playerNum == 1) {
            player1Score += delta;
            if (player1Score < 0) player1Score = 0;
        } else {
            player2Score += delta;
            if (player2Score < 0) player2Score = 0;
        }
    }

    private void incLives(int playerNum, int delta) {
        if (playerNum == 1) player1Lives += delta;
        else player2Lives += delta;

        if (player1Lives < 0) player1Lives = 0;
        if (player2Lives < 0) player2Lives = 0;
    }

    private void decLives(int playerNum, int delta) {
        incLives(playerNum, -delta);
    }

    private void convertExcessLivesToPoints(int playerNum) {
        // same policy as your Multi: cap to TOTAL_LIVES_CAP and convert excess to points
        int lives = getLives(playerNum);
        if (lives > TOTAL_LIVES_CAP) {
            int excess = lives - TOTAL_LIVES_CAP;
            int lifePoints = getLifeConversionValue();
            addScoreInternal(playerNum, excess * lifePoints);

            if (playerNum == 1) player1Lives = TOTAL_LIVES_CAP;
            else player2Lives = TOTAL_LIVES_CAP;
        }
    }

    private int getSurprisePoints() {
        switch (difficulty) {
            case "Easy": return 8;
            case "Medium": return 12;
            case "Hard": return 16;
            default: return 12;
        }
    }

    private int getLifeConversionValue() {
        switch (difficulty) {
            case "Easy": return 5;
            case "Medium": return 8;
            case "Hard": return 12;
            default: return 8;
        }
    }

    // ----------------------------
    // ✅ Question table (copied style from your Multi)
    // ----------------------------
    private QuestionResult calculateQuestionResult(int questionDifficulty, boolean correct) {
        int points = 0;
        int lives = 0;
        boolean random50 = random.nextBoolean();

        switch (difficulty) {
            case "Easy":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: points = 3; lives = 1; break;
                        case 2: points = 6; lives = 0; break;
                        case 3: points = 10; lives = 0; break;
                        case 4: points = 15; lives = 2; break;
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1: points = random50 ? -3 : 0; lives = 0; break;
                        case 2: points = random50 ? -6 : 0; lives = 0; break;
                        case 3: points = -10; lives = 0; break;
                        case 4: points = -15; lives = -1; break;
                    }
                }
                break;

            case "Medium":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: points = 8; lives = 1; break;
                        case 2: points = 10; lives = 1; break;
                        case 3: points = 15; lives = 1; break;
                        case 4: points = 20; lives = 2; break;
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1: points = -8; lives = 0; break;
                        case 2:
                            if (random50) { points = -10; lives = -1; }
                            else { points = 0; lives = 0; }
                            break;
                        case 3: points = -15; lives = -1; break;
                        case 4: points = -20; lives = random50 ? -1 : -2; break;
                    }
                }
                break;

            case "Hard":
                if (correct) {
                    switch (questionDifficulty) {
                        case 1: points = 10; lives = 1; break;
                        case 2: points = 15; lives = random50 ? 1 : 2; break;
                        case 3: points = 20; lives = 2; break;
                        case 4: points = 40; lives = 3; break;
                    }
                } else {
                    switch (questionDifficulty) {
                        case 1: points = -10; lives = -1; break;
                        case 2: points = -15; lives = random50 ? -1 : -2; break;
                        case 3: points = -20; lives = -2; break;
                        case 4: points = -40; lives = -3; break;
                    }
                }
                break;
        }

        return new QuestionResult(points, lives);
    }

    // Bonuses same signature as Multi (if you still want them in competitive)
    public boolean shouldRevealMineBonus(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 2 && correct;
    }

    public boolean shouldTrigger3x3Reveal(int questionDifficulty, boolean correct) {
        return "Easy".equals(difficulty) && questionDifficulty == 3 && correct;
    }

    // ----------------------------
    // ✅ History save (very similar to your Multi)
    // ----------------------------
    private void saveGameHistory() {
        if (sysData == null) return;

        // For competitive: store "final score" maybe winner score or max score
        int finalScore = Math.max(player1Score, player2Score);

        detailedHistory.setFinalScore(finalScore);
        detailedHistory.setDurationSeconds(elapsedSeconds);
        detailedHistory.setWon(gameWon);

        sysData.addDetailedGameHistory(detailedHistory);
    }

    // ----------------------------
    // ✅ Result class (with gameOver flag like your Competitive board expects)
    // ----------------------------
    public static class CellActionResult {
        public final boolean turnEnded;
        public final int scoreChanged;
        public final int livesChanged;
        public final String message;
        public final boolean gameOver;

        public CellActionResult(boolean turnEnded, int scoreChanged, int livesChanged, String message, boolean gameOver) {
            this.turnEnded = turnEnded;
            this.scoreChanged = scoreChanged;
            this.livesChanged = livesChanged;
            this.message = message;
            this.gameOver = gameOver;
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

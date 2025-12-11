package view;

import control.QuestionController;
import control.MultiPlayerGameController;
import model.Question;
import model.CellType;
import view.CellButton;
import view.QuestionCellDialog;
import view.QuestionTimeDialog;
import view.SurpriseBonusDialog;
import view.SurprisePenaltyDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MinesweeperBoardPanelTwoPlayer extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int rows;
    private final int cols;
    private final CellButton[][] cells;
    private int cellSize;
    private final MultiPlayerGameController gameController;
    private final QuestionController questionController;
    private final GameScreenMultiPlayer parentScreen;
    private final boolean isPlayer1Board;

    private int totalMines;
    private int revealedMines = 0;
    private int totalNonMineCells;
    private int revealedNonMineCells = 0;

    private boolean isFlagMode = false;
    private Random random = new Random();

    // Track first cell position to ensure it's not a mine
    private int firstClickRow = -1;
    private int firstClickCol = -1;
    private boolean boardGenerated = false;

    public MinesweeperBoardPanelTwoPlayer(int rows, int cols,
                                          MultiPlayerGameController gameController,
                                          QuestionController questionController,
                                          GameScreenMultiPlayer parentScreen,
                                          boolean isPlayer1Board) {
        this.rows = rows;
        this.cols = cols;
        this.gameController = gameController;
        this.questionController = questionController;
        this.parentScreen = parentScreen;
        this.isPlayer1Board = isPlayer1Board;
        this.cells = new CellButton[rows][cols];

        switch (gameController.getDifficulty()) {
            case "Easy":
                cellSize = 40;
                break;
            case "Medium":
                cellSize = 35;
                break;
            case "Hard":
                cellSize = 26;
                break;
            default:
                cellSize = 22;
        }

        setLayout(new GridLayout(rows, cols, 2, 2));
        setBackground(new Color(10, 10, 15));
        initializeBoard();
    }

    private void initializeBoard() {
        System.out.println("Initializing two-player board: " + gameController.getDifficulty() +
                " - Player " + (isPlayer1Board ? "1" : "2"));

        int boardWidth = cols * cellSize + (cols - 1) * 2;
        int boardHeight = rows * cellSize + (rows - 1) * 2;
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = new CellButton(cellSize);
                cells[i][j] = cell;
                add(cell);

                int r = i, c = j;

                cell.addActionListener(e -> handleCellClick(r, c));

                cell.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleRightClick(r, c);
                        }
                    }
                });
            }
        }
    }

    // Generate board AFTER first click, ensuring first cell is safe
    private void generateBoardWithSafeFirstCell(int firstRow, int firstCol) {
        if (boardGenerated) return;

        System.out.println("Generating board for " + (isPlayer1Board ? "Player 1" : "Player 2") +
                " with first click at: " + firstRow + "," + firstCol);

        firstClickRow = firstRow;
        firstClickCol = firstCol;

        placeMines();
        calculateNumbers();
        placeSpecialCells();

        boardGenerated = true;

        // Tell controller about total mines
        int playerNum = isPlayer1Board ? 1 : 2;
        gameController.setPlayerTotalMines(playerNum, totalMines);

        System.out.println("Board generated: " + totalMines + " mines, " +
                totalNonMineCells + " non-mine cells");
    }

    private void placeMines() {
        int minesToPlace = calculateMineCount();
        int placed = 0;

        // Create list of forbidden positions (first click + surrounding cells)
        List<String> forbiddenPositions = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = firstClickRow + dr;
                int nc = firstClickCol + dc;
                if (isValidCell(nr, nc)) {
                    forbiddenPositions.add(nr + "," + nc);
                }
            }
        }

        System.out.println("Forbidden positions: " + forbiddenPositions.size());

        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            String pos = r + "," + c;

            // Don't place mine if it's the first click or surrounding cells or already a mine
            if (!forbiddenPositions.contains(pos) && cells[r][c].getCellType() != CellType.MINE) {
                cells[r][c].setCellType(CellType.MINE);
                placed++;
            }
        }

        totalMines = minesToPlace;
        System.out.println("Placed exactly " + placed + " mines on " +
                (isPlayer1Board ? "Player 1" : "Player 2") + " board");
    }

    // Correct mine counts from requirements
    private int calculateMineCount() {
        switch (gameController.getDifficulty()) {
            case "Easy":
                return 10;
            case "Medium":
                return 26;
            case "Hard":
                return 44;
            default:
                return 26;
        }
    }

    private void calculateNumbers() {
        totalNonMineCells = (rows * cols) - totalMines;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() != CellType.MINE) {
                    int adjacentMines = calculateAdjacentMines(r, c);

                    if (adjacentMines > 0) {
                        cells[r][c].setCellType(CellType.NUMBER);
                        cells[r][c].setNumber(adjacentMines);
                    } else {
                        cells[r][c].setCellType(CellType.EMPTY);
                    }
                }
            }
        }
    }

    private void placeSpecialCells() {
        List<int[]> emptyCells = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() == CellType.EMPTY) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        Collections.shuffle(emptyCells);

        int questionCellsNeeded;
        int surpriseCellsNeeded;

        switch (gameController.getDifficulty()) {
            case "Easy" -> {
                questionCellsNeeded = 6;
                surpriseCellsNeeded = 2;
            }
            case "Medium" -> {
                questionCellsNeeded = 7;
                surpriseCellsNeeded = 3;
            }
            case "Hard" -> {
                questionCellsNeeded = 11;
                surpriseCellsNeeded = 4;
            }
            default -> {
                questionCellsNeeded = 7;
                surpriseCellsNeeded = 3;
            }
        }

        int index = 0;

        for (int i = 0; i < questionCellsNeeded && index < emptyCells.size(); i++) {
            int[] pos = emptyCells.get(index++);
            cells[pos[0]][pos[1]].setCellType(CellType.QUESTION);
        }

        for (int i = 0; i < surpriseCellsNeeded && index < emptyCells.size(); i++) {
            int[] pos = emptyCells.get(index++);
            cells[pos[0]][pos[1]].setCellType(CellType.SURPRISE);
        }

        System.out.println("Placed " + questionCellsNeeded + " question cells and " +
                surpriseCellsNeeded + " surprise cells on " +
                (isPlayer1Board ? "Player 1" : "Player 2") + " board");
    }

    private void handleCellClick(int r, int c) {
        if (gameController.isGameOver()) {
            return;
        }

        boolean isPlayer1Turn = gameController.getCurrentPlayer() == 1;
        if ((isPlayer1Board && !isPlayer1Turn) || (!isPlayer1Board && isPlayer1Turn)) {
            JOptionPane.showMessageDialog(this,
                    "It's not your turn! Current turn: Player " + gameController.getCurrentPlayer(),
                    "Wait Your Turn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Generate board on first click for THIS board only
        if (!boardGenerated) {
            generateBoardWithSafeFirstCell(r, c);
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.markFirstMoveDone(playerNum);
        }

        CellButton cell = cells[r][c];

        if (isFlagMode) {
            handleFlagPlacement(r, c);
            return;
        }

        if (cell.getState() == CellButton.CellState.HIDDEN) {
            revealCell(r, c);
        } else if (cell.getState() == CellButton.CellState.QUESTION && cell.canActivate()) {
            activateQuestionCell(r, c);
        } else if (cell.getState() == CellButton.CellState.SURPRISE && cell.canActivate()) {
            activateSurpriseCell(r, c);
        }
    }

    private void handleRightClick(int r, int c) {
        if (gameController.isGameOver()) {
            return;
        }

        boolean isPlayer1Turn = gameController.getCurrentPlayer() == 1;
        if ((isPlayer1Board && !isPlayer1Turn) || (!isPlayer1Board && isPlayer1Turn)) {
            return;
        }

        // Generate board on first right-click for THIS board only
        if (!boardGenerated) {
            generateBoardWithSafeFirstCell(r, c);
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.markFirstMoveDone(playerNum);
        }

        handleFlagPlacement(r, c);
    }

    private void handleFlagPlacement(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.getState() != CellButton.CellState.HIDDEN) {
            return;
        }

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            repaint();
        } else {
            CellType actualType = cell.getCellType();
            cell.setFlagged(true);

            if (actualType == CellType.MINE) {
                MultiPlayerGameController.CellActionResult result = gameController.flagMineCorrectly();
                cell.showCorrectFlagFeedback();
                cell.setBorder(new LineBorder(new Color(0, 255, 0, 150)));
                cell.setBackground(new Color(0, 255, 0, 150));
                cell.setPermanentBorderColor(new Color(0, 255, 0, 150));
                playCorrectSound();
                parentScreen.updateGameStateDisplay(result);
            } else {
                MultiPlayerGameController.CellActionResult result = gameController.flagIncorrectly();
                cell.showIncorrectFlagFeedback();
                cell.setBorder(new LineBorder(new Color(255, 0, 0, 150)));
                cell.setBackground(new Color(255, 0, 0, 150));
                cell.setPermanentBorderColor(new Color(255, 0, 0, 150));
                playIncorrectSound();
                parentScreen.updateGameStateDisplay(result);
            }

            repaint();

            // After placing a flag, check if this board is now "finished"
            checkBoardComplete();
        }
    }

    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;

        CellType type = cell.getCellType();
        cell.setState(CellButton.CellState.REVEALED);

        MultiPlayerGameController.CellActionResult result = null;

        switch (type) {
            case MINE -> {
                result = gameController.revealMine();
                cell.showMine();
                revealedMines++;

                System.out.println("Mine revealed! Total revealed mines: " + revealedMines + "/" + totalMines);

                // Update display BEFORE checking game over
                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                    if (result.turnEnded) {
                        parentScreen.updateActivePlayer();
                    }
                }

                checkAllMinesRevealed();

                if (!gameController.isGameOver()) {
                    JOptionPane.showMessageDialog(this,
                            "💣 BOOM! Mine hit! Lives left: " + gameController.getSharedLives() +
                                    "\nTurn ends! Next player's turn.",
                            "Mine!",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
            case NUMBER -> {
                result = gameController.revealNumberCell();
                cell.showNumber(calculateAdjacentMines(r, c));
                revealedNonMineCells++;

                System.out.println("Number revealed! Non-mine cells: " + revealedNonMineCells + "/" + totalNonMineCells);

                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                    if (result.turnEnded) {
                        parentScreen.updateActivePlayer();
                    }
                }
            }
            case EMPTY -> {
                result = gameController.revealEmptyCell();
                cell.showEmpty();
                revealedNonMineCells++;

                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                }

                cascadeReveal(r, c);

                System.out.println("Empty revealed! Non-mine cells: " + revealedNonMineCells + "/" + totalNonMineCells);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }
            case QUESTION -> {
                result = gameController.revealNumberCell();
                cell.showQuestion();
                revealedNonMineCells++;

                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                }

                cascadeReveal(r, c);

                System.out.println("Question revealed! Non-mine cells: " + revealedNonMineCells + "/" + totalNonMineCells);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }
            case SURPRISE -> {
                result = gameController.revealNumberCell();
                cell.showSurprise();
                revealedNonMineCells++;

                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                }

                cascadeReveal(r, c);

                System.out.println("Surprise revealed! Non-mine cells: " + revealedNonMineCells + "/" + totalNonMineCells);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }
        }

        // After any reveal (and cascades), check if this board is "finished"
        checkBoardComplete();

        repaint();

        if (gameController.isGameOver()) {
            handleGameEnd();
        }
    }

    private void cascadeReveal(int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int nr = r + dr;
                int nc = c + dc;

                if (!isValidCell(nr, nc)) continue;

                CellButton neighbor = cells[nr][nc];
                if (!neighbor.canReveal()) continue;

                CellType type = neighbor.getCellType();
                neighbor.setState(CellButton.CellState.REVEALED);

                switch (type) {
                    case EMPTY -> {
                        neighbor.showEmpty();
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case NUMBER -> {
                        neighbor.showNumber(calculateAdjacentMines(nr, nc));
                        revealedNonMineCells++;
                    }
                    case QUESTION -> {
                        neighbor.showQuestion();
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case SURPRISE -> {
                        neighbor.showSurprise();
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case MINE -> {
                        // Don't reveal mines during cascade
                    }
                }
            }
        }
    }

    private void checkAllMinesRevealed() {
        if (revealedMines >= totalMines) {
            int playerNum = isPlayer1Board ? 1 : 2;
            System.out.println("Player " + playerNum + " revealed ALL mines! Game Over!");

            gameController.handleAllMinesRevealed(playerNum);

            JOptionPane.showMessageDialog(this,
                    "Player " + playerNum + " revealed all mines!\nGame Over - You Lose!",
                    "All Mines Revealed!",
                    JOptionPane.ERROR_MESSAGE);

            handleGameEnd();
        }
    }

    /**
     * Check if THIS board meets any of the co-op end conditions.
     */
    private void checkBoardComplete() {
        if (gameController.isGameOver() || !boardGenerated) {
            return;
        }

        int hiddenCells = 0;
        int correctlyFlaggedMines = 0;
        int wrongFlags = 0;
        int totalFlags = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                CellType type = cb.getCellType();
                boolean revealed = (cb.getState() == CellButton.CellState.REVEALED);
                boolean flagged = cb.isFlagged();

                if (!revealed && !flagged) {
                    hiddenCells++;
                }

                if (flagged) {
                    totalFlags++;
                    if (type == CellType.MINE) {
                        correctlyFlaggedMines++;
                    } else {
                        wrongFlags++;
                    }
                }
            }
        }

        int playerNum = isPlayer1Board ? 1 : 2;
        boolean gameEnds = false;
        boolean playerWins = false;
        String endReason = "";

        if (hiddenCells == 0) {
            gameEnds = true;
            endReason = "Player " + playerNum + " has no more actions (all cells revealed/flagged).";

            if (correctlyFlaggedMines == totalMines && wrongFlags == 0) {
                playerWins = true;
            }
        } else if (revealedMines + correctlyFlaggedMines == totalMines) {
            gameEnds = true;
            endReason = "Player " + playerNum + " discovered all mines!";

            if (wrongFlags == 0) {
                playerWins = true;
            }
        } else if (correctlyFlaggedMines == totalMines) {
            gameEnds = true;
            endReason = "Player " + playerNum + " flagged all " + totalMines + " mines!";

            if (wrongFlags == 0) {
                playerWins = true;
            }
        }

        if (!gameEnds) {
            return;
        }

        System.out.println(endReason);

        if (playerWins) {
            System.out.println("WIN CONDITION MET");
            gameController.setPlayerBoardComplete(playerNum, true);

            JOptionPane.showMessageDialog(this,
                    endReason + "\n" +
                            "All mines correctly handled with no wrong flags!\n" +
                            "Co-op Victory! Final Score: " + gameController.getSharedScore(),
                    "VICTORY!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("LOSS CONDITION - Wrong flags detected: " + wrongFlags);
            gameController.giveUp();

            JOptionPane.showMessageDialog(this,
                    endReason + "\n" +
                            "However, there are " + wrongFlags + " wrong flag(s).\n" +
                            "Game Over – You Lose!",
                    "Wrong Flags",
                    JOptionPane.ERROR_MESSAGE);
        }

        handleGameEnd();
    }

    // Surprise no longer ends turn
    private void activateSurpriseCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            JOptionPane.showMessageDialog(this,
                    "This surprise cell has already been used!",
                    "Already Used",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int activationCost = gameController.getActivationCost();
        if (gameController.getSharedScore() < activationCost) {
            JOptionPane.showMessageDialog(this,
                    "Not enough points to activate surprise! Need " + activationCost + " points.",
                    "Insufficient Points",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Activate surprise cell?\n50% chance of reward or penalty!\nCosts " + activationCost + " points.",
                "Activate Surprise?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        MultiPlayerGameController.CellActionResult result = gameController.activateSurprise();
        cell.setUsed(true);

        if (result.pointsChanged > 0) {
            SurpriseBonusDialog dialog = new SurpriseBonusDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        } else {
            SurprisePenaltyDialog dialog = new SurprisePenaltyDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        }

        parentScreen.updateGameStateDisplay(result);
        repaint();

        if (gameController.isGameOver()) {
            handleGameEnd();
        }
    }

    // Question no longer ends turn
    private void activateQuestionCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            JOptionPane.showMessageDialog(this,
                    "This question cell has already been used!",
                    "Already Used",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int activationCost = gameController.getActivationCost();
        if (gameController.getSharedScore() < activationCost) {
            JOptionPane.showMessageDialog(this,
                    "Not enough points to activate question! Need " + activationCost + " points.",
                    "Insufficient Points",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        QuestionCellDialog cellDialog = new QuestionCellDialog(new Frame());
        cellDialog.setVisible(true);

        if (!cellDialog.shouldProceed()) {
            return;
        }

        cell.setUsed(true);
        showQuestionDialog(r, c);

        repaint();
    }

    /**
     * SHOW QUESTION DIALOG – chooses ANY difficulty (1–4) randomly,
     * and applies Easy-mode bonuses (reveal mine / reveal 3x3).
     */
    private void showQuestionDialog(int r, int c) {
        // -1 means "any difficulty"
        Question q = questionController.getRandomQuestion(-1);

        if (q == null) {
            JOptionPane.showMessageDialog(this, "No questions available!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String difficultyText = QuestionController.difficultyToString(q.getDifficulty());

        QuestionTimeDialog qd = new QuestionTimeDialog(
                new Frame(),
                difficultyText,
                q.getText(),
                q.getAnswers(),
                selectedIndex -> {
                    boolean correct = (selectedIndex == q.getCorrectIndex());

                    MultiPlayerGameController.CellActionResult result =
                            gameController.activateQuestion(q.getDifficulty(), correct);

                    // === Easy-mode board bonuses ===
                    if ("Easy".equals(gameController.getDifficulty()) && correct) {
                        // Medium question → reveal one random mine safely
                        if (gameController.shouldRevealMineBonus(q.getDifficulty(), true)) {
                            revealRandomMineBonus();
                        }
                        // Hard question → reveal random 3x3 safely
                        if (gameController.shouldTrigger3x3Reveal(q.getDifficulty(), true)) {
                            revealRandom3x3Bonus();
                        }
                        // After free reveals, re-check completion
                        checkBoardComplete();
                    }
                    // ===============================

                    if (correct) {
                        JOptionPane.showMessageDialog(this, "✓ Correct! " + result.message);
                    } else {
                        JOptionPane.showMessageDialog(this, "✗ Incorrect! " + result.message);
                    }

                    // Update score / lives + mini-stats AFTER applying bonuses
                    parentScreen.updateGameStateDisplay(result);

                    if (gameController.isGameOver()) {
                        handleGameEnd();
                    }
                }
        );

        qd.setVisible(true);
    }

    // Reveal ONE random hidden, unflagged mine as a FREE bonus (no life loss)
    private void revealRandomMineBonus() {
        List<int[]> candidates = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                if (cb.getCellType() == CellType.MINE &&
                        cb.getState() == CellButton.CellState.HIDDEN &&
                        !cb.isFlagged()) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(candidates, random);
        int[] pos = candidates.get(0);
        CellButton cell = cells[pos[0]][pos[1]];

        cell.setState(CellButton.CellState.REVEALED);
        cell.showMine();
        revealedMines++;

        System.out.println("Bonus: revealed mine at (" + pos[0] + "," + pos[1] + ")  -> " +
                revealedMines + "/" + totalMines);
    }

    // Reveal up to 3x3 hidden cells around a random center as FREE bonus
    private void revealRandom3x3Bonus() {
        List<int[]> candidates = new ArrayList<>();

        // any hidden, unflagged non-question/surprise cell can be a center
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                if (cb.getState() == CellButton.CellState.HIDDEN && !cb.isFlagged()) {
                    CellType t = cb.getCellType();
                    if (t == CellType.EMPTY || t == CellType.NUMBER || t == CellType.MINE) {
                        candidates.add(new int[]{r, c});
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(candidates, random);
        int[] center = candidates.get(0);
        int cr = center[0];
        int cc = center[1];

        int revealedCount = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int rr = cr + dr;
                int cc2 = cc + dc;

                if (!isValidCell(rr, cc2)) continue;

                CellButton cell = cells[rr][cc2];
                if (cell.getState() != CellButton.CellState.HIDDEN || cell.isFlagged()) continue;

                CellType t = cell.getCellType();

                // skip question/surprise so their logic stays intact
                if (t == CellType.QUESTION || t == CellType.SURPRISE) continue;

                cell.setState(CellButton.CellState.REVEALED);

                if (t == CellType.MINE) {
                    cell.showMine();
                    revealedMines++;
                } else if (t == CellType.NUMBER) {
                    cell.showNumber(calculateAdjacentMines(rr, cc2));
                    revealedNonMineCells++;
                } else if (t == CellType.EMPTY) {
                    cell.showEmpty();
                    revealedNonMineCells++;
                }

                revealedCount++;
            }
        }

        System.out.println("Bonus 3x3 reveal from center (" + cr + "," + cc +
                ") revealed " + revealedCount + " cells.");
        repaint();
    }

    private int calculateAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int nr = r + dr;
                int nc = c + dc;

                if (isValidCell(nr, nc) && cells[nr][nc].getCellType() == CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isValidCell(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void handleGameEnd() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getState() == CellButton.CellState.HIDDEN) {
                    cells[i][j].setState(CellButton.CellState.REVEALED);
                    if (cells[i][j].getCellType() == CellType.MINE) {
                        cells[i][j].showMine();
                    }
                }
            }
        }
        repaint();
    }

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    private void playCorrectSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void playIncorrectSound() {
    }

    public int getCellSize() {
        return cellSize;
    }

    public CellButton getCell(int row, int col) {
        if (isValidCell(row, col)) {
            return cells[row][col];
        }
        return null;
    }

    public int getFlaggedMinesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() == CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getCorrectFlagsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() == CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getIncorrectFlagsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() != CellType.MINE) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getRevealedCellsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getState() == CellButton.CellState.REVEALED) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getUsedQuestionsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.QUESTION && cells[i][j].isUsed()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalQuestionsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.QUESTION) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getUsedSurprisesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.SURPRISE && cells[i][j].isUsed()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalSurprisesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.SURPRISE) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getRevealedMinesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cb = cells[i][j];
                if (cb.getCellType() == CellType.MINE) {
                    CellButton.CellState st = cb.getState();
                    // count mines that are shown either as REVEALED or as MINE-state
                    if (st == CellButton.CellState.REVEALED ||
                        st == CellButton.CellState.MINE) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int getTotalMines() {
        return totalMines;
    }
}

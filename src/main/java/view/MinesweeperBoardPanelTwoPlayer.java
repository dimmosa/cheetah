package view;

import control.QuestionController;
import control.MultiPlayerGameController;
import model.Question;
import model.cell.CellType;
import view.CellButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MinesweeperBoardPanelTwoPlayer extends JPanel {

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

    // First click on this board (for this specific player board)
    private boolean firstClick = true;

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
                cellSize = 32;
                break;
            case "Medium":
                cellSize = 28;
                break;
            case "Hard":
                cellSize = 20;
                break;
            default:
                cellSize = 18;
        }

        setLayout(new GridLayout(rows, cols, 2, 2));
        setBackground(new Color(10, 10, 15));
        initializeBoard();
        generateBoard();
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

    private void generateBoard() {
        placeMines();
        calculateNumbers();
        placeSpecialCells();
    }

    private void placeMines() {
        int minesToPlace = calculateMineCount();
        int placed = 0;

        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (cells[r][c].getCellType() != CellType.MINE) {
                cells[r][c].setCellType(CellType.MINE);
                placed++;
                totalMines = minesToPlace;
            }
        }

        System.out.println("Placed " + placed + " mines on " +
                (isPlayer1Board ? "Player 1" : "Player 2") + " board");
    }

    private int calculateMineCount() {
        int totalCells = rows * cols;
        return (int) Math.ceil(totalCells * 0.15);
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

        int questionCellsNeeded = 0;
        int surpriseCellsNeeded = 0;

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
        }
    }

    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;

        // First click on this board – force a 3x3 safe area around the clicked cell
        if (firstClick) {
            firstClick = false;
            moveMineToSafeLocation(r, c);
        }

        CellType type = cell.getCellType();
        cell.setState(CellButton.CellState.REVEALED);

        MultiPlayerGameController.CellActionResult result = null;

        switch (type) {
            case MINE -> {
                result = gameController.revealMine();
                cell.showMine();
                revealedMines++;
                checkAllMinesRevealed();
                JOptionPane.showMessageDialog(this,
                        "💣 BOOM! Mine hit! Lives left: " + gameController.getSharedLives() +
                                "\nTurn ends! Next player's turn.",
                        "Mine!",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            case NUMBER -> {
                result = gameController.revealNumberCell();
                cell.showNumber(calculateAdjacentMines(r, c));
                revealedNonMineCells++;
                checkBoardComplete();
            }
            case EMPTY -> {
                result = gameController.revealEmptyCell();
                cell.showEmpty();
                revealedNonMineCells++;
                checkBoardComplete();
                cascadeReveal(r, c);
            }
            case QUESTION -> {
                result = gameController.revealNumberCell();
                cell.showQuestion();
                revealedNonMineCells++;
                checkBoardComplete();
                cascadeReveal(r, c); // NEW: cascade like empty
            }
            case SURPRISE -> {
                result = gameController.revealNumberCell();
                cell.showSurprise();
                revealedNonMineCells++;
                checkBoardComplete();
                cascadeReveal(r, c); // NEW: cascade like empty
            }
        }

        if (result != null) {
            parentScreen.updateGameStateDisplay(result);
            if (result.turnEnded) {
                parentScreen.updateActivePlayer();
            }
        }

        repaint();

        if (gameController.isGameOver()) {
            handleGameEnd();
        }
    }

    /**
     * Ensures the first click is always safe:
     * - Removes mines from the 3x3 area around the clicked cell
     * - Repositions those mines far from the clicked area
     * - Recalculates numbers
     * - Re-places special cells away from the first-click area
     */
    private void moveMineToSafeLocation(int clickedRow, int clickedCol) {
        System.out.println("First click at (" + clickedRow + "," + clickedCol + ") - clearing area...");

        // 1. Remove any mines in the 3x3 area around the clicked cell
        List<int[]> removedMines = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = clickedRow + dr;
                int nc = clickedCol + dc;

                if (isValidCell(nr, nc) && cells[nr][nc].getCellType() == CellType.MINE) {
                    cells[nr][nc].setCellType(CellType.EMPTY);
                    removedMines.add(new int[]{nr, nc});
                    System.out.println("Removed mine from (" + nr + "," + nc + ")");
                }
            }
        }

        // 2. Count how many mines remain
        int remainingMines = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() == CellType.MINE) {
                    remainingMines++;
                }
            }
        }

        // 3. Re-add mines to cells that are far enough from the clicked area
        int minesNeeded = totalMines - remainingMines;
        int placed = 0;

        System.out.println("Need to place " + minesNeeded + " mines back");

        while (placed < minesNeeded) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            int manhattanDist = Math.abs(r - clickedRow) + Math.abs(c - clickedCol);

            if (manhattanDist > 3 && cells[r][c].getCellType() != CellType.MINE) {
                cells[r][c].setCellType(CellType.MINE);
                placed++;
                System.out.println("Placed mine at (" + r + "," + c + ")");
            }
        }

        // 4. Recalculate numbers for all non-mine cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() != CellType.MINE) {
                    int adjacentMines = calculateAdjacentMines(r, c);

                    if (adjacentMines > 0) {
                        cells[r][c].setCellType(CellType.NUMBER);
                        cells[r][c].setNumber(adjacentMines);
                    } else {
                        cells[r][c].setCellType(CellType.EMPTY);
                        cells[r][c].setNumber(0);
                    }
                }
            }
        }

        // 5. Force the entire 3x3 area to be EMPTY (no numbers or special cells)
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = clickedRow + dr;
                int nc = clickedCol + dc;

                if (isValidCell(nr, nc) && cells[nr][nc].getCellType() != CellType.MINE) {
                    cells[nr][nc].setCellType(CellType.EMPTY);
                    cells[nr][nc].setNumber(0);
                }
            }
        }

        // 6. Place special cells again, but avoid the vicinity of the first-click area
        placeSpecialCellsAvoidingArea(clickedRow, clickedCol);

        System.out.println("First click setup complete - forced 3x3 EMPTY area around (" +
                clickedRow + "," + clickedCol + ")");
    }

    /**
     * Place special QUESTION and SURPRISE cells only in EMPTY cells
     * that are far enough (Manhattan distance > 2) from the first-click area.
     */
    private void placeSpecialCellsAvoidingArea(int avoidRow, int avoidCol) {
        List<int[]> emptyCells = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int manhattanDist = Math.abs(r - avoidRow) + Math.abs(c - avoidCol);
                if (cells[r][c].getCellType() == CellType.EMPTY && manhattanDist > 2) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        Collections.shuffle(emptyCells);

        int questionCellsNeeded = 0;
        int surpriseCellsNeeded = 0;

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
    }

    /**
     * Cascade reveal (flood-fill) from an EMPTY cell.
     * Only reveals:
     * - EMPTY cells (recursively)
     * - NUMBER cells
     *
     * It does NOT:
     * - Reveal MINE cells
     * - Auto-activate QUESTION or SURPRISE cells
     */
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

                // Never reveal mines in cascade
                if (type == CellType.MINE) {
                    continue;
                }

                // Do not auto-open question/surprise cells
                if (type == CellType.QUESTION || type == CellType.SURPRISE) {
                    continue;
                }

                neighbor.setState(CellButton.CellState.REVEALED);

                switch (type) {
                    case EMPTY -> {
                        neighbor.showEmpty();
                        revealedNonMineCells++;
                        // Continue flood-fill
                        cascadeReveal(nr, nc);
                    }
                    case NUMBER -> {
                        neighbor.showNumber(calculateAdjacentMines(nr, nc));
                        revealedNonMineCells++;
                    }
                    default -> {
                        // Nothing
                    }
                }
            }
        }
    }

    private void checkAllMinesRevealed() {
        if (revealedMines >= totalMines) {
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.handleAllMinesRevealed(playerNum);
            handleGameEnd();
        }
    }

    private void checkBoardComplete() {
        if (revealedNonMineCells >= totalNonMineCells) {
            int playerNum = isPlayer1Board ? 1 : 2;
            System.out.println("Player " + playerNum + " completed their board!");
            gameController.setPlayerBoardComplete(playerNum, true);

            if (gameController.isGameWon()) {
                JOptionPane.showMessageDialog(this,
                        "Both players cleared their boards!\n" +
                                "Victory! Final Score: " + gameController.getSharedScore(),
                        "VICTORY!",
                        JOptionPane.INFORMATION_MESSAGE);
                handleGameEnd();
            }
        }
    }

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
                "Activate surprise cell?\n50% chance of reward or penalty!",
                "Activate Surprise?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        MultiPlayerGameController.CellActionResult result = gameController.activateSurprise();
        cell.setUsed(true);

        JOptionPane.showMessageDialog(this,
                result.message,
                result.pointsChanged > 0 ? "Good Surprise!" : "Bad Surprise!",
                result.pointsChanged > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        parentScreen.updateGameStateDisplay(result);
        repaint();

        if (gameController.isGameOver()) {
            handleGameEnd();
        }
    }

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

        int choice = JOptionPane.showConfirmDialog(this,
                "Activate question cell?",
                "Activate Question?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        cell.setUsed(true);
        showQuestionDialog(r, c);

        repaint();
    }

    private void showQuestionDialog(int r, int c) {
        String difficulty = gameController.getDifficulty();
        int diff = QuestionController.getDifficultyFromString(difficulty);

        Question q = questionController.getRandomQuestion(diff);

        String[] options = q.getAnswers();
        int selectedIndex = JOptionPane.showOptionDialog(
                this,
                q.getText(),
                "Question - " + difficulty,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selectedIndex == -1) {
            selectedIndex = -2;
        }

        boolean correct = (selectedIndex == q.getCorrectIndex());

        MultiPlayerGameController.CellActionResult result =
                gameController.activateQuestion(diff, correct);

        if (correct) {
            JOptionPane.showMessageDialog(this, "✓ Correct! " + result.message);
        } else {
            JOptionPane.showMessageDialog(this, "✗ Incorrect! " + result.message);
        }

        parentScreen.updateGameStateDisplay(result);

        if (gameController.isGameOver()) {
            handleGameEnd();
        }
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
        // You can implement a different sound for incorrect actions if you want
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
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() != CellType.MINE) {
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
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() == CellType.MINE) {
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
}

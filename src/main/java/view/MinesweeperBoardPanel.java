package view;

import control.QuestionController;
import control.SinglePlayerGameControl;
import model.CellType;
import model.Question;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MinesweeperBoardPanel extends JPanel {

    private final int rows;
    private final int cols;
    private final CellButton[][] cells;
    private int cellSize;

    private final SinglePlayerGameControl controller;
    private final QuestionController questionController;

    private boolean isFlagMode = false;
    private final Random random = new Random();

    // delayed generation
    private boolean generated = false;

    // win helpers
    private int totalMines = 0;
    private int revealedMines = 0;

    private static final String PROP_FLAG_KIND = "flagKind";   // "correct" / "wrong" / null
    private static final String PROP_COUNTED  = "counted";     // true once we gave +1 reveal points

    public MinesweeperBoardPanel(int rows, int cols,
                                 SinglePlayerGameControl controller,
                                 QuestionController questionController) {
        this.rows = rows;
        this.cols = cols;
        this.controller = controller;
        this.questionController = questionController;
        this.cells = new CellButton[rows][cols];

        switch (controller.getDifficulty()) {
            case "Easy" -> cellSize = 40;
            case "Medium" -> cellSize = 32;
            case "Hard" -> cellSize = 24;
            default -> cellSize = 24;
        }

        setLayout(new GridLayout(rows, cols, 2, 2));
        setBackground(new Color(10, 10, 15));

        initializeBoard();
        // board is generated only on first interaction (left OR right)
    }

    private void initializeBoard() {
        int boardWidth = cols * cellSize + (cols - 1) * 2;
        int boardHeight = rows * cellSize + (rows - 1) * 2;
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = new CellButton(cellSize);
                cells[i][j] = cell;
                add(cell);

                int r = i, c = j;

                cell.addActionListener(e -> handleLeftClick(r, c));
                cell.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mousePressed(java.awt.event.MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) handleRightClick(r, c);
                    }
                });
            }
        }
    }

    // =========================
    // INPUT
    // =========================
    private void handleLeftClick(int r, int c) {
        if (controller.isGameOver()) return;

        // ✅ Generate on first left-click ONLY if not in flag mode
        if (!generated && !isFlagMode && !controller.isFlagMode()) {
            generateBoardEnsuringSafeFirstClick(r, c);
            generated = true;
        }

        CellButton cell = cells[r][c];

        // flag mode
        if (isFlagMode || controller.isFlagMode()) {
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
        if (controller.isGameOver()) return;

        // ✅ generate board on FIRST interaction even if it's RIGHT click
        if (!generated) {
            generateBoardEnsuringSafeFirstClick(r, c);
            generated = true;
        }

        handleFlagPlacement(r, c);
    }

    // =========================
    // GENERATION (FIRST CLICK SAFE + EMPTY)
    // =========================
    private void generateBoardEnsuringSafeFirstClick(int safeR, int safeC) {
        // reset types
        revealedMines = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c].setCellType(CellType.EMPTY);
                cells[r][c].setNumber(0);
                cells[r][c].putClientProperty(PROP_COUNTED, null);
                cells[r][c].putClientProperty(PROP_FLAG_KIND, null);

                // reset visuals/states
                cells[r][c].setFlagged(false);
                cells[r][c].setState(CellButton.CellState.HIDDEN);
                clearFlagStyle(cells[r][c]);
            }
        }

        int minesToPlace = minesForDifficulty();
        totalMines = minesToPlace;

        // safe zone: clicked cell + neighbors
        boolean[][] forbidden = new boolean[rows][cols];
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int rr = safeR + dr;
                int cc = safeC + dc;
                if (isValidCell(rr, cc)) forbidden[rr][cc] = true;
            }
        }

        int placed = 0;
        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (forbidden[r][c]) continue;
            if (cells[r][c].getCellType() == CellType.MINE) continue;

            cells[r][c].setCellType(CellType.MINE);
            placed++;
        }

        calculateNumbers();
        placeSpecialCells();

        // guarantee first click is EMPTY
        cells[safeR][safeC].setCellType(CellType.EMPTY);
        cells[safeR][safeC].setNumber(0);
    }

    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() == CellType.MINE) continue;

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

    private void placeSpecialCells() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() == CellType.EMPTY) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        Collections.shuffle(emptyCells, random);

        int questionCellsNeeded;
        int surpriseCellsNeeded;
        switch (controller.getDifficulty()) {
            case "Easy" -> { questionCellsNeeded = 6;  surpriseCellsNeeded = 2; }
            case "Medium" -> { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }
            case "Hard" -> { questionCellsNeeded = 11; surpriseCellsNeeded = 4; }
            default -> { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }
        }

        int idx = 0;
        for (int i = 0; i < questionCellsNeeded && idx < emptyCells.size(); i++) {
            int[] p = emptyCells.get(idx++);
            cells[p[0]][p[1]].setCellType(CellType.QUESTION);
        }
        for (int i = 0; i < surpriseCellsNeeded && idx < emptyCells.size(); i++) {
            int[] p = emptyCells.get(idx++);
            cells[p[0]][p[1]].setCellType(CellType.SURPRISE);
        }
    }

    // =========================
    // FLAG (NO UNFLAG ANYMORE)
    // =========================
    private void handleFlagPlacement(int r, int c) {
        CellButton cell = cells[r][c];

        // never flag revealed/special
        if (cell.getState() == CellButton.CellState.REVEALED ||
            cell.getState() == CellButton.CellState.QUESTION ||
            cell.getState() == CellButton.CellState.SURPRISE) {
            return;
        }

        // ✅ NO UNFLAG: if already flagged -> do nothing
        if (cell.isFlagged()) {
            return;
        }

        // ADD flag
        cell.setFlagged(true);
        cell.setState(CellButton.CellState.FLAGGED);

        boolean correct = (cell.getCellType() == CellType.MINE);
        controller.onFlagPlaced(correct);

        cell.putClientProperty(PROP_FLAG_KIND, correct ? "correct" : "wrong");
        applyFlagStyle(cell);

        updateGameScreen();
        checkEndConditions();
    }

    private void applyFlagStyle(CellButton cell) {
        Object kind = cell.getClientProperty(PROP_FLAG_KIND);

        if (kind == null) {
            cell.setBorder(null);
            cell.setBackground(null);
            cell.setOpaque(false);
            return;
        }

        cell.setOpaque(true);
        cell.setBorderPainted(true);
        cell.setContentAreaFilled(true);

        if ("correct".equals(kind)) {
            Color greenBorder = new Color(0, 255, 0, 180);
            Color greenFill = new Color(0, 255, 0, 70);
            cell.setBackground(greenFill);
            cell.setBorder(new LineBorder(greenBorder, 2, true));
        } else {
            Color redBorder = new Color(255, 0, 0, 180);
            Color redFill = new Color(255, 0, 0, 70);
            cell.setBackground(redFill);
            cell.setBorder(new LineBorder(redBorder, 2, true));
        }

        cell.revalidate();
        cell.repaint();
    }

    private void clearFlagStyle(CellButton cell) {
        cell.putClientProperty(PROP_FLAG_KIND, null);
        applyFlagStyle(cell);
    }

    // =========================
    // REVEAL
    // =========================
    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;
        if (cell.isFlagged()) return;

        CellType type = cell.getCellType();
        cell.setState(CellButton.CellState.REVEALED);

        switch (type) {
            case MINE -> {
                revealedMines++;
                controller.onMineHit();
                cell.showMine();

                ModernDialog.info(
                        SwingUtilities.getWindowAncestor(this),
                        "Mine!",
                        "You hit a mine!\nLives left: " + controller.getLives(),
                        ModernDialog.Theme.WARNING
                );
            }

            case NUMBER -> {
                countRevealPointsOnce(cell);
                cell.showNumber(calculateAdjacentMines(r, c));
            }

            case EMPTY -> {
                countRevealPointsOnce(cell);
                cell.showEmpty();
                cascadeReveal(r, c);
            }

            case QUESTION -> {
                countRevealPointsOnce(cell);
                cell.showQuestion();
                cell.setState(CellButton.CellState.QUESTION);
                cascadeReveal(r, c);
            }

            case SURPRISE -> {
                countRevealPointsOnce(cell);
                cell.showSurprise();
                cell.setState(CellButton.CellState.SURPRISE);
                cascadeReveal(r, c);
            }
        }

        updateGameScreen();
        checkEndConditions();
    }

    private void countRevealPointsOnce(CellButton cell) {
        if (cell.getClientProperty(PROP_COUNTED) != null) return;
        cell.putClientProperty(PROP_COUNTED, true);
        controller.onRevealNumberOrEmpty();
    }

    private void cascadeReveal(int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int nr = r + dr, nc = c + dc;
                if (!isValidCell(nr, nc)) continue;

                CellButton n = cells[nr][nc];
                if (!n.canReveal()) continue;
                if (n.isFlagged()) continue;
                if (n.getState() == CellButton.CellState.REVEALED) continue;

                CellType t = n.getCellType();
                if (t == CellType.MINE) continue;

                n.setState(CellButton.CellState.REVEALED);

                switch (t) {
                    case EMPTY -> {
                        n.showEmpty();
                        cascadeReveal(nr, nc);
                    }
                    case NUMBER -> n.showNumber(calculateAdjacentMines(nr, nc));
                    case QUESTION -> {
                        n.showQuestion();
                        n.setState(CellButton.CellState.QUESTION);
                        cascadeReveal(nr, nc);
                    }
                    case SURPRISE -> {
                        n.showSurprise();
                        n.setState(CellButton.CellState.SURPRISE);
                        cascadeReveal(nr, nc);
                    }
                }
            }
        }
    }

    // =========================
    // SURPRISE
    // =========================
    private void activateSurpriseCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                    "Already Used", "This surprise cell has already been used.", ModernDialog.Theme.INFO);
            return;
        }

        if (controller.getPoints() < controller.getActivationCost()) {
            ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                    "Not enough points",
                    "You need " + controller.getActivationCost() + " points.",
                    ModernDialog.Theme.WARNING);
            return;
        }

        boolean ok = ModernDialog.confirm(
                SwingUtilities.getWindowAncestor(this),
                "Activate Surprise?",
                "Cost: " + controller.getActivationCost() + " points\n50% bonus or penalty.",
                ModernDialog.Theme.INFO
        );
        if (!ok) return;

        SinglePlayerGameControl.CellActionResult res = controller.activateSurpriseSingle();
        cell.setUsed(true);

        ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                "Surprise", res.message, ModernDialog.Theme.INFO);

        updateGameScreen();
        checkEndConditions();
    }

    // =========================
    // QUESTION
    // =========================
    private void activateQuestionCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                    "Already Used", "This question cell has already been used.", ModernDialog.Theme.INFO);
            return;
        }

        if (controller.getPoints() < controller.getActivationCost()) {
            ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                    "Not enough points",
                    "You need " + controller.getActivationCost() + " points.",
                    ModernDialog.Theme.WARNING);
            return;
        }

        QuestionCellDialog start = new QuestionCellDialog(new Frame());
        start.setVisible(true);
        if (!start.shouldProceed()) return;

        cell.setUsed(true);
        showQuestionDialog();
    }

    private void showQuestionDialog() {
        String diffStr = controller.getDifficulty();
        int diff = QuestionController.getDifficultyFromString(diffStr);

        Question q = questionController.getRandomQuestion(diff);
        if (q == null) {
            ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                    "No Questions", "No questions available.", ModernDialog.Theme.WARNING);
            return;
        }

        QuestionTimeDialog qd = new QuestionTimeDialog(
                new Frame(),
                diffStr,
                q.getText(),
                q.getAnswers(),
                selectedIndex -> {
                    boolean correct = (selectedIndex == q.getCorrectIndex());
                    int qDifficulty = q.getDifficulty();

                    SinglePlayerGameControl.CellActionResult res =
                            controller.activateQuestionSingle(qDifficulty, correct);

                    ModernDialog.info(SwingUtilities.getWindowAncestor(this),
                            correct ? "Correct!" : "Wrong!",
                            res.message,
                            correct ? ModernDialog.Theme.SUCCESS : ModernDialog.Theme.WARNING);

                    updateGameScreen();
                    checkEndConditions();
                }
        );

        qd.setVisible(true);
    }

    // =========================
    // END
    // =========================
    private void checkEndConditions() {
        if (controller.getLives() <= 0) {
            controller.endGame(false);
            fireEndDialog();
            disableAll();
            return;
        }

        int correctFlags = countCorrectFlags();
        if (revealedMines + correctFlags >= totalMines) {
            controller.endGame(true);
            fireEndDialog();
            disableAll();
        }
    }

    private void fireEndDialog() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof GameScreenSinglePlayer)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((GameScreenSinglePlayer) parent).forceShowEndDialog();
        }
    }

    private void disableAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) cells[r][c].setEnabled(false);
        }
    }

    private int countCorrectFlags() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isFlagged() && cells[r][c].getCellType() == CellType.MINE) count++;
            }
        }
        return count;
    }

    // =========================
    // HELPERS
    // =========================
    private int calculateAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (isValidCell(nr, nc) && cells[nr][nc].getCellType() == CellType.MINE) count++;
            }
        }
        return count;
    }

    private boolean isValidCell(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void updateGameScreen() {
        SwingUtilities.invokeLater(() -> {
            Container parent = getParent();
            while (parent != null && !(parent instanceof GameScreenSinglePlayer)) {
                parent = parent.getParent();
            }
            if (parent != null) ((GameScreenSinglePlayer) parent).updateStatsDisplay();
        });
    }

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    // =========================================================
    // ✅ MISSING METHODS (Fix your compilation errors)
    // =========================================================

    // Mine count for difficulty (used before generation too)
    private int minesForDifficulty() {
        return switch (controller.getDifficulty()) {
            case "Easy" -> 10;
            case "Medium" -> 26;
            case "Hard" -> 44;
            default -> 26;
        };
    }

    // Used by GameScreenSinglePlayer sidebar
    public int getTotalMines() {
        // If board not generated yet, still show correct total (e.g., 10/10)
        return (totalMines > 0) ? totalMines : minesForDifficulty();
    }

    // Mines left = total mines - correct flags (NOT revealed mines!)
    public int getMinesLeftCalculated() {
        int left = getTotalMines() - countCorrectFlags();
        return Math.max(left, 0);
    }
}

package view;

import control.CompetitiveCellActionFactory;
import control.CompetitiveCellActionTemplate;
import control.CompetitiveGameController;
import control.QuestionController;
import model.CellType;
import model.Question;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

public class MinesweeperBoardPanelCompetitive extends JPanel {

    private final int rows;
    private final int cols;
    private final CellButton[][] cells;

    // ✅ UI Responsive settings
    private int cellSize;
    private final int gap = 2;
    private final int MIN_CELL = 18;
    private final int MAX_CELL = 60;

    private final CompetitiveGameController gameController;
    private final QuestionController questionController;
    private final GameScreenCompetitive parentScreen;
    private final boolean isPlayer1Board;

    private int totalMines;
    private int revealedMines = 0;
    private int totalNonMineCells;
    private int revealedNonMineCells = 0;

    private boolean isFlagMode = false;
    private final Random random = new Random();

    private int firstClickRow = -1;
    private int firstClickCol = -1;
    private boolean boardGenerated = false;
    private boolean endUiShown = false;

    // =========================
    // HOT/COLD HINT
    // =========================
    private final Map<CellButton, Color> hintOverlay = new HashMap<>();

    public MinesweeperBoardPanelCompetitive(int rows, int cols,
                                           CompetitiveGameController gameController,
                                           QuestionController questionController,
                                           GameScreenCompetitive parentScreen,
                                           boolean isPlayer1Board) {
        this.rows = rows;
        this.cols = cols;
        this.gameController = gameController;
        this.questionController = questionController;
        this.parentScreen = parentScreen;
        this.isPlayer1Board = isPlayer1Board;
        this.cells = new CellButton[rows][cols];

        // ✅ Base size by difficulty
        switch (gameController.getDifficulty()) {
            case "Easy":   cellSize = 45; break;
            case "Medium": cellSize = 38; break;
            case "Hard":   cellSize = 30; break;
            default:       cellSize = 28; break;
        }

        setLayout(new GridLayout(rows, cols, gap, gap));
        setBackground(new Color(10, 10, 15));
        setOpaque(true);

        initializeBoard();

        // ✅ RESPONSIVE: resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshResponsive();
            }
        });

        SwingUtilities.invokeLater(this::refreshResponsive);
    }

    // =========================
    // RESPONSIVE
    // =========================
    public void refreshResponsive() {
        updateCellSizeFromPanel();
        resizeAllCells();
        revalidate();
        repaint();
    }

    private void updateCellSizeFromPanel() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int usableW = w - (cols - 1) * gap;
        int usableH = h - (rows - 1) * gap;

        int byW = usableW / cols;
        int byH = usableH / rows;

        int newSize = Math.min(byW, byH);
        newSize = Math.max(MIN_CELL, Math.min(MAX_CELL, newSize));

        cellSize = newSize;
    }

    private void resizeAllCells() {
        Dimension d = new Dimension(cellSize, cellSize);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = cells[i][j];
                if (cell == null) continue;
                cell.setPreferredSize(d);
                cell.setMinimumSize(d);
                cell.setMaximumSize(d);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int w = cols * cellSize + (cols - 1) * gap;
        int h = rows * cellSize + (rows - 1) * gap;
        return new Dimension(w, h);
    }

    // =========================
    // END FLOW (ONLY ONCE)
    // =========================
    private void showEndOnce() {
        if (endUiShown) return;
        endUiShown = true;
        parentScreen.onGameEnded();
    }

    // =========================
    // BOARD INIT / GENERATION
    // =========================
    private void initializeBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = new CellButton(cellSize);

                // ✅ allow CellButton to find its board for hint overlay painting
                cell.putClientProperty("boardRef", this);

                cells[i][j] = cell;
                add(cell);

                final int r = i, c = j;

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

    private void generateBoardWithSafeFirstCell(int firstRow, int firstCol) {
        if (boardGenerated) return;

        firstClickRow = firstRow;
        firstClickCol = firstCol;

        placeMines();
        calculateNumbers();
        placeSpecialCells();

        boardGenerated = true;

        int playerNum = isPlayer1Board ? 1 : 2;
        gameController.setPlayerTotalMines(playerNum, totalMines);
    }

    private void placeMines() {
        int minesToPlace = calculateMineCount();
        int placed = 0;

        List<String> forbiddenPositions = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = firstClickRow + dr;
                int nc = firstClickCol + dc;
                if (isValidCell(nr, nc)) forbiddenPositions.add(nr + "," + nc);
            }
        }

        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            String pos = r + "," + c;

            if (!forbiddenPositions.contains(pos) && cells[r][c].getCellType() != CellType.MINE) {
                cells[r][c].setCellType(CellType.MINE);
                placed++;
            }
        }

        totalMines = minesToPlace;
    }

    private int calculateMineCount() {
        String d = gameController.getDifficulty();
        if ("Easy".equals(d)) return 10;
        if ("Medium".equals(d)) return 26;
        if ("Hard".equals(d)) return 44;
        return 26;
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
                if (cells[r][c].getCellType() == CellType.EMPTY) emptyCells.add(new int[]{r, c});
            }
        }

        Collections.shuffle(emptyCells);

        int questionCellsNeeded;
        int surpriseCellsNeeded;
        String d = gameController.getDifficulty();

        if ("Easy".equals(d)) { questionCellsNeeded = 6; surpriseCellsNeeded = 2; }
        else if ("Medium".equals(d)) { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }
        else if ("Hard".equals(d)) { questionCellsNeeded = 11; surpriseCellsNeeded = 4; }
        else { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }

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

    // =========================
    // INPUT HANDLING
    // =========================
    private void handleCellClick(int r, int c) {
        if (gameController.isGameOver()) return;

        int turnPlayer = gameController.getCurrentPlayer();
        boolean isTurnPlayer1 = (turnPlayer == 1);

        if ((isPlayer1Board && !isTurnPlayer1) || (!isPlayer1Board && isTurnPlayer1)) {
            ModernDialog.info(parentScreen.getFrame(),
                    "Not your turn",
                    "It's Player " + turnPlayer + "'s turn.",
                    ModernDialog.Theme.INFO);
            return;
        }

        if (!boardGenerated) {
            generateBoardWithSafeFirstCell(r, c);
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.markFirstMoveDone(playerNum);
            gameController.markGameStarted();
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
        if (gameController.isGameOver()) return;

        int turnPlayer = gameController.getCurrentPlayer();
        boolean isTurnPlayer1 = (turnPlayer == 1);

        if ((isPlayer1Board && !isTurnPlayer1) || (!isPlayer1Board && isTurnPlayer1)) return;

        if (!boardGenerated) {
            generateBoardWithSafeFirstCell(r, c);
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.markFirstMoveDone(playerNum);
            gameController.markGameStarted();
        }

        handleFlagPlacement(r, c);
    }

    // =========================
    // FLAG
    // =========================
    private void handleFlagPlacement(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.getState() != CellButton.CellState.HIDDEN &&
            cell.getState() != CellButton.CellState.FLAGGED) {
            return;
        }

        // remove
        if (cell.isFlagged()) {
            cell.setFlagged(false);
            cell.setState(CellButton.CellState.HIDDEN);
            repaint();
            return;
        }

        // add
        cell.setFlagged(true);
        cell.setState(CellButton.CellState.FLAGGED);

        CellType actualType = cell.getCellType();
        boolean correct = (actualType == CellType.MINE);
        int playerNum = isPlayer1Board ? 1 : 2;

        CompetitiveCellActionTemplate action =
                CompetitiveCellActionFactory.createForFlag(gameController, playerNum, correct);

        CompetitiveGameController.CellActionResult result = action.execute();

        if (correct) {
            cell.showCorrectFlagFeedback();
            AudioManager.play(AudioManager.Sfx.FLAG_RIGHT);

            cell.setBorder(new LineBorder(new Color(0, 255, 0, 150)));
            cell.setBackground(new Color(0, 255, 0, 150));
            cell.setPermanentBorderColor(new Color(0, 255, 0, 150));
        } else {
            cell.showIncorrectFlagFeedback();
            AudioManager.play(AudioManager.Sfx.BAD_SURPRISE);

            cell.setBorder(new LineBorder(new Color(255, 0, 0, 150)));
            cell.setBackground(new Color(255, 0, 0, 150));
            cell.setPermanentBorderColor(new Color(255, 0, 0, 150));
        }

        parentScreen.updateHUD();
        repaint();

        checkBoardComplete();

        if (result != null && result.turnEnded) parentScreen.updateActivePlayer();
        if (result != null && result.gameOver) showEndOnce();
    }

    // =========================
    // REVEAL
    // =========================
    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;

        CellType type = cell.getCellType();
        int playerNum = isPlayer1Board ? 1 : 2;

        CompetitiveCellActionTemplate action =
                CompetitiveCellActionFactory.createForReveal(gameController, playerNum, type);

        CompetitiveGameController.CellActionResult result = action.execute();

        switch (type) {
            case MINE -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showMine();
                AudioManager.play(AudioManager.Sfx.BOOM);

                revealedMines++;
                parentScreen.updateHUD();

                if (result != null && result.turnEnded) parentScreen.updateActivePlayer();

                if (!gameController.isGameOver()) {
                    ModernDialog.info(parentScreen.getFrame(),
                            "💣 Mine hit!",
                            (result != null ? result.message : "Mine hit!") +
                                    "\nLives left: " + gameController.getLives(playerNum) +
                                    "\nTurn ended!",
                            ModernDialog.Theme.DANGER);
                }
            }

            case NUMBER -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showNumber(calculateAdjacentMines(r, c));
                revealedNonMineCells++;

                parentScreen.updateHUD();
                if (result != null && result.turnEnded) parentScreen.updateActivePlayer();
            }

            case EMPTY -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showEmpty();
                revealedNonMineCells++;

                parentScreen.updateHUD();
                cascadeReveal(r, c);

                if (result != null && result.turnEnded) parentScreen.updateActivePlayer();
            }

            case QUESTION -> {
                cell.showQuestion();
                cell.setState(CellButton.CellState.QUESTION);
                revealedNonMineCells++;
                parentScreen.updateHUD();

                cascadeReveal(r, c);

                // ✅ FIXED: זהה למצב הרגיל - סיום תור רק אם result אומר כך
                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }

            case SURPRISE -> {
                cell.showSurprise();
                cell.setState(CellButton.CellState.SURPRISE);
                revealedNonMineCells++;
                parentScreen.updateHUD();

                cascadeReveal(r, c);

                // ✅ FIXED: זהה למצב הרגיל - סיום תור רק אם result אומר כך
                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }
        }
        checkBoardComplete();
        repaint();

        if (result != null && result.gameOver) showEndOnce();
        if (gameController.isGameOver()) showEndOnce();
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
                if (neighbor.isFlagged()) continue;
                if (neighbor.getState() == CellButton.CellState.REVEALED) continue;

                CellType t = neighbor.getCellType();

                switch (t) {
                    case EMPTY -> {
                        neighbor.setState(CellButton.CellState.REVEALED);
                        neighbor.showEmpty();
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case NUMBER -> {
                        neighbor.setState(CellButton.CellState.REVEALED);
                        neighbor.showNumber(calculateAdjacentMines(nr, nc));
                        revealedNonMineCells++;
                    }
                    case QUESTION -> {
                        neighbor.showQuestion();
                        neighbor.setState(CellButton.CellState.QUESTION);
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case SURPRISE -> {
                        neighbor.showSurprise();
                        neighbor.setState(CellButton.CellState.SURPRISE);
                        revealedNonMineCells++;
                        cascadeReveal(nr, nc);
                    }
                    case MINE -> { /* no auto reveal */ }
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
            ModernDialog.info(parentScreen.getFrame(),
                    "Already Used",
                    "This surprise cell has already been used.",
                    ModernDialog.Theme.INFO);
            return;
        }

        int playerNum = isPlayer1Board ? 1 : 2;
        int cost = gameController.getActivationCost();

        if (gameController.getScore(playerNum) < cost) {
            ModernDialog.info(parentScreen.getFrame(),
                    "Not enough points",
                    "Need " + cost + " points to activate Surprise.",
                    ModernDialog.Theme.WARNING);
            return;
        }

        boolean ok = ModernDialog.confirm(parentScreen.getFrame(),
                "Activate Surprise?",
                "Costs " + cost + " points.\n50% bonus / 50% penalty.\nContinue?",
                ModernDialog.Theme.WARNING);

        if (!ok) return;

        CompetitiveGameController.CellActionResult result = gameController.activateSurprise(playerNum);
        cell.setUsed(true);

        if (result != null && result.scoreChanged > 0) {
            AudioManager.play(AudioManager.Sfx.GOOD_SURPRISE);
            new SurpriseBonusDialog(parentScreen.getFrame(), gameController.getDifficulty()).setVisible(true);
        } else {
            AudioManager.play(AudioManager.Sfx.BAD_SURPRISE);
            new SurprisePenaltyDialog(parentScreen.getFrame(), gameController.getDifficulty()).setVisible(true);
        }

        parentScreen.updateHUD();
        if (result != null && result.turnEnded) {
            parentScreen.updateActivePlayer();
        }

        repaint();

        if (result != null && result.gameOver) showEndOnce();
        if (gameController.isGameOver()) showEndOnce();
    }

    // =========================
    // QUESTION
    // =========================
    private void activateQuestionCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            ModernDialog.info(parentScreen.getFrame(),
                    "Already Used",
                    "This question cell has already been used.",
                    ModernDialog.Theme.INFO);
            return;
        }

        int playerNum = isPlayer1Board ? 1 : 2;
        int cost = gameController.getActivationCost();

        if (gameController.getScore(playerNum) < cost) {
            ModernDialog.info(parentScreen.getFrame(),
                    "Not enough points",
                    "Need " + cost + " points to activate Question.",
                    ModernDialog.Theme.WARNING);
            return;
        }

        QuestionCellDialog cellDialog = new QuestionCellDialog(parentScreen.getFrame());
        cellDialog.setVisible(true);
        if (!cellDialog.shouldProceed()) return;

        AudioManager.play(AudioManager.Sfx.QUESTION_OPEN);
        cell.setUsed(true);

        Question q = questionController.getRandomQuestion(-1);
        if (q == null) {
            ModernDialog.info(parentScreen.getFrame(),
                    "Error",
                    "No questions available!",
                    ModernDialog.Theme.DANGER);
            return;
        }

        String difficultyText = QuestionController.difficultyToString(q.getDifficulty());

        QuestionTimeDialog qd = new QuestionTimeDialog(
                parentScreen.getFrame(),
                difficultyText,
                q.getText(),
                q.getAnswers(),
                selectedIndex -> {
                    // ✅ אם המשתמש סגר / דילג -> נגמר התור
                    if (selectedIndex == null) {
                        ModernDialog.info(parentScreen.getFrame(),
                                "Skipped",
                                "Question skipped - Turn ended.",
                                ModernDialog.Theme.INFO);
                        parentScreen.updateHUD();
                        parentScreen.updateActivePlayer();
                        return;
                    }

                    boolean correct = (selectedIndex == q.getCorrectIndex());

                    CompetitiveGameController.CellActionResult result =
                            gameController.activateQuestion(playerNum, q.getDifficulty(), correct);

                    if (correct) {
                        AudioManager.play(AudioManager.Sfx.ANSWER_RIGHT);
                        new Timer(1500, e -> AudioManager.stop(AudioManager.Sfx.ANSWER_RIGHT)).start();

                        ModernDialog.info(parentScreen.getFrame(),
                                "Correct ✅",
                                "✓ Correct! " + (result != null ? result.message : ""),
                                ModernDialog.Theme.SUCCESS);
                    } else {
                        AudioManager.play(AudioManager.Sfx.ANSWER_WRONG);

                        ModernDialog.info(parentScreen.getFrame(),
                                "Wrong ❌",
                                "✗ Incorrect! " + (result != null ? result.message : ""),
                                ModernDialog.Theme.DANGER);
                    }

                    parentScreen.updateHUD();
                    repaint();

                    if (result != null && result.gameOver) showEndOnce();
                    if (gameController.isGameOver()) showEndOnce();
                }
        );

        qd.setVisible(true);
    }

    // =========================
    // BOARD COMPLETE
    // =========================
    private void checkBoardComplete() {
        if (gameController.isGameOver() || !boardGenerated) return;

        int hiddenCells = 0;
        int correctlyFlaggedMines = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];

                boolean revealed = (cb.getState() == CellButton.CellState.REVEALED);
                boolean flagged = cb.isFlagged();

                if (!revealed && !flagged) hiddenCells++;

                if (flagged && cb.getCellType() == CellType.MINE) correctlyFlaggedMines++;
            }
        }

        boolean boardFinished = (hiddenCells == 0) || (revealedMines + correctlyFlaggedMines >= totalMines);
        if (!boardFinished) return;

        int playerNum = isPlayer1Board ? 1 : 2;
        gameController.setPlayerBoardComplete(playerNum, true);

        ModernDialog.info(parentScreen.getFrame(),
                "Board Completed",
                "Player " + playerNum + " finished their board!",
                ModernDialog.Theme.SUCCESS);

        if (gameController.isGameOver()) showEndOnce();
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

    // =========================
    // HOT/COLD HINT API
    // =========================
    public void showHotColdHint(int radius) {
        if (!boardGenerated || gameController.isGameOver()) return;

        Point mine = pickRandomHiddenMine();
        if (mine == null) {
            ModernDialog.info(parentScreen.getFrame(),
                    "No Mines Available",
                    "No hidden mines found to hint!",
                    ModernDialog.Theme.INFO);
            return;
        }

        hintOverlay.clear();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];

                if (cb.getState() != CellButton.CellState.HIDDEN) continue;
                if (cb.isFlagged()) continue;

                int dist = Math.abs(r - mine.x) + Math.abs(c - mine.y);

                if (radius >= 1 && dist == 1) {
                    hintOverlay.put(cb, new Color(255, 60, 60, 140));
                } else if (radius >= 2 && dist == 2) {
                    hintOverlay.put(cb, new Color(255, 180, 0, 120));
                } else if (radius >= 3 && dist == 3) {
                    hintOverlay.put(cb, new Color(0, 180, 255, 110));
                }
            }
        }

        repaint();

        Timer t = new Timer(2500, e -> {
            hintOverlay.clear();
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    public Color getHintOverlayFor(CellButton cb) {
        return hintOverlay.get(cb);
    }

    private Point pickRandomHiddenMine() {
        List<Point> mines = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                if (cb.getCellType() == CellType.MINE &&
                        cb.getState() == CellButton.CellState.HIDDEN &&
                        !cb.isFlagged()) {
                    mines.add(new Point(r, c));
                }
            }
        }
        return mines.isEmpty() ? null : mines.get(random.nextInt(mines.size()));
    }

    // =========================
    // API
    // =========================
    public void setFlagMode(boolean flagMode) { this.isFlagMode = flagMode; }
    public int getCellSize() { return cellSize; }

    public CellButton getCell(int row, int col) {
        return isValidCell(row, col) ? cells[row][col] : null;
    }

    public int getCorrectFlagsCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].isFlagged() && cells[r][c].getCellType() == CellType.MINE) count++;
        return count;
    }

    public int getIncorrectFlagsCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].isFlagged() && cells[r][c].getCellType() != CellType.MINE) count++;
        return count;
    }

    public int getUsedQuestionsCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].getCellType() == CellType.QUESTION && cells[r][c].isUsed()) count++;
        return count;
    }

    public int getTotalQuestionsCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].getCellType() == CellType.QUESTION) count++;
        return count;
    }

    public int getUsedSurprisesCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].getCellType() == CellType.SURPRISE && cells[r][c].isUsed()) count++;
        return count;
    }

    public int getTotalSurprisesCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (cells[r][c].getCellType() == CellType.SURPRISE) count++;
        return count;
    }

    public int getRevealedMinesCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                if (cb.getCellType() != CellType.MINE) continue;

                if (cb.isFlagged()) { count++; continue; }

                CellButton.CellState st = cb.getState();
                if (st == CellButton.CellState.REVEALED || st == CellButton.CellState.MINE) count++;
            }
        }
        return count;
    }

    public int getTotalMines() { return totalMines; }

    public void revealAllCellsForEnd(boolean forceGenerateIfNeeded, boolean playEndSfx) {
        if (forceGenerateIfNeeded && !boardGenerated) generateBoardWithSafeFirstCell(0, 0);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cell = cells[r][c];
                cell.setEnabled(false);

                if (!boardGenerated) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;

                switch (cell.getCellType()) {
                    case MINE -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showMine();
                        if (playEndSfx) AudioManager.play(AudioManager.Sfx.BOOM);
                    }
                    case NUMBER -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showNumber(calculateAdjacentMines(r, c));
                    }
                    case EMPTY -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showEmpty();
                    }
                    case QUESTION -> {
                        cell.showQuestion();
                        cell.setState(CellButton.CellState.QUESTION);
                    }
                    case SURPRISE -> {
                        cell.showSurprise();
                        cell.setState(CellButton.CellState.SURPRISE);
                    }
                }
            }
        }
        repaint();
    }
}
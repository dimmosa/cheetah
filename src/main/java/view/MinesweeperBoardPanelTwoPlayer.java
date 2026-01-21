package view;

import control.QuestionController;
import view.AudioManager;

import control.CellActionFactory;
import control.CellActionTemplate;
import control.MultiPlayerGameController;
import model.Question;
import model.CellType;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    private final Random random = new Random();

    private int firstClickRow = -1;
    private int firstClickCol = -1;
    private boolean boardGenerated = false;

    private boolean endUiShown = false;

    // =========================
    // HOT/COLD HINT (NEW)
    // =========================
    private final Map<CellButton, Color> hintOverlay = new HashMap<>();

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
            case "Easy" -> cellSize = 40;
            case "Medium" -> cellSize = 35;
            case "Hard" -> cellSize = 26;
            default -> cellSize = 22;
        }

        setLayout(new GridLayout(rows, cols, 2, 2));
        setBackground(new Color(10, 10, 15));
        initializeBoard();
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
        System.out.println("Initializing two-player board: " + gameController.getDifficulty() +
                " - Player " + (isPlayer1Board ? "1" : "2"));

        int boardWidth = cols * cellSize + (cols - 1) * 2;
        int boardHeight = rows * cellSize + (rows - 1) * 2;
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = new CellButton(cellSize);

                // ✅ allow CellButton to find its board for hint overlay painting
                cell.putClientProperty("boardRef", this);

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

        int playerNum = isPlayer1Board ? 1 : 2;
        gameController.setPlayerTotalMines(playerNum, totalMines);

        System.out.println("Board generated: " + totalMines + " mines, " +
                totalNonMineCells + " non-mine cells");
    }

    private void placeMines() {
        int minesToPlace = calculateMineCount();
        int placed = 0;

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
        System.out.println("Placed exactly " + placed + " mines on " +
                (isPlayer1Board ? "Player 1" : "Player 2") + " board");
    }

    private int calculateMineCount() {
        return switch (gameController.getDifficulty()) {
            case "Easy" -> 10;
            case "Medium" -> 26;
            case "Hard" -> 44;
            default -> 26;
        };
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
            case "Easy" -> { questionCellsNeeded = 6;  surpriseCellsNeeded = 2; }
            case "Medium" -> { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }
            case "Hard" -> { questionCellsNeeded = 11; surpriseCellsNeeded = 4; }
            default -> { questionCellsNeeded = 7; surpriseCellsNeeded = 3; }
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

    // =========================
    // INPUT HANDLING
    // =========================
    private void handleCellClick(int r, int c) {
        if (gameController.isGameOver()) return;

        boolean isPlayer1Turn = gameController.getCurrentPlayer() == 1;
        if ((isPlayer1Board && !isPlayer1Turn) || (!isPlayer1Board && isPlayer1Turn)) {
            JOptionPane.showMessageDialog(this,
                    "It's not your turn! Current turn: Player " + gameController.getCurrentPlayer(),
                    "Wait Your Turn", JOptionPane.INFORMATION_MESSAGE);
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

        boolean isPlayer1Turn = gameController.getCurrentPlayer() == 1;
        if ((isPlayer1Board && !isPlayer1Turn) || (!isPlayer1Board && isPlayer1Turn)) {
            return;
        }

        if (!boardGenerated) {
            generateBoardWithSafeFirstCell(r, c);
            int playerNum = isPlayer1Board ? 1 : 2;
            gameController.markFirstMoveDone(playerNum);
            gameController.markGameStarted();
        }

        handleFlagPlacement(r, c);
    }

    // =========================
    // FLAG (Factory + Template)
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

        // ✅ Action via Factory
        CellActionTemplate action =
                CellActionFactory.createForFlag(gameController, correct);

        MultiPlayerGameController.CellActionResult result = action.execute();

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

        parentScreen.updateGameStateDisplay(result);

        repaint();
        checkBoardComplete();
    }

    // =========================
    // REVEAL (Factory + Template)
    // =========================
    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;

        CellType type = cell.getCellType();

        // ✅ Action via Factory
        CellActionTemplate action =
                CellActionFactory.createForReveal(gameController, type);

        MultiPlayerGameController.CellActionResult result = action.execute();

        switch (type) {
            case MINE -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showMine();
                AudioManager.play(AudioManager.Sfx.BOOM);

                revealedMines++;

                parentScreen.updateGameStateDisplay(result);
                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }

                checkAllMinesRevealed();

                if (!gameController.isGameOver()) {
                    JOptionPane.showMessageDialog(this,
                            "💣 Mine hit! Lives left: " + gameController.getSharedLives() +
                                    "\nTurn ended!",
                            "Mine Revealed",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

            case NUMBER -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showNumber(calculateAdjacentMines(r, c));
                revealedNonMineCells++;

                parentScreen.updateGameStateDisplay(result);
                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }

            case EMPTY -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showEmpty();
                revealedNonMineCells++;

                parentScreen.updateGameStateDisplay(result);

                cascadeReveal(r, c);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }

            case QUESTION -> {
                cell.showQuestion();
                cell.setState(CellButton.CellState.QUESTION);
                revealedNonMineCells++;

                parentScreen.updateGameStateDisplay(result);

                cascadeReveal(r, c);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }

            case SURPRISE -> {
                cell.showSurprise();
                cell.setState(CellButton.CellState.SURPRISE);
                revealedNonMineCells++;

                parentScreen.updateGameStateDisplay(result);

                cascadeReveal(r, c);

                if (result != null && result.turnEnded) {
                    parentScreen.updateActivePlayer();
                }
            }
        }

        checkBoardComplete();
        repaint();

        if (gameController.isGameOver()) {
            showEndOnce();
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

                if (neighbor.isFlagged()) continue;
                if (neighbor.getState() == CellButton.CellState.REVEALED) continue;

                CellType type = neighbor.getCellType();

                switch (type) {
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
                    case MINE -> { }
                }
            }
        }
    }

    // =========================
    // WIN CHECKS
    // =========================
    private void checkAllMinesRevealed() {
        if (revealedMines >= totalMines) {
            int playerNum = isPlayer1Board ? 1 : 2;
            System.out.println("Player " + playerNum + " handled ALL mines! WIN!");

            gameController.handleAllMinesRevealed(playerNum);

            JOptionPane.showMessageDialog(this,
                    "All mines handled!\nCo-op Victory!\nFinal Score: " + gameController.getSharedScore(),
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);

            showEndOnce();
        }
    }

    private void checkBoardComplete() {
        if (gameController.isGameOver() || !boardGenerated) return;

        int hiddenCells = 0;
        int correctlyFlaggedMines = 0;
        int wrongFlags = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cb = cells[r][c];
                CellType type = cb.getCellType();

                boolean revealed = (cb.getState() == CellButton.CellState.REVEALED);
                boolean flagged = cb.isFlagged();

                if (!revealed && !flagged) hiddenCells++;

                if (flagged) {
                    if (type == CellType.MINE) correctlyFlaggedMines++;
                    else wrongFlags++;
                }
            }
        }

        int playerNum = isPlayer1Board ? 1 : 2;

        boolean gameEnds = false;
        boolean playerWins = false;

        if (hiddenCells == 0) {
            gameEnds = true;
            playerWins = (gameController.getSharedLives() > 0);
        } else if (revealedMines + correctlyFlaggedMines >= totalMines) {
            gameEnds = true;
            playerWins = (gameController.getSharedLives() > 0);
        }

        if (!gameEnds) return;

        if (playerWins) {
            gameController.setPlayerBoardComplete(playerNum, true);
            JOptionPane.showMessageDialog(this,
                    "All mines correctly handled!\nCo-op Victory!\nFinal Score: " + gameController.getSharedScore(),
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            gameController.giveUp();
            JOptionPane.showMessageDialog(this,
                    "Wrong flags detected: " + wrongFlags + "\nGame Over - You Lose!",
                    "Wrong Flags",
                    JOptionPane.ERROR_MESSAGE);
        }

        showEndOnce();
    }

    // =========================
    // SURPRISE
    // =========================
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
                    "Not enough points to activate surprise!\nNeed " + activationCost + " points.",
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
            AudioManager.play(AudioManager.Sfx.GOOD_SURPRISE);
            SurpriseBonusDialog dialog = new SurpriseBonusDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        } else {
            AudioManager.play(AudioManager.Sfx.BAD_SURPRISE);
            SurprisePenaltyDialog dialog = new SurprisePenaltyDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        }

        parentScreen.updateGameStateDisplay(result);
        repaint();

        if (gameController.isGameOver()) showEndOnce();
    }

    // =========================
    // QUESTION
    // =========================
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
                    "Not enough points to activate question!\nNeed " + activationCost + " points.",
                    "Insufficient Points",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        QuestionCellDialog cellDialog = new QuestionCellDialog(new Frame());
        cellDialog.setVisible(true);

        if (!cellDialog.shouldProceed()) return;

        // ✅ Question open sfx
        AudioManager.play(AudioManager.Sfx.QUESTION_OPEN);

        cell.setUsed(true);
        showQuestionDialog(r, c);

        repaint();
    }

    private void showQuestionDialog(int r, int c) {
        Question q = questionController.getRandomQuestion(-1);

        if (q == null) {
            JOptionPane.showMessageDialog(this,
                    "No questions available!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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

                    if ("Easy".equals(gameController.getDifficulty()) && correct) {
                        if (gameController.shouldRevealMineBonus(q.getDifficulty(), true)) {
                            revealRandomMineBonus();
                        }
                        if (gameController.shouldTrigger3x3Reveal(q.getDifficulty(), true)) {
                            revealRandom3x3Bonus();
                        }
                        checkBoardComplete();
                    }

                    if (correct) {
                    	AudioManager.play(AudioManager.Sfx.ANSWER_RIGHT);

                    	new javax.swing.Timer(1500, e -> {
                    	    AudioManager.stop(AudioManager.Sfx.ANSWER_RIGHT);
                    	}).start();

                        JOptionPane.showMessageDialog(this,
                                "✓ Correct! " + result.message,
                                "Correct Answer",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        AudioManager.play(AudioManager.Sfx.ANSWER_WRONG);
                        JOptionPane.showMessageDialog(this,
                                "✗ Incorrect! " + result.message,
                                "Wrong Answer",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    parentScreen.updateGameStateDisplay(result);

                    if (gameController.isGameOver()) showEndOnce();
                }
        );

        qd.setVisible(true);
    }

    // =========================
    // BONUSES (keep yours)
    // =========================
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

        if (candidates.isEmpty()) return;

        Collections.shuffle(candidates, random);
        int[] pos = candidates.get(0);
        CellButton cell = cells[pos[0]][pos[1]];

        cell.setState(CellButton.CellState.REVEALED);
        cell.showMine();

        // ✅ don't spam BOOM if you want, but here it's a “mine reveal bonus” so ok
        AudioManager.play(AudioManager.Sfx.BOOM);

        revealedMines++;

        flashBonusCell(cell);
        checkBoardComplete();
    }

    private void revealRandom3x3Bonus() {
        final int TARGET = 9;

        if (rows < 3 || cols < 3) {
            List<String> log = new ArrayList<>();
            topUpRevealAnyCellsOpenAllTypes(TARGET, log);
            checkBoardComplete();
            repaint();
            return;
        }

        List<int[]> blocks = new ArrayList<>();
        int best = 0;

        for (int tr = 0; tr <= rows - 3; tr++) {
            for (int tc = 0; tc <= cols - 3; tc++) {
                int eligible = countEligibleAllTypesIn3x3(tr, tc);
                if (eligible > 0) {
                    blocks.add(new int[]{tr, tc, eligible});
                    best = Math.max(best, eligible);
                }
            }
        }

        if (blocks.isEmpty()) {
            List<String> log = new ArrayList<>();
            topUpRevealAnyCellsOpenAllTypes(TARGET, log);
            checkBoardComplete();
            repaint();
            return;
        }

        List<int[]> bestBlocks = new ArrayList<>();
        for (int[] b : blocks) if (b[2] == best) bestBlocks.add(b);

        Collections.shuffle(bestBlocks, random);
        int tr = bestBlocks.get(0)[0];
        int tc = bestBlocks.get(0)[1];

        int inside = 0;
        List<String> revealedCellsLog = new ArrayList<>();

        for (int r = tr; r < tr + 3 && inside < TARGET; r++) {
            for (int c = tc; c < tc + 3 && inside < TARGET; c++) {
                CellButton cell = cells[r][c];
                if (cell.isFlagged()) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;
                if (!cell.canReveal()) continue;

                CellType t = cell.getCellType();

                switch (t) {
                    case MINE -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showMine();
                        // 🔇 IMPORTANT: don't play BOOM for every mine in bulk reveal
                        revealedMines++;
                    }
                    case NUMBER -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showNumber(calculateAdjacentMines(r, c));
                        revealedNonMineCells++;
                    }
                    case EMPTY -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showEmpty();
                        revealedNonMineCells++;
                    }
                    case QUESTION -> {
                        cell.showQuestion();
                        cell.setState(CellButton.CellState.QUESTION);
                        revealedNonMineCells++;
                    }
                    case SURPRISE -> {
                        cell.showSurprise();
                        cell.setState(CellButton.CellState.SURPRISE);
                        revealedNonMineCells++;
                    }
                }

                flashBonusCell(cell);
                revealedCellsLog.add("(" + r + "," + c + ":" + t + ")");
                inside++;
            }
        }

        if (inside < TARGET) {
            int need = TARGET - inside;
            topUpRevealAnyCellsOpenAllTypes(need, revealedCellsLog);
        }

        checkBoardComplete();
        repaint();
    }

    private int countEligibleAllTypesIn3x3(int tr, int tc) {
        int count = 0;
        for (int r = tr; r < tr + 3; r++) {
            for (int c = tc; c < tc + 3; c++) {
                CellButton cell = cells[r][c];
                if (cell.isFlagged()) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;
                if (!cell.canReveal()) continue;
                count++;
            }
        }
        return count;
    }

    private int topUpRevealAnyCellsOpenAllTypes(int need, List<String> revealedCellsLog) {
        if (need <= 0) return 0;

        List<int[]> pool = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cell = cells[r][c];
                if (cell.isFlagged()) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;
                if (!cell.canReveal()) continue;
                pool.add(new int[]{r, c});
            }
        }

        Collections.shuffle(pool, random);

        int added = 0;
        for (int i = 0; i < pool.size() && added < need; i++) {
            int r = pool.get(i)[0];
            int c = pool.get(i)[1];

            CellButton cell = cells[r][c];
            if (cell.isFlagged()) continue;
            if (cell.getState() == CellButton.CellState.REVEALED) continue;
            if (!cell.canReveal()) continue;

            CellType t = cell.getCellType();

            switch (t) {
                case MINE -> {
                    cell.setState(CellButton.CellState.REVEALED);
                    cell.showMine();
                    // 🔇 no BOOM spam here
                    revealedMines++;
                }
                case NUMBER -> {
                    cell.setState(CellButton.CellState.REVEALED);
                    cell.showNumber(calculateAdjacentMines(r, c));
                    revealedNonMineCells++;
                }
                case EMPTY -> {
                    cell.setState(CellButton.CellState.REVEALED);
                    cell.showEmpty();
                    revealedNonMineCells++;
                }
                case QUESTION -> {
                    cell.showQuestion();
                    cell.setState(CellButton.CellState.QUESTION);
                    revealedNonMineCells++;
                }
                case SURPRISE -> {
                    cell.showSurprise();
                    cell.setState(CellButton.CellState.SURPRISE);
                    revealedNonMineCells++;
                }
            }

            flashBonusCell(cell);
            revealedCellsLog.add("[TOP-UP](" + r + "," + c + ":" + t + ")");
            added++;
        }

        return added;
    }

    // =========================
    // HELPERS
    // =========================
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

    public void revealAllCellsForEnd(boolean forceGenerateIfNeeded, boolean playEndSfx) {
        if (forceGenerateIfNeeded && !boardGenerated) {
            generateBoardWithSafeFirstCell(0, 0);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cell = cells[r][c];
                cell.setEnabled(false);

                if (!boardGenerated) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;

                CellType type = cell.getCellType();

                switch (type) {
                    case MINE -> {
                        cell.setState(CellButton.CellState.REVEALED);
                        cell.showMine();
                        // ✅ בסוף משחק לא עושים BOOM
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

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    public int getCellSize() { return cellSize; }

    public CellButton getCell(int row, int col) {
        return isValidCell(row, col) ? cells[row][col] : null;
    }

    public int getCorrectFlagsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() == CellType.MINE) count++;
            }
        }
        return count;
    }

    public int getIncorrectFlagsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isFlagged() && cells[i][j].getCellType() != CellType.MINE) count++;
            }
        }
        return count;
    }

    public int getUsedQuestionsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.QUESTION && cells[i][j].isUsed()) count++;
            }
        }
        return count;
    }

    public int getTotalQuestionsCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.QUESTION) count++;
            }
        }
        return count;
    }

    public int getUsedSurprisesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.SURPRISE && cells[i][j].isUsed()) count++;
            }
        }
        return count;
    }

    public int getTotalSurprisesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getCellType() == CellType.SURPRISE) count++;
            }
        }
        return count;
    }

    public int getRevealedMinesCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cb = cells[i][j];
                if (cb.getCellType() != CellType.MINE) continue;

                if (cb.isFlagged()) { count++; continue; }

                CellButton.CellState st = cb.getState();
                if (st == CellButton.CellState.REVEALED || st == CellButton.CellState.MINE) count++;
            }
        }
        return count;
    }

    public int getTotalMines() { return totalMines; }

    private void flashBonusCell(CellButton cell) {
        Color old = cell.getBackground();
        cell.setBackground(new Color(0, 150, 255, 120));
        Timer t = new Timer(500, e -> cell.setBackground(old));
        t.setRepeats(false);
        t.start();
    }

    // =========================
    // HOT/COLD HINT API (NEW)
    // =========================
    public void showHotColdHint(int radius) {
        if (!boardGenerated) return;
        if (gameController.isGameOver()) return;

        Point mine = pickRandomHiddenMine();
        if (mine == null) return;

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

        if (mines.isEmpty()) return null;
        return mines.get(random.nextInt(mines.size()));
    }
}

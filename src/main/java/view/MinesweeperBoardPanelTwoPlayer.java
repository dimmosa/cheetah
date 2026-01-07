package view;

import control.QuestionController;
import control.RevealMineAction;
import control.SurpriseAction;
import control.FlagCorrectAction;
import control.FlagWrongAction;
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
    private boolean endUiShown = false;

    private void showEndOnce() {
        if (endUiShown) return;
        endUiShown = true;

        handleGameEnd();
        SwingUtilities.invokeLater(() -> parentScreen.showGameOverScreen());
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

        if (cell.getState() != CellButton.CellState.HIDDEN &&
            cell.getState() != CellButton.CellState.FLAGGED) {
            return;
        }

        // REMOVE FLAG
        if (cell.isFlagged()) {
            cell.setFlagged(false);
            cell.setState(CellButton.CellState.HIDDEN); // ⭐ חשוב
            repaint();
            return;
        }

        // ADD FLAG
        cell.setFlagged(true);
        cell.setState(CellButton.CellState.FLAGGED); // ⭐ זה מה שהיה חסר

        CellType actualType = cell.getCellType();

        if (actualType == CellType.MINE) {
            MultiPlayerGameController.CellActionResult result =
            		new FlagCorrectAction(gameController).execute();

            cell.showCorrectFlagFeedback();
            cell.setBorder(new LineBorder(new Color(0, 255, 0, 150)));
            cell.setBackground(new Color(0, 255, 0, 150));
            cell.setPermanentBorderColor(new Color(0, 255, 0, 150));

            playCorrectSound();
            parentScreen.updateGameStateDisplay(result);

        } else {
        	MultiPlayerGameController.CellActionResult result =
        			new FlagWrongAction(gameController).execute();

            cell.showIncorrectFlagFeedback();
            cell.setBorder(new LineBorder(new Color(255, 0, 0, 150)));
            cell.setBackground(new Color(255, 0, 0, 150));
            cell.setPermanentBorderColor(new Color(255, 0, 0, 150));

            playIncorrectSound();
            parentScreen.updateGameStateDisplay(result);
        }

        repaint();

        // ⭐ אחרי כל דגל – בדיקת סיום
        checkBoardComplete();
    }

    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];
        if (!cell.canReveal()) return;

        CellType type = cell.getCellType();
        cell.setState(CellButton.CellState.REVEALED);

        MultiPlayerGameController.CellActionResult result = null;

        switch (type) {
            case MINE -> {
            	result = new RevealMineAction(gameController).execute();
                cell.showMine();
                revealedMines++;

                System.out.println("Mine revealed! Total revealed mines: " + revealedMines + "/" + totalMines);

                // Update display BEFORE checking game over
                if (result != null) {
                    parentScreen.updateGameStateDisplay(result);
                    if (result.turnEnded) {
                    }
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
            System.out.println("Player " + playerNum + " handled ALL mines! WIN!");

            gameController.handleAllMinesRevealed(playerNum);

            JOptionPane.showMessageDialog(this,
                    "All mines handled!\nCo-op Victory!\nFinal Score: " + gameController.getSharedScore(),
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);

            showEndOnce();

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
            // אין יותר מה לעשות -> WIN אם יש חיים
            gameEnds = true;
            endReason = "Player " + playerNum + " has no more actions (all cells revealed/flagged).";
            playerWins = (gameController.getSharedLives() > 0);

        } else if (revealedMines + correctlyFlaggedMines >= totalMines) {
            // כל המוקשים “טופלו” (נחשפו/סומנו) -> WIN אם יש חיים
            gameEnds = true;
            endReason = "Player " + playerNum + " handled all mines (revealed/flagged).";
            playerWins = (gameController.getSharedLives() > 0);
        }

        if (!gameEnds) {
            return;
        }

        System.out.println(endReason);

        if (playerWins) {
            System.out.println("WIN CONDITION MET");
            gameController.setPlayerBoardComplete(playerNum, true);

            JOptionPane.showMessageDialog(this,
                    "All mines correctly handled!\nCo-op Victory!\nFinal Score: " + gameController.getSharedScore(),
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("LOSS CONDITION - Wrong flags detected: " + wrongFlags);
            gameController.giveUp();

            JOptionPane.showMessageDialog(this,
                    "Wrong flags detected: " + wrongFlags + "\nGame Over - You Lose!",
                    "Wrong Flags",
                    JOptionPane.ERROR_MESSAGE);
        }

        showEndOnce();

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

        MultiPlayerGameController.CellActionResult result = new SurpriseAction(gameController).execute();


        if (result.pointsChanged > 0) {
            SurpriseBonusDialog dialog = new SurpriseBonusDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        } else {
            SurprisePenaltyDialog dialog = new SurprisePenaltyDialog(new Frame(), gameController.getDifficulty());
            dialog.setVisible(true);
        }

        repaint();

        if (gameController.isGameOver()) {
        	handleGameEnd();
        	SwingUtilities.invokeLater(() -> parentScreen.showGameOverScreen());

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
                    "Not enough points to activate question!\nNeed " + activationCost + " points.",
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
                        JOptionPane.showMessageDialog(this, 
                                "✓ Correct! " + result.message, 
                                "Correct Answer", 
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                                "✗ Incorrect! " + result.message, 
                                "Wrong Answer", 
                                JOptionPane.ERROR_MESSAGE);
                    }

                    // Update score / lives + mini-stats AFTER applying bonuses
                    parentScreen.updateGameStateDisplay(result);

                    if (gameController.isGameOver()) {
                    	if (gameController.isGameOver()) {
                    	    showEndOnce();
                    	}

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
            System.out.println("[BONUS MINE] No hidden unflagged mines to hint.");
            return;
        }

        Collections.shuffle(candidates, random);
        int[] pos = candidates.get(0);
        CellButton cell = cells[pos[0]][pos[1]];

        // ✅ להפוך אותו ל"מטופל" לוגית
        cell.setState(CellButton.CellState.REVEALED);   // או CellState.MINE אם יש אצלך כזה
        cell.showMine();
        revealedMines++; // ✅ עכשיו גם השדה מתעדכן

        flashBonusCell(cell);

        System.out.println("[BONUS MINE] Revealed mine bonus at (" + pos[0] + "," + pos[1] + ")");
        
        // ✅ לבדוק אם זה גורם לניצחון
        checkBoardComplete();
    }


    // Reveal up to 3x3 hidden cells around a random center as FREE bonus
 // Reveal up to 3x3 hidden cells around a random center as FREE bonus
 // If not enough cells can be revealed in the 3x3 area, we "top-up" by revealing other safe hidden cells elsewhere.
    private void revealRandom3x3Bonus() {

        final int TARGET = 9;

        // לוח קטן מ-3×3: פשוט נפתח עד 9 "מה שנשאר" מכל הלוח
        if (rows < 3 || cols < 3) {
            List<String> log = new ArrayList<>();
            int total = topUpRevealAnyCellsOpenAllTypes(TARGET, log);
            System.out.println("[BONUS 3x3] Board<3x3 revealed=" + total);
            System.out.println("[BONUS 3x3] Revealed cells: " + log);
            checkBoardComplete();
            repaint();
            return;
        }

        // ----- לבחור 3×3 רנדומלי אבל לא “מת” (שיהיה מה לפתוח בפנים) -----
        List<int[]> blocks = new ArrayList<>();
        int best = 0;

        for (int tr = 0; tr <= rows - 3; tr++) {
            for (int tc = 0; tc <= cols - 3; tc++) {
                int eligible = countEligibleAllTypesIn3x3(tr, tc); // כולל Q/S/MINE/EMPTY/NUMBER
                if (eligible > 0) {
                    blocks.add(new int[]{tr, tc, eligible});
                    if (eligible > best) best = eligible;
                }
            }
        }

        int tr, tc;
        if (blocks.isEmpty()) {
            // אין שום בלוק עם מה לפתוח -> TOP-UP בלבד
            List<String> log = new ArrayList<>();
            int total = topUpRevealAnyCellsOpenAllTypes(TARGET, log);
            System.out.println("[BONUS 3x3] No eligible 3x3 blocks. TOP-UP only total=" + total);
            System.out.println("[BONUS 3x3] Revealed cells: " + log);
            checkBoardComplete();
            repaint();
            return;
        } else {
            // רנדומלי בין הטובים ביותר
            List<int[]> bestBlocks = new ArrayList<>();
            for (int[] b : blocks) if (b[2] == best) bestBlocks.add(b);
            Collections.shuffle(bestBlocks, random);
            tr = bestBlocks.get(0)[0];
            tc = bestBlocks.get(0)[1];
        }

        int inside = 0;
        int minesInside = 0;

        List<String> revealedCellsLog = new ArrayList<>();

        // ----- Step 1: לחשוף בתוך ה-3×3 "הכל" -----
        for (int r = tr; r < tr + 3 && inside < TARGET; r++) {
            for (int c = tc; c < tc + 3 && inside < TARGET; c++) {

                CellButton cell = cells[r][c];

                // לא נוגעים בדגלים או פתוחים
                if (cell.isFlagged()) continue;
                if (cell.getState() == CellButton.CellState.REVEALED) continue;

                // אם יש לך canReveal() - כדאי להשאיר:
                if (!cell.canReveal()) continue;

                CellType t = cell.getCellType();

                // נחשוף ויזואלית לפי סוג
                cell.setState(CellButton.CellState.REVEALED);

                switch (t) {
                    case MINE -> {
                        // ✅ נספר לניצחון, בלי חיים
                        cell.showMine();
                        flashBonusCell(cell);
                        revealedMines++;
                        minesInside++;
                    }
                    case NUMBER -> {
                        cell.showNumber(calculateAdjacentMines(r, c));
                        flashBonusCell(cell);
                        revealedNonMineCells++;
                    }
                    case EMPTY -> {
                        cell.showEmpty();
                        flashBonusCell(cell);
                        revealedNonMineCells++;
                    }
                    case QUESTION -> {
                        // ✅ לחשוף בלבד (לא להפעיל אוטומטית)
                        cell.showQuestion();
                        flashBonusCell(cell);
                        revealedNonMineCells++;

                        // חשוב: אצלך הפעלה תלויה ב-state == QUESTION.
                        // אם showQuestion() לא משנה state, חייבים לשים:
                        cell.setState(CellButton.CellState.QUESTION);
                    }
                    case SURPRISE -> {
                        cell.showSurprise();
                        flashBonusCell(cell);
                        revealedNonMineCells++;

                        // כנ"ל – כדי שהשחקן יוכל להפעיל:
                        cell.setState(CellButton.CellState.SURPRISE);
                    }
                }

                revealedCellsLog.add("(" + r + "," + c + ":" + t + ")");
                inside++;
            }
        }

        // ----- Step 2: TOP-UP עד 9 מכל הלוח (פותח הכל) -----
        int total = inside;
        if (total < TARGET) {
            int need = TARGET - total;
            int added = topUpRevealAnyCellsOpenAllTypes(need, revealedCellsLog);
            total += added;

            System.out.println("[BONUS 3x3] TOP-UP needed=" + need + " added=" + added + " totalNow=" + total);
        }

        System.out.println("[BONUS 3x3] Block top-left=(" + tr + "," + tc + ") | inside=" + inside +
                " | total=" + total + " | minesShownInside=" + minesInside);
        System.out.println("[BONUS 3x3] Revealed cells: " + revealedCellsLog);

        JOptionPane.showMessageDialog(
                this,
                "BONUS 3x3 activated!\n" +
                "Block top-left = (" + tr + "," + tc + ")\n" +
                "Revealed inside = " + inside + "\n" +
                "Total revealed = " + total + "\n" +
                "Mines revealed (no life loss) = " + minesInside,
                "3x3 Bonus",
                JOptionPane.INFORMATION_MESSAGE
        );

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

            cell.setState(CellButton.CellState.REVEALED);

            switch (t) {
                case MINE -> {
                    cell.showMine();
                    revealedMines++;
                }
                case NUMBER -> {
                    cell.showNumber(calculateAdjacentMines(r, c));
                    revealedNonMineCells++;
                }
                case EMPTY -> {
                    cell.showEmpty();
                    revealedNonMineCells++;
                }
                case QUESTION -> {
                    cell.showQuestion();
                    revealedNonMineCells++;
                    cell.setState(CellButton.CellState.QUESTION);
                }
                case SURPRISE -> {
                    cell.showSurprise();
                    revealedNonMineCells++;
                    cell.setState(CellButton.CellState.SURPRISE);
                }
            }

            flashBonusCell(cell);
            revealedCellsLog.add("[TOP-UP](" + r + "," + c + ":" + t + ")");
            added++;
        }

        return added;
    }


       private int revealExactly9FromBlock(int tr, int tc,
            List<String> revealedCellsLog,
            List<String> skippedMinesLog) {

     List<int[]> candidates = new ArrayList<>();

// collect revealable safe cells in this block
       for (int r = tr; r < tr + 3; r++) {
       for (int c = tc; c < tc + 3; c++) {
         CellButton cell = cells[r][c];

    if (cell.getState() != CellButton.CellState.HIDDEN || cell.isFlagged()) continue;

CellType t = cell.getCellType();
if (t == CellType.QUESTION || t == CellType.SURPRISE) continue;

if (t == CellType.MINE) {
skippedMinesLog.add("(" + r + "," + c + ")");
continue;
}

if (t == CellType.EMPTY || t == CellType.NUMBER) {
candidates.add(new int[]{r, c});
}
}
}

// candidates size is guaranteed >= 9 for perfect block
Collections.shuffle(candidates, random);

int revealed = 0;
for (int i = 0; i < 9; i++) {
int r = candidates.get(i)[0];
int c = candidates.get(i)[1];

CellButton cell = cells[r][c];
CellType t = cell.getCellType();

cell.setState(CellButton.CellState.REVEALED);
if (t == CellType.NUMBER) {
cell.showNumber(calculateAdjacentMines(r, c));
revealedNonMineCells++;
} else {
cell.showEmpty();
revealedNonMineCells++;
}

flashBonusCell(cell);
revealedCellsLog.add("(" + r + "," + c + ":" + t + ")");
revealed++;
}

return revealed;
}
    private int revealUpTo9FromBlock(int tr, int tc,
            List<String> revealedCellsLog,
            List<String> skippedMinesLog) {

int revealed = 0;

for (int r = tr; r < tr + 3 && revealed < 9; r++) {
for (int c = tc; c < tc + 3 && revealed < 9; c++) {

CellButton cell = cells[r][c];

if (cell.getState() != CellButton.CellState.HIDDEN || cell.isFlagged()) continue;

CellType t = cell.getCellType();
if (t == CellType.QUESTION || t == CellType.SURPRISE) continue;

if (t == CellType.MINE) {
skippedMinesLog.add("(" + r + "," + c + ")");
continue;
}

if (t != CellType.EMPTY && t != CellType.NUMBER) continue;

cell.setState(CellButton.CellState.REVEALED);
if (t == CellType.NUMBER) {
cell.showNumber(calculateAdjacentMines(r, c));
revealedNonMineCells++;
} else {
cell.showEmpty();
revealedNonMineCells++;
}

flashBonusCell(cell);
revealedCellsLog.add("(" + r + "," + c + ":" + t + ")");
revealed++;
}
}

return revealed;
}
    private int revealAllRemainingSafeHidden() {
        int revealed = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton cell = cells[r][c];

                if (cell.getState() != CellButton.CellState.HIDDEN || cell.isFlagged()) continue;

                CellType t = cell.getCellType();
                if (t == CellType.QUESTION || t == CellType.SURPRISE) continue;
                if (t == CellType.MINE) continue;

                if (t != CellType.EMPTY && t != CellType.NUMBER) continue;

                cell.setState(CellButton.CellState.REVEALED);
                if (t == CellType.NUMBER) {
                    cell.showNumber(calculateAdjacentMines(r, c));
                    revealedNonMineCells++;
                } else {
                    cell.showEmpty();
                    revealedNonMineCells++;
                }

                flashBonusCell(cell);
                revealed++;
            }
        }

        return revealed;
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

                if (cb.getCellType() != CellType.MINE) continue;

                // ✅ אם מוקש סומן בדגל נכון – זה גם "טופל"
                if (cb.isFlagged()) {
                    count++;
                    continue;
                }

                CellButton.CellState st = cb.getState();
                if (st == CellButton.CellState.REVEALED || st == CellButton.CellState.MINE) {
                    count++;
                }
            }
        }
        return count;
    }


    public int getTotalMines() {
        return totalMines;
    }
    private void flashBonusCell(CellButton cell) {
        Color old = cell.getBackground();
        cell.setBackground(new Color(0, 150, 255, 120)); // blue highlight
        Timer t = new Timer(500, e -> cell.setBackground(old));
        t.setRepeats(false);
        t.start();
    }

}
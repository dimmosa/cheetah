package view;

import control.SinglePlayerGameControl;
import control.QuestionController;
import model.Question;
import model.CellType;
import model.FlagResult;
import view.CellButton;
import view.QuestionCellDialog;
import view.QuestionTimeDialog;
import view.SurpriseBonusDialog;
import view.SurprisePenaltyDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MinesweeperBoardPanel extends JPanel {

    private final int rows;
    private final int cols;
    private final CellButton[][] cells;
    private int cellSize;
    private final SinglePlayerGameControl controller;
    private final QuestionController questionController;

    private boolean isFlagMode = false;
    private java.util.Random random = new java.util.Random();

    public MinesweeperBoardPanel(int rows, int cols, SinglePlayerGameControl controller, QuestionController questionController) {
        this.rows = rows;
        this.cols = cols;
        this.controller = controller;
        this.questionController = questionController;
        this.cells = new CellButton[rows][cols];

        switch (controller.getDifficulty()) {
            case "Easy":
                cellSize = 40;
                break;
            case "Medium":
                cellSize = 32;
                break;
            case "Hard":
                cellSize = 24;
                break;
            default:
                cellSize = 24;
        }

        setLayout(new GridLayout(rows, cols, 2, 2));
        setBackground(new Color(10, 10, 15));
        initializeBoard();
        generateBoard();

    }

    private void initializeBoard() {
        System.out.println("Initializing board: " + controller.getDifficulty());

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

        System.out.println("Board generated successfully!");
    }


    private void placeMines() {
        int minesToPlace = controller.getTotalMines();
        int placed = 0;

        while (placed < minesToPlace) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (cells[r][c].getCellType() != CellType.MINE) {
                cells[r][c].setCellType(CellType.MINE);
                placed++;
            }
        }

        System.out.println("Placed " + placed + " mines");
    }


    private void calculateNumbers() {
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
        java.util.List<int[]> emptyCells = new java.util.ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getCellType() == CellType.EMPTY) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        java.util.Collections.shuffle(emptyCells);

        int questionCellsNeeded = 0;
        int surpriseCellsNeeded = 0;

        switch (controller.getDifficulty()) {
            case "Easy" -> { questionCellsNeeded = 6; surpriseCellsNeeded = 2; }
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
                surpriseCellsNeeded + " surprise cells on single-player board");
    }


    private void handleFlagPlacement(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.getState() != CellButton.CellState.HIDDEN) {
            return;
        }

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            controller.removeFlag(r, c, cell.getCellType());
            updateGameScreen();
        } else {
            CellType actualType = cell.getCellType();
            FlagResult result = controller.placeFlag(r, c, actualType);

            cell.setFlagged(true);

            if (result == FlagResult.CORRECT_MINE) {
                cell.showCorrectFlagFeedback();
                cell.setBorder(new LineBorder(new Color(0, 255, 0, 150)));
                playCorrectSound();
            } else if (result == FlagResult.INCORRECT) {
                cell.showIncorrectFlagFeedback();
                cell.setBorder(new LineBorder(new Color(255, 0, 0, 150)));
                playIncorrectSound();
            }

            updateGameScreen();

            if (controller.isGameOver()) {
                handleGameEnd();
            }
        }
    }

    private void handleCellClick(int r, int c) {
        if (controller.isGameOver()) return;

        CellButton cell = cells[r][c];

        if (isFlagMode || controller.isFlagMode()) {
            handleFlagPlacement(r, c);
            return;
        }

        if (cell.getState() == CellButton.CellState.HIDDEN) {
            revealCell(r, c);
        }
        else if (cell.getState() == CellButton.CellState.QUESTION && cell.canActivate()) {
            activateQuestionCell(r, c);
        }
        else if (cell.getState() == CellButton.CellState.SURPRISE && cell.canActivate()) {
            activateSurpriseCell(r, c);
        }
    }

    private void handleRightClick(int r, int c) {
        if (controller.isGameOver()) return;
        handleFlagPlacement(r, c);
    }

    private void revealCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (!cell.canReveal()) return;

        CellType type = cell.getCellType();
        boolean validMove = controller.revealCell(r, c, type);

        if (!validMove) return;

        cell.setState(CellButton.CellState.REVEALED);

        switch (type) {
            case MINE -> {
                cell.showMine();
                JOptionPane.showMessageDialog(
                        this,
                        "💣 BOOM! Mine hit! Lives left: " + controller.getLives(),
                        "Mine!",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            case NUMBER -> {
                int adjacentMines = calculateAdjacentMines(r, c);
                cell.showNumber(adjacentMines);

                if (adjacentMines == 0) {
                    cascadeReveal(r, c);
                }
            }
            case EMPTY -> {
                cell.showEmpty();
                cascadeReveal(r, c);
            }
            case QUESTION -> cell.showQuestion();
            case SURPRISE -> cell.showSurprise();
        }

        updateGameScreen();

        if (controller.isGameOver()) handleGameEnd();
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
                controller.revealCell(nr, nc, type);

                switch (type) {
                    case EMPTY -> {
                        neighbor.showEmpty();
                        cascadeReveal(nr, nc);
                    }
                    case NUMBER -> {
                        int adjacentMines = calculateAdjacentMines(nr, nc);
                        neighbor.showNumber(adjacentMines);

                        if (adjacentMines == 0) {
                            cascadeReveal(nr, nc);
                        }
                    }
                    case QUESTION -> neighbor.showQuestion();
                    case SURPRISE -> neighbor.showSurprise();

                    case MINE -> {
                        neighbor.showMine();
                    }
                }
            }
        }
    }


    private void activateSurpriseCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            JOptionPane.showMessageDialog(
                    this,
                    "This surprise cell has already been used!",
                    "Already Used",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Activate surprise cell?\nCost: " + controller.getActivationCost() + " points\n50% chance of reward or penalty!",
                "Activate Surprise?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        boolean goodSurprise = controller.activateSurpriseCell();

        cell.setUsed(true);

        if (goodSurprise) {
            SurpriseBonusDialog dialog = new SurpriseBonusDialog(new Frame(),controller.getDifficulty());
            dialog.setVisible(true);
        } else {
            SurprisePenaltyDialog dialog = new SurprisePenaltyDialog(new Frame(),controller.getDifficulty());
            dialog.setVisible(true);
        }

        updateGameScreen();

        if (controller.isGameOver()) {
            handleGameEnd();
        }
    }

    private void activateQuestionCell(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.isUsed()) {
            JOptionPane.showMessageDialog(
                    this,
                    "This question cell has already been used!",
                    "Already Used",
                    JOptionPane.INFORMATION_MESSAGE
            );            return;
        }

        if (controller.getPoints() < controller.getActivationCost()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not enough points! Need " + controller.getActivationCost() + " points.",
                    "Insufficient Points",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        QuestionCellDialog cellDialog = new QuestionCellDialog(new Frame());
        cellDialog.setVisible(true);

        if (!cellDialog.shouldProceed()) {
            return;
        }

        boolean canActivate = controller.activateQuestionCell();

        if (!canActivate) {
            return;
        }

        cell.setUsed(true);

        showQuestionDialog(r, c);

        updateGameScreen();
    }

    private void showQuestionDialog(int r, int c) {
        String difficulty = controller.getDifficulty();

        int diff = QuestionController.getDifficultyFromString(difficulty);

        Question q = questionController.getRandomQuestion(diff);


        QuestionTimeDialog qd = new QuestionTimeDialog(
                new Frame(),
                difficulty,
                q.getText(),
                q.getAnswers(),
                selectedIndex -> {
                    boolean correct = (selectedIndex == q.getCorrectIndex());

                    controller.handleQuestionAnswer(difficulty, correct);

                    if (correct) {
                        JOptionPane.showMessageDialog(this, "✓ Correct! Points rewarded!");
                    } else {
                        JOptionPane.showMessageDialog(this, "✗ Incorrect! Penalties applied.");
                    }

                    updateGameScreen();
                }
        );

        qd.setVisible(true);
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


    private void updateGameScreen() {
        SwingUtilities.invokeLater(() -> {
            Container parent = getParent();
            while (parent != null && !(parent instanceof GameScreenSinglePlayer)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                ((GameScreenSinglePlayer) parent).updateStatsDisplay();
            }
        });
    }


    private void handleGameEnd() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getState() == CellButton.CellState.HIDDEN) {
                    cells[i][j].setState(CellButton.CellState.REVEALED);

                }
            }
        }

        String message = controller.getRemainingMines() == 0 ?
                "🎉 VICTORY! All mines found!\nFinal Score: " + controller.getPoints() :
                "💀 GAME OVER! No lives left.\nFinal Score: " + controller.getPoints();

        JOptionPane.showMessageDialog(
                this,
                message,
                "Game End",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void setFlagMode(boolean flagMode) {
        this.isFlagMode = flagMode;
    }

    private void playCorrectSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void playIncorrectSound() {
    }
}
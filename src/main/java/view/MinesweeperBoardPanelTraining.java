// ===============================
// MinesweeperBoardPanelTraining.java  (FIXED)
// ===============================
package view;

import model.CellType;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MinesweeperBoardPanelTraining extends JPanel {

    private final int rows = 5;
    private final int cols = 5;
    private final CellButton[][] cells;
    private final int cellSize = 60;

    private final Consumer<String> onAction;
    private boolean trainingEnabled = false;

    // ✅ allow hint only when current step is HINT
    private boolean hintAllowed = false;

    private final int MINE_ROW = 1;
    private final int MINE_COL = 1;
    private final int QUESTION_ROW = 3;
    private final int QUESTION_COL = 3;
    private final int SURPRISE_ROW = 3;
    private final int SURPRISE_COL = 1;

    public MinesweeperBoardPanelTraining(Consumer<String> onAction) {
        this.onAction = onAction;
        this.cells = new CellButton[rows][cols];

        setLayout(new GridLayout(rows, cols, 3, 3));
        setBackground(new Color(10, 10, 15));

        int boardWidth = cols * cellSize + (cols - 1) * 3;
        int boardHeight = rows * cellSize + (rows - 1) * 3;
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 255, 255, 50), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        initializeBoard();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        GradientPaint bg = new GradientPaint(
                0, 0, new Color(8, 10, 18),
                w, h, new Color(2, 4, 10)
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(0, 255, 255, 60));
        g2.drawRoundRect(2, 2, w - 4, h - 4, 18, 18);

        g2.dispose();
    }

    private void initializeBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellButton cell = new CellButton(cellSize);
                cells[i][j] = cell;
                add(cell);

                int r = i, c = j;

                cell.addActionListener(e -> {
                    if (trainingEnabled) handleCellClick(r, c);
                });

                cell.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e) && trainingEnabled) {
                            handleRightClick(r, c);
                        }
                    }
                });
            }
        }
    }

    public void resetBoard() {
        clearHighlights();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j].reset();
                cells[i][j].setCellType(CellType.EMPTY);
                cells[i][j].setState(CellButton.CellState.HIDDEN);
                cells[i][j].setFlagged(false);
                cells[i][j].setUsed(false);
                cells[i][j].setPermanentBorderColor(null);
                cells[i][j].clearDemoIcon();
            }
        }

        cells[MINE_ROW][MINE_COL].setCellType(CellType.MINE);
        cells[QUESTION_ROW][QUESTION_COL].setCellType(CellType.QUESTION);
        cells[SURPRISE_ROW][SURPRISE_COL].setCellType(CellType.SURPRISE);

        if (MINE_ROW > 0) {
            cells[MINE_ROW - 1][MINE_COL].setCellType(CellType.NUMBER);
            cells[MINE_ROW - 1][MINE_COL].setNumber(1);
        }
        if (MINE_COL > 0) {
            cells[MINE_ROW][MINE_COL - 1].setCellType(CellType.NUMBER);
            cells[MINE_ROW][MINE_COL - 1].setNumber(1);
        }

        revalidate();
        repaint();
    }

    public void prepareForStep(String stepName) {
        resetBoard();
        clearHighlights();
        clearAllDemoIcons();

        // ✅ only allow hint when the step is HINT
        hintAllowed = "HINT".equals(stepName);

        switch (stepName) {
            case "FLAG" -> {
                revealNumbersAroundMineForContext();
                cells[MINE_ROW][MINE_COL].showFlagIcon();
                highlightCell(MINE_ROW, MINE_COL);
            }
            case "MINE" -> {
                revealNumbersAroundMineForContext();
                cells[MINE_ROW][MINE_COL].setFlagged(true);
                cells[MINE_ROW][MINE_COL].forceShowMineIcon();
                highlightCell(MINE_ROW, MINE_COL);
            }
            case "QUESTION" -> {
                revealNumbersAroundMineForContext();
                cells[MINE_ROW][MINE_COL].setFlagged(true);
                cells[MINE_ROW][MINE_COL].setState(CellButton.CellState.REVEALED);
                cells[MINE_ROW][MINE_COL].showMine();

                cells[QUESTION_ROW][QUESTION_COL].setState(CellButton.CellState.REVEALED);
                cells[QUESTION_ROW][QUESTION_COL].showQuestion();
                highlightCell(QUESTION_ROW, QUESTION_COL);
            }
            case "SURPRISE" -> {
                revealNumbersAroundMineForContext();
                cells[MINE_ROW][MINE_COL].setFlagged(true);
                cells[MINE_ROW][MINE_COL].setState(CellButton.CellState.REVEALED);
                cells[MINE_ROW][MINE_COL].showMine();

                cells[QUESTION_ROW][QUESTION_COL].setState(CellButton.CellState.REVEALED);
                cells[QUESTION_ROW][QUESTION_COL].showQuestion();
                cells[QUESTION_ROW][QUESTION_COL].setUsed(true);

                cells[SURPRISE_ROW][SURPRISE_COL].setState(CellButton.CellState.REVEALED);
                cells[SURPRISE_ROW][SURPRISE_COL].showSurprise();
                highlightCell(SURPRISE_ROW, SURPRISE_COL);
            }
            case "HINT" -> {
                revealNumbersAroundMineForContext();
                cells[MINE_ROW][MINE_COL].setFlagged(true);
                cells[MINE_ROW][MINE_COL].setState(CellButton.CellState.REVEALED);
                cells[MINE_ROW][MINE_COL].showMine();

                cells[QUESTION_ROW][QUESTION_COL].setState(CellButton.CellState.REVEALED);
                cells[QUESTION_ROW][QUESTION_COL].showQuestion();
                cells[QUESTION_ROW][QUESTION_COL].setUsed(true);

                cells[SURPRISE_ROW][SURPRISE_COL].setState(CellButton.CellState.REVEALED);
                cells[SURPRISE_ROW][SURPRISE_COL].showSurprise();
                cells[SURPRISE_ROW][SURPRISE_COL].setUsed(true);

                // ❌ DO NOT call showHotColdHint here (otherwise it runs automatically)
                // showHotColdHint(6);

                // optional: just highlight something if you want
                // highlightCell(MINE_ROW, MINE_COL);
            }
            default -> { }
        }

        revalidate();
        repaint();
    }

    private void revealNumbersAroundMineForContext() {
        if (MINE_ROW > 0) {
            cells[MINE_ROW - 1][MINE_COL].setState(CellButton.CellState.REVEALED);
            cells[MINE_ROW - 1][MINE_COL].showNumber(1);
        }
        if (MINE_COL > 0) {
            cells[MINE_ROW][MINE_COL - 1].setState(CellButton.CellState.REVEALED);
            cells[MINE_ROW][MINE_COL - 1].showNumber(1);
        }
    }

    private void clearAllDemoIcons() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j].clearDemoIcon();
            }
        }
    }

    private void highlightCell(int row, int col) {
        CellButton cell = cells[row][col];

        Timer old = (Timer) cell.getClientProperty("highlightTimer");
        if (old != null) old.stop();

        final float[] t = {0f};

        Timer pulseTimer = new Timer(35, e -> {
            t[0] += 0.18f;
            float s = (float) ((Math.sin(t[0]) + 1) / 2.0);
            int a = 140 + (int) (115 * s);
            cell.setPermanentBorderColor(new Color(0, 255, 255, a));
            cell.repaint();
        });

        pulseTimer.start();
        cell.putClientProperty("highlightTimer", pulseTimer);
    }

    private void clearHighlights() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j].setPermanentBorderColor(null);
                Timer timer = (Timer) cells[i][j].getClientProperty("highlightTimer");
                if (timer != null) {
                    timer.stop();
                    cells[i][j].putClientProperty("highlightTimer", null);
                }
            }
        }
    }

    private void handleCellClick(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.getState() == CellButton.CellState.REVEALED) {
            if (cell.getCellType() == CellType.QUESTION && !cell.isUsed()) {
                cell.setUsed(true);
                onAction.accept("QUESTION");
            } else if (cell.getCellType() == CellType.SURPRISE && !cell.isUsed()) {
                cell.setUsed(true);
                onAction.accept("SURPRISE");
            }
            return;
        }

        CellType type = cell.getCellType();

        switch (type) {
            case MINE -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showMine();
                onAction.accept("MINE");
            }
            case QUESTION -> {
                if (!cell.isUsed()) {
                    cell.setState(CellButton.CellState.REVEALED);
                    cell.showQuestion();
                    cell.setUsed(true);
                    onAction.accept("QUESTION");
                }
            }
            case SURPRISE -> {
                if (!cell.isUsed()) {
                    cell.setState(CellButton.CellState.REVEALED);
                    cell.showSurprise();
                    cell.setUsed(true);
                    onAction.accept("SURPRISE");
                }
            }
            case NUMBER -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showNumber(cell.getNumber());
            }
            case EMPTY -> {
                cell.setState(CellButton.CellState.REVEALED);
                cell.showEmpty();
            }
        }
    }

    private void handleRightClick(int r, int c) {
        CellButton cell = cells[r][c];

        if (cell.getState() != CellButton.CellState.HIDDEN) return;

        if (!cell.isFlagged()) {
            cell.setFlagged(true);

            if (cell.getCellType() == CellType.MINE) {
                cell.showCorrectFlagFeedback();
                onAction.accept("FLAG");
            } else {
                cell.showIncorrectFlagFeedback();
            }
        } else {
            cell.setFlagged(false);
        }
    }

    public void setTrainingEnabled(boolean enabled) {
        this.trainingEnabled = enabled;

        if (!enabled) {
            clearHighlights();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    cells[i][j].setEnabled(false);
                }
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    cells[i][j].setEnabled(true);
                }
            }
        }
    }

    // ✅ called ONLY from the dialog button
    public void useHintFromDialog() {
        if (!hintAllowed) return;
        showHotColdHint(6);
        onAction.accept("HINT"); // ✅ now we notify dialog AFTER user clicked button
    }

    // ✅ show hint visuals only (NO auto-callback!)
    private void showHotColdHint(int numCells) {
        java.util.List<int[]> hiddenCells = new java.util.ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getState() == CellButton.CellState.HIDDEN) {
                    hiddenCells.add(new int[]{i, j});
                }
            }
        }

        java.util.Collections.shuffle(hiddenCells);

        for (int i = 0; i < Math.min(numCells, hiddenCells.size()); i++) {
            int[] pos = hiddenCells.get(i);
            int r = pos[0];
            int c = pos[1];

            double distance = Math.sqrt(
                    Math.pow(r - MINE_ROW, 2) + Math.pow(c - MINE_COL, 2)
            );

            String hint;
            Color hintColor;

            if (distance < 1.5) {
                hint = "🔥 HOT";
                hintColor = new Color(255, 60, 120);
            } else if (distance < 2.5) {
                hint = "🌡️ WARM";
                hintColor = new Color(255, 210, 60);
            } else {
                hint = "❄️ COLD";
                hintColor = new Color(80, 200, 255);
            }

            cells[r][c].showHint(hint, hintColor);
        }
    }
}

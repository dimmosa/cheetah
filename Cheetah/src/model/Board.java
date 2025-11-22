package model;

import model.cell.*;

import java.util.Random;

public class Board {
    private final int size;
    private final Cell[][] grid;

    public Board(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Board size must be greater than 0.");
        this.size = size;
        this.grid = new Cell[size][size];
    }

    public int getSize() {
        return size;
    }

    public Cell[][] getCells() {
        return grid;
    }

    public Cell getCell(int r, int c) {
        if (!isInBounds(r, c)) return null;
        return grid[r][c];
    }

    public void setCell(int r, int c, Cell cell) {
        if (!isInBounds(r, c))
            throw new IndexOutOfBoundsException("Cell position out of bounds.");
        grid[r][c] = cell;
    }

    public boolean isInBounds(int r, int c) {
        return r >= 0 && c >= 0 && r < size && c < size;
    }

    public void resetBoard() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = null;
            }
        }
    }

    public void generateBoard(int mines, int questions, int surprises) {
        Random rand = new Random();

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = new EmptyCell(r, c);

        for (int i = 0; i < mines; ) {
            int r = rand.nextInt(size), c = rand.nextInt(size);
            if (grid[r][c].getType() != CellType.MINE) {
                grid[r][c] = new MineCell(r, c);
                i++;
            }
        }

        for (int i = 0; i < questions; ) {
            int r = rand.nextInt(size), c = rand.nextInt(size);
            if (grid[r][c].getType() == CellType.EMPTY) {
                grid[r][c] = new QuestionCell(r, c,1);
                i++;
            }
        }

        for (int i = 0; i < surprises; ) {
            int r = rand.nextInt(size), c = rand.nextInt(size);
            if (grid[r][c].getType() == CellType.EMPTY) {
                grid[r][c] = new SurpriseCell(r, c);
                i++;
            }
        }

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c].getType() == CellType.EMPTY) {
                    int adjacentMines = countAdjacentMines(r, c);
                    if (adjacentMines > 0)
                        grid[r][c] = new NumberCell(r, c, adjacentMines);
                }
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr, nc = c + dc;
                if (nr == r && nc == c) continue;
                if (isInBounds(nr, nc) && grid[nr][nc].getType() == CellType.MINE) count++;
            }
        return count;
    }

    public boolean allMinesRevealed() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c].getType() == CellType.MINE && !grid[r][c].isRevealed())
                    return false;
        return true;
    }
}

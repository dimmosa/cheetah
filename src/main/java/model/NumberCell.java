package model;

public class NumberCell extends Cell {
    private int adjacentMines;

    public NumberCell(int row, int col, int adjacentMines) {
        super(row, col);
        this.adjacentMines = adjacentMines;
    }

    @Override
    public CellType getType() {
        return CellType.NUMBER;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }
}

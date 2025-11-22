package model.cell;

public class EmptyCell extends Cell {
    public EmptyCell(int row, int col) {
        super(row, col);
    }

    @Override
    public CellType getType() { return CellType.EMPTY; }
}

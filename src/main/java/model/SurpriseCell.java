package model;

public class SurpriseCell extends Cell {
    public SurpriseCell(int row, int col) {
        super(row, col);
    }

    @Override
    public CellType getType() { return CellType.SURPRISE; }
}

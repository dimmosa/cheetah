package model;

public abstract class Cell {
    protected int row;
    protected int col;
    protected boolean revealed = false;
    protected boolean flagged = false;
    protected boolean used = false;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public abstract CellType getType();

    public boolean isRevealed() { return revealed; }
    public void reveal() { this.revealed = true; }

    public boolean isFlagged() { return flagged; }
    public void toggleFlag() { this.flagged = !flagged; }

    public boolean isUsed() { return used; }
    public void markUsed() { this.used = true; }

    public int getRow() { return row; }
    public int getCol() { return col; }
}

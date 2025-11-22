package model.cell;

public class QuestionCell extends Cell {
    private int difficulty;

    public QuestionCell(int row, int col, int difficulty) {
        super(row, col);
        this.difficulty = difficulty;
    }

    public int getDifficulty() { return difficulty; }

    @Override
    public CellType getType() { return CellType.QUESTION; }
}

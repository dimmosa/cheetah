package model;

import model.*;
import java.util.*;

public class Game {

    private Board board;
    private int rows;
    private int cols;

    private int mines;
    private int questions;
    private int surprises;

    private int lives;
    private int score;

    private boolean gameOver;

    public Game(int rows, int cols, int mines, int questions, int surprises, int lives) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questions = questions;
        this.surprises = surprises;
        this.lives = lives;
        this.score = 0;
        this.board = new Board(rows);
        this.gameOver = false;

        generateBoard();
    }

    public Board getBoard() {
        return board;
    }

    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isGameOver() { return gameOver; }

    public void addScore(int pts) { score += pts; }
    public void addLife(int val) { lives += val; if (lives < 0) lives = 0; }


    private void generateBoard() {
        placeMines();
        placeNumbersAndEmpty();
        placeQuestionCells();
        placeSurpriseCells();
    }

    private void placeMines() {
        Random rand = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (board.getCell(r, c) == null) {
                board.setCell(r, c, new MineCell(r, c));
                placed++;
            }
        }
    }

    private void placeNumbersAndEmpty() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board.getCell(r, c) != null) continue;

                int adjMines = countAdjacentMines(r, c);
                if (adjMines == 0)
                    board.setCell(r, c, new EmptyCell(r, c));
                else
                    board.setCell(r, c, new NumberCell(r, c, adjMines));
            }
        }
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (board.isInBounds(nr, nc) && board.getCell(nr, nc) instanceof MineCell)
                    count++;
            }
        }
        return count;
    }

    private void placeQuestionCells() {
        List<EmptyCell> emptyCells = collectEmptyCells();
        Collections.shuffle(emptyCells);
        for (int i = 0; i < Math.min(questions, emptyCells.size()); i++) {
            EmptyCell ec = emptyCells.get(i);
            board.setCell(ec.getRow(), ec.getCol(), new QuestionCell(ec.getRow(), ec.getCol(), 0));
        }
    }

    private void placeSurpriseCells() {
        List<EmptyCell> emptyCells = collectEmptyCells();
        Collections.shuffle(emptyCells);
        for (int i = 0; i < Math.min(surprises, emptyCells.size()); i++) {
            EmptyCell ec = emptyCells.get(i);
            board.setCell(ec.getRow(), ec.getCol(), new SurpriseCell(ec.getRow(), ec.getCol()));
        }
    }

    private List<EmptyCell> collectEmptyCells() {
        List<EmptyCell> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board.getCell(r, c) instanceof EmptyCell)
                    list.add((EmptyCell) board.getCell(r, c));
            }
        }
        return list;
    }

    public boolean revealCell(int r, int c) {
        if (gameOver || !board.isInBounds(r, c)) return false;

        Cell cell = board.getCell(r, c);
        if (cell.isRevealed() || cell.isFlagged()) return false;

        cell.reveal();

        if (cell instanceof MineCell) {
            lives--;
            if (lives <= 0) gameOver = true;
            return false;
        }

        if (cell instanceof NumberCell && ((NumberCell) cell).getAdjacentMines() == 0)
            floodReveal(r, c);

        checkWin();
        return true;
    }

    private void floodReveal(int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr;
                int nc = c + dc;
                if (!board.isInBounds(nr, nc)) continue;
                Cell cell = board.getCell(nr, nc);
                if (!cell.isRevealed() && !(cell instanceof MineCell)) {
                    cell.reveal();
                    if (cell instanceof NumberCell && ((NumberCell) cell).getAdjacentMines() == 0)
                        floodReveal(nr, nc);
                }
            }
        }
    }

    public void toggleFlag(int r, int c) {
        if (!board.isInBounds(r, c)) return;
        Cell cell = board.getCell(r, c);
        if (!cell.isRevealed())
            cell.toggleFlag();
    }

    private void checkWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!(cell instanceof MineCell) && !cell.isRevealed())
                    return;
            }
        }
        gameOver = true;
    }

}

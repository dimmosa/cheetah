package model;

public class Player {
    private String name;
    private int remainingMines;
    private String color;

    public Player(String name, String color, int remainingMines) {
        this.name = name;
        this.color = color;
        this.remainingMines = remainingMines;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public int getRemainingMines() { return remainingMines; }
    public void setRemainingMines(int x) { this.remainingMines = x; }
}

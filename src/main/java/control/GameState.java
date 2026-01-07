package control;

public record GameState(int sharedScore, int sharedLives, int currentPlayer, boolean gameOver) {}

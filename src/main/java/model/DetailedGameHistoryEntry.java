package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailedGameHistoryEntry {

    // ---------------- NEW/EXTENDED FIELDS ----------------
    private TwoPlayerMode mode = TwoPlayerMode.COOP; // default for old files
    private String winner = "";                      // for COOP empty, for VS username / "Draw"

    // NEW: end reason
    // Allowed: WIN / LOST / GIVE_UP / DRAW
    private String endReason = "";

    // NEW: per-player scores (especially for VS)
    private int player1Score = 0;
    private int player2Score = 0;

    // ---------------- EXISTING FIELDS ----------------
    private String player1;
    private String player2;
    private String difficulty;
    private int finalScore;
    private int durationSeconds;
    private boolean won;

    private int player1QuestionsAnswered;
    private int player1QuestionsCorrect;
    private int player1SurprisesActivated;
    private int player1GoodSurprises;
    private int player1BadSurprises;
    private int player1MinesFlagged;
    private int player1CorrectFlags;
    private int player1WrongFlags;
    private int player1CellsRevealed;
    private int player1MinesRevealed;

    private int player2QuestionsAnswered;
    private int player2QuestionsCorrect;
    private int player2SurprisesActivated;
    private int player2GoodSurprises;
    private int player2BadSurprises;
    private int player2MinesFlagged;
    private int player2CorrectFlags;
    private int player2WrongFlags;
    private int player2CellsRevealed;
    private int player2MinesRevealed;

    private String timestamp;

    public DetailedGameHistoryEntry() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public DetailedGameHistoryEntry(String p1, String p2, String difficulty, int finalScore,
                                    int durationSeconds, boolean won) {
        this.player1 = p1;
        this.player2 = p2;
        this.difficulty = difficulty;
        this.finalScore = finalScore;
        this.durationSeconds = durationSeconds;
        this.won = won;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ---------------- CSV (BACKWARD COMPATIBLE) ----------------
    // Base columns were 27 (index 0..26)
    // Later you added Mode+Winner (2 columns) => 29 (index 0..28)
    // Now we add EndReason + P1Score + P2Score (3 columns) => 32 (index 0..31)

    public String toCSV() {
        String safeWinner = (winner == null) ? "" : winner.replace(",", " ");
        String safeMode = (mode == null) ? TwoPlayerMode.COOP.name() : mode.name();
        String safeReason = normalizeEndReason(endReason);

        return String.format(
                "%s,%s,%s,%s,%d,%d,%b," +
                        "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d," +
                        "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d," +
                        "%s,%s,%s,%d,%d",
                timestamp, player1, player2, difficulty, finalScore, durationSeconds, won,
                player1QuestionsAnswered, player1QuestionsCorrect, player1SurprisesActivated,
                player1GoodSurprises, player1BadSurprises, player1MinesFlagged,
                player1CorrectFlags, player1WrongFlags, player1CellsRevealed, player1MinesRevealed,
                player2QuestionsAnswered, player2QuestionsCorrect, player2SurprisesActivated,
                player2GoodSurprises, player2BadSurprises, player2MinesFlagged,
                player2CorrectFlags, player2WrongFlags, player2CellsRevealed, player2MinesRevealed,
                safeMode,
                safeWinner,
                safeReason,
                player1Score,
                player2Score
        );
    }

    public static String getCSVHeader() {
        return "Timestamp,Player1,Player2,Difficulty,FinalScore,Duration,Won," +
                "P1_Questions,P1_Correct,P1_Surprises,P1_GoodSurprises,P1_BadSurprises," +
                "P1_MinesFlagged,P1_CorrectFlags,P1_WrongFlags,P1_CellsRevealed,P1_MinesRevealed," +
                "P2_Questions,P2_Correct,P2_Surprises,P2_GoodSurprises,P2_BadSurprises," +
                "P2_MinesFlagged,P2_CorrectFlags,P2_WrongFlags,P2_CellsRevealed,P2_MinesRevealed," +
                "Mode,Winner,EndReason,P1_Score,P2_Score";
    }

    public static DetailedGameHistoryEntry fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 27) return null;

        DetailedGameHistoryEntry entry = new DetailedGameHistoryEntry();
        try {
            // 0..26 (old format)
            entry.timestamp = parts[0];
            entry.player1 = parts[1];
            entry.player2 = parts[2];
            entry.difficulty = parts[3];
            entry.finalScore = Integer.parseInt(parts[4]);
            entry.durationSeconds = Integer.parseInt(parts[5]);
            entry.won = Boolean.parseBoolean(parts[6]);

            entry.player1QuestionsAnswered = Integer.parseInt(parts[7]);
            entry.player1QuestionsCorrect = Integer.parseInt(parts[8]);
            entry.player1SurprisesActivated = Integer.parseInt(parts[9]);
            entry.player1GoodSurprises = Integer.parseInt(parts[10]);
            entry.player1BadSurprises = Integer.parseInt(parts[11]);
            entry.player1MinesFlagged = Integer.parseInt(parts[12]);
            entry.player1CorrectFlags = Integer.parseInt(parts[13]);
            entry.player1WrongFlags = Integer.parseInt(parts[14]);
            entry.player1CellsRevealed = Integer.parseInt(parts[15]);
            entry.player1MinesRevealed = Integer.parseInt(parts[16]);

            entry.player2QuestionsAnswered = Integer.parseInt(parts[17]);
            entry.player2QuestionsCorrect = Integer.parseInt(parts[18]);
            entry.player2SurprisesActivated = Integer.parseInt(parts[19]);
            entry.player2GoodSurprises = Integer.parseInt(parts[20]);
            entry.player2BadSurprises = Integer.parseInt(parts[21]);
            entry.player2MinesFlagged = Integer.parseInt(parts[22]);
            entry.player2CorrectFlags = Integer.parseInt(parts[23]);
            entry.player2WrongFlags = Integer.parseInt(parts[24]);
            entry.player2CellsRevealed = Integer.parseInt(parts[25]);
            entry.player2MinesRevealed = Integer.parseInt(parts[26]);

            // 27: Mode (optional)
            if (parts.length >= 28) {
                try {
                    entry.mode = TwoPlayerMode.valueOf(parts[27].trim());
                } catch (Exception ignored) {
                    entry.mode = TwoPlayerMode.COOP;
                }
            } else {
                entry.mode = TwoPlayerMode.COOP;
            }

            // 28: Winner (optional)
            if (parts.length >= 29) {
                entry.winner = parts[28].trim();
            } else {
                entry.winner = "";
            }

            // 29: EndReason (optional)
            if (parts.length >= 30) {
                entry.endReason = normalizeEndReason(parts[29].trim());
            } else {
                entry.endReason = ""; // old records
            }

            // 30..31: per-player scores (optional)
            if (parts.length >= 32) {
                entry.player1Score = safeInt(parts[30], 0);
                entry.player2Score = safeInt(parts[31], 0);
            } else {
                entry.player1Score = 0;
                entry.player2Score = 0;
            }

            // If endReason missing but we have won boolean -> derive something reasonable
            if (entry.endReason == null || entry.endReason.isBlank()) {
                entry.endReason = entry.won ? "WIN" : "LOST";
            }

            return entry;

        } catch (Exception e) {
            return null;
        }
    }

    private static int safeInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    // Normalize "GIVE UP" -> "GIVE_UP", "LOSE" -> "LOST", etc.
    private static String normalizeEndReason(String s) {
        if (s == null) return "";
        String t = s.trim().toUpperCase().replace(' ', '_');
        return switch (t) {
            case "VICTORY", "WINNER", "WON" -> "WIN";
            case "DEFEAT", "LOSE", "LOSS" -> "LOST";
            case "GIVEUP", "GIVE_UP" -> "GIVE_UP";
            case "DRAW", "TIE" -> "DRAW";
            default -> t; // keep as-is if already valid
        };
    }

    // ---------------- Getters/Setters ----------------
    public TwoPlayerMode getMode() { return mode; }
    public void setMode(TwoPlayerMode mode) { this.mode = mode; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = normalizeEndReason(endReason); }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getFinalScore() { return finalScore; }
    public void setFinalScore(int finalScore) { this.finalScore = finalScore; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getPlayer1QuestionsAnswered() { return player1QuestionsAnswered; }
    public void setPlayer1QuestionsAnswered(int val) { this.player1QuestionsAnswered = val; }

    public int getPlayer1QuestionsCorrect() { return player1QuestionsCorrect; }
    public void setPlayer1QuestionsCorrect(int val) { this.player1QuestionsCorrect = val; }

    public int getPlayer1SurprisesActivated() { return player1SurprisesActivated; }
    public void setPlayer1SurprisesActivated(int val) { this.player1SurprisesActivated = val; }

    public int getPlayer1GoodSurprises() { return player1GoodSurprises; }
    public void setPlayer1GoodSurprises(int val) { this.player1GoodSurprises = val; }

    public int getPlayer1BadSurprises() { return player1BadSurprises; }
    public void setPlayer1BadSurprises(int val) { this.player1BadSurprises = val; }

    public int getPlayer1MinesFlagged() { return player1MinesFlagged; }
    public void setPlayer1MinesFlagged(int val) { this.player1MinesFlagged = val; }

    public int getPlayer1CorrectFlags() { return player1CorrectFlags; }
    public void setPlayer1CorrectFlags(int val) { this.player1CorrectFlags = val; }

    public int getPlayer1WrongFlags() { return player1WrongFlags; }
    public void setPlayer1WrongFlags(int val) { this.player1WrongFlags = val; }

    public int getPlayer1CellsRevealed() { return player1CellsRevealed; }
    public void setPlayer1CellsRevealed(int val) { this.player1CellsRevealed = val; }

    public int getPlayer1MinesRevealed() { return player1MinesRevealed; }
    public void setPlayer1MinesRevealed(int val) { this.player1MinesRevealed = val; }

    public int getPlayer2QuestionsAnswered() { return player2QuestionsAnswered; }
    public void setPlayer2QuestionsAnswered(int val) { this.player2QuestionsAnswered = val; }

    public int getPlayer2QuestionsCorrect() { return player2QuestionsCorrect; }
    public void setPlayer2QuestionsCorrect(int val) { this.player2QuestionsCorrect = val; }

    public int getPlayer2SurprisesActivated() { return player2SurprisesActivated; }
    public void setPlayer2SurprisesActivated(int val) { this.player2SurprisesActivated = val; }

    public int getPlayer2GoodSurprises() { return player2GoodSurprises; }
    public void setPlayer2GoodSurprises(int val) { this.player2GoodSurprises = val; }

    public int getPlayer2BadSurprises() { return player2BadSurprises; }
    public void setPlayer2BadSurprises(int val) { this.player2BadSurprises = val; }

    public int getPlayer2MinesFlagged() { return player2MinesFlagged; }
    public void setPlayer2MinesFlagged(int val) { this.player2MinesFlagged = val; }

    public int getPlayer2CorrectFlags() { return player2CorrectFlags; }
    public void setPlayer2CorrectFlags(int val) { this.player2CorrectFlags = val; }

    public int getPlayer2WrongFlags() { return player2WrongFlags; }
    public void setPlayer2WrongFlags(int val) { this.player2WrongFlags = val; }

    public int getPlayer2CellsRevealed() { return player2CellsRevealed; }
    public void setPlayer2CellsRevealed(int val) { this.player2CellsRevealed = val; }

    public int getPlayer2MinesRevealed() { return player2MinesRevealed; }
    public void setPlayer2MinesRevealed(int val) { this.player2MinesRevealed = val; }

    // Convenience increments
    public void incrementPlayer1Questions(boolean correct) {
        player1QuestionsAnswered++;
        if (correct) player1QuestionsCorrect++;
    }

    public void incrementPlayer2Questions(boolean correct) {
        player2QuestionsAnswered++;
        if (correct) player2QuestionsCorrect++;
    }

    public void incrementPlayer1Surprise(boolean good) {
        player1SurprisesActivated++;
        if (good) player1GoodSurprises++;
        else player1BadSurprises++;
    }

    public void incrementPlayer2Surprise(boolean good) {
        player2SurprisesActivated++;
        if (good) player2GoodSurprises++;
        else player2BadSurprises++;
    }

    public void incrementPlayer1Flag(boolean correct) {
        player1MinesFlagged++;
        if (correct) player1CorrectFlags++;
        else player1WrongFlags++;
    }

    public void incrementPlayer2Flag(boolean correct) {
        player2MinesFlagged++;
        if (correct) player2CorrectFlags++;
        else player2WrongFlags++;
    }

    public void incrementPlayer1CellRevealed() { player1CellsRevealed++; }
    public void incrementPlayer2CellRevealed() { player2CellsRevealed++; }

    public void incrementPlayer1MineRevealed() { player1MinesRevealed++; }
    public void incrementPlayer2MineRevealed() { player2MinesRevealed++; }
}

package model;

import model.DetailedGameHistoryEntry;
import model.GameHistoryEntry;
import model.GameRecord;
import model.PlayerStats;
import java.io.*;
import java.util.*;

public class GameHistoryRepository {

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".minesweeper";
    private final String FILE = APP_DIR + File.separator + "history.csv";
    private static final String DETAILED_HISTORY_FILE = APP_DIR + File.separator + "detailed_history.csv";

    public List<GameHistoryEntry> loadSimpleHistoryForUser(String username) {
        System.out.println("history: " + username);
        List<GameHistoryEntry> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");
                if (p.length < 6) continue;

                if (!p[0].equals(username))
                    continue;

                GameHistoryEntry r = new GameHistoryEntry();

                r.setPlayer1(p[0]);
                r.setPlayer2(p[1]);
                r.setDifficulty(p[2]);
                r.setFinalScore(Integer.parseInt(p[3]));
                r.setDurationSeconds(Integer.parseInt(p[4]));
                r.setWon(Boolean.parseBoolean(p[5]));

                records.add(r);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    public List<GameHistoryEntry> loadSimpleHistoryCombined() {
        List<GameHistoryEntry> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");
                if (p.length < 6) continue;


                GameHistoryEntry r = new GameHistoryEntry();

                r.setPlayer1(p[0]);
                r.setPlayer2(p[1]);
                r.setDifficulty(p[2]);
                r.setFinalScore(Integer.parseInt(p[3]));
                r.setDurationSeconds(Integer.parseInt(p[4]));
                r.setWon(Boolean.parseBoolean(p[5]));

                records.add(r);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }


    public List<GameRecord> loadHistoryForUser(String username) {
        System.out.println("history" + username);
        List<GameRecord> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] p = line.split(",");

                if (!p[0].equals(username))
                    continue;

                GameRecord r = new GameRecord();
                r.setDate(p[1]);
                r.setDifficulty(p[2]);
                r.setPlayers(p[3]);
                r.setTotalScore(Integer.parseInt(p[4]));
                r.setDuration(p[5]);
                r.setTotalQuestions(Integer.parseInt(p[6]));
                r.setOpened(Integer.parseInt(p[7]));
                r.setNotOpened(Integer.parseInt(p[8]));
                r.setCorrectOverall(Integer.parseInt(p[9]));
                r.setWrongOverall(Integer.parseInt(p[10]));
                r.setSurprises(Integer.parseInt(p[11]));
                r.setLivesLost(Boolean.parseBoolean(p[12]));
                r.setLivesRemaining(Integer.parseInt(p[13]));
                r.setMultiplayer(Boolean.parseBoolean(p[14]));

                int index = 15;
                while (index < p.length) {
                    PlayerStats ps = new PlayerStats();
                    ps.setName(p[index++]);
                    ps.setCorrect(Integer.parseInt(p[index++]));
                    ps.setWrong(Integer.parseInt(p[index++]));
                    ps.setFlags(Integer.parseInt(p[index++]));
                    ps.setWarnings(Integer.parseInt(p[index++]));
                    ps.setSurprises(Integer.parseInt(p[index++]));
                    ps.setMistakes(Integer.parseInt(p[index++]));
                    r.getPlayerStats().add(ps);
                }

                records.add(r);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    public List<DetailedGameHistoryEntry> loadDetailedHistoryForUser(String username) {
        List<DetailedGameHistoryEntry> records = new ArrayList<>();

        File file = new File(DETAILED_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("Detailed history file not found: " + DETAILED_HISTORY_FILE);
            return records;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                DetailedGameHistoryEntry entry = DetailedGameHistoryEntry.fromCSV(line);
                if (entry != null) {
                    if (entry.getPlayer1().equals(username) || entry.getPlayer2().equals(username)) {
                        records.add(entry);
                    }
                }
            }

            System.out.println("Loaded " + records.size() + " detailed records for " + username);

        } catch (IOException e) {
            System.err.println("Error loading detailed history: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }


}
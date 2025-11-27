package model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class SysData {

    private static SysData instance;
    private List<Question> questions;
    private List<GameHistoryEntry> history;
    private List<DetailedGameHistoryEntry> detailedGameHistory;
    private int nextQuestionId = 1;

    // base dir in user home
    private static final String APP_DIR =
            System.getProperty("user.home") + File.separator + ".minesweeper";

    // local override files
    private final String QUESTIONS_CSV = APP_DIR + File.separator + "Questions.csv";
    private final String HISTORY_CSV = APP_DIR + File.separator + "history.csv";
    private static final String DETAILED_HISTORY_FILE = APP_DIR + File.separator + "detailed_history.csv";

    // resource path inside the jar (src/main/resources/Questions.csv)
    private static final String QUESTIONS_RESOURCE = "/Questions.csv";

    public SysData() {
        questions = new ArrayList<>();
        history = new ArrayList<>();
        detailedGameHistory = new ArrayList<>();

        ensureAppDirectory();

        loadQuestions();
        loadHistory();
        loadDetailedHistory();
        calculateNextQuestionId();
    }

    public static SysData getInstance() {
        if (instance == null) {
            instance = new SysData();
        }
        return instance;
    }

    /**
     * Make sure the application directory exists and that
     * history files exist. We do NOT create a questions file here.
     */
    private void ensureAppDirectory() {
        File dir = new File(APP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // history.csv
        File historyFile = new File(HISTORY_CSV);
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // detailed_history.csv
        File detailedHistoryFile = new File(DETAILED_HISTORY_FILE);
        if (!detailedHistoryFile.exists()) {
            try {
                detailedHistoryFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateNextQuestionId() {
        int maxId = 0;
        for (Question q : questions) {
            if (q.getId() > maxId) maxId = q.getId();
        }
        nextQuestionId = maxId + 1;
    }

    /**
     * Load questions.
     * - If local ~/.minesweeper/Questions.csv exists → load from there.
     * - Otherwise → load from classpath resource /Questions.csv inside the jar.
     */
    public void loadQuestions() {
        questions.clear();

        Reader reader = null;
        try {
            File file = new File(QUESTIONS_CSV);

            if (file.exists()) {
                // local override
                reader = new FileReader(file);
                System.out.println("[SysData] Loading questions from " + file.getAbsolutePath());
            } else {
                // bundled CSV inside the jar
                InputStream is = getClass().getResourceAsStream(QUESTIONS_RESOURCE);
                if (is == null) {
                    System.err.println("[SysData] ERROR: " + QUESTIONS_RESOURCE + " not found on classpath!");
                    return;
                }
                reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                System.out.println("[SysData] Loading questions from classpath resource " + QUESTIONS_RESOURCE);
            }

            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] parts;
                boolean firstLine = true;
                while ((parts = csvReader.readNext()) != null) {
                    if (firstLine) { // skip header
                        firstLine = false;
                        continue;
                    }
                    if (parts.length < 8) continue;

                    int id = Integer.parseInt(parts[0].trim());
                    String text = parts[1].trim();
                    int diff = Integer.parseInt(parts[2].trim());
                    String[] answers = {
                            parts[3].trim(),
                            parts[4].trim(),
                            parts[5].trim(),
                            parts[6].trim()
                    };
                    int correctIndex = switch (parts[7].trim().toUpperCase()) {
                        case "A" -> 0;
                        case "B" -> 1;
                        case "C" -> 2;
                        case "D" -> 3;
                        default -> -1;
                    };

                    questions.add(new Question(id, text, answers, correctIndex, diff));
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            calculateNextQuestionId();
        }
    }

    /**
     * Save questions to the local override CSV in ~/.minesweeper.
     * From the next run, loadQuestions() will use this file.
     */
    public boolean saveQuestions() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(QUESTIONS_CSV))) {
            String[] header = {"ID", "Question", "Difficulty", "A", "B", "C", "D", "Correct Answer"};
            writer.writeNext(header);

            for (Question q : questions) {
                String correctLetter = switch (q.getCorrectIndex()) {
                    case 0 -> "A";
                    case 1 -> "B";
                    case 2 -> "C";
                    case 3 -> "D";
                    default -> "";
                };
                String[] line = {
                        String.valueOf(q.getId()),
                        q.getText(),
                        String.valueOf(q.getDifficulty()),
                        q.getAnswers()[0],
                        q.getAnswers()[1],
                        q.getAnswers()[2],
                        q.getAnswers()[3],
                        correctLetter
                };
                writer.writeNext(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void loadHistory() {
        history.clear();
        File file = new File(HISTORY_CSV);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String p1 = parts[0].trim();
                    String p2 = parts[1].trim();
                    String diff = parts[2].trim();
                    int score = Integer.parseInt(parts[3].trim());
                    int duration = parts.length >= 5 ? Integer.parseInt(parts[4].trim()) : 0;
                    boolean won = parts.length >= 6 && Boolean.parseBoolean(parts[5].trim());
                    history.add(new GameHistoryEntry(p1, p2, diff, score, duration, won));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveHistory() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_CSV))) {
            for (GameHistoryEntry h : history) {
                pw.println(h.getPlayer1() + "," + h.getPlayer2() + "," + h.getDifficulty() + "," +
                        h.getFinalScore() + "," + h.getDurationSeconds() + "," + h.isWon());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addQuestion(Question newQuestion) {
        newQuestion.setId(nextQuestionId++);
        questions.add(newQuestion);
        return saveQuestions();
    }

    public boolean updateQuestion(Question updatedQuestion) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId() == updatedQuestion.getId()) {
                questions.set(i, updatedQuestion);
                saveQuestions();
                return true;
            }
        }
        return false;
    }

    public boolean deleteQuestion(int id) {
        boolean removed = questions.removeIf(q -> q.getId() == id);
        if (removed) saveQuestions();
        return removed;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<GameHistoryEntry> getHistory() {
        return history;
    }

    public void addGameHistory(GameHistoryEntry gameHistoryEntry) {
        history.add(gameHistoryEntry);
        saveHistory();
    }

    public void addDetailedGameHistory(DetailedGameHistoryEntry entry) {
        if (detailedGameHistory != null) {
            detailedGameHistory.add(entry);
            saveDetailedHistory();
        }
    }

    public List<DetailedGameHistoryEntry> getDetailedGameHistory() {
        return new ArrayList<>(detailedGameHistory);
    }

    private void saveDetailedHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DETAILED_HISTORY_FILE))) {
            writer.write(DetailedGameHistoryEntry.getCSVHeader());
            writer.newLine();

            for (DetailedGameHistoryEntry entry : detailedGameHistory) {
                writer.write(entry.toCSV());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error saving detailed history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDetailedHistory() {
        File file = new File(DETAILED_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("No detailed history file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                DetailedGameHistoryEntry entry = DetailedGameHistoryEntry.fromCSV(line);
                if (entry != null) {
                    detailedGameHistory.add(entry);
                }
            }

            System.out.println("Loaded " + detailedGameHistory.size() + " detailed history entries");
        } catch (IOException e) {
            System.err.println("Error loading detailed history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearDetailedHistory() {
        detailedGameHistory.clear();
        saveDetailedHistory();
    }

    public List<DetailedGameHistoryEntry> getHistoryForPlayer(String playerName) {
        List<DetailedGameHistoryEntry> playerHistory = new ArrayList<>();
        for (DetailedGameHistoryEntry entry : detailedGameHistory) {
            if (entry.getPlayer1().equals(playerName) || entry.getPlayer2().equals(playerName)) {
                playerHistory.add(entry);
            }
        }
        return playerHistory;
    }
}

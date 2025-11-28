package model;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

public class SysData {

    private static SysData instance;
    private List<Question> questions;
    private List<GameHistoryEntry> history;
    private List<DetailedGameHistoryEntry> detailedGameHistory;
    private int nextQuestionId = 1;

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".minesweeper";
    private final String QUESTIONS_CSV = APP_DIR + File.separator + "Questions.csv";
    private final String HISTORY_CSV = APP_DIR + File.separator + "history.csv";
    private static final String DETAILED_HISTORY_FILE = APP_DIR + File.separator + "detailed_history.csv";

    public SysData() {
        questions = new ArrayList<>();
        history = new ArrayList<>();
        detailedGameHistory = new ArrayList<>();

        ensureAppDirectory();

        loadQuestions();
        loadHistory();
        calculateNextQuestionId();
    }

    public static SysData getInstance() {
        if (instance == null) {
            instance = new SysData();
        }
        return instance;
    }

    private void ensureAppDirectory() {
        File dir = new File(APP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File questionsFile = new File(QUESTIONS_CSV);
        if (!questionsFile.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/Questions.csv");
                 FileOutputStream fos = new FileOutputStream(questionsFile)) {
                if (is != null) {
                    is.transferTo(fos);
                } else {
                    questionsFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File historyFile = new File(HISTORY_CSV);
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

    public void loadQuestions() {
        questions.clear();
        File file = new File(QUESTIONS_CSV);
        if (!file.exists()) return;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] parts;
            boolean firstLine = true;
            while ((parts = reader.readNext()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (parts.length < 8) continue;

                int id = Integer.parseInt(parts[0].trim());
                String text = parts[1].trim();
                int diff = Integer.parseInt(parts[2].trim());
                String[] answers = { parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim() };
                int correctIndex = switch (parts[7].trim().toUpperCase()) {
                    case "A" -> 0;
                    case "B" -> 1;
                    case "C" -> 2;
                    case "D" -> 3;
                    default -> -1;
                };
                questions.add(new Question(id, text, answers, correctIndex, diff));
            }
        } catch (Exception e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
        }
        calculateNextQuestionId();
    }

    public boolean saveQuestions() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(QUESTIONS_CSV))) {
            String[] header = {"ID", "Question", "Difficulty", "A", "B", "C", "D", "Correct Answer"};
            writer.writeNext(header);

            for (Question q : questions) {
                String correctLetter = switch (q.getCorrectIndex()) {
                    case 1 -> "A";
                    case 2 -> "B";
                    case 3 -> "C";
                    case 4 -> "D";
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

    public List<Question> getQuestions() { return questions; }
    public List<GameHistoryEntry> getHistory() { return history; }

    public void addGameHistory(GameHistoryEntry gameHistoryEntry) {
        history.add(gameHistoryEntry);
        saveHistory();
    }

    public void addDetailedGameHistory(DetailedGameHistoryEntry entry) {
        if(detailedGameHistory!=null){
        detailedGameHistory.add(entry);
        saveDetailedHistory();
        }
    }

    public List<DetailedGameHistoryEntry> getDetailedGameHistory() {
        return new ArrayList<>(detailedGameHistory);
    }

    private void saveDetailedHistory() {
        System.out.println("detailed history saved");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DETAILED_HISTORY_FILE,true))) {
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
            String line = reader.readLine();

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

package model;

public class Question {
    private int id;
    private String text;
    private String[] answers = new String[4];
    private int correctIndex;
    private int difficulty;

    public Question(int id, String text, String[] answers, int correctIndex, int difficulty) {
        this.id = id;
        this.text = text;
        this.answers = answers;
        this.correctIndex = correctIndex;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getAnswers() { return answers; }
    public int getCorrectIndex() { return correctIndex; }
    public int getDifficulty() { return difficulty; }
    public void setId(int id) { this.id = id; }
    public void setText(String text) {
        this.text = text;
    }
    public void setAnswers(String[] answers) {
        this.answers = answers;
    }
    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficultyAsString() {
        switch (difficulty) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            case 4: return "Expert";
            default: return "Unknown";
        }
    }

    public static int getDifficultyFromString(String diff) {
        if (diff == null) return -1;

        switch (diff.trim().toLowerCase()) {
            case "easy": return 1;
            case "medium": return 2;
            case "hard": return 3;
            case "expert": return 4;
            default: return -1;
        }
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", difficulty=" + getDifficultyAsString() +
                ", text='" + text + '\'' +
                '}';
    }
}

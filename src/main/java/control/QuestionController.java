package control;

import model.Question;
import model.SysData;

import java.util.*;

public class QuestionController {

    private final SysData sysData;
    private final Random random;
    private final Set<Integer> usedQuestionIds;

    public QuestionController() {
        this.sysData = SysData.getInstance();
        this.random = new Random();
        this.usedQuestionIds = new HashSet<>();
    }

    /**
     * difficulty > 0  -> only questions with that difficulty (1..4)
     * difficulty <= 0 -> ANY difficulty (1..4)
     */
    public Question getRandomQuestion(int difficulty) {

        List<Question> all = sysData.getQuestions();

        List<Question> filtered = new ArrayList<>();
        for (Question q : all) {
            boolean matchesDifficulty =
                    (difficulty <= 0) || (q.getDifficulty() == difficulty);

            if (matchesDifficulty && !usedQuestionIds.contains(q.getId())) {
                filtered.add(q);
            }
        }

        // If we've used all questions of that difficulty set,
        // allow re-use (still respecting difficulty filter).
        if (filtered.isEmpty()) {
            for (Question q : all) {
                boolean matchesDifficulty =
                        (difficulty <= 0) || (q.getDifficulty() == difficulty);

                if (matchesDifficulty) {
                    filtered.add(q);
                }
            }
        }

        if (filtered.isEmpty()) {
            return createFallbackQuestion(difficulty);
        }

        Question selected = filtered.get(random.nextInt(filtered.size()));

        usedQuestionIds.add(selected.getId());

        return selected;
    }

    public void resetUsedQuestions() {
        usedQuestionIds.clear();
    }

    private Question createFallbackQuestion(int diff) {
        String[] dummy = {"A", "B", "C", "D"};
        return new Question(
                -1,
                "No questions available for this difficulty.",
                dummy,
                0,
                diff
        );
    }

    public static int getDifficultyFromString(String diff) {
        if (diff == null) return -1;

        switch (diff.trim().toLowerCase()) {
            case "easy":
                return 1;
            case "medium":
                return 2;
            case "hard":
                return 3;
            case "expert":
                return 4;
            default:
                return -1;
        }
    }

    public List<Question> getAllQuestions() {
        return sysData.getQuestions();
    }

    public Question getQuestionById(int id) {
        return SysData.getInstance().getQuestions().stream()
                .filter(q -> q.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean addQuestion(Question q) {
        return sysData.addQuestion(q);
    }

    public boolean updateQuestion(Question q) {
        return sysData.updateQuestion(q);
    }

    public boolean deleteQuestion(int id) {
        return sysData.deleteQuestion(id);
    }

    public static String difficultyToString(int diff) {
        switch (diff) {
            case 1:
                return "Easy";
            case 2:
                return "Medium";
            case 3:
                return "Hard";
            case 4:
                return "Expert";
            default:
                return "Unknown";
        }
    }

    public static int difficultyFromString(String diff) {
        if (diff == null) return -1;
        switch (diff.trim().toLowerCase()) {
            case "easy":
                return 1;
            case "medium":
                return 2;
            case "hard":
                return 3;
            case "expert":
                return 4;
            default:
                return -1;
        }
    }
}

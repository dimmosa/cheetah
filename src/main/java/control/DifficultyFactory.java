package control;

public final class DifficultyFactory {

    public record Config(int rows, int cols, int sharedLives, int activationCost) {}

    private DifficultyFactory() {}

    public static Config create(String difficulty) {
        if (difficulty == null) difficulty = "Medium";

        return switch (difficulty) {
            case "Easy"   -> new Config(9, 9, 10, 5);
            case "Medium" -> new Config(13, 13, 8, 8);
            case "Hard"   -> new Config(16, 16, 6, 12);
            default       -> new Config(16, 16, 8, 8);
        };
    }
}
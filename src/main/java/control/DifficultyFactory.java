package control;


public class DifficultyFactory {
    
    public record Config(int rows, int cols, int maxLives, int activationCost) {}
    
    public static Config create(String difficulty) {
        return switch (difficulty) {
            case "Easy" -> new Config(8, 8, 10, 5);    // max 10
            case "Medium" -> new Config(12, 12, 8, 8); // max 8
            case "Hard" -> new Config(16, 16, 6, 12);  // max 6
            default -> new Config(8, 8, 10, 5);
        };
    }
}
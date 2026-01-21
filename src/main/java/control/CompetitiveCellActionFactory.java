package control;

import model.CellType;

public final class CompetitiveCellActionFactory {

    private CompetitiveCellActionFactory() {}

    public static CompetitiveCellActionTemplate createForReveal(
            CompetitiveGameController controller, int player, CellType type) {

        if (controller == null) throw new IllegalArgumentException("controller is null");
        if (type == null) throw new IllegalArgumentException("type is null");

        return new CompetitiveCellActionTemplate() {
            @Override
            public CompetitiveGameController.CellActionResult execute() {

                return switch (type) {
                    case MINE -> controller.revealMine(player);

                    // safe reveal actions that END turn in your controller
                    case NUMBER -> controller.revealNumberCell(player);
                    case EMPTY  -> controller.revealEmptyCell(player);

                    // ✅ FIX: revealing QUESTION/SURPRISE ends turn immediately
                    case QUESTION, SURPRISE -> {
                        controller.endTurn();
                        yield new CompetitiveGameController.CellActionResult(
                                true, 0, 0,
                                "Special cell revealed (" + type + "). Turn ends.",
                                controller.isGameOver()
                        );
                    }

                    default -> new CompetitiveGameController.CellActionResult(
                            false, 0, 0,
                            "Unsupported cell type: " + type,
                            controller.isGameOver()
                    );
                };
            }
        };
    }

    public static CompetitiveCellActionTemplate createForFlag(
            CompetitiveGameController controller, int player, boolean correct) {

        if (controller == null) throw new IllegalArgumentException("controller is null");

        return new CompetitiveCellActionTemplate() {
            @Override
            public CompetitiveGameController.CellActionResult execute() {
                return correct
                        ? controller.flagMineCorrectly(player)
                        : controller.flagIncorrectly(player);
            }
        };
    }
}

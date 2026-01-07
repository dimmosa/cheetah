package control;

import model.CellType;
import view.CellButton;

public class CellActionFactory {

    public static CellActionTemplate createForReveal(MultiPlayerGameController game, CellType type) {
    	return switch (type) {
        case MINE -> new RevealMineAction(game);
        case NUMBER, QUESTION, SURPRISE -> new RevealNumberAction(game);
        case EMPTY -> new RevealEmptyAction(game);
    };

    }

    public static CellActionTemplate createForFlag(MultiPlayerGameController game, boolean correctMine) {
        return correctMine ? new FlagCorrectAction(game) : new FlagWrongAction(game);
    }

    public static CellActionTemplate createForSurprise(MultiPlayerGameController game) {
        return new SurpriseAction(game);
    }
}

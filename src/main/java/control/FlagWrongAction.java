package control;

public class FlagWrongAction extends CellActionTemplate {

    public FlagWrongAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.flagIncorrectly(); // ✅ -3
    }
}
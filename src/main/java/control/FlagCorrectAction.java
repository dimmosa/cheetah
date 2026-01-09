package control;

public class FlagCorrectAction extends CellActionTemplate {

    public FlagCorrectAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.flagMineCorrectly();
    }
}